package eu.domibus.api.model;

import eu.domibus.api.message.MessageSubtype;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Federico Martini
 * @author Cosmin Baciu
 * @since 3.2
 */
@Entity
@Table(name = "TB_MESSAGE_LOG")
@DiscriminatorValue("USER_MESSAGE")
@NamedQueries({
        @NamedQuery(name = "UserMessageLog.findRetryMessages",
                query = "select userMessageLog.messageId " +
                        "from UserMessageLog userMessageLog " +
                        "where userMessageLog.messageStatus = eu.domibus.api.model.MessageStatus.WAITING_FOR_RETRY " +
                        "and userMessageLog.nextAttempt < :CURRENT_TIMESTAMP " +
                        "and 1 <= userMessageLog.sendAttempts " +
                        "and userMessageLog.sendAttempts <= userMessageLog.sendAttemptsMax " +
                        "and (userMessageLog.sourceMessage is null or userMessageLog.sourceMessage=false)" +
                        "and (userMessageLog.scheduled is null or userMessageLog.scheduled=false)"),
        @NamedQuery(name = "UserMessageLog.findReadyToPullMessages", query = "SELECT mi.messageId,mi.timestamp FROM UserMessageLog as um ,MessageInfo mi where um.messageStatus=eu.domibus.api.model.MessageStatus.READY_TO_PULL and um.messageId=mi.messageId order by mi.timestamp desc"),
        @NamedQuery(name = "UserMessageLog.getMessageStatus", query = "select userMessageLog.messageStatus from UserMessageLog userMessageLog where userMessageLog.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "UserMessageLog.findByMessageId", query = "select userMessageLog from UserMessageLog userMessageLog where userMessageLog.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "UserMessageLog.findByMessageIdAndRole", query = "select userMessageLog from UserMessageLog userMessageLog where userMessageLog.messageId=:MESSAGE_ID and userMessageLog.mshRole=:MSH_ROLE"),
        @NamedQuery(name = "UserMessageLog.findBackendForMessage", query = "select userMessageLog.backend from UserMessageLog userMessageLog where userMessageLog.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "UserMessageLog.findEntries", query = "select userMessageLog from UserMessageLog userMessageLog"),
        @NamedQuery(name = "UserMessageLog.findDeletedUserMessagesOlderThan",
                query = "SELECT uml.messageInfo.messageId   as " + UserMessageLogDto.MESSAGE_ID + "           ,                            " +
                        "       uml.messageSubtype          as " + UserMessageLogDto.MESSAGE_SUBTYPE + "      ,                            " +
                        "       uml.backend                 as " + UserMessageLogDto.MESSAGE_BACKEND + "      ,                            " +
                        "       p.value                     as " + UserMessageLogDto.PROP_VALUE + "           ,                            " +
                        "       p.name                      as " + UserMessageLogDto.PROP_NAME + "                                         " +
                        "FROM UserMessageLog uml                                                                                           " +
                        "JOIN UserMessage um on um.messageInfo =  uml.messageInfo.entityId                                                 " +
                        "left join um.messageProperties.property p                                                                         " +
                        "WHERE uml.messageStatus = eu.domibus.api.model.MessageStatus.DELETED                                              " +
                        "AND uml.deleted IS NOT NULL AND uml.mpc = :MPC AND uml.deleted < :DATE                                            "),
        @NamedQuery(name = "UserMessageLog.findUndownloadedUserMessagesOlderThan",
                query = "SELECT uml.messageInfo.messageId   as " + UserMessageLogDto.MESSAGE_ID + "           ,                            " +
                        "       uml.messageSubtype          as " + UserMessageLogDto.MESSAGE_SUBTYPE + "      ,                            " +
                        "       uml.backend                 as " + UserMessageLogDto.MESSAGE_BACKEND + "      ,                            " +
                        "       p.value                     as " + UserMessageLogDto.PROP_VALUE + "           ,                            " +
                        "       p.name                      as " + UserMessageLogDto.PROP_NAME + "                                         " +
                        "from UserMessageLog uml                                                                                           " +
                        "JOIN UserMessage um on um.messageInfo =  uml.messageInfo.entityId                                                 " +
                        "left join um.messageProperties.property p                                                                         " +
                        "where (uml.messageStatus = eu.domibus.api.model.MessageStatus.RECEIVED                                            " +
                        "or uml.messageStatus = eu.domibus.api.model.MessageStatus.RECEIVED_WITH_WARNINGS)                                 " +
                        "and uml.deleted is null and uml.mpc = :MPC and uml.received < :DATE                                               "),
        @NamedQuery(name = "UserMessageLog.findDownloadedUserMessagesOlderThan",
                query ="SELECT uml.messageInfo.messageId   as " + UserMessageLogDto.MESSAGE_ID + "           ,                            " +
                        "       uml.messageSubtype          as " + UserMessageLogDto.MESSAGE_SUBTYPE + "      ,                            " +
                        "       uml.backend                 as " + UserMessageLogDto.MESSAGE_BACKEND + "      ,                            " +
                        "       p.value                     as " + UserMessageLogDto.PROP_VALUE + "           ,                            " +
                        "       p.name                      as " + UserMessageLogDto.PROP_NAME + "                                         " +
                        "FROM UserMessageLog uml                                                                                           " +
                        "JOIN UserMessage um on um.messageInfo =  uml.messageInfo.entityId                                                 " +
                        "left join um.messageProperties.property p                                                                         " +
                        "where (uml.messageStatus = eu.domibus.api.model.MessageStatus.DOWNLOADED)                                         " +
                        "and uml.mpc = :MPC and uml.downloaded is not null and uml.downloaded < :DATE                                      "),
        @NamedQuery(name = "UserMessageLog.findSentUserMessagesWithPayloadNotClearedOlderThan",
                query = "SELECT uml.messageInfo.messageId   as " + UserMessageLogDto.MESSAGE_ID + "           ,                            " +
                        "       uml.messageSubtype          as " + UserMessageLogDto.MESSAGE_SUBTYPE + "      ,                            " +
                        "       uml.backend                 as " + UserMessageLogDto.MESSAGE_BACKEND + "      ,                            " +
                        "       p.value                     as " + UserMessageLogDto.PROP_VALUE + "           ,                            " +
                        "       p.name                      as " + UserMessageLogDto.PROP_NAME + "                                         " +
                        "FROM UserMessageLog uml                                                                                           " +
                        "JOIN UserMessage um on um.messageInfo =  uml.messageInfo.entityId                                                 " +
                        "left join um.messageProperties.property p                                                                         " +
                        "where (uml.messageStatus = eu.domibus.api.model.MessageStatus.ACKNOWLEDGED or uml.messageStatus = eu.domibus.api.model.MessageStatus.SEND_FAILURE) " +
                        "and uml.deleted is null and uml.mpc = :MPC and uml.modificationTime is not null and uml.modificationTime < :DATE  "),
        @NamedQuery(name = "UserMessageLog.findSentUserMessagesOlderThan",
                query = "SELECT uml.messageInfo.messageId   as " + UserMessageLogDto.MESSAGE_ID + "           ,                            " +
                        "       uml.messageSubtype          as " + UserMessageLogDto.MESSAGE_SUBTYPE + "      ,                            " +
                        "       uml.backend                 as " + UserMessageLogDto.MESSAGE_BACKEND + "      ,                            " +
                        "       p.value                     as " + UserMessageLogDto.PROP_VALUE + "           ,                            " +
                        "       p.name                      as " + UserMessageLogDto.PROP_NAME + "                                         " +
                        "FROM UserMessageLog uml                                                                                           " +
                        "JOIN UserMessage um on um.messageInfo =  uml.messageInfo.entityId                                                 " +
                        "left join um.messageProperties.property p                                                                         " +
                        "where (uml.messageStatus = eu.domibus.api.model.MessageStatus.ACKNOWLEDGED or uml.messageStatus = eu.domibus.api.model.MessageStatus.SEND_FAILURE) " +
                        "and uml.mpc = :MPC and uml.modificationTime is not null and uml.modificationTime < :DATE                          "
        ),
        @NamedQuery(name = "UserMessageLog.setNotificationStatus", query = "update UserMessageLog userMessageLog set userMessageLog.notificationStatus=:NOTIFICATION_STATUS where userMessageLog.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "UserMessageLog.countEntries", query = "select count(userMessageLog.messageId) from UserMessageLog userMessageLog"),
        @NamedQuery(name = "UserMessageLog.setMessageStatusAndNotificationStatus",
                query = "update UserMessageLog userMessageLog set userMessageLog.deleted=:TIMESTAMP, userMessageLog.messageStatus=:MESSAGE_STATUS, userMessageLog.notificationStatus=:NOTIFICATION_STATUS where userMessageLog.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "UserMessageLog.findAllInfo", query = "select userMessageLog from UserMessageLog userMessageLog"),
        @NamedQuery(name = "UserMessageLog.deleteMessageLogs", query = "delete from UserMessageLog uml where uml.messageId in :MESSAGEIDS"),
})
public class UserMessageLog extends AbstractNoGeneratedPkEntity {

    @Column(name = "BACKEND")
    private String backend;

    @Column(name = "RECEIVED")
    @Temporal(TemporalType.TIMESTAMP)
    private Date received;

    @Column(name = "DOWNLOADED")
    @Temporal(TemporalType.TIMESTAMP)
    private Date downloaded;

    @Column(name = "FAILED")
    @Temporal(TemporalType.TIMESTAMP)
    private Date failed;

    @Column(name = "RESTORED")
    @Temporal(TemporalType.TIMESTAMP)
    private Date restored;

    /**
     * The Date when this message was deleted, A message shall be deleted when one of the following conditions apply:
     * <p>
     * - An outgoing message has been sent without error eb:Error/@severity failure failure, and an AS4 receipt has been
     * received
     * - An outgoing message has been sent without error eb:Error/@severity failure, and AS4 is disabled
     * - An outgoing message could not be sent and the final AS4 retry has passed
     * - An outgoing message could not be sent and AS4 is disabled (eb:Error/@severity failure, [CORE 6.2.5])
     * <p>
     * - A received message
     */
    @Column(name = "DELETED")
    @Temporal(TemporalType.TIMESTAMP)
    private Date deleted;

    @Column(name = "NEXT_ATTEMPT")
    @Temporal(TemporalType.TIMESTAMP)
    private Date nextAttempt;

    @Column(name = "SEND_ATTEMPTS")
    private int sendAttempts;

    @Column(name = "SEND_ATTEMPTS_MAX")
    private int sendAttemptsMax;

    @Column(name = "SCHEDULED")
    protected Boolean scheduled;

    @Version
    @Column(name = "VERSION")
    protected int version;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinColumn(name = "MESSAGE_STATUS_ID_FK")
    private MessageStatusEntity messageStatus;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinColumn(name = "MSH_ROLE_ID_FK")
    private MSHRoleEntity mshRole;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinColumn(name = "MSH_ROLE_ID_FK")
    private NotificationStatusEntity notificationStatus;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinColumn(name = "MESSAGE_SUBTYPE_ID_FK")
    private MessageSubtypeEntity messageSubtype;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    private UserMessage userMessage;

    public boolean isTestMessage() {
        if(MessageSubtype.TEST == messageSubtype.getMessageSubtype()) {
            return true;
        }
        return false;
    }

    public String getBackend() {
        return backend;
    }

    public void setBackend(String backend) {
        this.backend = backend;
    }

    public Date getReceived() {
        return received;
    }

    public void setReceived(Date received) {
        this.received = received;
    }

    public Date getDownloaded() {
        return downloaded;
    }

    public void setDownloaded(Date downloaded) {
        this.downloaded = downloaded;
    }

    public Date getFailed() {
        return failed;
    }

    public void setFailed(Date failed) {
        this.failed = failed;
    }

    public Date getRestored() {
        return restored;
    }

    public void setRestored(Date restored) {
        this.restored = restored;
    }

    public Date getDeleted() {
        return deleted;
    }

    public void setDeleted(Date deleted) {
        this.deleted = deleted;
    }

    public Date getNextAttempt() {
        return nextAttempt;
    }

    public void setNextAttempt(Date nextAttempt) {
        this.nextAttempt = nextAttempt;
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

    public Boolean getScheduled() {
        return scheduled;
    }

    public void setScheduled(Boolean scheduled) {
        this.scheduled = scheduled;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public MessageStatus getMessageStatus() {
        return messageStatus.getMessageStatus();
    }

    public void setMessageStatus(MessageStatusEntity messageStatus) {
        this.messageStatus = messageStatus;
    }

    public MSHRoleEntity getMshRole() {
        return mshRole;
    }

    public void setMshRole(MSHRoleEntity mshRole) {
        this.mshRole = mshRole;
    }

    public NotificationStatusEntity getNotificationStatus() {
        return notificationStatus;
    }

    public void setNotificationStatus(NotificationStatusEntity notificationStatus) {
        this.notificationStatus = notificationStatus;
    }

    public MessageSubtypeEntity getMessageSubtype() {
        return messageSubtype;
    }

    public void setMessageSubtype(MessageSubtypeEntity messageSubtype) {
        this.messageSubtype = messageSubtype;
    }


    public UserMessage getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(UserMessage userMessage) {
        this.userMessage = userMessage;
    }
}
