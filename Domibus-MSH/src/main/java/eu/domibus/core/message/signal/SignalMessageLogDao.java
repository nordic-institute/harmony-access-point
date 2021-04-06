package eu.domibus.core.message.signal;

import com.google.common.collect.Maps;
import eu.domibus.api.message.MessageSubtype;
import eu.domibus.api.model.*;
import eu.domibus.core.dao.BasicDao;
import eu.domibus.core.message.MessageLogInfo;
import eu.domibus.core.message.MessageLogInfoFilter;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Federico Martini
 * @since 3.2
 */
@Repository
public class SignalMessageLogDao extends BasicDao<SignalMessageLog> {

    @Autowired
    private SignalMessageLogInfoFilter signalMessageLogInfoFilter;

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SignalMessageLogDao.class);

    public SignalMessageLogDao() {
        super(SignalMessageLog.class);
    }

    public MessageStatusEntity getMessageStatus(String messageId) {
        TypedQuery<MessageStatusEntity> query = em.createNamedQuery("SignalMessageLog.getMessageStatus", MessageStatusEntity.class);
        query.setParameter("MESSAGE_ID", messageId);
        return DataAccessUtils.singleResult(query.getResultList());

    }

    public SignalMessageLog findByMessageId(String messageId) {
        TypedQuery<SignalMessageLog> query = em.createNamedQuery("SignalMessageLog.findByMessageId", SignalMessageLog.class);
        query.setParameter("MESSAGE_ID", messageId);
        return query.getSingleResult();
    }

    public SignalMessageLog findByMessageId(String messageId, MSHRole mshRole) {
        TypedQuery<SignalMessageLog> query = em.createNamedQuery("SignalMessageLog.findByMessageIdAndRole", SignalMessageLog.class);
        query.setParameter("MESSAGE_ID", messageId);
        query.setParameter("MSH_ROLE", mshRole);

        try {
            return query.getSingleResult();
        } catch (NoResultException nrEx) {
            LOG.debug("Query SignalMessageLog.findByMessageId did not find any result for message with id [" + messageId + "] and MSH role [" + mshRole + "]");
            return null;
        }
    }

    public int countAllInfo(boolean asc, Map<String, Object> filters) {
        final Map<String, Object> filteredEntries = Maps.filterEntries(filters, input -> input.getValue() != null);
        if (filteredEntries.size() == 0) {
            return countAll();
        }
        String filteredSignalMessageLogQuery = signalMessageLogInfoFilter.countSignalMessageLogQuery(asc, filters);
        TypedQuery<Number> countQuery = em.createQuery(filteredSignalMessageLogQuery, Number.class);
        countQuery = signalMessageLogInfoFilter.applyParameters(countQuery, filters);
        final Number count = countQuery.getSingleResult();
        return count.intValue();
    }

    public List<MessageLogInfo> findAllInfoPaged(int from, int max, String column, boolean asc, Map<String, Object> filters) {
        String filteredSignalMessageLogQuery = signalMessageLogInfoFilter.filterMessageLogQuery(column, asc, filters);
        TypedQuery<MessageLogInfo> typedQuery = em.createQuery(filteredSignalMessageLogQuery, MessageLogInfo.class);
        TypedQuery<MessageLogInfo> queryParameterized = signalMessageLogInfoFilter.applyParameters(typedQuery, filters);
        queryParameterized.setFirstResult(from);
        queryParameterized.setMaxResults(max);
        return queryParameterized.getResultList();
    }

    @Timer(clazz = SignalMessageLogDao.class, value = "deleteMessages.deleteMessageLogs")
    @Counter(clazz = SignalMessageLogDao.class, value = "deleteMessages.deleteMessageLogs")
    public int deleteMessageLogs(List<String> messageIds) {
        final Query deleteQuery = em.createNamedQuery("SignalMessageLog.deleteMessageLogs");
        deleteQuery.setParameter("MESSAGEIDS", messageIds);
        int result = deleteQuery.executeUpdate();
        LOG.trace("deleteSignalMessageLogs result [{}]", result);
        return result;
    }


    public Integer countAll() {
        final Query nativeQuery = em.createNativeQuery("SELECT count(sm.ID_PK) FROM  TB_SIGNAL_MESSAGE sm");
        final Number singleResult = (Number) nativeQuery.getSingleResult();
        return singleResult.intValue();
    }

    protected MessageLogInfoFilter getMessageLogInfoFilter() {
        return signalMessageLogInfoFilter;
    }

    public String findLastTestMessageId(String party) {
        Map<String, Object> filters = new HashMap<>();
        filters.put("messageSubtype", MessageSubtype.TEST);
        filters.put("mshRole", MSHRole.RECEIVING);
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

}
