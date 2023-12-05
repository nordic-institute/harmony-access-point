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
                query = "select userMessageLog.entityId " +
                        "from UserMessageLog userMessageLog " +
                        "where userMessageLog.entityId >= :MIN_ENTITY_ID " +
                        "and userMessageLog.entityId < :MAX_ENTITY_ID " +
                        "and userMessageLog.messageStatus = :WAITING_FOR_RETRY " +
                        "and userMessageLog.nextAttempt < :CURRENT_TIMESTAMP " +
                        "and 1 <= userMessageLog.sendAttempts " +
                        "and userMessageLog.sendAttempts <= userMessageLog.sendAttemptsMax " +
                        "and (userMessageLog.scheduled is null or userMessageLog.scheduled=false)"),
        @NamedQuery(name = "UserMessageLog.getMessageStatusById", query = "select userMessageLog.messageStatus from UserMessageLog userMessageLog where userMessageLog.userMessage.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "UserMessageLog.getMessageStatusByIdAndRole", query = "select userMessageLog.messageStatus from UserMessageLog userMessageLog where userMessageLog.userMessage.messageId=:MESSAGE_ID " +
                "and userMessageLog.mshRole = :MSH_ROLE"),
        @NamedQuery(name = "UserMessageLog.getMessageStatusByEntityId", query = "select userMessageLog.messageStatus from UserMessageLog userMessageLog where userMessageLog.userMessage.entityId=:MESSAGE_ENTITY_ID"),
        @NamedQuery(name = "UserMessageLog.findByMessageId", query = "select userMessageLog from UserMessageLog userMessageLog where userMessageLog.userMessage.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "UserMessageLog.findByMessageIdAndRole", query = "select userMessageLog from UserMessageLog userMessageLog where userMessageLog.userMessage.messageId=:MESSAGE_ID " +
                "and userMessageLog.mshRole = :MSH_ROLE"),
        @NamedQuery(name = "UserMessageLog.findBackendForMessage", query = "select userMessageLog.backend from UserMessageLog userMessageLog where userMessageLog.userMessage.messageId=:MESSAGE_ID " +
                "and userMessageLog.mshRole = :MSH_ROLE"),
        @NamedQuery(name = "UserMessageLog.findBackendForMessageEntityId", query = "select userMessageLog.backend from UserMessageLog userMessageLog where userMessageLog.entityId=:MESSAGE_ENTITY_ID"),
        @NamedQuery(name = "UserMessageLog.findEntries", query = "select userMessageLog from UserMessageLog userMessageLog"),
        @NamedQuery(name = "UserMessageLog.findDeletedUserMessagesOlderThan",
                query = "SELECT new eu.domibus.api.model.UserMessageLogDto(um.entityId,um.messageId,uml.backend,p)      " + // need this property in WSPlugin
                        "FROM UserMessageLog uml                                                                        " +
                        "INNER JOIN uml.userMessage um                                                                  " +
                        "left join um.messageProperties p on p.name = 'finalRecipient'                                  " +
                        "where (uml.messageStatus IN :MSG_STATUSES )                                                    " +
                        "and um.mpc = :MPC                                                                              " +
                        "and uml.deleted IS NOT NULL                                                                    " +
                        "and uml.deleted < :DATE                                                                        " +
                        "and ((:EARCHIVE_IS_ACTIVE = true and uml.archived is not null) or :EARCHIVE_IS_ACTIVE = false)"),
        @NamedQuery(name = "UserMessageLog.findMessagesWithSenderAndRecipientAndWithoutStatusDuringPeriod",
                query = "SELECT DISTINCT new eu.domibus.api.model.UserMessageLogDto(um.entityId, um.messageId, um.mshRole.entityId)" +
                        "FROM UserMessageLog uml                                                                      " +
                        "JOIN uml.userMessage um                                                                      " +
                        "left join um.messageProperties p                                                             " +
                        "WHERE uml.messageStatus NOT IN :MESSAGE_STATUSES                               " +
                        "AND uml.deleted IS NULL                                                                      " +
                        "AND (                                                                                        " +
                        "    (:ORIGINAL_USER is null)                                                                 " +
                        "    OR (                                                                                     " +
                        "         (p.name = 'finalRecipient' and p.value = :ORIGINAL_USER)                            " +
                        "         OR (p.name = 'originalSender' and p.value = :ORIGINAL_USER)                         " +
                        "        )                                                                                    " +
                        "     )                                                                                       " +
                        "AND (:START_DATE is null or uml.userMessage.entityId >= :START_DATE)                         " +
                        "AND (:END_DATE is null or uml.userMessage.entityId < :END_DATE)                             "),
        @NamedQuery(name = "UserMessageLog.findMessagesWithSenderAndRecipientAndStatusDuringPeriod",
                query = "SELECT DISTINCT new eu.domibus.api.model.UserMessageLogDto(um.entityId, um.messageId, um.mshRole.entityId)" +
                        "FROM UserMessageLog uml                                                                      " +
                        "JOIN uml.userMessage um                                                                      " +
                        "left join um.messageProperties p                                                             " +
                        "WHERE uml.messageStatus IN :MESSAGE_STATUSES                                   " +
                        "AND uml.deleted IS NULL                                                                      " +
                        "AND (                                                                                        " +
                        "    (:ORIGINAL_USER is null)                                                                 " +
                        "    OR (                                                                                     " +
                        "         (p.name = 'finalRecipient' and p.value = :ORIGINAL_USER)                            " +
                        "         OR (p.name = 'originalSender' and p.value = :ORIGINAL_USER)                         " +
                        "        )                                                                                    " +
                        "     )                                                                                       " +
                        "AND (:START_DATE is null or uml.userMessage.entityId >= :START_DATE)                         " +
                        "AND (:END_DATE is null or uml.userMessage.entityId < :END_DATE)                             "),
        @NamedQuery(name = "UserMessageLog.findFailedMessagesDuringPeriod",
                query = "SELECT um.entityId                 as " + UserMessageLogDto.ENTITY_ID + "            ,      " +
                        "       um.messageId                as " + UserMessageLogDto.MESSAGE_ID + "           ,      " +
                        "       um.mshRole.entityId         as " + UserMessageLogDto.MESSAGE_ROLE + "         ,      " +
                        "       p.value                     as " + UserMessageLogDto.PROP_VALUE + "           ,      " +
                        "       p.name                      as " + UserMessageLogDto.PROP_NAME + "                   " +
                        "FROM UserMessageLog uml                                                                      " +
                        "JOIN uml.userMessage um                                                                      " +
                        "left join um.messageProperties p                                                             " +
                        "WHERE uml.messageStatus = :MESSAGE_STATUS                                                    " +
                        "AND uml.deleted IS NULL                                                                      " +
                        "AND (                                                                                        " +
                        "       (:FINAL_RECIPIENT is null and :ORIGINAL_USER is null)                                 " +
                        "       OR (p.name = 'finalRecipient' and p.value = :FINAL_RECIPIENT)                         " +
                        "       OR (p.name = 'originalSender' and p.value = :ORIGINAL_USER)                           " +
                        ")                                                                                            " +
                        "AND (:START_DATE is null or uml.userMessage.entityId >= :START_DATE)                         " +
                        "AND (:END_DATE is null or uml.userMessage.entityId < :END_DATE)                             "),

        @NamedQuery(name = "UserMessageLog.findUndownloadedUserMessagesOlderThan",
                query = "SELECT new eu.domibus.api.model.UserMessageLogDto(um.entityId,um.messageId,uml.backend,p)      " + // need this property in WSPlugin
                        "FROM UserMessageLog uml                                                                        " +
                        "INNER JOIN uml.userMessage um                                                                  " +
                        "left join um.messageProperties p on p.name = 'finalRecipient'                                  " +
                        "where uml.messageStatus IN :MSG_STATUSES                                                       " +
                        "and um.mpc = :MPC                                                                              " +
                        "and uml.deleted is null                                                                        " +
                        "and uml.received < :DATE                                                                       " +
                        "and ((:EARCHIVE_IS_ACTIVE = true and uml.archived is not null) or :EARCHIVE_IS_ACTIVE = false)"),
        @NamedQuery(name = "UserMessageLog.findDownloadedUserMessagesOlderThan",
                query = "SELECT new eu.domibus.api.model.UserMessageLogDto(um.entityId,um.messageId,uml.backend,p)      " + // need this property in WSPlugin
                        "FROM UserMessageLog uml                                                                        " +
                        "INNER JOIN uml.userMessage um                                                                  " +
                        "left join um.messageProperties p on p.name = 'finalRecipient'                                  " +
                        "where uml.messageStatus IN :MSG_STATUSES                                                       " +
                        "and um.mpc = :MPC                                                                              " +
                        "and uml.downloaded is not null and uml.downloaded < :DATE                                      " +
                        "and ((:EARCHIVE_IS_ACTIVE = true and uml.archived is not null) or :EARCHIVE_IS_ACTIVE = false)"),
        @NamedQuery(name = "UserMessageLog.findSentUserMessagesWithPayloadNotClearedOlderThan",
                query = "SELECT new eu.domibus.api.model.UserMessageLogDto(um.entityId,um.messageId,uml.backend,p)      " + // need this property in WSPlugin
                        "FROM UserMessageLog uml                                                                        " +
                        "INNER JOIN uml.userMessage um                                                                  " +
                        "left join um.messageProperties p on p.name = 'finalRecipient'                                  " +
                        "where uml.messageStatus IN :MSG_STATUSES                                                       " +
                        "and um.mpc = :MPC                                                                              " +
                        "and uml.deleted is null                                                                        " +
                        "and uml.modificationTime is not null                                                           " +
                        "and uml.modificationTime < :DATE                                                               " +
                        "and ((:EARCHIVE_IS_ACTIVE = true and uml.archived is not null) or :EARCHIVE_IS_ACTIVE = false)"),
        @NamedQuery(name = "UserMessageLog.findSentUserMessagesOlderThan",
                query = "SELECT new eu.domibus.api.model.UserMessageLogDto(um.entityId,um.messageId,uml.backend,p)      " + // need this property in WSPlugin
                        "FROM UserMessageLog uml                                                                        " +
                        "INNER JOIN uml.userMessage um                                                                  " +
                        "left join um.messageProperties p on p.name = 'finalRecipient'                                  " +
                        "where uml.messageStatus IN :MSG_STATUSES                                                       " +
                        "and um.mpc = :MPC                                                                              " +
                        "and uml.modificationTime is not null                                                           " +
                        "and uml.modificationTime < :DATE                                                               " +
                        "and ((:EARCHIVE_IS_ACTIVE = true and uml.archived is not null) or :EARCHIVE_IS_ACTIVE = false)"),
        @NamedQuery(name = "UserMessageLog.findAllMessages",
                query = "SELECT um.entityId                 as " + UserMessageLogDto.ENTITY_ID + "             ,     " +
                        "       um.messageId                as " + UserMessageLogDto.MESSAGE_ID + "            ,     " +
                        "       um.mshRole.entityId         as " + UserMessageLogDto.MESSAGE_ROLE + "         ,      " +
                        "       um.testMessage              as " + UserMessageLogDto.TEST_MESSAGE + "          ,     " +
                        "       uml.backend                 as " + UserMessageLogDto.MESSAGE_BACKEND + "       ,     " +
                        "       p.value                     as " + UserMessageLogDto.PROP_VALUE + "            ,     " +
                        "       p.name                      as " + UserMessageLogDto.PROP_NAME + "                   " +
                        "FROM UserMessageLog uml                                                                     " +
                        "JOIN uml.userMessage um                                                                     " +
                        "left join um.messageProperties p                                                            "
                        ),
        @NamedQuery(name = "UserMessageLog.countEntries", query = "select count(userMessageLog.entityId) from UserMessageLog userMessageLog"),
        @NamedQuery(name = "UserMessageLog.findAllInfo", query = "select userMessageLog from UserMessageLog userMessageLog"),
        @NamedQuery(name = "UserMessageLog.findMessagesForArchivingAsc",
                query = "select new EArchiveBatchUserMessage(uml.entityId, uml.userMessage.messageId, uml.messageStatus.entityId)                " +
                        "from UserMessageLog uml                                                                     " +
                        "where uml.entityId > :LAST_ENTITY_ID                                                        " +
                        "  and (:MAX_ENTITY_ID IS NULL OR uml.entityId < :MAX_ENTITY_ID)                             " +
                        "  and uml.messageStatus in :STATUSES                                          " +
                        "  and uml.deleted IS NULL                                                                   " +
                        "  and uml.exported IS NULL                                                                  " +
                        "  and uml.userMessage.testMessage IS FALSE                                                  " +
                        "  and (uml.userMessage.messageFragment IS FALSE OR uml.userMessage.messageFragment IS NULL) " +
                        "order by uml.entityId asc                                                                   "),
        @NamedQuery(name = "UserMessageLog.countMessagesForArchiving",
                query = "select new java.lang.Long(count(uml.entityId)) " +
                        "from UserMessageLog uml " +
                        "where uml.entityId > :LAST_ENTITY_ID " +
                        "  and uml.entityId < :MAX_ENTITY_ID " +
                        "  and uml.messageStatus in :STATUSES " +
                        "  and uml.userMessage.testMessage IS FALSE " +
                        "  and uml.deleted IS NULL " +
                        "  and uml.exported IS NULL "),
        @NamedQuery(name = "UserMessageLog.deleteMessageLogs", query =
                "delete from UserMessageLog uml where uml.entityId in :IDS"),
        @NamedQuery(name = "UserMessageLog.updateArchived", query =
                "UPDATE UserMessageLog uml                                          " +
                        "SET uml.archived = :CURRENT_TIMESTAMP                      " +
                        "WHERE uml.entityId IN( :ENTITY_IDS )                       "),
        @NamedQuery(name = "UserMessageLog.updateExported", query =
                "UPDATE UserMessageLog uml                                          " +
                        "SET uml.exported = :CURRENT_TIMESTAMP                      " +
                        "WHERE uml.entityId IN( :ENTITY_IDS )                       "),
        @NamedQuery(name = "UserMessageLog.updateDeleted", query =
                "UPDATE UserMessageLog uml                                          " +
                        "SET uml.deleted = :CURRENT_TIMESTAMP,                      " +
                        "   uml.messageStatus = :DELETED_STATUS                    " +
                        "WHERE uml.entityId IN( :ENTITY_IDS )                       "),
        @NamedQuery(name = "UserMessageLog.findUnsentAndWaitingForRetryMessages",
                query = "select um.messageId " +
                        "FROM UserMessageLog uml " +
                        "INNER JOIN uml.userMessage um " +
                        "where uml.received <= :MINUTES_AGO_TIMESTAMP " +
                        "and (uml.messageStatus = :SEND_ENQUEUED " +
                        "       or (uml.messageStatus = :WAITING_FOR_RETRY " +
                        "               and uml.entityId < :MAX_ENTITY_ID))"),

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

    @Column(name = "EXPORTED")
    @Temporal(TemporalType.TIMESTAMP)
    private Date exported;

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

    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
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

    @Enumerated(EnumType.STRING)
    @Column(name = "PROCESSING_TYPE")
    private ProcessingType processingType;

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

    public Date getExported() {
        return exported;
    }

    public void setExported(Date archived) {
        this.exported = archived;
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
