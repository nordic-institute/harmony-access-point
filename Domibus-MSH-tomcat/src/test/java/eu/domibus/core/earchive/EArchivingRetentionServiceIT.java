package eu.domibus.core.earchive;

import eu.domibus.AbstractIT;
import eu.domibus.api.earchive.EArchiveBatchFilter;
import eu.domibus.api.earchive.EArchiveBatchRequestDTO;
import eu.domibus.api.earchive.EArchiveBatchStatus;
import eu.domibus.api.earchive.EArchiveRequestType;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.earchive.eark.FileSystemEArchivePersistenceE2EIT;
import eu.domibus.core.earchive.job.EArchivingRetentionService;
import eu.domibus.core.earchive.storage.EArchiveFileStorageProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static eu.domibus.api.earchive.EArchiveBatchStatus.DELETED;
import static eu.domibus.api.earchive.EArchiveBatchStatus.EXPIRED;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_EARCHIVE_ACTIVE;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_EARCHIVE_STORAGE_LOCATION;
import static java.util.Collections.singletonList;

/**
 * @author Ion Perpegel
 * @since 5.0
 */

public class EArchivingRetentionServiceIT extends AbstractIT {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EArchivingRetentionServiceIT.class);

    @Autowired
    EArchivingDefaultService eArchivingService;

    @Autowired
    EArchiveBatchDao eArchiveBatchDao;

    @Autowired
    EArchivingRetentionService eArchivingRetentionService;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected EArchiveFileStorageProvider storageProvider;

    EArchiveBatchEntity batch1, batch2, batch3, batch4;
    private File temp;

    @Before
    public void setUp() throws Exception {
        waitUntilDatabaseIsInitialized();

        temp = Files.createTempDirectory(Paths.get("target"), "tmpDirPrefix").toFile();
        LOG.info("temp folder created: [{}]", temp.getAbsolutePath());

        domibusPropertyProvider.setProperty(DomainService.DEFAULT_DOMAIN, DOMIBUS_EARCHIVE_ACTIVE, "true");
        domibusPropertyProvider.setProperty(DOMIBUS_EARCHIVE_ACTIVE, "true");
        domibusPropertyProvider.setProperty(DomainService.DEFAULT_DOMAIN, DOMIBUS_EARCHIVE_STORAGE_LOCATION, temp.getAbsolutePath());
        domibusPropertyProvider.setProperty(DOMIBUS_EARCHIVE_STORAGE_LOCATION, temp.getAbsolutePath());

        storageProvider.getCurrentStorage().reset();

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

    @After
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(temp);
        LOG.info("temp folder deleted: [{}]", temp.getAbsolutePath());
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

    @Test
    @Transactional
    public void cleanStoredBatches() {
        eArchivingRetentionService.cleanStoredBatches();

        EArchiveBatchFilter filter = new EArchiveBatchFilter(singletonList(DELETED), null, null, null, null, null, null, null, null);
        List<EArchiveBatchRequestDTO> batchRequestsCount = eArchivingService.getBatchRequestList(filter);
        // Only one expired
        Assert.assertEquals(1, batchRequestsCount.size());
    }
}
