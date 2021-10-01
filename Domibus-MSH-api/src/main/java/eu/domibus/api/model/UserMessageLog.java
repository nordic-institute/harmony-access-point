package eu.domibus.api.model;

import eu.domibus.api.scheduler.Reprogrammable;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Federico Martini
 * @author Cosmin Baciu
 * @since 3.2
 */
@Entity
@Table(name = "TB_USER_MESSAGE_LOG")
@NamedQueries({
        @NamedQuery(name = "UserMessageLog.findRetryMessages",
                query = "select um.messageId " +
                        "from UserMessageLog userMessageLog join userMessageLog.userMessage um " +
                        "where userMessageLog.messageStatus.messageStatus = eu.domibus.api.model.MessageStatus.WAITING_FOR_RETRY " +
                        "and userMessageLog.nextAttempt < :CURRENT_TIMESTAMP " +
                        "and 1 <= userMessageLog.sendAttempts " +
                        "and userMessageLog.sendAttempts <= userMessageLog.sendAttemptsMax " +
                        "and (um.sourceMessage is null or um.sourceMessage=false)" +
                        "and (userMessageLog.scheduled is null or userMessageLog.scheduled=false)"),
        @NamedQuery(name = "UserMessageLog.findReadyToPullMessages", query = "SELECT um.messageId, um.timestamp FROM UserMessageLog as ml join ml.userMessage um where ml.messageStatus.messageStatus=eu.domibus.api.model.MessageStatus.READY_TO_PULL order by um.timestamp desc"),
        @NamedQuery(name = "UserMessageLog.getMessageStatus", query = "select userMessageLog.messageStatus from UserMessageLog userMessageLog where userMessageLog.userMessage.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "UserMessageLog.findByMessageId", query = "select userMessageLog from UserMessageLog userMessageLog where userMessageLog.userMessage.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "UserMessageLog.findByMessageIdAndRole", query = "select userMessageLog from UserMessageLog userMessageLog where userMessageLog.userMessage.messageId=:MESSAGE_ID and userMessageLog.mshRole.role=:MSH_ROLE"),
        @NamedQuery(name = "UserMessageLog.findBackendForMessage", query = "select userMessageLog.backend from UserMessageLog userMessageLog where userMessageLog.userMessage.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "UserMessageLog.findEntries", query = "select userMessageLog from UserMessageLog userMessageLog"),
        @NamedQuery(name = "UserMessageLog.findDeletedUserMessagesOlderThan",
                query = "SELECT um.entityId   as " + UserMessageLogDto.ENTITY_ID + "           ,                            " +
                        "       um.messageId   as " + UserMessageLogDto.MESSAGE_ID + "           ,                            " +
                        "       um.testMessage              as " + UserMessageLogDto.TEST_MESSAGE + "      ,                            " +
                        "       uml.backend                 as " + UserMessageLogDto.MESSAGE_BACKEND + "      ,                            " +
                        "       p.value                     as " + UserMessageLogDto.PROP_VALUE + "           ,                            " +
                        "       p.name                      as " + UserMessageLogDto.PROP_NAME + "                                         " +
                        "FROM UserMessageLog uml                                                                                           " +
                        "JOIN uml.userMessage um                                                                                           " +
                        "left join um.messageProperties p                                                                         " +
                        "WHERE uml.messageStatus.messageStatus = eu.domibus.api.model.MessageStatus.DELETED                                              " +
                        "AND uml.deleted IS NOT NULL AND um.mpc.value = :MPC AND uml.deleted < :DATE                                            "),
        @NamedQuery(name = "UserMessageLog.findUndownloadedUserMessagesOlderThan",
                query = "SELECT um.entityId   as " + UserMessageLogDto.ENTITY_ID + "           ,                            " +
                        "       um.messageId   as " + UserMessageLogDto.MESSAGE_ID + "           ,                            " +
                        "       um.testMessage          as " + UserMessageLogDto.TEST_MESSAGE + "      ,                            " +
                        "       uml.backend                 as " + UserMessageLogDto.MESSAGE_BACKEND + "      ,                            " +
                        "       p.value                     as " + UserMessageLogDto.PROP_VALUE + "           ,                            " +
                        "       p.name                      as " + UserMessageLogDto.PROP_NAME + "                                         " +
                        "from UserMessageLog uml                                                                                           " +
                        "JOIN uml.userMessage um                                                                                           " +
                        "left join um.messageProperties p                                                                                  " +
                        "where (uml.messageStatus.messageStatus = eu.domibus.api.model.MessageStatus.RECEIVED                                            " +
                        "or uml.messageStatus.messageStatus = eu.domibus.api.model.MessageStatus.RECEIVED_WITH_WARNINGS)                                 " +
                        "and uml.deleted is null and um.mpc.value = :MPC and uml.received < :DATE                                          "),
        @NamedQuery(name = "UserMessageLog.findDownloadedUserMessagesOlderThan",
                query = "SELECT um.entityId   as " + UserMessageLogDto.ENTITY_ID + "           ,                            " +
                        "       um.messageId   as " + UserMessageLogDto.MESSAGE_ID + "           ,                            " +
                        "       um.testMessage          as " + UserMessageLogDto.TEST_MESSAGE + "      ,                            " +
                        "       uml.backend                 as " + UserMessageLogDto.MESSAGE_BACKEND + "      ,                            " +
                        "       p.value                     as " + UserMessageLogDto.PROP_VALUE + "           ,                            " +
                        "       p.name                      as " + UserMessageLogDto.PROP_NAME + "                                         " +
                        "FROM UserMessageLog uml                                                                                           " +
                        "JOIN uml.userMessage um                                                                                           " +
                        "left join um.messageProperties p                                                                                  " +
                        "where (uml.messageStatus.messageStatus = eu.domibus.api.model.MessageStatus.DOWNLOADED)                                         " +
                        "and um.mpc.value = :MPC and uml.downloaded is not null and uml.downloaded < :DATE                                      "),
        @NamedQuery(name = "UserMessageLog.findSentUserMessagesWithPayloadNotClearedOlderThan",
                query = "SELECT um.entityId   as " + UserMessageLogDto.ENTITY_ID + "           ,                            " +
                        "       um.messageId   as " + UserMessageLogDto.MESSAGE_ID + "           ,                            " +
                        "       um.testMessage          as " + UserMessageLogDto.TEST_MESSAGE + "      ,                            " +
                        "       uml.backend                 as " + UserMessageLogDto.MESSAGE_BACKEND + "      ,                            " +
                        "       p.value                     as " + UserMessageLogDto.PROP_VALUE + "           ,                            " +
                        "       p.name                      as " + UserMessageLogDto.PROP_NAME + "                                         " +
                        "FROM UserMessageLog uml                                                                                           " +
                        "JOIN uml.userMessage um                                                                                               " +
                        "left join um.messageProperties p                                                                         " +
                        "where (uml.messageStatus.messageStatus = eu.domibus.api.model.MessageStatus.ACKNOWLEDGED or uml.messageStatus.messageStatus = eu.domibus.api.model.MessageStatus.SEND_FAILURE) " +
                        "and uml.deleted is null and um.mpc.value = :MPC and uml.modificationTime is not null and uml.modificationTime < :DATE  "),
        @NamedQuery(name = "UserMessageLog.findSentUserMessagesOlderThan",
                query = "SELECT um.entityId   as " + UserMessageLogDto.ENTITY_ID + "           ,                            " +
                        "       um.messageId   as " + UserMessageLogDto.MESSAGE_ID + "           ,                            " +
                        "       um.testMessage          as " + UserMessageLogDto.TEST_MESSAGE + "      ,                            " +
                        "       uml.backend                 as " + UserMessageLogDto.MESSAGE_BACKEND + "      ,                            " +
                        "       p.value                     as " + UserMessageLogDto.PROP_VALUE + "           ,                            " +
                        "       p.name                      as " + UserMessageLogDto.PROP_NAME + "                                         " +
                        "FROM UserMessageLog uml                                                                                           " +
                        "JOIN uml.userMessage um                                                                                           " +
                        "left join um.messageProperties p                                                                         " +
                        "where (uml.messageStatus.messageStatus = eu.domibus.api.model.MessageStatus.ACKNOWLEDGED or uml.messageStatus.messageStatus = eu.domibus.api.model.MessageStatus.SEND_FAILURE) " +
                        "and um.mpc.value = :MPC and uml.modificationTime is not null and uml.modificationTime < :DATE                          "),
        @NamedQuery(name = "UserMessageLog.countEntries", query = "select count(userMessageLog.entityId) from UserMessageLog userMessageLog"),
        @NamedQuery(name = "UserMessageLog.findAllInfo", query = "select userMessageLog from UserMessageLog userMessageLog"),
        @NamedQuery(name = "UserMessageLog.findMessagesForArchivingDesc",
                query = "select new eu.domibus.api.model.UserMessageDTO(uml.entityId, uml.userMessage.messageId) " +
                        "from UserMessageLog uml " +
                        "where uml.entityId > :LAST_ENTITY_ID " +
                        "  and uml.entityId < :MAX_ENTITY_ID " +
                        "  and uml.messageStatus.messageStatus in :STATUSES " +
                        "  and uml.deleted IS NULL " +
                        "order by uml.entityId desc"),
        @NamedQuery(name = "UserMessageLog.deleteMessageLogs", query = "delete from UserMessageLog uml where uml.entityId in :IDS"),
        @NamedQuery(name = "UserMessageLog.updateArchived", query =
                "UPDATE UserMessageLog uml " +
                "SET uml.archived = current_time " +
                "WHERE uml.entityId = :ENTITY_ID "),
})
public class UserMessageLog extends AbstractNoGeneratedPkEntity implements Reprogrammable {

    @Column(name = "BACKEND")
    private String backend;

    @Column(name = "RECEIVED")
    @Temporal(TemporalType.TIMESTAMP)
    private Date received;

    @Column(name = "DOWNLOADED")
    @Temporal(TemporalType.TIMESTAMP)
    private Date downloaded;

    @Column(name = "ACKNOWLEDGED")
    @Temporal(TemporalType.TIMESTAMP)
    private Date acknowledged;

    @Column(name = "FAILED")
    @Temporal(TemporalType.TIMESTAMP)
    private Date failed;

    @Column(name = "RESTORED")
    @Temporal(TemporalType.TIMESTAMP)
    private Date restored;

    @Column(name = "ARCHIVED")
    @Temporal(TemporalType.TIMESTAMP)
    private Date archived;

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

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "FK_TIMEZONE_OFFSET")
    private TimezoneOffset timezoneOffset;

    @Column(name = "SEND_ATTEMPTS")
    private int sendAttempts;

    @Column(name = "SEND_ATTEMPTS_MAX")
    private int sendAttemptsMax;

    @Column(name = "SCHEDULED")
    protected Boolean scheduled;

    @Version
    @Column(name = "VERSION")
    protected int version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MESSAGE_STATUS_ID_FK")
    private MessageStatusEntity messageStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MSH_ROLE_ID_FK")
    private MSHRoleEntity mshRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NOTIFICATION_STATUS_ID_FK")
    private NotificationStatusEntity notificationStatus;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PK")
    @MapsId
    private UserMessage userMessage;

    public UserMessageLog() {
        setReceived(new Date());
        setSendAttempts(0);
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

    public Date getAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(Date acknowledged) {
        this.acknowledged = acknowledged;
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

    public Date getArchived() {
        return archived;
    }

    public void setArchived(Date archived) {
        this.archived = archived;
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

    public UserMessage getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(UserMessage userMessage) {
        this.userMessage = userMessage;
    }
}
