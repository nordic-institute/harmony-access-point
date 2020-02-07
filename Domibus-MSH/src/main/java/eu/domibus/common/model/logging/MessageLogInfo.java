package eu.domibus.common.model.logging;

import eu.domibus.api.message.MessageSubtype;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;
import eu.domibus.ebms3.common.model.MessageType;
import eu.domibus.ebms3.common.model.PartyId;
import eu.domibus.ebms3.common.model.PartyInfo;
import eu.domibus.ebms3.common.model.UserMessage;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Date;
import java.util.Optional;
import java.util.Set;

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

    private String conversationId;

    private MessageType messageType;

    private MessageSubtype messageSubtype;

    private Date deleted;

    private String originalSender;

    private String finalRecipient;

    private String refToMessageId;

    private Date failed;

    private Date restored;

    private Boolean messageFragment;

    private Boolean sourceMessage;

    public MessageLogInfo() {
    }

    //constructor for signal messages
    public MessageLogInfo(final String messageId,
                          final MessageStatus messageStatus,
                          final NotificationStatus notificationStatus,
                          final MSHRole mshRole,
                          final MessageType messageType,
                          final Date deleted,
                          final Date received,
                          final int sendAttempts,
                          final int sendAttemptsMax,
                          final Date nextAttempt,
                          final String conversationId,
                          final Set<PartyId> parties,
                          final String originalSender,
                          final String finalRecipient,
                          final String refToMessageId,
                          final Date failed,
                          final Date restored,
                          final MessageSubtype messageSubtype) {
        this.messageId = messageId;
        this.messageStatus = messageStatus;
        this.notificationStatus = notificationStatus;
        this.mshRole = mshRole;
        this.messageType = messageType;
        this.deleted = deleted;
        this.received = received;
        this.sendAttempts = sendAttempts;
        this.sendAttemptsMax = sendAttemptsMax;
        this.nextAttempt = nextAttempt;
        //message information UserMessage/SignalMessage
        this.conversationId = conversationId;

        Optional<PartyId> partyFrom = parties.stream().filter(partyId -> PartyInfo.DIRECTION_FROM.equals(partyId.getDirection())).findFirst();
        this.fromPartyId = partyFrom.get().getValue();

        Optional<PartyId> partyTo = parties.stream().filter(partyId -> PartyInfo.DIRECTION_TO.equals(partyId.getDirection())).findFirst();
        this.toPartyId = partyTo.get().getValue();
        this.originalSender = originalSender;
        this.finalRecipient = finalRecipient;
        this.refToMessageId = refToMessageId;
        this.failed = failed;
        this.restored = restored;
        this.messageSubtype = messageSubtype;
    }


    //constructor for user messages
    public MessageLogInfo(final String messageId,
                          final MessageStatus messageStatus,
                          final NotificationStatus notificationStatus,
                          final MSHRole mshRole,
                          final MessageType messageType,
                          final Date deleted,
                          final Date received,
                          final int sendAttempts,
                          final int sendAttemptsMax,
                          final Date nextAttempt,
                          final String conversationId,
                          final Set<PartyId> parties,
                          final String originalSender,
                          final String finalRecipient,
                          final String refToMessageId,
                          final Date failed,
                          final Date restored,
                          final MessageSubtype messageSubtype,
                          final Boolean messageFragment,
                          final Boolean sourceMessage) {
        this(messageId, messageStatus, notificationStatus, mshRole, messageType, deleted, received,
                sendAttempts, sendAttemptsMax, nextAttempt, conversationId, parties,
                originalSender, finalRecipient, refToMessageId, failed, restored, messageSubtype);

        this.messageFragment = messageFragment;
        this.sourceMessage = sourceMessage;
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

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public void setMessageSubtype(MessageSubtype messageSubtype) {
        this.messageSubtype = messageSubtype;
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

    public Date getFailed() {
        return failed;
    }

    public Date getRestored() {
        return restored;
    }

    public MessageSubtype getMessageSubtype() {
        return messageSubtype;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        MessageLogInfo that = (MessageLogInfo) o;

        return new EqualsBuilder()
                .append(messageId, that.messageId)
                .append(fromPartyId, that.fromPartyId)
                .append(toPartyId, that.toPartyId)
                .append(mshRole, that.mshRole)
                .append(messageType, that.messageType)
                .append(messageSubtype, that.messageSubtype)
                .append(messageFragment, that.messageFragment)
                .append(sourceMessage, that.sourceMessage)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(messageId)
                .append(fromPartyId)
                .append(toPartyId)
                .append(mshRole)
                .append(messageType)
                .append(messageSubtype)
                .append(messageFragment)
                .append(sourceMessage)
                .toHashCode();
    }
}
