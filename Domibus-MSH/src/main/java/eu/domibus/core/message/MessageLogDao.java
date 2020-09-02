package eu.domibus.core.message;

import eu.domibus.api.message.MessageSubtype;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.core.dao.ListDao;
import eu.domibus.ebms3.common.model.MessageType;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.logging.MDCKey;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.sql.Timestamp;
import java.util.*;


/**
 * @param <F> MessageLog type: either UserMessageLog or SignalMessageLog
 * @author Federico Martini
 * @since 3.2
 */
public abstract class MessageLogDao<F extends MessageLog> extends ListDao<F> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageLog.class);

    protected static final String STR_MESSAGE_ID = "MESSAGE_ID";

    public MessageLogDao(final Class<F> type) {
        super(type);
    }

    @MDCKey(DomibusLogger.MDC_MESSAGE_ID)
    public void setMessageStatus(F messageLog, MessageStatus messageStatus) {
        messageLog.setMessageStatus(messageStatus);

        switch (messageStatus) {
            case DELETED:
            case ACKNOWLEDGED:
            case ACKNOWLEDGED_WITH_WARNING:
                messageLog.setDeleted(new Date());
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
        final String messageId = messageLog.getMessageId();
        if (StringUtils.isNotBlank(messageId)) {
            LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, messageId);
        }
        LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_STATUS_UPDATE, messageLog.getMessageType(), messageStatus);
    }

    public abstract MessageStatus getMessageStatus(String messageId);

    protected abstract MessageLog findByMessageId(String messageId);

    protected abstract MessageLog findByMessageId(String messageId, MSHRole mshRole);

    @Override
    protected List<Predicate> getPredicates(Map<String, Object> filters, CriteriaBuilder cb, Root<F> mle) {
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

    protected abstract MessageLogInfoFilter getMessageLogInfoFilter();

    public abstract String findLastTestMessageId(String party);

    protected String findLastTestMessageId(String party, MessageType messageType, MSHRole mshRole) {
        Map<String, Object> filters = new HashMap<>();
        filters.put("messageSubtype", MessageSubtype.TEST);
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
}
