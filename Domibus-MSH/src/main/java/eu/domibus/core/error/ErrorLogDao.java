package eu.domibus.core.error;

import eu.domibus.core.dao.ListDao;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.ebms3.common.model.AbstractBaseEntity;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@Transactional
/**
 * @author Christian Koch, Stefan Mueller
 */
public class ErrorLogDao extends ListDao<ErrorLogEntry> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(ErrorLogDao.class);

    @Autowired
    private ErrorLogEntryTruncateUtil errorLogEntryTruncateUtil;

    public ErrorLogDao() {
        super(ErrorLogEntry.class);
    }

    public List<ErrorLogEntry> getErrorsForMessage(final String messageId) {
        final TypedQuery<ErrorLogEntry> query = this.em.createNamedQuery("ErrorLogEntry.findErrorsByMessageId", ErrorLogEntry.class);
        query.setParameter("MESSAGE_ID", messageId);
        return query.getResultList();
    }

    @Override
    protected List<Predicate> getPredicates(Map<String, Object> filters, CriteriaBuilder cb, Root<ErrorLogEntry> ele) {
        List<Predicate> predicates = new ArrayList<Predicate>();
        for (final Map.Entry<String, Object> filter : filters.entrySet()) {
            if (filter.getValue() != null) {
                if (filter.getValue() instanceof String) {
                    if (!filter.getValue().toString().isEmpty()) {
                        switch (filter.getKey().toString()) {
                            case "":
                                break;
                            default:
                                predicates.add(cb.like(ele.<String>get(filter.getKey()), (String) filter.getValue()));
                                break;
                        }
                    }
                } else if (filter.getValue() instanceof Date) {
                    if (!filter.getValue().toString().isEmpty()) {
                        switch (filter.getKey().toString()) {
                            case "":
                                break;
                            case "timestampFrom":
                                predicates.add(cb.greaterThanOrEqualTo(ele.<Date>get("timestamp"), (Timestamp) filter.getValue()));
                                break;
                            case "timestampTo":
                                predicates.add(cb.lessThanOrEqualTo(ele.<Date>get("timestamp"), (Timestamp) filter.getValue()));
                                break;
                            case "notifiedFrom":
                                predicates.add(cb.greaterThanOrEqualTo(ele.<Date>get("notified"), (Timestamp) filter.getValue()));
                                break;
                            case "notifiedTo":
                                predicates.add(cb.lessThanOrEqualTo(ele.<Date>get("notified"), (Timestamp) filter.getValue()));
                                break;
                            default:
                                predicates.add(cb.like(ele.<String>get(filter.getKey()), (String) filter.getValue()));
                                break;
                        }
                    }
                } else {
                    predicates.add(cb.equal(ele.<String>get(filter.getKey()), filter.getValue()));
                }
            }
        }
        return predicates;
    }

    public List<ErrorLogEntry> findAll() {
        final TypedQuery<ErrorLogEntry> query = this.em.createNamedQuery("ErrorLogEntry.findEntries", ErrorLogEntry.class);
        return query.getResultList();
    }

    public long countEntries() {
        final TypedQuery<Long> query = this.em.createNamedQuery("ErrorLogEntry.countEntries", Long.class);
        return query.getSingleResult();
    }

    @Override
    public void create(ErrorLogEntry errorLogEntry) {
        errorLogEntryTruncateUtil.truncate(errorLogEntry);
        super.create(errorLogEntry);
    }

    @Timer(clazz = ErrorLogDao.class,value = "deleteMessages.deleteErrorLogsByMessageIdInError")
    @Counter(clazz = ErrorLogDao.class,value = "deleteMessages.deleteErrorLogsByMessageIdInError")
    public int deleteErrorLogsByMessageIdInError(List<String> messageIds) {
        final Query deleteQuery = em.createNamedQuery("ErrorLogEntry.deleteByMessageIdsInError");
        deleteQuery.setParameter("MESSAGEIDS", messageIds);
        int result  = deleteQuery.executeUpdate();
        LOG.trace("deleteErrorLogsByMessageIdInError result [{}]", result);
        return result;
    }

    public int deleteErrorLogsWithoutMessageIdOlderThan(int days, int batchSize) {
        int result = 0;
        LOG.debug("Going to delete ErrorLogs without messageIds older than [{}] in a batch size of [{}]", days, batchSize);
        Date deletionTime = DateUtils.addDays(new Date(), -days);

        //search the entries
        final Query selectQuery = em.createNamedQuery("ErrorLogEntry.findErrorsWithoutMessageIds");
        selectQuery.setFirstResult(0);
        if (batchSize > 0) {
            selectQuery.setMaxResults(batchSize);
        }
        selectQuery.setParameter("DELETION_DATE", deletionTime);
        List<ErrorLogEntry> errorLogEntries = selectQuery.getResultList();
        LOG.debug("[{}] ErrorLogs found", errorLogEntries.size());

        //deletion
        if (CollectionUtils.isNotEmpty(errorLogEntries)) {
            final Query deleteQuery = em.createNamedQuery("ErrorLogEntry.deleteErrorsWithoutMessageIds");
            List<Long> entityIds =  errorLogEntries.stream().map(AbstractBaseEntity::getEntityId).collect(Collectors.toList());
            deleteQuery.setParameter("ENTITY_IDS", entityIds);
            result  = deleteQuery.executeUpdate();
            LOG.debug("Cleaned [{}] ErrorLogs without messageIds", result);
        }
        return result;
    }
}
