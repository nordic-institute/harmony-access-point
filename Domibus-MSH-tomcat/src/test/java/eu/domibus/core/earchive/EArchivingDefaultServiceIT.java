package eu.domibus.core.earchive;

import eu.domibus.AbstractIT;
import eu.domibus.api.earchive.EArchiveBatchFilter;
import eu.domibus.api.earchive.EArchiveBatchRequestDTO;
import eu.domibus.api.earchive.EArchiveBatchStatus;
import eu.domibus.api.earchive.EArchiveRequestType;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.common.JPAConstants;
import eu.domibus.common.MessageDaoTestUtil;
import eu.domibus.core.message.UserMessageDefaultService;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static eu.domibus.api.model.DomibusDatePrefixedSequenceIdGeneratorGenerator.DATETIME_FORMAT_DEFAULT;
import static eu.domibus.api.model.DomibusDatePrefixedSequenceIdGeneratorGenerator.MAX;
import static eu.domibus.core.earchive.EArchivingDefaultService.CONTINUOUS_ID;
import static eu.domibus.core.earchive.EArchivingDefaultService.SANITY_ID;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Locale.ENGLISH;

/**
 * @author François Gautier
 * @since 5.0
 */
@Transactional
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

    @Autowired
    UserMessageDefaultService userMessageDefaultService;

    @PersistenceContext(unitName = JPAConstants.PERSISTENCE_UNIT_NAME)
    protected EntityManager em;

    @Autowired
    protected PlatformTransactionManager transactionManager;


    EArchiveBatchEntity batch1;
    EArchiveBatchEntity batch2;
    UserMessageLog uml1;
    UserMessageLog uml2;
    UserMessageLog uml3;
    UserMessageLog uml4;
    UserMessageLog uml5;
    UserMessageLog uml6;
    UserMessageLog uml7_not_archived;
    UserMessageLog uml8_not_archived;

    @Before
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
                EArchiveBatchStatus.EXPORTED,
                DateUtils.addDays(currentDate, -30),
                uml1.getEntityId(),
                uml3.getEntityId(),
                3,
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
                2,
                "/tmp/batch"));

        eArchiveBatchUserMessageDao.create(batch2, Arrays.asList(
                new EArchiveBatchUserMessage(uml4.getEntityId(), uml4.getUserMessage().getMessageId()),
                new EArchiveBatchUserMessage(uml5.getEntityId(), uml5.getUserMessage().getMessageId())
        ));
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
        Assert.assertEquals(2, batchRequestsCount.longValue());
    }

    @Test
    @Transactional
    public void getBatchListDefaultFilter() {
        EArchiveBatchFilter filter = new EArchiveBatchFilter();
        List<EArchiveBatchRequestDTO> batchRequestsCount = eArchivingService.getBatchRequestList(filter);
        Assert.assertEquals(2, batchRequestsCount.size());
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
    public void getBatchUserMessageListExported() {
        // given
        String batchId = batch1.getBatchId();
        // when
        Long messageCount = eArchivingService.getExportedBatchUserMessageListCount(batchId);
        List<String> messageList = eArchivingService.getExportedBatchUserMessageList(batchId, null, null);
        // then
        Assert.assertNotNull(messageCount);
        Assert.assertEquals(3, messageList.size());
        Assert.assertEquals(messageCount.intValue(), messageList.size());
    }


    @Test
    @Transactional
    public void getBatchUserMessageListFailed() {
        // given - batch2 has status failed
        String batchId = batch2.getBatchId();
        // when
        Long messageCount = eArchivingService.getExportedBatchUserMessageListCount(batchId);
        List<String> messageList = eArchivingService.getExportedBatchUserMessageList(batchId, null, null);
        // then - no messages are exported!
        Assert.assertNotNull(messageCount);
        Assert.assertEquals(0, messageList.size());
        Assert.assertEquals(messageCount.intValue(), messageList.size());

    }

    @Test
    @Transactional
    public void getNotArchivedMessages() {
        Date currentDate = Calendar.getInstance().getTime();
        Long startDate =  Long.parseLong(ZonedDateTime.ofInstant(DateUtils.addDays(currentDate, -30).toInstant(),
                ZoneOffset.UTC).format(ofPattern(DATETIME_FORMAT_DEFAULT, ENGLISH)) + MAX);
        Long endDate  = Long.parseLong(ZonedDateTime.ofInstant(DateUtils.addDays(currentDate, 1).toInstant(),
                ZoneOffset.UTC).format(ofPattern(DATETIME_FORMAT_DEFAULT, ENGLISH)) + MAX);

        List<String> messages = eArchivingService.getNotArchivedMessages(startDate,
                endDate, null, null);

        // According to the discussion service must return all messages which does not have set archive date!
        int expectedCount = 8;
        Assert.assertTrue(expectedCount <= messages.size()); // the db may contain messages from other non-transactional tests
        Assert.assertTrue(messages.contains(uml1.getUserMessage().getMessageId()));
        Assert.assertTrue(messages.contains(uml8_not_archived.getUserMessage().getMessageId()));
    }

    @Test
    @Transactional
    public void testExecuteBatchIsArchived() {
        // given
        List<EArchiveBatchUserMessage> messageList = eArchiveBatchUserMessageDao.getBatchMessageList(batch1.getBatchId(), null, null);
        Assert.assertEquals(3, messageList.size());
        Assert.assertNotEquals(EArchiveBatchStatus.ARCHIVED, batch1.getEArchiveBatchStatus());

        // when
        eArchivingService.executeBatchIsArchived(batch1, messageList);

        //then
        EArchiveBatchEntity batchUpdated = eArchiveBatchDao.findEArchiveBatchByBatchId(batch1.getBatchId());
        // messages and
        Assert.assertEquals(EArchiveBatchStatus.ARCHIVED, batchUpdated.getEArchiveBatchStatus());
    }

    @Test
    @Transactional
    public void testExecuteBatchIsArchivedDelete() {
        // given
        List<EArchiveBatchUserMessage> messageList = eArchiveBatchUserMessageDao.getBatchMessageList(batch1.getBatchId(), null, null);
        Assert.assertEquals(3, messageList.size());
        Assert.assertNotEquals(EArchiveBatchStatus.ARCHIVED, batch1.getEArchiveBatchStatus());

        // when
        eArchivingService.executeBatchIsArchived(batch1, messageList);

        //then
        EArchiveBatchEntity batchUpdated = eArchiveBatchDao.findEArchiveBatchByBatchId(batch1.getBatchId());
        // messages and
        Assert.assertEquals(EArchiveBatchStatus.ARCHIVED, batchUpdated.getEArchiveBatchStatus());

        //delete messages
        List<Long> entityIds = new ArrayList<>();
        messageList.stream().forEach(ml -> entityIds.add(ml.getUserMessageEntityId()));

        List<String> messageIds =  new ArrayList<>();
        messageList.stream().forEach(ml -> messageIds.add(ml.getMessageId()));

        userMessageDefaultService.deleteMessages(entityIds, messageIds);

    }

    @Test
    @Transactional
    public void testSetBatchClientStatusFail() {
        // given
        Assert.assertNotEquals(EArchiveBatchStatus.ARCHIVE_FAILED, batch1.getEArchiveBatchStatus());
        String message = UUID.randomUUID().toString();
        // when
        eArchivingService.setBatchClientStatus(batch1.getBatchId(), EArchiveBatchStatus.ARCHIVE_FAILED, message);
        //then
        EArchiveBatchEntity batchUpdated = eArchiveBatchDao.findEArchiveBatchByBatchId(batch1.getBatchId());
        Assert.assertEquals(EArchiveBatchStatus.ARCHIVE_FAILED, batchUpdated.getEArchiveBatchStatus());
        Assert.assertEquals(message, batchUpdated.getErrorMessage());
    }
}
