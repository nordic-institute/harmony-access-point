package eu.domibus.core.message.pull;

import java.util.List;

/**
 * @author Thomas Dussart
 * @since 3.3.4
 */

// do we need mshRole here??
public interface MessagingLockDao {

    PullMessageId getNextPullMessageToProcess(String initiator, String mpc);

    MessagingLock getLock(String messageId);

    void save(MessagingLock messagingLock);

    void delete(String messageId);

    void delete(MessagingLock messagingLock);

    MessagingLock findMessagingLockForMessageId(String messageId);

    List<MessagingLock> findStaledMessages();

    List<MessagingLock> findDeletedMessages();

//    List<MessagingLock> findReadyToPull(String mpc, String initiator);

    List<MessagingLock> findWaitingForReceipt();

}
