package eu.domibus.core.earchive;

import eu.domibus.AbstractIT;
import eu.domibus.api.earchive.EArchiveBatchFilter;
import eu.domibus.api.util.DateUtil;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import static eu.domibus.api.model.DomibusDatePrefixedSequenceIdGeneratorGenerator.dateToPKUserMessageId;
import static eu.domibus.core.earchive.EArchivingDefaultService.CONTINUOUS_ID;
import static eu.domibus.core.earchive.EArchivingDefaultService.SANITY_ID;

/**
 * @author François Gautier
 * @since 5.0
 */
public class EArchivingServiceIT extends AbstractIT {

    @Autowired
    private EArchivingDefaultService eArchivingService;

    @Autowired
    private EArchiveBatchStartDao eArchiveBatchStartDao;

    @Autowired
    private EArchiveBatchDao eArchiveBatchDao;

    @Before
    @Transactional
    public void setUp() throws Exception {
        Assert.assertEquals(101000000000000L, ((long) eArchiveBatchStartDao.findByReference(CONTINUOUS_ID).getLastPkUserMessage()));
        Assert.assertEquals(101000000000000L, ((long) eArchiveBatchStartDao.findByReference(SANITY_ID).getLastPkUserMessage()));
        // prepare

        Date currentDate = Calendar.getInstance().getTime();
        eArchiveBatchDao.merge(EArchiveTestUtils.createEArchiveBatchEntity(
                UUID.randomUUID().toString(),
                RequestType.CONTINUOUS,
                EArchiveBatchStatus.STARTED,
                DateUtils.addDays(currentDate, -10),
                2110100000000001L,
                1,
                "/tmp/batch",
                "{\"2110100000000002\"}"));

        eArchiveBatchDao.merge(EArchiveTestUtils.createEArchiveBatchEntity(
                UUID.randomUUID().toString(),
                RequestType.MANUAL,
                EArchiveBatchStatus.FAILED,
                DateUtils.addDays(currentDate, -5),
                2110100000000002L,
                1,
                "/tmp/batch",
                "{\"2110100000000003\"}"));

        eArchiveBatchDao.merge(EArchiveTestUtils.createEArchiveBatchEntity(
                UUID.randomUUID().toString(),
                RequestType.CONTINUOUS,
                EArchiveBatchStatus.STARTING,
                DateUtils.addDays(currentDate, 0),
                2110100000000003L,
                1,

                "/tmp/batch",
                "{\"2110100000000004\"}"));
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
        Long batchRequestsCount = eArchivingService.getQueuedBatchRequestsCount(new EArchiveBatchFilter());
        Assert.assertEquals(3, batchRequestsCount.longValue());
    }

    @Test
    @Transactional
    public void getBatchCountWithFilter1() {
        Date currentDate = Calendar.getInstance().getTime();
        EArchiveBatchFilter filter  =new EArchiveBatchFilter(RequestType.CONTINUOUS.name(), DateUtils.addDays(currentDate, -30) , DateUtils.addDays(currentDate, -1), null, null );
        Long batchRequestsCount = eArchivingService.getQueuedBatchRequestsCount(filter);
        Assert.assertEquals(1, batchRequestsCount.longValue());
    }
}
