package eu.domibus.core.replication;

import eu.domibus.api.message.MessageSubtype;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;
import eu.domibus.common.dao.FilterableDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;

/**
 * DAO implementation for {@link UIMessageEntity}
 *
 * @author Catalin Enache
 * @since 4.0
 */
@Repository
public class UIMessageDaoImpl extends FilterableDao<UIMessageEntity> implements UIMessageDao {

    /**
     * logger
     */
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UIMessageDaoImpl.class);

    /**
     * message id
     */
    private static final String MESSAGE_ID = "MESSAGE_ID";

    /**
     * just map special column names which doesn't match filterKey with the same name
     */
    private static Map<String, String> filterKeyToColumnMap = new HashMap<>();

    static {
        filterKeyToColumnMap.put("fromPartyId", "fromId");
        filterKeyToColumnMap.put("toPartyId", "toId");
        filterKeyToColumnMap.put("originalSender", "fromScheme");
        filterKeyToColumnMap.put("finalRecipient", "toScheme");
    }


    public UIMessageDaoImpl() {
        super(UIMessageEntity.class);
    }

    /**
     * Find {@link UIMessageEntity} by messageId
     *
     * @param messageId
     * @return an instance of {@link UIMessageEntity}
     */
    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
    public UIMessageEntity findUIMessageByMessageId(final String messageId) {

        final TypedQuery<UIMessageEntity> query = this.em.createNamedQuery("UIMessageEntity.findUIMessageByMessageId", UIMessageEntity.class);
        query.setParameter(MESSAGE_ID, messageId);

        return DataAccessUtils.singleResult(query.getResultList());
    }

    /**
     * Counts the messages from {@code TB_MESSAGE_UI} table
     * filter object should contain messageType
     *
     * @param filters it should include messageType always - User or Signal message
     * @return number of messages
     */
    @Override
    public long countEntries(Map<String, Object> filters) {
        long startTime = System.currentTimeMillis();

        Long result = super.countEntries(filters);

        if (LOG.isDebugEnabled()) {
            final long endTime = System.currentTimeMillis();
            LOG.debug("[{}] milliseconds to countMessages", endTime - startTime);
        }
        return result;
    }

    /**
     * list the messages from {@code TB_MESSAGE_UI} table with pagination
     *
     * @param from       the beginning of the page
     * @param max        how many messages in a page
     * @param sortColumn which column to order by
     * @param asc        ordering type - ascending or descending
     * @param filters    it should include messageType always - User or Signal message
     * @return a list of {@link UIMessageEntity}
     */
    @Override
    public List<UIMessageEntity> findPaged(int from, int max, String sortColumn, boolean asc, Map<String, Object> filters) {
        long startTime = System.currentTimeMillis();

        List<UIMessageEntity> result = super.findPaged(from, max, sortColumn, asc, filters);

        final long endTime = System.currentTimeMillis();
        LOG.debug("[{}] milliseconds to findPaged [{}] messages", endTime - startTime, max);
        return result;
    }

    @Override
    protected String getSortColumn(String sortColumn) {
        String filterColumn = filterKeyToColumnMap.get(sortColumn);
        if (StringUtils.isNotBlank(filterColumn)) {
            return filterColumn;
        } else {
            return sortColumn;
        }
    }

    /**
     * Creates or updates an existing {@link UIMessageEntity}
     *
     * @param uiMessageEntity
     */
    @Override
    public void saveOrUpdate(final UIMessageEntity uiMessageEntity) {
        // Sonar Bug: ignored because the following call happens within a transaction that gets started by the service calling this method
        UIMessageEntity uiMessageEntityFound = findUIMessageByMessageId(uiMessageEntity.getMessageId()); //NOSONAR
        Date currentDate = new Date(System.currentTimeMillis());
        uiMessageEntity.setLastModified(currentDate);
        uiMessageEntity.setLastModified2(currentDate);
        if (uiMessageEntityFound != null) {
            LOG.debug("TB_MESSAGE_UI will be updated for messageId=[{}]", uiMessageEntityFound.getMessageId());
            uiMessageEntity.setEntityId(uiMessageEntityFound.getEntityId());
            em.merge(uiMessageEntity);
            LOG.debug("uiMessageEntity having messageId=[{}] have been updated", uiMessageEntity.getMessageId());
            return;
        }
        em.persist(uiMessageEntity);
        LOG.debug("uiMessageEntity having messageId=[{}] have been inserted", uiMessageEntity.getMessageId());
    }

    @Override
    public boolean updateMessageStatus(String messageId, MessageStatus messageStatus, Date deleted, Date nextAttempt,
                                       Date failed, Date lastModified) {
        try {
            int rowsUpdated = this.em.createNamedQuery("UIMessageEntity.updateMessageStatus", UIMessageEntity.class)
                    .setParameter(1, messageStatus.name())
                    .setParameter(2, deleted, TemporalType.TIMESTAMP)
                    .setParameter(3, nextAttempt, TemporalType.TIMESTAMP)
                    .setParameter(4, failed, TemporalType.TIMESTAMP)
                    .setParameter(5, lastModified, TemporalType.TIMESTAMP)
                    .setParameter(6, messageId)
                    .executeUpdate();
            return rowsUpdated == 1;

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean updateNotificationStatus(String messageId, NotificationStatus notificationStatus, Date lastModified2) {
        try {
            int rowsUpdated = this.em.createNamedQuery("UIMessageEntity.updateNotificationStatus", UIMessageEntity.class)
                    .setParameter(1, notificationStatus.name())
                    .setParameter(2, lastModified2, TemporalType.TIMESTAMP)
                    .setParameter(3, messageId)
                    .executeUpdate();
            return rowsUpdated == 1;

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean updateMessage(String messageId, MessageStatus messageStatus, Date deleted, Date failed, Date restored, Date nextAttempt,
                                 Integer sendAttempts, Integer sendAttemptsMax, Date lastModified) {
        try {
            int rowsUpdated = this.em.createNamedQuery("UIMessageEntity.updateMessage", UIMessageEntity.class)
                    .setParameter(1, messageStatus.name())
                    .setParameter(2, deleted, TemporalType.TIMESTAMP)
                    .setParameter(3, failed, TemporalType.TIMESTAMP)
                    .setParameter(4, restored, TemporalType.TIMESTAMP)
                    .setParameter(5, nextAttempt, TemporalType.TIMESTAMP)
                    .setParameter(6, sendAttempts)
                    .setParameter(7, sendAttemptsMax)
                    .setParameter(8, lastModified, TemporalType.TIMESTAMP)
                    .setParameter(9, messageId)
                    .executeUpdate();
            return rowsUpdated == 1;

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * builds the predicates list based on search criteria (filters)
     *
     * @param filters
     * @param cb
     * @param ume
     * @return
     */
    protected List<Predicate> getPredicates(Map<String, Object> filters, CriteriaBuilder cb, Root<?> ume) {
        List<Predicate> predicates = new ArrayList<>();
        for (Map.Entry<String, Object> filter : filters.entrySet()) {
            String filterKey = filter.getKey();
            Object filterValue = filter.getValue();
            if (filterValue != null) {
                if (filterValue instanceof String) {
                    addStringPredicates(cb, ume, predicates, filter, filterKey, filterValue);
                } else if (filterValue instanceof Date) {
                    addDatePredicates(cb, ume, predicates, filterKey, filterValue);
                } else {
                    predicates.add(cb.equal(ume.<String>get(filterKey), filterValue));
                }
            } else {
                if (filterKey.equals("messageSubtype")) {
                    predicates.add(cb.isNull(ume.<MessageSubtype>get("messageSubtype")));
                }
            }
        }
        return predicates;
    }

    private void addStringPredicates(CriteriaBuilder cb, Root<?> ume, List<Predicate> predicates, Map.Entry<String, Object> filter, String filterKey, Object filterValue) {
        if (StringUtils.isNotBlank(filterKey) && !filter.getValue().toString().isEmpty()) {

            String filterColumn = filterKeyToColumnMap.get(filterKey);
            predicates.add(cb.equal(ume.get(filterColumn != null ? filterColumn : filterKey), filterValue));
        }
    }

    private void addDatePredicates(CriteriaBuilder cb, Root<?> ume, List<Predicate> predicates, String filterKey, Object filterValue) {
        if (!filterValue.toString().isEmpty()) {
            switch (filterKey) {
                case "receivedFrom":
                    predicates.add(cb.greaterThanOrEqualTo(ume.<Date>get("received"), (Date) filterValue));
                    break;
                case "receivedTo":
                    predicates.add(cb.lessThanOrEqualTo(ume.<Date>get("received"), (Date) filterValue));
                    break;
                default:
                    break;
            }
        }
    }

}