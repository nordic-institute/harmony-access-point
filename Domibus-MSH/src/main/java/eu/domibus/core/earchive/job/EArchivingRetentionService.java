package eu.domibus.core.earchive.job;

import eu.domibus.api.earchive.EArchiveBatchStatus;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.earchive.EArchiveBatchDao;
import eu.domibus.core.earchive.EArchiveBatchEntity;
import eu.domibus.core.earchive.storage.EArchiveFileStorageProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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

    public void cleanStoredBatches() {
        final Integer maxBatchesToDelete = domibusPropertyProvider.getIntegerProperty(DOMIBUS_EARCHIVE_RETENTION_DELETE_MAX);

        List<EArchiveBatchStatus> eligibleStatuses = Arrays.asList(
                EArchiveBatchStatus.EXPIRED,
                EArchiveBatchStatus.ARCHIVED,
                EArchiveBatchStatus.ARCHIVE_FAILED,
                EArchiveBatchStatus.FAILED);
        List<EArchiveBatchEntity> batches = eArchiveBatchDao.findBatchesByStatus(eligibleStatuses, maxBatchesToDelete);

        LOG.debug("[{}] batches eligible for deletion found", batches.size());

        batches.stream().forEach(batch -> deleteBatch(batch));
    }

    protected void deleteBatch(EArchiveBatchEntity batch) {
        LOG.debug("Deleting earchive structure for batchId [{}]", batch.getBatchId());

        try (FileObject batchDirectory = VFS.getManager().resolveFile(storageProvider.getCurrentStorage().getStorageDirectory(), batch.getBatchId())) {
            batchDirectory.deleteAll();
            batch.setEArchiveBatchStatus(EArchiveBatchStatus.DELETED);
            VFS.getManager().closeFileSystem(batchDirectory.getFileSystem());
        } catch (Exception e) {
            LOG.error("Error when deleting batch [{}]", batch.getBatchId(), e);
        }
    }
}
