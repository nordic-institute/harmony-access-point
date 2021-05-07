package eu.domibus.core.replication;

import eu.domibus.api.model.*;
import eu.domibus.api.scheduler.Reprogrammable;
import eu.domibus.common.MessageStatus;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.util.Date;

/**
 * Entity which maps 1 record of {@code TB_MESSAGE_UI} table
 *
 * @author Catalin Enache
 * @since 4.0
 *
 */
@Entity
@Table(name = "TB_MESSAGE_UI")
@NamedQueries({
        @NamedQuery(name = "UIMessageEntity.findUIMessageByMessageId",
                query = "select uiMessageEntity from UIMessageEntity uiMessageEntity where uiMessageEntity.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "UIMessageEntity.deleteUIMessagesByMessageIds",
                query = "delete from UIMessageEntity uiMessageEntity where uiMessageEntity.messageId IN :MESSAGEIDS"),
})
@SqlResultSetMapping(name="updateResult", columns = { @ColumnResult(name = "count")})
@NamedNativeQueries({
        @NamedNativeQuery(
                name    =   "UIMessageEntity.updateMessage",
                query   =   "UPDATE TB_MESSAGE_UI SET MESSAGE_STATUS=?1, NOTIFICATION_STATUS=?2, DELETED=?3, FAILED=?4, RESTORED=?5, NEXT_ATTEMPT=?6, FK_TIMEZONE_OFFSET=?7, SEND_ATTEMPTS=?8, SEND_ATTEMPTS_MAX=?9, LAST_MODIFIED=?10 " +
                        " WHERE MESSAGE_ID=?11"
                ,resultSetMapping = "updateResult"
        )
})
public class UIMessageEntity extends AbstractBaseEntity implements Reprogrammable {

    @Column(name = "MESSAGE_ID")
    private String messageId;

    @Column(name = "MESSAGE_STATUS")
    @Enumerated(EnumType.STRING)
    private MessageStatus messageStatus;

    @Column(name = "NOTIFICATION_STATUS")
    @Enumerated(EnumType.STRING)
    private NotificationStatus notificationStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "MESSAGE_TYPE")
    private MessageType messageType;

    @Column(name = "TEST_MESSAGE")
    private Boolean testMessage;

    @Enumerated(EnumType.STRING)
    @Column(name = "MSH_ROLE")
    private MSHRole mshRole;

    @Column(name = "CONVERSATION_ID", nullable = false)
    protected String conversationId;

    @Column(name = "DELETED")
    @Temporal(TemporalType.TIMESTAMP)
    private Date deleted;

    @Column(name = "RECEIVED")
    @Temporal(TemporalType.TIMESTAMP)
    private Date received;

    @Column(name = "RESTORED")
    @Temporal(TemporalType.TIMESTAMP)
    private Date restored;

    @Column(name = "FAILED")
    @Temporal(TemporalType.TIMESTAMP)
    private Date failed;

    @Column(name = "SEND_ATTEMPTS")
    private int sendAttempts;

    @Column(name = "SEND_ATTEMPTS_MAX")
    private int sendAttemptsMax;

    @Column(name = "NEXT_ATTEMPT")
    @Temporal(TemporalType.TIMESTAMP)
    private Date nextAttempt;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "FK_TIMEZONE_OFFSET")
    private TimezoneOffset timezoneOffset;

    @Column(name = "FROM_ID")
    private String fromId;

    @Column(name = "FROM_SCHEME")
    private String fromScheme;

    @Column(name = "TO_ID")
    private String toId;

    @Column(name = "TO_SCHEME")
    private String toScheme;

    @Column(name = "REF_TO_MESSAGE_ID")
    private String refToMessageId;

    @Column(name = "ACTION")
    private String action;

    @Column(name = "SERVICE_TYPE")
    private String serviceType;

    @Column(name = "SERVICE_VALUE")
    private String serviceValue;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "LAST_MODIFIED", nullable = false)
    private Date lastModified;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public MessageStatus getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(MessageStatus messageStatus) {
        this.messageStatus = messageStatus;
    }

    public NotificationStatus getNotificationStatus() {
        return notificationStatus;
    }

    public void setNotificationStatus(NotificationStatus notificationStatus) {
        this.notificationStatus = notificationStatus;
    }

    public MSHRole getMshRole() {
        return mshRole;
    }

    public void setMshRole(MSHRole mshRole) {
        this.mshRole = mshRole;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public Boolean getTestMessage() {
        return testMessage;
    }

    public void setTestMessage(Boolean testMesage) {
        this.testMessage = testMesage;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public Date getDeleted() {
        return deleted;
    }

    public void setDeleted(Date deleted) {
        this.deleted = deleted;
    }

    public Date getReceived() {
        return received;
    }

    public void setReceived(Date received) {
        this.received = received;
    }

    public Date getRestored() {
        return restored;
    }

    public void setRestored(Date restored) {
        this.restored = restored;
    }

    public Date getFailed() {
        return failed;
    }

    public void setFailed(Date failed) {
        this.failed = failed;
    }

    public int getSendAttempts() {
        return sendAttempts;
    }

    public void setSendAttempts(int sendAttempts) {
        this.sendAttempts = sendAttempts;
    }

    public int getSendAttemptsMax() {
        return sendAttemptsMax;
    }

    public void setSendAttemptsMax(int sendAttemptsMax) {
        this.sendAttemptsMax = sendAttemptsMax;
    }

    @Override
    public Date getNextAttempt() {
        return nextAttempt;
    }

    @Override
    public void setNextAttempt(Date nextAttempt) {
        this.nextAttempt = nextAttempt;
    }

    @Override
    public TimezoneOffset getTimezoneOffset() {
        return timezoneOffset;
    }

    @Override
    public void setTimezoneOffset(TimezoneOffset timezoneOffset) {
        this.timezoneOffset = timezoneOffset;
    }

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public String getFromScheme() {
        return fromScheme;
    }

    public void setFromScheme(String fromScheme) {
        this.fromScheme = fromScheme;
    }

    public String getToId() {
        return toId;
    }

    public void setToId(String toId) {
        this.toId = toId;
    }

    public String getToScheme() {
        return toScheme;
    }

    public void setToScheme(String toScheme) {
        this.toScheme = toScheme;
    }

    public String getRefToMessageId() {
        return refToMessageId;
    }

    public void setRefToMessageId(String refToMessageId) {
        this.refToMessageId = refToMessageId;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date version) {
        this.lastModified = version;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getServiceValue() {
        return serviceValue;
    }

    public void setServiceValue(String serviceValue) {
        this.serviceValue = serviceValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof UIMessageEntity)) return false;

        UIMessageEntity that = (UIMessageEntity) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(sendAttempts, that.sendAttempts)
                .append(sendAttemptsMax, that.sendAttemptsMax)
                .append(messageId, that.messageId)
                .append(messageStatus, that.messageStatus)
                .append(notificationStatus, that.notificationStatus)
                .append(messageType, that.messageType)
                .append(mshRole, that.mshRole)
                .append(conversationId, that.conversationId)
                .append(deleted, that.deleted)
                .append(received, that.received)
                .append(restored, that.restored)
                .append(failed, that.failed)
                .append(nextAttempt, that.nextAttempt)
                .append(fromId, that.fromId)
                .append(fromScheme, that.fromScheme)
                .append(toId, that.toId)
                .append(toScheme, that.toScheme)
                .append(refToMessageId, that.refToMessageId)
                .append(action, this.action)
                .append(serviceType, this.serviceType)
                .append(serviceValue, this.serviceValue)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(messageId)
                .append(messageStatus)
                .append(notificationStatus)
                .append(messageType)
                .append(mshRole)
                .append(conversationId)
                .append(deleted)
                .append(received)
                .append(restored)
                .append(failed)
                .append(sendAttempts)
                .append(sendAttemptsMax)
                .append(nextAttempt)
                .append(fromId)
                .append(fromScheme)
                .append(toId)
                .append(toScheme)
                .append(refToMessageId)
                .append(action)
                .append(serviceType)
                .append(serviceValue)
                .toHashCode();
    }
}
