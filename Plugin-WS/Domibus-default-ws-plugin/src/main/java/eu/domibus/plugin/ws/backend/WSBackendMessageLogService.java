package eu.domibus.plugin.ws.backend;

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
            final String fromPartyId,
            final String originalSender,
            final String finalRecipient,
            LocalDateTime receivedFrom,
            LocalDateTime receivedTo,
            int maxPendingMessagesRetrieveCount);
}

