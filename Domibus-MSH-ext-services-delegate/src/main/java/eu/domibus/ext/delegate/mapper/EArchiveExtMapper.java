package eu.domibus.ext.delegate.mapper;

import eu.domibus.api.earchive.EArchiveBatchRequestDTO;
import eu.domibus.ext.domain.archive.BatchStatusDTO;
import eu.domibus.ext.domain.archive.ExportedBatchDTO;
import eu.domibus.ext.domain.archive.QueuedBatchDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * @author Joze Rihtarsic
 * @since 5.0
 */
@Mapper(componentModel = "spring")
public interface EArchiveExtMapper {


    @Mapping(source = "timestamp", target = "enqueuedTimestamp")
    @Mapping(source = "messageEndId", target = "messageEndDate", qualifiedByName = "messageIdToMessageDateHour")
    @Mapping(source = "messageStartId", target = "messageStartDate", qualifiedByName = "messageIdToMessageDateHour")
    QueuedBatchDTO archiveBatchToQueuedBatch(EArchiveBatchRequestDTO archiveBatchDTO);

    @Mapping(source = "timestamp", target = "enqueuedTimestamp")
    @Mapping(source = "messageEndId", target = "messageEndDate", qualifiedByName = "messageIdToMessageDateHour")
    @Mapping(source = "messageStartId", target = "messageStartDate", qualifiedByName = "messageIdToMessageDateHour")
    ExportedBatchDTO archiveBatchToExportBatch(EArchiveBatchRequestDTO archiveBatchDTO);

    BatchStatusDTO archiveBatchToBatchStatus(EArchiveBatchRequestDTO archiveBatchDTO);

    @Named("messageIdToMessageDateHour")
    default Long messageIdToMessageDateHour(Long messageId) {
        return messageId == null ? null : messageId / 10000000000L;
    }
}
