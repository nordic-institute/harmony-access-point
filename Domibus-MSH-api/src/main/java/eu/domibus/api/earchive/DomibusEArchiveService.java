package eu.domibus.api.earchive;

import java.util.Date;
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

    List<String> getBatchUserMessageList(String batchId, Integer pageStart, Integer pageSize);

    Long getBatchUserMessageListCount(String batchId);

    /**
     * Get not archive message from start message id till end message id.
     * @param startMessageId - start message id for the select of not archived messages
     * @param endMessageId - end  message id for the select of not archived messages
     * @param pageStart - return result for page
     * @param pageSize - return page size
     * @return list of message ids
     */
    List<String> getNotArchivedMessages(Long startMessageId, Long endMessageId, Integer pageStart, Integer pageSize);

    /**
     * Get not archive message count from start message id till end message id.
     * @param startMessageId - start message id for the select of not archived messages
     * @param endMessageId - end  message id for the select of not archived messages
     * @return count of message ids
     */
    Long getNotArchivedMessagesCount(Long startMessageId, Long endMessageId);

    EArchiveBatchRequestDTO reExportBatch(String batchId);

    EArchiveBatchRequestDTO getBatch(String batchId);

    EArchiveBatchRequestDTO setBatchClientStatus(String batchId, EArchiveBatchStatus batchStatus, String message);
}
