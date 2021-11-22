package eu.domibus.core.message;

import eu.domibus.api.model.*;
import eu.domibus.api.util.DateUtil;
import eu.domibus.core.earchive.EArchiveBatchUserMessage;
import eu.domibus.core.message.dictionary.NotificationStatusDao;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.core.scheduler.ReprogrammableService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.logging.MDCKey;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.procedure.ProcedureOutputs;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

import static eu.domibus.api.model.DomibusDatePrefixedSequenceIdGeneratorGenerator.DATETIME_FORMAT_DEFAULT;
import static eu.domibus.api.model.DomibusDatePrefixedSequenceIdGeneratorGenerator.MAX;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Locale.ENGLISH;

/**
 * @author Christian Koch, Stefan Mueller, Federico Martini
 * @since 3.0
 */
@Repository
public class UserMessageLogDao extends MessageLogDao<UserMessageLog> {

    private static final String STR_MESSAGE_ID = "MESSAGE_ID";
    public static final int IN_CLAUSE_MAX_SIZE = 1000;

    private final DateUtil dateUtil;

    private final UserMessageLogInfoFilter userMessageLogInfoFilter;

    private final MessageStatusDao messageStatusDao;

    private final NotificationStatusDao notificationStatusDao;

    private final ReprogrammableService reprogrammableService;

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageLogDao.class);

    public UserMessageLogDao(DateUtil dateUtil,
                             UserMessageLogInfoFilter userMessageLogInfoFilter,
                             MessageStatusDao messageStatusDao,
                             NotificationStatusDao notificationStatusDao,
                             ReprogrammableService reprogrammableService) {
        super(UserMessageLog.class);
        this.dateUtil = dateUtil;
        this.userMessageLogInfoFilter = userMessageLogInfoFilter;
        this.messageStatusDao = messageStatusDao;
        this.notificationStatusDao = notificationStatusDao;
        this.reprogrammableService = reprogrammableService;
    }

    public List<String> findRetryMessages() {
        TypedQuery<String> query = this.em.createNamedQuery("UserMessageLog.findRetryMessages", String.class);
        query.setParameter("CURRENT_TIMESTAMP", dateUtil.getUtcDate());

        return query.getResultList();
    }

    public List<EArchiveBatchUserMessage> findMessagesForArchivingAsc(long lastUserMessageLogId, long maxEntityIdToArchived, int size) {
        LOG.debug("UserMessageLog.findMessagesForArchivingAsc -> lastUserMessageLogId : [{}] maxEntityIdToArchived : [{}] size : [{}] ",
                lastUserMessageLogId,
                maxEntityIdToArchived,
                size);
        TypedQuery<EArchiveBatchUserMessage> query = this.em.createNamedQuery("UserMessageLog.findMessagesForArchivingAsc", EArchiveBatchUserMessage.class);

        query.setParameter("LAST_ENTITY_ID", lastUserMessageLogId);
        query.setParameter("MAX_ENTITY_ID", maxEntityIdToArchived);
        query.setParameter("STATUSES", MessageStatus.getFinalStates());
        query.setMaxResults(size);

        return query.getResultList();
    }

    public List<EArchiveBatchUserMessage> findMessagesNotFinalAsc(long lastUserMessageLogId, long maxEntityIdToArchived) {
        LOG.debug("UserMessageLog.findMessagesNotFinalDesc -> lastUserMessageLogId : [{}] maxEntityIdToArchived : [{}]",
                lastUserMessageLogId,
                maxEntityIdToArchived);
        TypedQuery<EArchiveBatchUserMessage> query = this.em.createNamedQuery("UserMessageLog.findMessagesForArchivingAsc", EArchiveBatchUserMessage.class);

        query.setParameter("LAST_ENTITY_ID", lastUserMessageLogId);
        query.setParameter("MAX_ENTITY_ID", maxEntityIdToArchived);
        query.setParameter("STATUSES", MessageStatus.getNotFinalStates());

        return query.getResultList();
    }

    public List<String> findFailedMessages(String finalRecipient) {
        return findFailedMessages(finalRecipient, null, null);
    }

    public List<String> findFailedMessages(String finalRecipient, Date failedStartDate, Date failedEndDate) {
        String queryString = "select distinct m.messageId from UserMessageLog ml join ml.userMessage m " +
                "left join m.messageProperties p, " +
                "where ml.messageStatus.messageStatus = 'SEND_FAILURE' and ml.deleted is null ";
        if (StringUtils.isNotEmpty(finalRecipient)) {
            queryString += " and p.name = 'finalRecipient' and p.value = :FINAL_RECIPIENT";
        }
        if (failedStartDate != null) {
            queryString += " and ml.failed >= :START_DATE";
        }
        if (failedEndDate != null) {
            queryString += " and ml.failed <= :END_DATE";
        }
        TypedQuery<String> query = this.em.createQuery(queryString, String.class);
        if (StringUtils.isNotEmpty(finalRecipient)) {
            query.setParameter("FINAL_RECIPIENT", finalRecipient);
        }
        if (failedStartDate != null) {
            query.setParameter("START_DATE", failedStartDate);
        }
        if (failedEndDate != null) {
            query.setParameter("END_DATE", failedEndDate);
        }
        return query.getResultList();
    }

    public List<String> findMessagesToDelete(String finalRecipient, Date startDate, Date endDate) {
        TypedQuery<String> query = this.em.createNamedQuery("UserMessageLog.findMessagesToDeleteNotInFinalStatusDuringPeriod", String.class);
        query.setParameter("MESSAGE_STATUSES", UserMessageLog.FINAL_STATUSES_FOR_MESSAGE);
        query.setParameter("FINAL_RECIPIENT", finalRecipient);
        query.setParameter("START_DATE", Long.parseLong(ZonedDateTime.ofInstant(startDate.toInstant(), ZoneOffset.UTC).format(ofPattern(DATETIME_FORMAT_DEFAULT, ENGLISH)) + MAX));

        query.setParameter("END_DATE", Long.parseLong(ZonedDateTime.ofInstant(endDate.toInstant(), ZoneOffset.UTC).format(ofPattern(DATETIME_FORMAT_DEFAULT, ENGLISH)) + MAX));
        return query.getResultList();
    }

    /**
     * Finds a UserMessageLog by message id. If the message id is not found it catches the exception raised Hibernate and returns null.
     *
     * @param messageId The message id
     * @return The UserMessageLog
     */
    @Transactional
    public UserMessageLog findByMessageIdSafely(String messageId) {
        try {
            final UserMessageLog userMessageLog = findByMessageId(messageId);
            initializeChildren(userMessageLog);
            return userMessageLog;
        } catch (NoResultException nrEx) {
            LOG.debug("Could not find any result for message with id [" + messageId + "]");
            return null;
        }
    }

    private void initializeChildren(UserMessageLog userMessageLog) {
        //initialize values from the second level cache
        userMessageLog.getMessageStatus();
        userMessageLog.getMshRole();
        userMessageLog.getNotificationStatus();
    }

    public MessageStatus getMessageStatus(String messageId) {
        try {
            TypedQuery<MessageStatusEntity> query = em.createNamedQuery("UserMessageLog.getMessageStatus", MessageStatusEntity.class);
            query.setParameter(STR_MESSAGE_ID, messageId);
            return query.getSingleResult().getMessageStatus();
        } catch (NoResultException nrEx) {
            LOG.debug("No result for message with id [" + messageId + "]");
            return MessageStatus.NOT_FOUND;
        }
    }

    @Transactional(readOnly = true)
    public UserMessageLog findByEntityId(Long entityId) {
        final UserMessageLog userMessageLog = super.read(entityId);

        initializeChildren(userMessageLog);

        return userMessageLog;
    }

    public UserMessageLog findByMessageId(String messageId) {
        //TODO do not bubble up DAO specific exceptions; just return null and make sure it is treated accordingly
        TypedQuery<UserMessageLog> query = em.createNamedQuery("UserMessageLog.findByMessageId", UserMessageLog.class);
        query.setParameter(STR_MESSAGE_ID, messageId);
        return query.getSingleResult();

    }

    public UserMessageLog findMessageToDeleteNotInFinalStatus(String messageId) {
        TypedQuery<UserMessageLog> query = em.createNamedQuery("UserMessageLog.findMessageToDeleteNotInFinalStatus", UserMessageLog.class);
        query.setParameter("MESSAGE_STATUSES", UserMessageLog.FINAL_STATUSES_FOR_MESSAGE);
        query.setParameter(STR_MESSAGE_ID, messageId);
        try {
            return query.getSingleResult();
        } catch (NoResultException nrEx) {
            LOG.debug("Query UserMessageLog.findMessageToDeleteNotInFinalStatus did not find any result for message with id [" + messageId + "]");
            return null;
        }
    }

    public UserMessageLog findByMessageId(String messageId, MSHRole mshRole) {
        TypedQuery<UserMessageLog> query = this.em.createNamedQuery("UserMessageLog.findByMessageIdAndRole", UserMessageLog.class);
        query.setParameter(STR_MESSAGE_ID, messageId);
        query.setParameter("MSH_ROLE", mshRole);

        try {
            return query.getSingleResult();
        } catch (NoResultException nrEx) {
            LOG.debug("Query UserMessageLog.findByMessageId did not find any result for message with id [" + messageId + "] and MSH role [" + mshRole + "]");
            return null;
        }
    }

    public List<UserMessageLogDto> getDeletedUserMessagesOlderThan(Date date, String mpc, Integer expiredDeletedMessagesLimit) {
        return getMessagesOlderThan(date, mpc, expiredDeletedMessagesLimit, "UserMessageLog.findDeletedUserMessagesOlderThan");
    }

    public List<UserMessageLogDto> getUndownloadedUserMessagesOlderThan(Date date, String mpc, Integer expiredNotDownloadedMessagesLimit) {
        return getMessagesOlderThan(date, mpc, expiredNotDownloadedMessagesLimit, "UserMessageLog.findUndownloadedUserMessagesOlderThan");
    }

    public List<UserMessageLogDto> getDownloadedUserMessagesOlderThan(Date date, String mpc, Integer expiredDownloadedMessagesLimit) {
        return getMessagesOlderThan(date, mpc, expiredDownloadedMessagesLimit, "UserMessageLog.findDownloadedUserMessagesOlderThan");
    }

    public List<UserMessageLogDto> getSentUserMessagesOlderThan(Date date, String mpc, Integer expiredSentMessagesLimit, boolean isDeleteMessageMetadata) {
        if (isDeleteMessageMetadata) {
            return getMessagesOlderThan(date, mpc, expiredSentMessagesLimit, "UserMessageLog.findSentUserMessagesOlderThan");
        }
        // return only messages with payload not already cleared
        return getSentUserMessagesWithPayloadNotClearedOlderThan(date, mpc, expiredSentMessagesLimit);
    }

    public void deleteExpiredMessages(Date startDate, Date endDate, String mpc, Integer expiredMessagesLimit, String queryName) {
        StoredProcedureQuery query = em.createStoredProcedureQuery(queryName)
                .registerStoredProcedureParameter(
                        "MPC",
                        String.class,
                        ParameterMode.IN
                )
                .registerStoredProcedureParameter(
                        "STARTDATE",
                        Date.class,
                        ParameterMode.IN
                )
                .registerStoredProcedureParameter(
                        "ENDDATE",
                        Date.class,
                        ParameterMode.IN
                )
                .registerStoredProcedureParameter(
                        "MAXCOUNT",
                        Integer.class,
                        ParameterMode.IN
                )
                .setParameter("MPC", mpc)
                .setParameter("STARTDATE", startDate)
                .setParameter("ENDDATE", endDate)
                .setParameter("MAXCOUNT", expiredMessagesLimit);

        try {
            query.execute();
        } finally {
            try {
                query.unwrap(ProcedureOutputs.class).release();
                LOG.debug("Finished releasing delete procedure");
            } catch (Exception ex) {
                LOG.error("Finally exception when using the stored procedure to delete", ex);
            }
        }
    }

    protected List<UserMessageLogDto> getSentUserMessagesWithPayloadNotClearedOlderThan(Date date, String mpc, Integer expiredSentMessagesLimit) {
        return getMessagesOlderThan(date, mpc, expiredSentMessagesLimit, "UserMessageLog.findSentUserMessagesWithPayloadNotClearedOlderThan");
    }

    /**
     * EDELIVERY-7772 Hibernate setResultTransformer deprecated
     */
    private List<UserMessageLogDto> getMessagesOlderThan(Date startDate, String mpc, Integer expiredMessagesLimit, String queryName) {
        Query query = em.createNamedQuery(queryName);

        query.unwrap(org.hibernate.query.Query.class)
                .setResultTransformer(new UserMessageLogDtoResultTransformer());
        query.setParameter("DATE", startDate);
        query.setParameter("MPC", mpc);
        query.setMaxResults(expiredMessagesLimit);

        try {
            return query.getResultList();
        } catch (NoResultException nrEx) {
            LOG.debug("Query [{}] did not find any result for startDate [{}] and MPC [{}]", queryName, startDate, mpc);
            return Collections.emptyList();
        }
    }

    @Transactional
    public int getMessagesNewerThan(Date startDate, String mpc, MessageStatus messageStatus, String partitionName) {
        String sqlString = "select count(*) from " +
                "             TB_USER_MESSAGE_LOG PARTITION ($PARTITION) " +
                "             inner join  TB_USER_MESSAGE   on TB_USER_MESSAGE_LOG.ID_PK=TB_USER_MESSAGE.ID_PK" +
                "             inner join  TB_D_MESSAGE_STATUS on TB_USER_MESSAGE_LOG.MESSAGE_STATUS_ID_FK=TB_D_MESSAGE_STATUS.ID_PK" +
                "             inner join  TB_D_MPC on TB_USER_MESSAGE.MPC_ID_FK=TB_D_MPC.ID_PK" +
                "           where TB_D_MESSAGE_STATUS.STATUS=:MESSAGESTATUS" +
                "             and TB_D_MPC.VALUE=:MPC" +
                "             and TB_USER_MESSAGE_LOG.$DATE_COLUMN is not null" +
                "             and TB_USER_MESSAGE_LOG.$DATE_COLUMN > :STARTDATE";

        sqlString = sqlString.replace("$PARTITION", partitionName);
        sqlString = sqlString.replace("$DATE_COLUMN", getDateColumn(messageStatus));

        LOG.trace("sqlString to find non expired messages: [{}]", sqlString);
        final Query countQuery = em.createNativeQuery(sqlString);
        countQuery.setParameter("MPC", mpc);
        countQuery.setParameter("STARTDATE", startDate);
        countQuery.setParameter("MESSAGESTATUS", messageStatus);
        int result = ((BigDecimal) countQuery.getSingleResult()).intValue();
        LOG.debug("count by message status result [{}] for mpc [{}] on partition [{}]", result, mpc, partitionName);
        return result;
    }

    protected String getDateColumn(MessageStatus messageStatus) {
        switch (messageStatus) {
            case ACKNOWLEDGED:
            case RECEIVED:
            case DOWNLOADED:
                return messageStatus.name();
            case SEND_FAILURE:
                return "FAILED";
            default:
                LOG.warn("Messages with status [{}] are not defined on the retention mechanism", messageStatus);
                return "INVALID_STATUS_FOR_RETENTION";
        }
    }

    public String findBackendForMessageId(String messageId) {
        TypedQuery<String> query = em.createNamedQuery("UserMessageLog.findBackendForMessage", String.class);
        query.setParameter(STR_MESSAGE_ID, messageId);
        return query.getSingleResult();
    }

    public void setAsNotified(final UserMessageLog messageLog) {
        final NotificationStatusEntity status = notificationStatusDao.findOrCreate(NotificationStatus.NOTIFIED);
        messageLog.setNotificationStatus(status);
    }


    @Override
    public List<MessageLogInfo> findAllInfoPaged(int from, int max, String column, boolean asc, Map<String, Object> filters) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Retrieving messages for parameters from [{}] max [{}] column [{}] asc [{}]", from, max, column, asc);
            for (Map.Entry<String, Object> stringObjectEntry : filters.entrySet()) {
                if (stringObjectEntry.getValue() != null) {
                    LOG.debug("Setting parameters for query ");
                    LOG.debug(stringObjectEntry.getKey() + "  " + stringObjectEntry.getValue());
                }
            }
        }

        long startTime = 0;
        if (LOG.isDebugEnabled()) {
            startTime = System.currentTimeMillis();
        }

        final List<MessageLogInfo> resultList = super.findAllInfoPaged(from, max, column, asc, filters);

        if (LOG.isDebugEnabled()) {
            final long endTime = System.currentTimeMillis();
            LOG.debug("[{}] millisecond to execute query for [{}] results", endTime - startTime, resultList.size());
        }
        return resultList;
    }

    @Timer(clazz = UserMessageLogDao.class, value = "deleteMessages.deleteMessageLogs")
    @Counter(clazz = UserMessageLogDao.class, value = "deleteMessages.deleteMessageLogs")
    public int deleteMessageLogs(List<Long> ids) {
        final Query deleteQuery = em.createNamedQuery("UserMessageLog.deleteMessageLogs");
        deleteQuery.setParameter("IDS", ids);
        int result = deleteQuery.executeUpdate();
        LOG.trace("deleteUserMessageLogs result [{}]", result);
        return result;
    }

    @Transactional
    public int countUnarchivedMessagesOnPartition(String partitionName) {
        final Query countQuery = em.createNativeQuery("SELECT COUNT(*) FROM tb_user_message_log PARTITION (" + partitionName + ") WHERE archived IS NULL");
        try {
            int result = ((BigDecimal) countQuery.getSingleResult()).intValue();
            LOG.debug("count unarchived messages result [{}]", result);
            return result;
        } catch (NoResultException nre) {
            LOG.warn("Could not count unarchived messages for partition [{}], result [{}]", partitionName, nre);
            return -1;
        }
    }

    @Transactional
    public int countByMessageStatusOnPartition(List<String> messageStatuses, String partitionName) {
        String sqlString = "SELECT COUNT(*) FROM TB_USER_MESSAGE_LOG PARTITION (" + partitionName + ") INNER JOIN TB_D_MESSAGE_STATUS dms ON MESSAGE_STATUS_ID_FK=dms.ID_PK WHERE dms.STATUS NOT IN :MESSAGE_STATUSES";
        try {
            final Query countQuery = em.createNativeQuery(sqlString);
            countQuery.setParameter("MESSAGE_STATUSES", messageStatuses);
            int result = ((BigDecimal) countQuery.getSingleResult()).intValue();
            LOG.debug("count by message status result [{}]", result);
            return result;
        } catch (NoResultException nre) {
            LOG.warn("Could not count in progress messages for partition [{}], result [{}]", partitionName, nre);
            return -1;
        }
    }

    protected MessageLogInfoFilter getMessageLogInfoFilter() {
        return userMessageLogInfoFilter;
    }

    public String findLastTestMessageId(String party) {
        Map<String, Object> filters = new HashMap<>();
        filters.put("testMessage", true);
        filters.put("mshRole", MSHRole.SENDING);
        filters.put("toPartyId", party);
        String filteredMessageLogQuery = getMessageLogInfoFilter().filterMessageLogQuery("received", false, filters);
        TypedQuery<MessageLogInfo> typedQuery = em.createQuery(filteredMessageLogQuery, MessageLogInfo.class);
        TypedQuery<MessageLogInfo> queryParameterized = getMessageLogInfoFilter().applyParameters(typedQuery, filters);
        queryParameterized.setFirstResult(0);
        queryParameterized.setMaxResults(1);
        long startTime = 0;
        if (LOG.isDebugEnabled()) {
            startTime = System.currentTimeMillis();
        }
        final List<MessageLogInfo> resultList = queryParameterized.getResultList();
        if (LOG.isDebugEnabled()) {
            final long endTime = System.currentTimeMillis();
            LOG.debug("[{}] millisecond to execute query for [{}] results", endTime - startTime, resultList.size());
        }
        return resultList.isEmpty() ? null : resultList.get(0).getMessageId();
    }

    @MDCKey(DomibusLogger.MDC_MESSAGE_ID)
    public void setMessageStatus(UserMessageLog messageLog, MessageStatus messageStatus) {
        MessageStatusEntity messageStatusEntity = messageStatusDao.findOrCreate(messageStatus);
        messageLog.setMessageStatus(messageStatusEntity);

        switch (messageStatus) {
            case DELETED:
                messageLog.setDeleted(new Date());
                reprogrammableService.removeRescheduleInfo(messageLog);
                break;
            case ACKNOWLEDGED:
            case ACKNOWLEDGED_WITH_WARNING:
                messageLog.setAcknowledged(new Date());
                reprogrammableService.removeRescheduleInfo(messageLog);
                break;
            case DOWNLOADED:
                messageLog.setDownloaded(new Date());
                reprogrammableService.removeRescheduleInfo(messageLog);
                break;
            case SEND_FAILURE:
                messageLog.setFailed(new Date());
                reprogrammableService.removeRescheduleInfo(messageLog);
                break;
            default:
        }
        LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_STATUS_UPDATE, "USER_MESSAGE", messageStatus);
    }


    @Override
    protected List<Predicate> getPredicates(Map<String, Object> filters, CriteriaBuilder cb, Root<UserMessageLog> mle) {
        List<Predicate> predicates = new ArrayList<>();
        for (Map.Entry<String, Object> filter : filters.entrySet()) {
            if (filter.getValue() != null) {
                if (filter.getValue() instanceof String) {
                    if (!filter.getValue().toString().isEmpty()) {
                        switch (filter.getKey()) {
                            case "":
                                break;
                            default:
                                predicates.add(cb.like(mle.get(filter.getKey()), (String) filter.getValue()));
                                break;
                        }
                    }
                } else if (filter.getValue() instanceof Date) {
                    if (!filter.getValue().toString().isEmpty()) {
                        switch (filter.getKey()) {
                            case "receivedFrom":
                                predicates.add(cb.greaterThanOrEqualTo(mle.<Date>get("received"), Timestamp.valueOf(filter.getValue().toString())));
                                break;
                            case "receivedTo":
                                predicates.add(cb.lessThanOrEqualTo(mle.<Date>get("received"), Timestamp.valueOf(filter.getValue().toString())));
                                break;
                            default:
                                break;
                        }
                    }
                } else {
                    predicates.add(cb.equal(mle.<String>get(filter.getKey()), filter.getValue()));
                }
            }
        }
        return predicates;
    }

    public UserMessageLog findById(Long entityId) {
        return this.em.find(UserMessageLog.class, entityId);
    }

    public void updateArchived(List<Long> entityIds) {
        if (CollectionUtils.isEmpty(entityIds)) {
            return;
        }

        int totalSize = entityIds.size();

        int maxBatchesToCreate = (totalSize - 1) / IN_CLAUSE_MAX_SIZE;

        IntStream.range(0, maxBatchesToCreate + 1)
                .mapToObj(createList(entityIds, totalSize, maxBatchesToCreate))
                .forEach(this::updateArchivedBatched);

    }

    private IntFunction<List<Long>> createList(List<Long> entityIds, int totalSize, int maxBatchesToCreate) {
        return i -> entityIds.subList(
                getFromIndex(i),
                getToIndex(i, totalSize, maxBatchesToCreate));
    }

    private int getFromIndex(int i) {
        return i * IN_CLAUSE_MAX_SIZE;
    }

    private int getToIndex(int i, int totalSize, int maxBatchesToCreate) {
        if (i == maxBatchesToCreate) {
            return totalSize;
        }
        return (i + 1) * IN_CLAUSE_MAX_SIZE;
    }

    public void updateArchivedBatched(List<Long> entityIds) {
        Query namedQuery = this.em.createNamedQuery("UserMessageLog.updateArchived");

        namedQuery.setParameter("ENTITY_IDS", entityIds);
        namedQuery.setParameter("CURRENT_TIMESTAMP", dateUtil.getUtcDate());
        int i = namedQuery.executeUpdate();
        if (LOG.isTraceEnabled()) {
            LOG.trace("UserMessageLogs [{}] updated(0:no, 1: yes) with current_time: [{}]", entityIds, i);
        }
    }
}
