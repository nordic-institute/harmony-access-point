package eu.domibus.plugin.ws.backend;

import eu.domibus.plugin.ws.exception.WSPluginException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author François Gautier
 * @since 5.1
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
     * @throws WSPluginException if not all messages are found/updated
     */
    void updateForRetry(List<String> messageIDs) throws WSPluginException;
}

