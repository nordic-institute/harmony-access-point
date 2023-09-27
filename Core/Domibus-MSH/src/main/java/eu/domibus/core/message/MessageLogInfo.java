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

    private Long fromPartyIdPk;
    private String fromPartyId;

    private Long toPartyIdPk;
    private String toPartyId;

    private Long messageStatusId;

    private MessageStatus messageStatus;

    private Long notificationStatusId;

    private NotificationStatus notificationStatus;

    private Date received;

    private Long mshRoleId;

    private MSHRole mshRole;

    private int sendAttempts;

    private int sendAttemptsMax;

    private Date nextAttempt;

    private Long nextAttemptTimezonePk;

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

    private Long actionId;

    private String action;

    private Long serviceId;

    private String serviceType;

    private String serviceValue;

    private String pluginType;

    private Long partLength;

    private Date archived;

    public MessageLogInfo() {
    }

    //constructor for signal messages
    public MessageLogInfo(final String messageId,
                          final Long messageStatusId,
                          final Long mshRoleId,
                          final Date deleted,
                          final Date received,
                          final String conversationId,
                          final Long fromPartyIdPk,
                          final Long toPartyIdPk,
                          final String originalSender,
                          final String finalRecipient,
                          final String refToMessageId,
                          final Boolean testMessage) {
        this.messageType = MessageType.SIGNAL_MESSAGE;
        this.messageId = messageId;
        this.messageStatusId = messageStatusId;
        this.mshRoleId = mshRoleId;
        this.deleted = deleted;
        this.received = received;
        //message information UserMessage/SignalMessage
        this.conversationId = conversationId;
        this.fromPartyIdPk = fromPartyIdPk;
        this.toPartyIdPk = toPartyIdPk;
        this.originalSender = originalSender;
        this.finalRecipient = finalRecipient;
        this.refToMessageId = refToMessageId;
        this.testMessage = testMessage;

        this.partLength = 0L;
    }

    //constructor for user messages
    public MessageLogInfo(final String messageId,
                          final Long messageStatusId,
                          final Long notificationStatusId,
                          final Long mshRoleId,
                          final Date deleted,
                          final Date received,
                          final int sendAttempts,
                          final int sendAttemptsMax,
                          final Date nextAttempt,
                          final Long nextAttemptTimezonePk,
                          final String conversationId,
                          final Long fromPartyIdPk,
                          final Long toPartyIdPk,
                          final String originalSender,
                          final String finalRecipient,
                          final String refToMessageId,
                          final Date failed,
                          final Date restored,
                          final Boolean testMessage,
                          final Boolean messageFragment,
                          final Boolean sourceMessage,
                          final Long actionId,
                          final Long serviceId,
                          final String pluginType,
                          final Long partLength,
                          final Date archived
    ) {
        this(messageId, messageStatusId, mshRoleId, deleted, received, conversationId, fromPartyIdPk, toPartyIdPk,
                originalSender, finalRecipient, refToMessageId, testMessage);

        this.messageType = MessageType.USER_MESSAGE;
        this.notificationStatusId = notificationStatusId;
        this.sendAttempts = sendAttempts;
        this.sendAttemptsMax = sendAttemptsMax;
        this.nextAttempt = nextAttempt;
        this.nextAttemptTimezonePk = nextAttemptTimezonePk;
        this.messageFragment = messageFragment;
        this.sourceMessage = sourceMessage;
        this.actionId = actionId;
        this.serviceId = serviceId;
        this.failed = failed;
        this.restored = restored;
        this.pluginType = pluginType;
        this.partLength = partLength;
        this.archived = archived;
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

    public Long getFromPartyIdPk() {
        return fromPartyIdPk;
    }

    public String getFromPartyId() {
        return fromPartyId;
    }

    public void setFromPartyId(String fromPartyId) {
        this.fromPartyId = fromPartyId;
    }

    public Long getToPartyIdPk() {
        return toPartyIdPk;
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

    public Long getMessageStatusId() {
        return messageStatusId;
    }

    public MessageStatus getMessageStatus() {
        return messageStatus;
    }

    public Long getNotificationStatusId() {
        return notificationStatusId;
    }

    public NotificationStatus getNotificationStatus() {
        return notificationStatus;
    }

    public Long getMshRoleId() {
        return mshRoleId;
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

    public Long getNextAttemptTimezonePk() {
        return nextAttemptTimezonePk;
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

    public Long getActionId() {
        return actionId;
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

    public Long getServiceId() {
        return serviceId;
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

    public String getPluginType() {
        return pluginType;
    }

    public void setPluginType(String pluginType) {
        this.pluginType = pluginType;
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

    public Long getPartLength() {
        return partLength;
    }

    public void setPartLength(Long partLength) {
        this.partLength = partLength;
    }

    public Date getArchived() {
        return archived;
    }

    public void setArchived(Date archived) {
        this.archived = archived;
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
                .append(messageFragment, that.messageFragment)
                .append(action, that.action)
                .append(serviceType, that.serviceType)
                .append(serviceValue, that.serviceValue)
                .append(pluginType, that.pluginType)
                .append(partLength, that.partLength)
                .append(archived, that.partLength)
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
                .append(pluginType)
                .append(partLength)
                .append(archived)
                .toHashCode();
    }
}
