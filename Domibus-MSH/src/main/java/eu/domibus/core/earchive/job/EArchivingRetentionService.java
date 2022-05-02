package eu.domibus.core.earchive.job;

import eu.domibus.api.earchive.EArchiveBatchStatus;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.earchive.EArchiveBatchDao;
import eu.domibus.core.earchive.EArchiveBatchEntity;
import eu.domibus.core.earchive.storage.EArchiveFileStorageProvider;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

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

    public EArchivingRetentionService(EArchiveBatchDao eArchiveBatchDao, DomibusPropertyProvider domibusPropertyProvider, EArchiveFileStorageProvider storageProvider) {
        this.eArchiveBatchDao = eArchiveBatchDao;
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.storageProvider = storageProvider;
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
        LOG.debug("Deleting earchive structure for batchId [{}]", batch.getBatchId());
        Path folderToClean = Paths.get(storageProvider.getCurrentStorage().getStorageDirectory().getAbsolutePath(), batch.getBatchId());
        LOG.info("Clean folder [{}]", folderToClean);
        try (Stream<Path> walk = Files.walk(folderToClean)) {
            walk.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
            batch.setEArchiveBatchStatus(EArchiveBatchStatus.DELETED);
        } catch (Exception e) {
            LOG.error("Error when deleting batch [{}]", batch.getBatchId(), e);
        }
    }
}
