package eu.domibus.core.message;

import com.google.common.collect.Maps;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.core.plugin.notification.NotificationStatus;
import eu.domibus.ebms3.common.model.MessageType;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Christian Koch, Stefan Mueller, Federico Martini
 * @since 3.0
 */
@Repository
public class UserMessageLogDao extends MessageLogDao<UserMessageLog> {

    @Autowired
    private UserMessageLogInfoFilter userMessageLogInfoFilter;

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
        String queryString = "select distinct m.messageInfo.messageId from UserMessage m " +
                "inner join m.messageProperties.property p, UserMessageLog ml " +
                "where ml.messageId = m.messageInfo.messageId and ml.messageStatus = 'SEND_FAILURE' and ml.messageType = 'USER_MESSAGE' and ml.deleted is null ";
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
    public UserMessageLog findByMessageIdSafely(String messageId) {
        try {
            return findByMessageId(messageId);
        } catch (NoResultException nrEx) {
            LOG.debug("Could not find any result for message with id [" + messageId + "]");
            return null;
        }
    }

    @Override
    public MessageStatus getMessageStatus(String messageId) {
        try {
            TypedQuery<MessageStatus> query = em.createNamedQuery("UserMessageLog.getMessageStatus", MessageStatus.class);
            query.setParameter(STR_MESSAGE_ID, messageId);
            return query.getSingleResult();
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

    public List<String> getUndownloadedUserMessagesOlderThan(Date date, String mpc, Integer expiredNotDownloadedMessagesLimit) {
        return getMessagesOlderThan(date, mpc, expiredNotDownloadedMessagesLimit, "UserMessageLog.findUndownloadedUserMessagesOlderThan");
    }

    public List<String> getDownloadedUserMessagesOlderThan(Date date, String mpc, Integer expiredDownloadedMessagesLimit) {
        return getMessagesOlderThan(date, mpc, expiredDownloadedMessagesLimit, "UserMessageLog.findDownloadedUserMessagesOlderThan");
    }

    public List<String> getSentUserMessagesOlderThan(Date date, String mpc, Integer expiredSentMessagesLimit) {
        return getMessagesOlderThan(date, mpc, expiredSentMessagesLimit, "UserMessageLog.findSentUserMessagesOlderThan");
    }

    private List<String> getMessagesOlderThan(Date startDate, String mpc, Integer expiredMessagesLimit, String queryName) {
        TypedQuery<String> query = em.createNamedQuery(queryName, String.class);
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
        messageLog.setNotificationStatus(NotificationStatus.NOTIFIED);
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

    @Override
    protected MessageLogInfoFilter getMessageLogInfoFilter() {
        return userMessageLogInfoFilter;
    }

    @Override
    public String findLastTestMessageId(String party) {
        return super.findLastTestMessageId(party, MessageType.USER_MESSAGE, MSHRole.SENDING);
    }

}
