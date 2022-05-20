package eu.domibus.core.message;

import eu.domibus.api.model.DomibusBaseEntity;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.core.dao.ListDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.logging.MDCKey;
import org.apache.commons.lang3.StringUtils;

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
public abstract class MessageLogDao<F extends DomibusBaseEntity> extends ListDao<F> implements MessageLogDaoBase {

    public MessageLogDao(final Class<F> type) {
        super(type);
    }

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

    @Override
    public long countEntries(Map<String, Object> filters) {
        MessageLogInfoFilter filterService = getMessageLogInfoFilter();
        String queryString = filterService.getCountMessageLogQuery(filters);
        TypedQuery<Number> query = em.createQuery(queryString, Number.class);
        query = filterService.applyParameters(query, filters);
        final Number count = query.getSingleResult();
        return count.intValue();
    }

    @Override
    public boolean hasMoreEntriesThan(Map<String, Object> filters, int limit) {
        MessageLogInfoFilter filterService = getMessageLogInfoFilter();
        String queryString = filterService.getMessageLogIdQuery(filters);
        TypedQuery<Number> query = em.createQuery(queryString, Number.class);
        query = filterService.applyParameters(query, filters);
        query.setMaxResults(1);
        query.setFirstResult(limit + 1);
        final List<Number> results = query.getResultList();
        return results.size() > 0;
    }

    public List<MessageLogInfo> findAllInfoPaged(int from, int max, String column, boolean asc, Map<String, Object> filters) {
        MessageLogInfoFilter filterService = getMessageLogInfoFilter();
        String filteredMessageLogQuery = filterService.filterMessageLogQuery(column, asc, filters);
        TypedQuery<MessageLogInfo> typedQuery = em.createQuery(filteredMessageLogQuery, MessageLogInfo.class);
        TypedQuery<MessageLogInfo> queryParameterized = filterService.applyParameters(typedQuery, filters);
        queryParameterized.setFirstResult(from);
        queryParameterized.setMaxResults(max);
        return queryParameterized.getResultList();
    }

}
