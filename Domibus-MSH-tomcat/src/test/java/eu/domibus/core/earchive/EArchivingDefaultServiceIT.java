package eu.domibus.core.earchive;

import eu.domibus.AbstractIT;
import eu.domibus.api.earchive.EArchiveBatchFilter;
import eu.domibus.api.earchive.EArchiveBatchStatus;
import eu.domibus.api.earchive.EArchiveBatchRequestDTO;
import eu.domibus.api.model.ListUserMessageDto;
import eu.domibus.api.model.UserMessageDTO;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.common.MessageDaoTestUtil;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static eu.domibus.api.model.DomibusDatePrefixedSequenceIdGeneratorGenerator.dateToPKUserMessageId;
import static eu.domibus.core.earchive.EArchivingDefaultService.CONTINUOUS_ID;
import static eu.domibus.core.earchive.EArchivingDefaultService.SANITY_ID;

/**
 * @author François Gautier
 * @since 5.0
 */
public class EArchivingDefaultServiceIT extends AbstractIT {

    @Autowired
    private EArchivingDefaultService eArchivingService;

    @Autowired
    private EArchiveBatchStartDao eArchiveBatchStartDao;

    @Autowired
    private EArchiveBatchDao eArchiveBatchDao;

    @Autowired
    EArchiveBatchUserMessageDao eArchiveBatchUserMessageDao;

    @Autowired
    MessageDaoTestUtil messageDaoTestUtil;

    EArchiveBatchEntity batch1;
    EArchiveBatchEntity batch2;
    EArchiveBatchEntity batch3;
    UserMessageLog uml1;
    UserMessageLog uml2;
    UserMessageLog uml3;
    UserMessageLog notArchivedUML;

    @Before
    @Transactional
    public void setUp() throws Exception {
        waitUntilDatabaseIsInitialized();
        Assert.assertEquals(101000000000000L, ((long) eArchiveBatchStartDao.findByReference(CONTINUOUS_ID).getLastPkUserMessage()));
        Assert.assertEquals(101000000000000L, ((long) eArchiveBatchStartDao.findByReference(SANITY_ID).getLastPkUserMessage()));
        // prepare
        Date currentDate = Calendar.getInstance().getTime();
        batch1 = EArchiveTestUtils.createEArchiveBatchEntity(
                UUID.randomUUID().toString(),
                RequestType.CONTINUOUS,
                EArchiveBatchStatus.STARTED,
                DateUtils.addDays(currentDate, -30),
                2110100000000001L,
                1,
                "/tmp/batch",
                "{\"2110100000000002\"}",
                null);
        eArchiveBatchDao.create(batch1);

        uml1 = messageDaoTestUtil.createUserMessageLog(UUID.randomUUID().toString(),currentDate);
        uml2 = messageDaoTestUtil.createUserMessageLog(UUID.randomUUID().toString(),currentDate);
        uml3 = messageDaoTestUtil.createUserMessageLog(UUID.randomUUID().toString(),currentDate);
        notArchivedUML =  messageDaoTestUtil.createUserMessageLog(UUID.randomUUID().toString(),currentDate);
        eArchiveBatchUserMessageDao.create(batch1, Arrays.asList(
                uml1.getEntityId(),
                uml2.getEntityId(),
                uml3.getEntityId()));

        batch2 = eArchiveBatchDao.merge(EArchiveTestUtils.createEArchiveBatchEntity(
                UUID.randomUUID().toString(),
                RequestType.CONTINUOUS,
                EArchiveBatchStatus.FAILED,
                DateUtils.addDays(currentDate, -5),
                2110100000000002L,
                1,
                "/tmp/batch",
                "{\"2110100000000003\"}",
                null));

        eArchiveBatchUserMessageDao.create(batch2, Arrays.asList(
                messageDaoTestUtil.createUserMessageLog(UUID.randomUUID().toString(),currentDate).getEntityId()
        ));

        batch3 = eArchiveBatchDao.merge(EArchiveTestUtils.createEArchiveBatchEntity(
                UUID.randomUUID().toString(),
                RequestType.MANUAL,
                EArchiveBatchStatus.EXPORTED,
                DateUtils.addDays(currentDate, 0),
                2110100000000003L,
                1,
                "/tmp/batch",
                "{\"2110100000000004\"}",
                batch2.getEntityId() )); // is copy from 2
        eArchiveBatchUserMessageDao.create(batch3, Arrays.asList(
                messageDaoTestUtil.createUserMessageLog(UUID.randomUUID().toString(),currentDate).getEntityId()));

    }

    @Test
    @Transactional
    public void updateStartDateContinuousArchive() {

        Long startMessageDate = 21102610L;
        eArchivingService.updateStartDateContinuousArchive(startMessageDate);

        Assert.assertEquals(dateToPKUserMessageId(startMessageDate),
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

        Assert.assertEquals(dateToPKUserMessageId(startMessageDate),
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
        EArchiveBatchFilter filter  =new EArchiveBatchFilter( );
        List<EArchiveBatchRequestDTO> batchRequestsCount = eArchivingService.getBatchRequestList(filter);
        Assert.assertEquals(2, batchRequestsCount.size());
        // descending order
        Assert.assertEquals(uml1.getEntityId(), batchRequestsCount.get(1).getMessageStartId().longValue());
        Assert.assertEquals(uml3.getEntityId(), batchRequestsCount.get(1).getMessageEndId().longValue());
    }
    @Test
    @Transactional
    public void getBatchListWithExported() {
        EArchiveBatchFilter filter  =new EArchiveBatchFilter();
        filter.setShowReExported(Boolean.TRUE);
        List<EArchiveBatchRequestDTO> batchRequestsCount = eArchivingService.getBatchRequestList(filter);
        Assert.assertEquals(3, batchRequestsCount.size());
        Assert.assertEquals(uml1.getEntityId(), batchRequestsCount.get(2).getMessageStartId().longValue());
        Assert.assertEquals(uml3.getEntityId(), batchRequestsCount.get(2).getMessageEndId().longValue());
    }

    @Test
    @Transactional
    public void getBatchListFilterDates() {
        Date currentDate = Calendar.getInstance().getTime();
        EArchiveBatchFilter filter  =new EArchiveBatchFilter(null, DateUtils.addDays(currentDate, -40) , DateUtils.addDays(currentDate, -20), null, null );
        List<EArchiveBatchRequestDTO> batchRequestsCount = eArchivingService.getBatchRequestList(filter);
        // second batch2 with only one message
        Assert.assertEquals(1, batchRequestsCount.size());
    }

    @Test
    @Transactional
    public void getBatchListFilterStatus() {
        EArchiveBatchFilter filter  =new EArchiveBatchFilter(RequestType.CONTINUOUS.name(), null ,null, null, null );
        List<EArchiveBatchRequestDTO> batchRequestsCount = eArchivingService.getBatchRequestList(filter);
        // second batch2 with only one message
        Assert.assertEquals(1, batchRequestsCount.size());
        Assert.assertEquals(batchRequestsCount.get(0).getMessageEndId(), batchRequestsCount.get(0).getMessageEndId());
    }

    @Test
    @Transactional
    public void getBatchUserMessageList() {
        ListUserMessageDto listUserMessageDto = eArchivingService.getBatchUserMessageList(batch1.getBatchId());

        Assert.assertEquals(3, listUserMessageDto.getUserMessageDtos().size());
    }


    @Test
    @Transactional
    public void getNotArchivedMessages() {
        Date currentDate = Calendar.getInstance().getTime();
        ListUserMessageDto listUserMessageDto = eArchivingService.getNotArchivedMessages(DateUtils.addDays(currentDate, -30),
                DateUtils.addDays(currentDate, 1), null, null);

        Assert.assertEquals(1, listUserMessageDto.getUserMessageDtos().size());
        Assert.assertEquals(notArchivedUML.getEntityId(), listUserMessageDto.getUserMessageDtos().get(0).getEntityId());
    }

}
