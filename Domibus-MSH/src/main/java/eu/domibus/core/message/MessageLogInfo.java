package eu.domibus.core.message;

import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.MessageType;
import eu.domibus.api.model.NotificationStatus;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Date;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
public class MessageLogInfo {
    // order of the fields is important for CSV generation

    private String messageId;

    private String fromPartyId;

    private String toPartyId;

    private MessageStatus messageStatus;

    private NotificationStatus notificationStatus;

    private Date received;

    private MSHRole mshRole;

    private int sendAttempts;

    private int sendAttemptsMax;

    private Date nextAttempt;

    private String nextAttemptTimezoneId;

    private Integer nextAttemptOffsetSeconds;

    private String conversationId;

    private MessageType messageType;

    private Boolean testMessage;

    private Date deleted;

    private String originalSender;

    private String finalRecipient;

    private String refToMessageId;

    private Date failed;

    private Date restored;

    private Boolean messageFragment;

    private Boolean sourceMessage;

    private String action;

    private String serviceType;

    private String serviceValue;

    public MessageLogInfo() {
    }

    //constructor for signal messages
    public MessageLogInfo(final String messageId,
                          final MessageStatus messageStatus,
                          final MSHRole mshRole,
                          final Date deleted,
                          final Date received,
                          final String conversationId,
                          final String fromPartyId,
                          final String toPartyId,
                          final String originalSender,
                          final String finalRecipient,
                          final String refToMessageId,
                          final Boolean testMessage) {
        this.messageType = MessageType.SIGNAL_MESSAGE;
        this.messageId = messageId;
        this.messageStatus = messageStatus;
        this.mshRole = mshRole;
        this.deleted = deleted;
        this.received = received;
        //message information UserMessage/SignalMessage
        this.conversationId = conversationId;
        this.fromPartyId = fromPartyId;
        this.toPartyId = toPartyId;
        this.originalSender = originalSender;
        this.finalRecipient = finalRecipient;
        this.refToMessageId = refToMessageId;
        this.testMessage = testMessage;
    }

    //constructor for user messages
    public MessageLogInfo(final String messageId,
                          final MessageStatus messageStatus,
                          final NotificationStatus notificationStatus,
                          final MSHRole mshRole,
                          final Date deleted,
                          final Date received,
                          final int sendAttempts,
                          final int sendAttemptsMax,
                          final Date nextAttempt,
                          final String nextAttemptTimezoneId,
                          final Integer nextAttemptOffsetSeconds,
                          final String conversationId,
                          final String fromPartyId,
                          final String toPartyId,
                          final String originalSender,
                          final String finalRecipient,
                          final String refToMessageId,
                          final Date failed,
                          final Date restored,
                          final Boolean testMessage,
                          final Boolean messageFragment,
                          final Boolean sourceMessage,
                          final String action,
                          final String serviceType,
                          final String serviceValue
    ) {
        this(messageId, messageStatus, mshRole, deleted, received, conversationId, fromPartyId, toPartyId,
                originalSender, finalRecipient, refToMessageId, testMessage);

        this.messageType = MessageType.USER_MESSAGE;
        this.notificationStatus = notificationStatus;
        this.sendAttempts = sendAttempts;
        this.sendAttemptsMax = sendAttemptsMax;
        this.nextAttempt = nextAttempt;
        this.nextAttemptTimezoneId = nextAttemptTimezoneId;
        this.nextAttemptOffsetSeconds = nextAttemptOffsetSeconds;
        this.messageFragment = messageFragment;
        this.sourceMessage = sourceMessage;
        this.action = action;
        this.serviceType = serviceType;
        this.serviceValue = serviceValue;
        this.failed = failed;
        this.restored = restored;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public void setMessageStatus(MessageStatus messageStatus) {
        this.messageStatus = messageStatus;
    }

    public void setNotificationStatus(NotificationStatus notificationStatus) {
        this.notificationStatus = notificationStatus;
    }

    public void setReceived(Date received) {
        this.received = received;
    }

    public void setMshRole(MSHRole mshRole) {
        this.mshRole = mshRole;
    }

    public void setSendAttempts(int sendAttempts) {
        this.sendAttempts = sendAttempts;
    }

    public void setSendAttemptsMax(int sendAttemptsMax) {
        this.sendAttemptsMax = sendAttemptsMax;
    }

    public void setNextAttempt(Date nextAttempt) {
        this.nextAttempt = nextAttempt;
    }

    public Boolean getTestMessage() {
        return testMessage;
    }

    public void setTestMessage(Boolean testMessage) {
        this.testMessage = testMessage;
    }

    public void setDeleted(Date deleted) {
        this.deleted = deleted;
    }

    public void setFailed(Date failed) {
        this.failed = failed;
    }

    public void setRestored(Date restored) {
        this.restored = restored;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getFromPartyId() {
        return fromPartyId;
    }

    public void setFromPartyId(String fromPartyId) {
        this.fromPartyId = fromPartyId;
    }

    public String getToPartyId() {
        return toPartyId;
    }

    public void setToPartyId(String toPartyId) {
        this.toPartyId = toPartyId;
    }

    public String getOriginalSender() {
        return originalSender;
    }

    public void setOriginalSender(String originalSender) {
        this.originalSender = originalSender;
    }

    public String getFinalRecipient() {
        return finalRecipient;
    }

    public void setFinalRecipient(String finalRecipient) {
        this.finalRecipient = finalRecipient;
    }

    public String getRefToMessageId() {
        return refToMessageId;
    }

    public void setRefToMessageId(String refToMessageId) {
        this.refToMessageId = refToMessageId;
    }

    public String getMessageId() {
        return messageId;
    }

    public MessageStatus getMessageStatus() {
        return messageStatus;
    }

    public NotificationStatus getNotificationStatus() {
        return notificationStatus;
    }

    public MSHRole getMshRole() {
        return mshRole;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public Date getDeleted() {
        return deleted;
    }

    public Date getReceived() {
        return received;
    }

    public int getSendAttempts() {
        return sendAttempts;
    }

    public int getSendAttemptsMax() {
        return sendAttemptsMax;
    }

    public Date getNextAttempt() {
        return nextAttempt;
    }

    public String getNextAttemptTimezoneId() {
        return nextAttemptTimezoneId;
    }

    public Integer getNextAttemptOffsetSeconds() {
        return nextAttemptOffsetSeconds;
    }

    public Date getFailed() {
        return failed;
    }

    public Date getRestored() {
        return restored;
    }

    public Boolean getMessageFragment() {
        return messageFragment;
    }

    public void setMessageFragment(Boolean messageFragment) {
        this.messageFragment = messageFragment;
    }

    public Boolean getSourceMessage() {
        return sourceMessage;
    }

    public void setSourceMessage(Boolean sourceMessage) {
        this.sourceMessage = sourceMessage;
    }

    public String getAction() {
        return action;
    }

    public String getServiceType() {
        return serviceType;
    }

    public String getServiceValue() {
        return serviceValue;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public void setServiceValue(String serviceValue) {
        this.serviceValue = serviceValue;
    }

    public void setNextAttemptTimezoneId(String nextAttemptTimezoneId) {
        this.nextAttemptTimezoneId = nextAttemptTimezoneId;
    }

    public void setNextAttemptOffsetSeconds(Integer nextAttemptOffsetSeconds) {
        this.nextAttemptOffsetSeconds = nextAttemptOffsetSeconds;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        MessageLogInfo that = (MessageLogInfo) o;

        return new EqualsBuilder()
                .append(sendAttempts, that.sendAttempts)
                .append(sendAttemptsMax, that.sendAttemptsMax)
                .append(messageId, that.messageId)
                .append(fromPartyId, that.fromPartyId)
                .append(toPartyId, that.toPartyId)
                .append(messageStatus, that.messageStatus)
                .append(notificationStatus, that.notificationStatus)
                .append(received, that.received)
                .append(mshRole, that.mshRole)
                .append(nextAttempt, that.nextAttempt)
                .append(nextAttemptTimezoneId, that.nextAttemptTimezoneId)
                .append(nextAttemptOffsetSeconds, that.nextAttemptOffsetSeconds)
                .append(conversationId, that.conversationId)
                .append(deleted, that.deleted)
                .append(originalSender, that.originalSender)
                .append(finalRecipient, that.finalRecipient)
                .append(refToMessageId, that.refToMessageId)
                .append(failed, that.failed)
                .append(restored, that.restored)
                .append(messageFragment, this.messageFragment)
                .append(action, this.action)
                .append(serviceType, this.serviceType)
                .append(serviceValue, this.serviceValue)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(messageId)
                .append(fromPartyId)
                .append(toPartyId)
                .append(messageStatus)
                .append(notificationStatus)
                .append(received)
                .append(mshRole)
                .append(sendAttempts)
                .append(sendAttemptsMax)
                .append(nextAttempt)
                .append(nextAttemptTimezoneId)
                .append(nextAttemptOffsetSeconds)
                .append(conversationId)
                .append(deleted)
                .append(originalSender)
                .append(finalRecipient)
                .append(refToMessageId)
                .append(failed)
                .append(restored)
                .append(messageFragment)
                .append(action)
                .append(serviceType)
                .append(serviceValue)
                .toHashCode();
    }
}
