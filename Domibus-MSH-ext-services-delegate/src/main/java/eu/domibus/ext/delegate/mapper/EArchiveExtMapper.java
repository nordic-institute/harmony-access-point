package eu.domibus.ext.delegate.mapper;

import eu.domibus.api.earchive.EArchiveBatchRequestDTO;
import eu.domibus.ext.domain.archive.BatchStatusDTO;
import eu.domibus.ext.domain.archive.ExportedBatchDTO;
import eu.domibus.ext.domain.archive.QueuedBatchDTO;
import org.mapstruct.Mapper;

/**
 * @author Joze Rihtarsic
 * @since 5.0
 */
@Mapper(componentModel = "spring")
public interface EArchiveExtMapper {

    QueuedBatchDTO archiveBatchToQueuedBatch(EArchiveBatchRequestDTO archiveBatchDTO);

    ExportedBatchDTO archiveBatchToExportBatch(EArchiveBatchRequestDTO archiveBatchDTO);

    BatchStatusDTO archiveBatchToBatchStatus(EArchiveBatchRequestDTO archiveBatchDTO);
}
