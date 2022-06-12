package eu.domibus.api.earchive;

import java.util.List;

/**
 * @author François Gautier
 * @since 5.0
 */
public interface DomibusEArchiveService {

    void updateStartDateContinuousArchive(Long startDate);

    void updateStartDateSanityArchive(Long startDate);

    Long getStartDateContinuousArchive();

    Long getStartDateSanityArchive();

    List<EArchiveBatchRequestDTO> getBatchRequestList(EArchiveBatchFilter filter);

    Long getBatchRequestListCount(EArchiveBatchFilter filter);

    /**
     * Get exported messages ids in batch. If batch is in status: STARTED, QUEUED or FAILED, it returns empty list!
     * If batch for "batch id" doesn't exist, method throws and error DomibusEArchiveException.
     *
     * @param batchId   - batch message id
     * @param pageStart - return result for the page
     * @param pageSize  - return result for the page size
     * @return list of exported message ids in a batch for given page
     */
    List<String> getExportedBatchUserMessageList(String batchId, Integer pageStart, Integer pageSize);

    /**
     * Get count of exported messages in batch. If batch is in status STARTED, QUEUED or FAILED, it returns 0!
     * If batch for "batch id" doesn't exist, method throws and error DomibusEArchiveException.
     *
     * @param batchId - batch message id
     * @return number of exported messages in a batch
     */
    Long getExportedBatchUserMessageListCount(String batchId);

    /**
     * Get not archive message from start message id till end message id.
     *
     * @param startMessageId - start message id for the select of not archived messages
     * @param endMessageId   - end  message id for the select of not archived messages
     * @param pageStart      - return result for page
     * @param pageSize       - return result for the page size
     * @return list of message ids
     */
    List<String> getNotArchivedMessages(Long startMessageId, Long endMessageId, Integer pageStart, Integer pageSize);

    /**
     * Get not archive message count from start message id till end message id.
     *
     * @param startMessageId - start message id for the select of not archived messages
     * @param endMessageId   - end  message id for the select of not archived messages
     * @return count of message ids
     */
    Long getNotArchivedMessagesCount(Long startMessageId, Long endMessageId);

    EArchiveBatchRequestDTO reExportBatch(String batchId);

    EArchiveBatchRequestDTO getBatch(String batchId);

    EArchiveBatchRequestDTO setBatchClientStatus(String batchId, EArchiveBatchStatus batchStatus, String message);
}
