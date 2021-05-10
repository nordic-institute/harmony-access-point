package eu.domibus.core.message;

import com.google.common.collect.Maps;
import eu.domibus.api.model.*;
import eu.domibus.core.dao.ListDao;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.logging.MDCKey;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.procedure.ProcedureOutputs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.sql.Timestamp;
import java.util.*;

/**
 * @author Christian Koch, Stefan Mueller, Federico Martini
 * @since 3.0
 */
@Repository
public class UserMessageLogDao extends ListDao<UserMessageLog> {

    private static final String STR_MESSAGE_ID = "MESSAGE_ID";

    @Autowired
    private UserMessageLogInfoFilter userMessageLogInfoFilter;

    @Autowired
    protected MessageStatusDao messageStatusDao;

    @Autowired
    protected NotificationStatusDao notificationStatusDao;

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageLogDao.class);

    public UserMessageLogDao() {
        super(UserMessageLog.class);
    }

    public List<String> findRetryMessages() {
        TypedQuery<String> query = this.em.createNamedQuery("UserMessageLog.findRetryMessages", String.class);
        query.setParameter("CURRENT_TIMESTAMP", new Date(System.currentTimeMillis()));

        return query.getResultList();
    }

    public List<String> findFailedMessages(String finalRecipient) {
        return findFailedMessages(finalRecipient, null, null);
    }

    public List<String> findFailedMessages(String finalRecipient, Date failedStartDate, Date failedEndDate) {
        String queryString = "select distinct m.messageId from UserMessageLog ml join ml.userMessage m " +
                "inner join m.messageProperties p,  " +
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


    public UserMessageLog findByMessageId(String messageId) {
        //TODO do not bubble up DAO specific exceptions; just return null and make sure it is treated accordingly
        TypedQuery<UserMessageLog> query = em.createNamedQuery("UserMessageLog.findByMessageId", UserMessageLog.class);
        query.setParameter(STR_MESSAGE_ID, messageId);
        return query.getSingleResult();

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
            LOG.debug("Query [{}] did not find any result for startDate [{}] startDate and MPC [{}]", queryName, startDate, mpc);
            return Collections.emptyList();
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

    public int countAllInfo(boolean asc, Map<String, Object> filters) {
        LOG.debug("Count all");
        final Map<String, Object> filteredEntries = Maps.filterEntries(filters, input -> input.getValue() != null);
        if (filteredEntries.size() == 0) {
            LOG.debug("Filter empty");
            return countAll();
        }
        String filteredUserMessageLogQuery = userMessageLogInfoFilter.countUserMessageLogQuery(asc, filters);
        TypedQuery<Number> countQuery = em.createQuery(filteredUserMessageLogQuery, Number.class);
        countQuery = userMessageLogInfoFilter.applyParameters(countQuery, filters);
        final Number count = countQuery.getSingleResult();
        return count.intValue();
    }

    public Integer countAll() {
        LOG.debug("Executing native query");
        final Query nativeQuery = em.createNativeQuery("SELECT count(um.ID_PK) FROM  TB_USER_MESSAGE um");
        final Number singleResult = (Number) nativeQuery.getSingleResult();
        return singleResult.intValue();
    }

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

        String filteredUserMessageLogQuery = userMessageLogInfoFilter.filterMessageLogQuery(column, asc, filters);
        TypedQuery<MessageLogInfo> typedQuery = em.createQuery(filteredUserMessageLogQuery, MessageLogInfo.class);
        TypedQuery<MessageLogInfo> queryParameterized = userMessageLogInfoFilter.applyParameters(typedQuery, filters);
        queryParameterized.setFirstResult(from);
        queryParameterized.setMaxResults(max);
        long startTime = 0;
        if (LOG.isDebugEnabled()) {
            startTime = System.currentTimeMillis();
        }
        final List<MessageLogInfo> resultList = queryParameterized.getResultList();
        if (LOG.isDebugEnabled()) {
            final long endTime = System.currentTimeMillis();
            LOG.debug("[{}] millisecond to execute query for [{}] results", endTime - startTime, resultList.size());
        }
        return resultList;
    }

    @Timer(clazz = UserMessageLogDao.class, value = "deleteMessages.deleteMessageLogs")
    @Counter(clazz = UserMessageLogDao.class, value = "deleteMessages.deleteMessageLogs")
    public int deleteMessageLogs(List<String> messageIds) {
        final Query deleteQuery = em.createNamedQuery("UserMessageLog.deleteMessageLogs");
        deleteQuery.setParameter("MESSAGEIDS", messageIds);
        int result = deleteQuery.executeUpdate();
        LOG.trace("deleteUserMessageLogs result [{}]", result);
        return result;
    }

    protected MessageLogInfoFilter getMessageLogInfoFilter() {
        return userMessageLogInfoFilter;
    }

    public String findLastTestMessageId(String party) {
        return findLastTestMessageId(party, MessageType.USER_MESSAGE, MSHRole.SENDING);
    }

    protected String findLastTestMessageId(String party, MessageType messageType, MSHRole mshRole) {
        Map<String, Object> filters = new HashMap<>();
        filters.put("testMessage", true);
        filters.put("mshRole", mshRole);
        filters.put("toPartyId", party);
        filters.put("messageType", messageType);
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
                messageLog.setNextAttempt(null);
                break;
            case ACKNOWLEDGED:
            case ACKNOWLEDGED_WITH_WARNING:
                messageLog.setNextAttempt(null);
                break;
            case DOWNLOADED:
                messageLog.setDownloaded(new Date());
                messageLog.setNextAttempt(null);
                break;
            case SEND_FAILURE:
                messageLog.setFailed(new Date());
                messageLog.setNextAttempt(null);
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
}
