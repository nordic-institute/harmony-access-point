package eu.domibus.core.earchive;

import eu.domibus.AbstractIT;
import eu.domibus.api.earchive.EArchiveBatchFilter;
import eu.domibus.api.earchive.EArchiveBatchRequestDTO;
import eu.domibus.api.earchive.EArchiveBatchStatus;
import eu.domibus.api.earchive.EArchiveRequestType;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.common.MessageDaoTestUtil;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static eu.domibus.core.earchive.EArchivingDefaultService.CONTINUOUS_ID;
import static eu.domibus.core.earchive.EArchivingDefaultService.SANITY_ID;

/**
 * @author François Gautier
 * @since 5.0
 */
public class EArchivingDefaultServiceIT extends AbstractIT {

    @Autowired
    EArchivingDefaultService eArchivingService;

    @Autowired
    EArchiveBatchStartDao eArchiveBatchStartDao;

    @Autowired
    EArchiveBatchDao eArchiveBatchDao;

    @Autowired
    EArchiveBatchUserMessageDao eArchiveBatchUserMessageDao;

    @Autowired
    EArchiveBatchUtils eArchiveBatchUtils;

    @Autowired
    MessageDaoTestUtil messageDaoTestUtil;

    EArchiveBatchEntity batch1;
    EArchiveBatchEntity batch2;
    EArchiveBatchEntity batch3;
    UserMessageLog uml1;
    UserMessageLog uml2;
    UserMessageLog uml3;
    UserMessageLog uml4;
    UserMessageLog uml5;
    UserMessageLog uml6;
    UserMessageLog uml7_not_archived;
    UserMessageLog uml8_not_archived;

    @Before
    @Transactional
    public void setUp() throws Exception {
        waitUntilDatabaseIsInitialized();
        Assert.assertEquals(101000000000000L, ((long) eArchiveBatchStartDao.findByReference(CONTINUOUS_ID).getLastPkUserMessage()));
        Assert.assertEquals(101000000000000L, ((long) eArchiveBatchStartDao.findByReference(SANITY_ID).getLastPkUserMessage()));
        // prepare
        Date currentDate = Calendar.getInstance().getTime();

        uml1 = messageDaoTestUtil.createUserMessageLog("uml1-" + UUID.randomUUID(), currentDate);
        uml2 = messageDaoTestUtil.createUserMessageLog("uml2-" + UUID.randomUUID(), currentDate);
        uml3 = messageDaoTestUtil.createUserMessageLog("uml3-" + UUID.randomUUID(), currentDate);
        uml4 = messageDaoTestUtil.createUserMessageLog("uml4-" + UUID.randomUUID(), currentDate);
        uml5 = messageDaoTestUtil.createUserMessageLog("uml5-" + UUID.randomUUID(), currentDate);
        uml6 = messageDaoTestUtil.createUserMessageLog("uml6-" + UUID.randomUUID(), currentDate);
        uml7_not_archived = messageDaoTestUtil.createUserMessageLog("uml7-" + UUID.randomUUID(), currentDate);
        uml8_not_archived = messageDaoTestUtil.createUserMessageLog("uml8-" + UUID.randomUUID(), currentDate);

        batch1 = eArchiveBatchDao.merge(EArchiveTestUtils.createEArchiveBatchEntity(
                UUID.randomUUID().toString(),
                EArchiveRequestType.CONTINUOUS,
                EArchiveBatchStatus.STARTED,
                DateUtils.addDays(currentDate, -30),
                uml1.getEntityId(),
                uml3.getEntityId(),
                1,
                "/tmp/batch"));
        eArchiveBatchUserMessageDao.create(batch1, Arrays.asList(
                new EArchiveBatchUserMessage(uml1.getEntityId(), uml1.getUserMessage().getMessageId()),
                new EArchiveBatchUserMessage(uml2.getEntityId(), uml2.getUserMessage().getMessageId()),
                new EArchiveBatchUserMessage(uml3.getEntityId(), uml3.getUserMessage().getMessageId())));

        batch2 = eArchiveBatchDao.merge(EArchiveTestUtils.createEArchiveBatchEntity(
                UUID.randomUUID().toString(),
                EArchiveRequestType.CONTINUOUS,
                EArchiveBatchStatus.FAILED,
                DateUtils.addDays(currentDate, -5),
                2110100000000011L,
                2110100000000020L,
                1,
                "/tmp/batch"));

        eArchiveBatchUserMessageDao.create(batch2, Arrays.asList(
                new EArchiveBatchUserMessage(uml4.getEntityId(), uml4.getUserMessage().getMessageId()),
                new EArchiveBatchUserMessage(uml5.getEntityId(), uml5.getUserMessage().getMessageId())
        ));

        batch3 = eArchiveBatchDao.merge(EArchiveTestUtils.createEArchiveBatchEntity(
                batch2.getBatchId(),
                EArchiveRequestType.MANUAL,
                EArchiveBatchStatus.EXPORTED,
                DateUtils.addDays(currentDate, 0),
                2110100000000021L,
                2110110000000001L,
                1,
                "/tmp/batch")); // is copy from 2
        eArchiveBatchUserMessageDao.create(batch3, Collections.singletonList(
                new EArchiveBatchUserMessage(uml6.getEntityId(), uml6.getUserMessage().getMessageId())));

    }

    @Test
    @Transactional
    public void updateStartDateContinuousArchive() {

        Long startMessageDate = 21102610L;
        eArchivingService.updateStartDateContinuousArchive(startMessageDate);

        Assert.assertEquals(eArchiveBatchUtils.dateToPKUserMessageId(startMessageDate),
                eArchiveBatchStartDao.findByReference(CONTINUOUS_ID).getLastPkUserMessage());

    }

    @Test
    @Transactional
    public void getStartDateContinuousArchive() {
        Long startDateContinuousArchive = eArchivingService.getStartDateContinuousArchive();

        Assert.assertEquals(10100L, startDateContinuousArchive.longValue());
    }

    @Test
    @Transactional
    public void updateStartDateSanityArchive() {
        Long startMessageDate = 102710L;
        eArchivingService.updateStartDateSanityArchive(startMessageDate);

        Assert.assertEquals(eArchiveBatchUtils.dateToPKUserMessageId(startMessageDate),
                eArchiveBatchStartDao.findByReference(SANITY_ID).getLastPkUserMessage());
    }

    @Test
    @Transactional
    public void getStartDateSanityArchive() {
        Long startDateSanityArchive = eArchivingService.getStartDateSanityArchive();

        Assert.assertEquals(10100L, startDateSanityArchive.longValue());
    }

    @Test
    @Transactional
    public void getBatchCount() {
        Long batchRequestsCount = eArchivingService.getBatchRequestListCount(new EArchiveBatchFilter());
        Assert.assertEquals(3, batchRequestsCount.longValue());
    }

    @Test
    @Transactional
    public void getBatchListDefaultFilter() {
        EArchiveBatchFilter filter = new EArchiveBatchFilter();
        List<EArchiveBatchRequestDTO> batchRequestsCount = eArchivingService.getBatchRequestList(filter);
        Assert.assertEquals(3, batchRequestsCount.size());
        // descending order
        // the batch 1 is last in list
        Assert.assertEquals(batch1.getBatchId(), batchRequestsCount.get(batchRequestsCount.size() - 1).getBatchId());
        Assert.assertEquals(uml1.getEntityId(), batchRequestsCount.get(batchRequestsCount.size() - 1).getMessageStartId().longValue());
        Assert.assertEquals(uml3.getEntityId(), batchRequestsCount.get(batchRequestsCount.size() - 1).getMessageEndId().longValue());
    }

    @Test
    @Transactional
    public void getBatchListFilterDates() {
        Date currentDate = Calendar.getInstance().getTime();
        EArchiveBatchFilter filter = new EArchiveBatchFilter(null, DateUtils.addDays(currentDate, -40), DateUtils.addDays(currentDate, -20), null, null);
        List<EArchiveBatchRequestDTO> batchRequestsCount = eArchivingService.getBatchRequestList(filter);
        // second batch2 with only one message
        Assert.assertEquals(1, batchRequestsCount.size());
    }

    @Test
    @Transactional
    public void getBatchListFilterStatus() {
        EArchiveBatchFilter filter = new EArchiveBatchFilter(Collections.singletonList(EArchiveRequestType.CONTINUOUS), null, null, null, null);
        List<EArchiveBatchRequestDTO> batchRequestsCount = eArchivingService.getBatchRequestList(filter);
        // second batch2 with only one message
        Assert.assertEquals(2, batchRequestsCount.size());
        Assert.assertEquals(batchRequestsCount.get(0).getMessageEndId(), batchRequestsCount.get(0).getMessageEndId());
    }

    @Test
    @Transactional
    public void getBatchUserMessageList() {
        List<String> messageList = eArchivingService.getBatchUserMessageList(batch1.getBatchId(), null, null);

        Assert.assertEquals(3, messageList.size());
    }

    @Test
    @Transactional
    public void getNotArchivedMessages() {
        Date currentDate = Calendar.getInstance().getTime();
        List<String> messages = eArchivingService.getNotArchivedMessages(DateUtils.addDays(currentDate, -30),
                DateUtils.addDays(currentDate, 1), null, null);

        // According to the discussion service must return all messages which does not have set archive date!
        int expectedCount = 8;
        Assert.assertEquals(expectedCount, messages.size());
        Assert.assertEquals(uml1.getUserMessage().getMessageId(), messages.get(0));
        Assert.assertEquals(uml8_not_archived.getUserMessage().getMessageId(), messages.get(expectedCount - 1));
    }

}
