package eu.domibus.core.earchive.job;

import com.codahale.metrics.MetricRegistry;
import eu.domibus.api.earchive.EArchiveBatchStatus;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.earchive.EArchiveBatchDao;
import eu.domibus.core.earchive.EArchiveBatchEntity;
import eu.domibus.core.earchive.storage.EArchiveFileStorageProvider;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.codahale.metrics.MetricRegistry.name;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_EARCHIVE_RETENTION_DAYS;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_EARCHIVE_RETENTION_DELETE_MAX;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
public class EArchivingRetentionService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EArchivingRetentionService.class);

    private final EArchiveBatchDao eArchiveBatchDao;

    private final DomibusPropertyProvider domibusPropertyProvider;

    protected final EArchiveFileStorageProvider storageProvider;

    private final MetricRegistry metricRegistry;

    public EArchivingRetentionService(EArchiveBatchDao eArchiveBatchDao,
                                      DomibusPropertyProvider domibusPropertyProvider,
                                      EArchiveFileStorageProvider storageProvider,
                                      MetricRegistry metricRegistry) {
        this.eArchiveBatchDao = eArchiveBatchDao;
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.storageProvider = storageProvider;
        this.metricRegistry = metricRegistry;
    }

    @Transactional(timeout = 120) // 2 minutes
    public void expireBatches() {
        final Integer retentionInDays = domibusPropertyProvider.getIntegerProperty(DOMIBUS_EARCHIVE_RETENTION_DAYS);
        final Date limitDate = Date.from(java.time.ZonedDateTime.now(ZoneOffset.UTC).minusDays(retentionInDays).toInstant());

        LOG.debug("Expiring batches older than [{}]", limitDate);

        eArchiveBatchDao.expireBatches(limitDate);
    }

    @Timer(clazz = EArchivingRetentionService.class, value = "earchive_cleanStoredBatches")
    @Counter(clazz = EArchivingRetentionService.class, value = "earchive_cleanStoredBatches")
    public void cleanStoredBatches() {
        final Integer maxBatchesToDelete = domibusPropertyProvider.getIntegerProperty(DOMIBUS_EARCHIVE_RETENTION_DELETE_MAX);

        List<EArchiveBatchStatus> eligibleStatuses = Arrays.asList(
                EArchiveBatchStatus.EXPIRED,
                EArchiveBatchStatus.ARCHIVED,
                EArchiveBatchStatus.ARCHIVE_FAILED,
                EArchiveBatchStatus.FAILED);
        List<EArchiveBatchEntity> batches = eArchiveBatchDao.findBatchesByStatus(eligibleStatuses, maxBatchesToDelete);

        LOG.debug("[{}] batches eligible for deletion found", batches.size());

        batches.forEach(this::deleteBatch);
    }

    protected void deleteBatch(EArchiveBatchEntity batch) {
        com.codahale.metrics.Timer.Context metricDeleteBatch = metricRegistry.timer(name("earchive_cleanStoredBatches", "delete_one_batch", "timer")).time();

        LOG.debug("Deleting earchive structure for batchId [{}]", batch.getBatchId());
        Path folderToClean = Paths.get(storageProvider.getCurrentStorage().getStorageDirectory().getAbsolutePath(), batch.getBatchId());
        LOG.debug("Clean folder [{}]", folderToClean);

        try {
            FileUtils.deleteDirectory(folderToClean.toFile());
            eArchiveBatchDao.setStatus(batch, EArchiveBatchStatus.DELETED, "", "");
        } catch (Exception e) {
            LOG.error("Error when deleting batch [{}]", batch.getBatchId(), e);
        }
        metricDeleteBatch.stop();
    }
}
