package eu.domibus.core.ebms3.sender.retry;

import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
public interface RetryService {

    void enqueueMessage(String messageId);

    List<String> getMessagesNotAlreadyScheduled();

    void resetWaitingForReceiptPullMessages();

    void bulkExpirePullMessages();

    void bulkDeletePullMessages();
}
