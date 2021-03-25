package eu.domibus.core.message;

import eu.domibus.api.model.*;
import eu.domibus.api.message.MessageSubtype;
import eu.domibus.api.ebms3.Ebms3Constants;
import eu.domibus.api.model.SignalMessageLog;
import eu.domibus.core.message.signal.SignalMessageLogDao;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.core.replication.UIReplicationSignalService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Date;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Service
public class UserMessageLogDefaultService {

    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageLogDefaultService.class);

    @Autowired
    protected UserMessageLogDao userMessageLogDao;

    @Autowired
    protected SignalMessageLogDao signalMessageLogDao;

    @Autowired
    protected BackendNotificationService backendNotificationService;

    @Autowired
    protected UIReplicationSignalService uiReplicationSignalService;

    @Autowired
    protected MessageStatusDao messageStatusDao;

    @Autowired
    protected MshRoleDao mshRoleDao;

    @Autowired
    protected NotificationStatusDao notificationStatusDao;

    @Autowired
    protected MessageSubtypeDao messageSubtypeDao;

    private UserMessageLog createUserMessageLog(UserMessage userMessage, String messageStatus, String notificationStatus, String mshRole, Integer maxAttempts, String mpc, String backendName, String endpoint) {
        UserMessageLog userMessageLog = new UserMessageLog();
        userMessageLog.setUserMessage(userMessage);
        final MessageStatusEntity messageStatusEntity = messageStatusDao.findMessageStatus(MessageStatus.valueOf(messageStatus));
        userMessageLog.setMessageStatus(messageStatusEntity);

        final MSHRoleEntity mshRoleEntity = mshRoleDao.findByRole(MSHRole.valueOf(mshRole));
        userMessageLog.setMshRole(mshRoleEntity);

        final NotificationStatusEntity notificationStatusEntity = notificationStatusDao.findByStatus(NotificationStatus.valueOf(notificationStatus));
        userMessageLog.setNotificationStatus(notificationStatusEntity);

        userMessageLog.setSendAttemptsMax(maxAttempts);
        userMessageLog.setBackend(backendName);

        return userMessageLog;
    }

    @Transactional
    public UserMessageLog save(UserMessage userMessage, String messageStatus, String notificationStatus, String mshRole, Integer maxAttempts, String mpc, String backendName, String endpoint, String service, String action, Boolean sourceMessage, Boolean messageFragment) {
        final MessageStatus status = MessageStatus.valueOf(messageStatus);
        // Builds the user message log
        final UserMessageLog userMessageLog = createUserMessageLog(userMessage, messageStatus, notificationStatus, mshRole, maxAttempts, mpc, backendName, endpoint);
        userMessageLog.setUserMessage(userMessage);

        // Sets the subtype
        MessageSubtype messageSubtype = null;
        if (checkTestMessage(service, action)) {
            messageSubtype = MessageSubtype.TEST;
        }
        final MessageSubtypeEntity messageSubtypeEntity = messageSubtypeDao.findByType(messageSubtype);
        userMessageLog.setMessageSubtype(messageSubtypeEntity);
        if (!MessageSubtype.TEST.equals(messageSubtype)) {
            backendNotificationService.notifyOfMessageStatusChange(userMessage, userMessageLog, status, new Timestamp(System.currentTimeMillis()));
        }
        final MessageStatusEntity messageStatusEntity = messageStatusDao.findMessageStatus(status);
        //we set the status after we send the status change event; otherwise the old status and the new status would be the same
        userMessageLog.setMessageStatus(messageStatusEntity);
        userMessageLogDao.create(userMessageLog);

        return userMessageLog;
    }

    protected void updateUserMessageStatus(final UserMessage userMessage, final UserMessageLog messageLog, final MessageStatus newStatus) {
        LOG.debug("Updating message status to [{}]", newStatus);

        if (!messageLog.isTestMessage()) {
            backendNotificationService.notifyOfMessageStatusChange(userMessage, messageLog, newStatus, new Timestamp(System.currentTimeMillis()));
        }
        userMessageLogDao.setMessageStatus(messageLog, newStatus);

        uiReplicationSignalService.messageChange(userMessage.getMessageId());
    }

    public void setMessageAsDeleted(final UserMessage userMessage, final UserMessageLog messageLog) {
        updateUserMessageStatus(userMessage, messageLog, MessageStatus.DELETED);
    }

    /**
     * Find the {@link SignalMessageLog} and set to {@link MessageStatus#DELETED}
     * Propagate the change to the UiReplication
     *
     */
    public boolean setSignalMessageAsDeleted(final SignalMessage signalMessage) {

        if (signalMessage == null) {
            LOG.debug("Could not delete SignalMessage: received SignalMessage is null ");
            return false;
        }
        if (isBlank(signalMessage.getSignalMessageId())) {
            LOG.debug("Could not delete SignalMessage: received messageId is empty [{}",
                    signalMessage
                    );
            return false;
        }

        String signalMessageId = signalMessage.getSignalMessageId();
        setSignalMessageAsDeleted(signalMessageId);
        LOG.debug("SignalMessage [{}] was set as DELETED.", signalMessageId);
        return true;
    }

    protected void setSignalMessageAsDeleted(final String signalMessageId) {
        final SignalMessageLog signalMessageLog = signalMessageLogDao.findByMessageId(signalMessageId);
        final MessageStatusEntity messageStatusEntity = messageStatusDao.findMessageStatus(MessageStatus.DELETED);
        signalMessageLog.setDeleted(new Date());
        signalMessageLog.setMessageStatus(messageStatusEntity);
        uiReplicationSignalService.messageChange(signalMessageId);
    }

    public void setMessageAsDownloaded(UserMessage userMessage, UserMessageLog userMessageLog) {
        updateUserMessageStatus(userMessage, userMessageLog, MessageStatus.DOWNLOADED);
    }

    public void setMessageAsAcknowledged(UserMessage userMessage, UserMessageLog userMessageLog) {
        updateUserMessageStatus(userMessage, userMessageLog, MessageStatus.ACKNOWLEDGED);
    }

    public void setMessageAsAckWithWarnings(UserMessage userMessage, UserMessageLog userMessageLog) {
        updateUserMessageStatus(userMessage, userMessageLog, MessageStatus.ACKNOWLEDGED_WITH_WARNING);
    }

    public void setMessageAsSendFailure(UserMessage userMessage, UserMessageLog userMessageLog) {
        updateUserMessageStatus(userMessage, userMessageLog, MessageStatus.SEND_FAILURE);
    }

    /**
     * Checks <code>service</code> and <code>action</code> to determine if it's a TEST message
     *
     * @param service Service
     * @param action  Action
     * @return True, if it's a test message and false otherwise
     */
    protected Boolean checkTestMessage(final String service, final String action) {
        return Ebms3Constants.TEST_SERVICE.equalsIgnoreCase(service)
                && Ebms3Constants.TEST_ACTION.equalsIgnoreCase(action);

    }
}
