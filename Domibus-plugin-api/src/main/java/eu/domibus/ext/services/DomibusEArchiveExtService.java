package eu.domibus.ext.services;

import eu.domibus.ext.domain.archive.*;

import java.util.List;

/**
 * @author François Gautier
 * @since 5.0
 */
public interface DomibusEArchiveExtService {

    void updateStartDateContinuousArchive(Long startDate);

    Long getStartDateContinuousArchive();

    void updateStartDateSanityArchive(Long startDate);

    Long getStartDateSanityArchive();

    Long getQueuedBatchRequestsCount(QueuedBatchFilterDTO filter);

    List<BatchDTO> getQueuedBatchRequests(QueuedBatchFilterDTO filter, Integer pageStart, Integer pageSize);

    Long getExportedBatchRequestsCount(ExportedBatchFilterDTO filter);

    List<BatchDTO> getExportedBatchRequests(ExportedBatchFilterDTO filter, Integer pageStart, Integer pageSize);

    Long getBatchMessageCount(String batchId);

    List<String> getBatchMessageIds(String batchId, Integer pageStart, Integer pageSize);

    BatchStatusDTO reExportBatch(String batchId);

    BatchDTO getBatch(String batchId);

    BatchStatusDTO setBatchClientStatus(String batchId, BatchArchiveStatusType batchStatus, String message);

    List<String> getNotArchivedMessages(NotArchivedMessagesFilterDTO filter, Integer pageStart, Integer pageSize);

    Long getNotArchivedMessageCount(NotArchivedMessagesFilterDTO filter);
}
