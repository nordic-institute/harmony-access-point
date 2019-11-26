package eu.domibus.core.replication;

/**
 * Signals the creation or the update of a User or Signal message
 *
 * since 4.1.2
 * @author Catalin Enache
 */
public interface UIReplicationSignalService {

    boolean isReplicationEnabled();

    void userMessageReceived(String messageId);

    void userMessageSubmitted(String messageId);

    void messageChange(String messageId);

    void signalMessageSubmitted(String messageId);

    void signalMessageReceived(String messageId);
}
