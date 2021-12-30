package eu.domibus.core.earchive;

import eu.domibus.AbstractIT;
import eu.domibus.api.earchive.EArchiveBatchFilter;
import eu.domibus.api.earchive.EArchiveBatchRequestDTO;
import eu.domibus.api.earchive.EArchiveBatchStatus;
import eu.domibus.api.earchive.EArchiveRequestType;
import eu.domibus.core.earchive.job.EArchivingRetentionService;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static eu.domibus.api.earchive.EArchiveBatchStatus.EXPIRED;
import static java.util.Collections.singletonList;

/**
 * @author Ion Perpegel
 * @since 5.0
 */

public class EArchivingRetentionServiceIT extends AbstractIT {

    @Autowired
    EArchivingDefaultService eArchivingService;

    @Autowired
    EArchiveBatchDao eArchiveBatchDao;

    @Autowired
    EArchivingRetentionService eArchivingRetentionService;

    EArchiveBatchEntity batch1, batch2, batch3, batch4;

    @Before
    public void setUp() throws Exception {
        waitUntilDatabaseIsInitialized();
        // prepare
        Date currentDate = Calendar.getInstance().getTime();

        batch1 = eArchiveBatchDao.merge(EArchiveTestUtils.createEArchiveBatchEntity(
                UUID.randomUUID().toString(),
                EArchiveRequestType.CONTINUOUS,
                EArchiveBatchStatus.EXPORTED,
                DateUtils.addDays(currentDate, -31),
                2110100000000011L,
                2110100000000020L,
                1,
                "/tmp/batch"));

        batch2 = eArchiveBatchDao.merge(EArchiveTestUtils.createEArchiveBatchEntity(
                UUID.randomUUID().toString(),
                EArchiveRequestType.CONTINUOUS,
                EArchiveBatchStatus.STARTED,
                DateUtils.addDays(currentDate, -31),
                2110100000000011L,
                2110100000000020L,
                1,
                "/tmp/batch"));

        batch3 = eArchiveBatchDao.merge(EArchiveTestUtils.createEArchiveBatchEntity(
                UUID.randomUUID().toString(),
                EArchiveRequestType.CONTINUOUS,
                EArchiveBatchStatus.EXPORTED,
                DateUtils.addDays(currentDate, -29),
                2110100000000021L,
                2110100000000030L,
                1,
                "/tmp/batch"));

        batch4 = eArchiveBatchDao.merge(EArchiveTestUtils.createEArchiveBatchEntity(
                UUID.randomUUID().toString(),
                EArchiveRequestType.CONTINUOUS,
                EArchiveBatchStatus.FAILED,
                DateUtils.addDays(currentDate, -29),
                2110100000000021L,
                2110100000000030L,
                1,
                "/tmp/batch"));

    }


    @Test
    @Transactional
    public void getBatchListFilterDates() {
        eArchivingRetentionService.expireBatches();

//        Date currentDate = Calendar.getInstance().getTime();
        EArchiveBatchFilter filter = new EArchiveBatchFilter(singletonList(EXPIRED), null, null, null, null, null, null, null, null);
        List<EArchiveBatchRequestDTO> batchRequestsCount = eArchivingService.getBatchRequestList(filter);
        // Only one expired
        Assert.assertEquals(1, batchRequestsCount.size());
    }

}
