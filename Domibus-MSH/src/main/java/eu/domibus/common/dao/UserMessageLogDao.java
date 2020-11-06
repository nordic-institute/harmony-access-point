package eu.domibus.common.dao;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Maps;
import eu.domibus.api.message.MessageSubtype;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;
import eu.domibus.common.model.logging.MessageLogInfo;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.common.model.logging.UserMessageLogInfoFilter;
import eu.domibus.ebms3.common.model.MessageType;
import eu.domibus.ebms3.common.model.PartyId;
import eu.domibus.ebms3.common.model.PartyInfo;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.*;

/**
 * @author Christian Koch, Stefan Mueller, Federico Martini
 * @since 3.0
 */
@Repository
public class UserMessageLogDao extends MessageLogDao<UserMessageLog> {

    @Autowired
    private UserMessageLogInfoFilter userMessageLogInfoFilter;

    @Autowired
    private MetricRegistry metricRegistry;

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
        com.codahale.metrics.Timer.Context authorizeUserMessageMetric = metricRegistry.timer(MetricRegistry.name(UserMessageLog.class, "getMessageStatus")).time();

        try {
            TypedQuery<MessageStatus> query = em.createNamedQuery("UserMessageLog.getMessageStatus", MessageStatus.class);
            query.setParameter(STR_MESSAGE_ID, messageId);
            return query.getSingleResult();
        } catch (NoResultException nrEx) {
            LOG.debug("No result for message with id [" + messageId + "]");
            return MessageStatus.NOT_FOUND;
        } finally {
            authorizeUserMessageMetric.stop();
        }
    }


    public UserMessageLog findByMessageId(String messageId) {
        com.codahale.metrics.Timer.Context authorizeUserMessageMetric = metricRegistry.timer(MetricRegistry.name(UserMessageLog.class, "findByMessageId")).time();

        //TODO do not bubble up DAO specific exceptions; just return null and make sure it is treated accordingly
        TypedQuery<UserMessageLog> query = em.createNamedQuery("UserMessageLog.findByMessageId", UserMessageLog.class);
        query.setParameter(STR_MESSAGE_ID, messageId);
        UserMessageLog singleResult = query.getSingleResult();

        authorizeUserMessageMetric.stop();

        return singleResult;
    }

    public Boolean messageExists(String messageId, MSHRole mshRole) {
        com.codahale.metrics.Timer.Context authorizeUserMessageMetric = metricRegistry.timer(MetricRegistry.name(UserMessageLog.class, "messageExists")).time();

        TypedQuery<Long> query = this.em.createNamedQuery("UserMessageLog.messageExist", Long.class);
        query.setParameter(STR_MESSAGE_ID, messageId);
        query.setParameter("MSH_ROLE", mshRole);

        try {
            Long count = query.getSingleResult();
            return ((count.equals(0L)) ? false : true);
        } catch (NoResultException nrEx) {
            LOG.debug("Query UserMessageLog.messageExist did not find any result for message with id [" + messageId + "] and MSH role [" + mshRole + "]");
            return null;
        } finally {
            authorizeUserMessageMetric.stop();
        }
    }

    public UserMessageLog findByMessageId(String messageId, MSHRole mshRole) {
        com.codahale.metrics.Timer.Context authorizeUserMessageMetric = metricRegistry.timer(MetricRegistry.name(UserMessageLog.class, "findByMessageIdAndRole")).time();

        TypedQuery<UserMessageLog> query = this.em.createNamedQuery("UserMessageLog.findByMessageIdAndRole", UserMessageLog.class);
        query.setParameter(STR_MESSAGE_ID, messageId);
        query.setParameter("MSH_ROLE", mshRole);

        try {
            return query.getSingleResult();
        } catch (NoResultException nrEx) {
            LOG.debug("Query UserMessageLog.findByMessageId did not find any result for message with id [" + messageId + "] and MSH role [" + mshRole + "]");
            return null;
        } finally {
            authorizeUserMessageMetric.stop();
        }
    }

    public List<String> getUndownloadedUserMessagesOlderThan(Date date, String mpc, Integer expiredNotDownloadedMessagesLimit) {
        return getMessagesOlderThan(date, mpc, expiredNotDownloadedMessagesLimit, "UserMessageLog.findUndownloadedUserMessagesOlderThan");
    }

    public List<String> getDownloadedUserMessagesOlderThan(Date date, String mpc, Integer expiredDownloadedMessagesLimit) {
        return getMessagesOlderThan(date, mpc, expiredDownloadedMessagesLimit, "UserMessageLog.findDownloadedUserMessagesOlderThan");
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

    @Transactional
    public void setAsNotified(final UserMessageLog messageLog) {
        messageLog.setNotificationStatus(NotificationStatus.NOTIFIED);
        super.update(messageLog);
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

        String filteredUserMessageLogQuery = userMessageLogInfoFilter.filterUserMessageLogQuery(column, asc, filters);
        TypedQuery<UserMessageLog> typedQuery = em.createQuery(filteredUserMessageLogQuery, UserMessageLog.class);
        TypedQuery<UserMessageLog> queryParameterized = userMessageLogInfoFilter.applyParameters(typedQuery, filters);
        queryParameterized.setFirstResult(from);
        queryParameterized.setMaxResults(max);
        long startTime = 0;
        if (LOG.isDebugEnabled()) {
            startTime = System.currentTimeMillis();
        }
        final List<UserMessageLog> userMessageLogs = queryParameterized.getResultList();
        List<MessageLogInfo> resultList = convert(userMessageLogs);
        if (LOG.isDebugEnabled()) {
            final long endTime = System.currentTimeMillis();
            LOG.debug("[{}] millisecond to execute query for [{}] results", endTime - startTime, resultList.size());
        }
        return resultList;
    }

    protected List<MessageLogInfo> convert(List<UserMessageLog> list) {
        List<MessageLogInfo> result = new ArrayList<>();
        for (UserMessageLog msg : list) {
            MessageLogInfo messageLogInfo = new MessageLogInfo();
            messageLogInfo.setMessageId(msg.getMessageId());
            messageLogInfo.setMessageStatus(msg.getMessageStatus());
            messageLogInfo.setNotificationStatus(msg.getNotificationStatus());
            messageLogInfo.setMshRole(msg.getMshRole());
            messageLogInfo.setMessageType(msg.getMessageType());
            messageLogInfo.setDeleted(msg.getDeleted());
            messageLogInfo.setReceived(msg.getReceived());
            messageLogInfo.setSendAttempts(msg.getSendAttempts());
            messageLogInfo.setSendAttemptsMax(msg.getSendAttemptsMax());
            messageLogInfo.setNextAttempt(msg.getNextAttempt());
            UserMessage userMessage = msg.getUserMessage();
            messageLogInfo.setConversationId(userMessage.getCollaborationInfo().getConversationId());


            Set<PartyId> parties = userMessage.getPartyInfo().getParties();
            Optional<PartyId> partyFrom = parties.stream().filter(partyId -> PartyInfo.DIRECTION_FROM.equals(partyId.getDirection())).findFirst();
            if(partyFrom.isPresent()) {
                messageLogInfo.setFromPartyId(partyFrom.get().getValue());
            }


            Optional<PartyId> partyTo = parties.stream().filter(partyId -> PartyInfo.DIRECTION_TO.equals(partyId.getDirection())).findFirst();
            if(partyTo.isPresent()) {
                messageLogInfo.setToPartyId(partyTo.get().getValue());
            }
            messageLogInfo.setFailed(msg.getFailed());
            //TODO add other fields
            /*

                "info.refToMessageId," +
                "log.failed," +
                "log.restored," +
                "log.messageSubtype," +
                "log.messageFragment," +
                "log.sourceMessage" +
             */
            result.add(messageLogInfo);
        }

        return result;
    }

    public String findLastUserTestMessageId(String party) {
        Map<String, Object> filters = new HashMap<>();
        filters.put("messageSubtype", MessageSubtype.TEST);
        filters.put("mshRole", MSHRole.SENDING);
        filters.put("toPartyId", party);
        filters.put("messageType", MessageType.USER_MESSAGE);
        String filteredUserMessageLogQuery = userMessageLogInfoFilter.filterUserMessageLogQuery("received", false, filters);
        TypedQuery<MessageLogInfo> typedQuery = em.createQuery(filteredUserMessageLogQuery, MessageLogInfo.class);
        TypedQuery<MessageLogInfo> queryParameterized = userMessageLogInfoFilter.applyParameters(typedQuery, filters);
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

}