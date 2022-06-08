package eu.domibus.plugin.ws.backend;

import eu.domibus.plugin.ws.generated.body.FaultDetail;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Catalin Enache
 * @since 5.0
 */
public interface WSBackendMessageLogService {

    void create(WSBackendMessageLogEntity wsBackendMessageLogEntity);

    WSBackendMessageLogEntity findByMessageId(final String messageId);

    void delete(WSBackendMessageLogEntity wsBackendMessageLogEntity);

    List<WSBackendMessageLogEntity> findAllWithFilter(
            final String messageId,
            final String originalSender,
            final String finalRecipient,
            LocalDateTime receivedFrom,
            LocalDateTime receivedTo,
            int maxPendingMessagesRetrieveCount);

    /**
     * Update the {@link WSBackendMessageLogEntity} for retry the push
     *
     * @param messageIDs list of messageId to update for retry the push
     * @return null if all messageId are found
     */
    FaultDetail updateForRetry(List<String> messageIDs);
}

