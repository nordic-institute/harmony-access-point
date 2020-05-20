package eu.domibus.core.message;

import eu.domibus.api.message.MessageSubtype;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.core.ebms3.Ebms3Constants;
import eu.domibus.core.message.signal.SignalMessageLog;
import eu.domibus.core.message.signal.SignalMessageLogDao;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.core.plugin.notification.NotificationStatus;
import eu.domibus.core.replication.UIReplicationSignalService;
import eu.domibus.ebms3.common.model.MessageType;
import eu.domibus.ebms3.common.model.SignalMessage;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;

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

    private UserMessageLog createUserMessageLog(String messageId, String messageStatus, String notificationStatus, String mshRole, Integer maxAttempts, String mpc, String backendName, String endpoint) {
        // Builds the user message log
        UserMessageLogEntityBuilder umlBuilder = UserMessageLogEntityBuilder.create()
                .setMessageId(messageId)
                .setMessageStatus(MessageStatus.valueOf(messageStatus))
                .setMshRole(MSHRole.valueOf(mshRole))
                .setNotificationStatus(NotificationStatus.valueOf(notificationStatus))
                .setMpc(mpc)
                .setSendAttemptsMax(maxAttempts)
                .setBackendName(backendName)
                .setEndpoint(endpoint);

        return umlBuilder.build();
    }

    @Transactional
    public UserMessageLog save(String messageId, String messageStatus, String notificationStatus, String mshRole, Integer maxAttempts, String mpc, String backendName, String endpoint, String service, String action, Boolean sourceMessage, Boolean messageFragment) {
        final MessageStatus status = MessageStatus.valueOf(messageStatus);
        // Builds the user message log
        final UserMessageLog userMessageLog = createUserMessageLog(messageId, messageStatus, notificationStatus, mshRole, maxAttempts, mpc, backendName, endpoint);
        userMessageLog.setSourceMessage(sourceMessage);
        userMessageLog.setMessageFragment(messageFragment);

        // Sets the subtype
        MessageSubtype messageSubtype = null;
        if (checkTestMessage(service, action)) {
            messageSubtype = MessageSubtype.TEST;
        }
        userMessageLog.setMessageSubtype(messageSubtype);
        if (!MessageSubtype.TEST.equals(messageSubtype)) {
            backendNotificationService.notifyOfMessageStatusChange(userMessageLog, status, new Timestamp(System.currentTimeMillis()));
        }
        //we set the status after we send the status change event; otherwise the old status and the new status would be the same
        userMessageLog.setMessageStatus(status);
        userMessageLogDao.create(userMessageLog);

        return userMessageLog;
    }

    protected void updateUserMessageStatus(final UserMessage userMessage, final UserMessageLog messageLog, final MessageStatus newStatus) {
        LOG.debug("Updating message status to [{}]", newStatus);

        if (MessageType.USER_MESSAGE == messageLog.getMessageType() && !messageLog.isTestMessage()) {
            backendNotificationService.notifyOfMessageStatusChange(userMessage, messageLog, newStatus, new Timestamp(System.currentTimeMillis()));
        }
        userMessageLogDao.setMessageStatus(messageLog, newStatus);

        uiReplicationSignalService.messageChange(messageLog.getMessageId());
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
        if (signalMessage.getMessageInfo() == null) {
            LOG.debug("Could not delete SignalMessage: received messageInfo is null [{}]", signalMessage);
            return false;
        }
        if (isBlank(signalMessage.getMessageInfo().getMessageId())) {
            LOG.debug("Could not delete SignalMessage: received messageId is empty [{}|{}]",
                    signalMessage,
                    signalMessage.getMessageInfo());
            return false;
        }

        String msgId = signalMessage.getMessageInfo().getMessageId();
        setSignalMessageAsDeleted(msgId);
        LOG.debug("SignalMessage [{}] was set as DELETED.", msgId);
        return true;
    }

    protected void setSignalMessageAsDeleted(final String signalMessageId) {
        final SignalMessageLog signalMessageLog = signalMessageLogDao.findByMessageId(signalMessageId);
        signalMessageLogDao.setMessageStatus(signalMessageLog, MessageStatus.DELETED);
        uiReplicationSignalService.messageChange(signalMessageLog.getMessageId());
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
