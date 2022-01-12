package eu.domibus.test;

import eu.domibus.api.model.MessageStatus;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.user.ui.UserRoleDao;
import org.awaitility.Awaitility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.with;

@Service
public class DomibusConditionUtil {

    @Autowired
    protected UserRoleDao userRoleDao;

    @Autowired
    protected UserMessageLogDao userMessageLogDao;

    public void waitUntilDatabaseIsInitialized() {
        with().pollInterval(500, TimeUnit.MILLISECONDS).await().atMost(120, TimeUnit.SECONDS).until(databaseIsInitialized());
    }

    public Callable<Boolean> databaseIsInitialized() {
        return () -> {
            try {
                return userRoleDao.listRoles().size() > 0;
            } catch (Exception e) {
            }
            return false;
        };
    }

    public void waitUntilMessageHasStatus(String messageId, MessageStatus messageStatus) {
        Awaitility.with().pollInterval(500, TimeUnit.MILLISECONDS).await().atMost(20, TimeUnit.SECONDS).until(messageHasStatus(messageId, messageStatus));
    }

    public void waitUntilMessageIsAcknowledged(String messageId) {
        waitUntilMessageHasStatus(messageId, MessageStatus.ACKNOWLEDGED);
    }

    public void waitUntilMessageIsReceived(String messageId) {
        waitUntilMessageHasStatus(messageId, MessageStatus.RECEIVED);
    }

    public void waitUntilMessageIsInWaitingForRetry(String messageId) {
        waitUntilMessageHasStatus(messageId, MessageStatus.WAITING_FOR_RETRY);
    }

    public Callable<Boolean> messageHasStatus(String messageId, MessageStatus messageStatus) {
        return () -> messageStatus == userMessageLogDao.getMessageStatus(messageId);
    }
}
