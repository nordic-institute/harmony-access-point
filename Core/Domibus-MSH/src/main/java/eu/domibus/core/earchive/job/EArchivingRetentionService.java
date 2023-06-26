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
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.util.*;

import static com.codahale.metrics.MetricRegistry.name;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

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
        final Boolean eArchiveActive = domibusPropertyProvider.getBooleanProperty(DOMIBUS_EARCHIVE_ACTIVE);
        if (!eArchiveActive) {
            LOG.debug("eArchiving is not enabled for current domain, so no storage cleaning.");
            return;
        }

        final Integer maxBatchesToDelete = domibusPropertyProvider.getIntegerProperty(DOMIBUS_EARCHIVE_RETENTION_DELETE_MAX);

        Set<EArchiveBatchStatus> eligibleStatuses = EnumSet.of(EArchiveBatchStatus.EXPIRED,
                EArchiveBatchStatus.ARCHIVED,
                EArchiveBatchStatus.ARCHIVE_FAILED,
                EArchiveBatchStatus.FAILED);

        final Boolean deleteFromDB = domibusPropertyProvider.getBooleanProperty(DOMIBUS_EARCHIVE_RETENTION_DELETE_DB);
        if (deleteFromDB) {
            // for getting old, not-deleted batches from db (when deleteFromDB was false)
            eligibleStatuses.add(EArchiveBatchStatus.DELETED);
        }
        List<EArchiveBatchEntity> batches = eArchiveBatchDao.findBatchesByStatus(new ArrayList<>(eligibleStatuses), maxBatchesToDelete);
        LOG.debug("[{}] batches eligible for deletion found", batches.size());

        batches.forEach((batch) -> deleteBatch(batch, deleteFromDB));
    }

    protected void deleteBatch(EArchiveBatchEntity batch, Boolean deleteFromDB) {
        com.codahale.metrics.Timer.Context metricDeleteBatch = metricRegistry.timer(name("earchive_cleanStoredBatches", "delete_one_batch", "timer")).time();

        LOG.debug("Deleting eArchive structure for batchId [{}]", batch.getBatchId());
        Path folderToClean = Paths.get(storageProvider.getCurrentStorage().getStorageDirectory().getAbsolutePath(), batch.getBatchId());
        LOG.debug("Clean folder [{}]", folderToClean);

        try {
            if (batch.getEArchiveBatchStatus() != EArchiveBatchStatus.DELETED) {
                LOG.debug("Deleting eArchive files from disk [{}]", folderToClean);
                FileUtils.deleteDirectory(folderToClean.toFile());
            }
            if (deleteFromDB) {
                LOG.debug("Deleting physically batch [{}] from the database", batch.getBatchId());
                eArchiveBatchDao.delete(batch);
            } else {
                LOG.debug("Marking as deleted the batch [{}]", batch.getBatchId());
                eArchiveBatchDao.setStatus(batch, EArchiveBatchStatus.DELETED, "", "");
            }
        } catch (Exception e) {
            LOG.error("Error when deleting batch [{}]", batch.getBatchId(), e);
        }
        metricDeleteBatch.stop();
    }
}
