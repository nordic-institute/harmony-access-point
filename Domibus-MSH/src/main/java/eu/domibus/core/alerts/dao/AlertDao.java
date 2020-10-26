package eu.domibus.core.alerts.dao;

import com.google.common.collect.Lists;
import eu.domibus.core.alerts.model.common.AlertCriteria;
import eu.domibus.core.alerts.model.persist.*;
import eu.domibus.core.dao.BasicDao;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Repository
public class AlertDao extends BasicDao<Alert> {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(AlertDao.class);

    public AlertDao() {
        super(Alert.class);
    }

    public List<Alert> findRetryAlerts() {
        final TypedQuery<Alert> namedQuery = em.createNamedQuery("Alert.findRetry", Alert.class);
        return namedQuery.getResultList();
    }

    public List<Alert> filterAlerts(AlertCriteria alertCriteria) {

        QueryInfo queryInfo = initQuery(alertCriteria, Alert.class);

        final Boolean ascending = alertCriteria.getAsc();
        final String orderBy = alertCriteria.getOrderBy();
        if (orderBy != null && ascending != null) {
            if (ascending) {
                queryInfo.getQuery().orderBy(queryInfo.getBuilder().asc(queryInfo.getRoot().get(orderBy)));
            } else {
                queryInfo.getQuery().orderBy(queryInfo.getBuilder().desc(queryInfo.getRoot().get(orderBy)));
            }
        }
        final TypedQuery<Alert> query = em.createQuery(queryInfo.getQuery());
        query.setFirstResult(alertCriteria.getPage() * alertCriteria.getPageSize());
        query.setMaxResults(alertCriteria.getPageSize());
        return query.getResultList();
    }

    private void addDynamicPredicates(AlertCriteria alertCriteria, CriteriaBuilder builder, Root<Alert> subRoot, List<Predicate> predicates) {
        final Map<String, String> parameters = alertCriteria.getParameters();

        if (!parameters.isEmpty()) {
            final SetJoin<Alert, Event> eventJoin = subRoot.join(Alert_.events);
            MapJoin<Event, String, AbstractEventProperty> propJoin = eventJoin.join(Event_.properties);
            parameters.forEach((key, value) -> {
                final MapJoin<Event, String, StringEventProperty> treat = builder.treat(propJoin, StringEventProperty.class);
                //because event properties are key value, we need to create a join on each parameters.
                final Predicate parameterPredicate = builder.and(
                        builder.equal(treat.get(StringEventProperty_.key), key),
                        builder.equal(treat.get(StringEventProperty_.stringValue), value));
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Add dynamic non date criteria key:[{}] equals:[{}] for alert type:[{}]", key, value, alertCriteria.getAlertType().name());
                }
                predicates.add(parameterPredicate);
            });
        }

        final Date dynamicaPropertyFrom = alertCriteria.getDynamicaPropertyFrom();
        final Date dynamicaPropertyTo = alertCriteria.getDynamicaPropertyTo();
        final String uniqueDynamicDateParameter = alertCriteria.getUniqueDynamicDateParameter();
        if (uniqueDynamicDateParameter == null) {
            return;
        }

        final SetJoin<Alert, Event> eventJoin = subRoot.join(Alert_.events);
        MapJoin<Event, String, AbstractEventProperty> propJoin = eventJoin.join(Event_.properties);
        if (dynamicaPropertyFrom != null && dynamicaPropertyTo != null) {
            final MapJoin<Event, String, DateEventProperty> treat = builder.treat(propJoin, DateEventProperty.class);
            final Predicate dynamicDateBetween = builder.and(
                    builder.equal(treat.get(DateEventProperty_.key), uniqueDynamicDateParameter),
                    builder.between(treat.get(DateEventProperty_.dateValue), dynamicaPropertyFrom, dynamicaPropertyTo));
            if (LOG.isDebugEnabled()) {
                LOG.debug("Add between date criteria key:[{}] between:[{}] and:[{}] for alert type:[{}]", uniqueDynamicDateParameter, dynamicaPropertyFrom, dynamicaPropertyTo, alertCriteria.getAlertType().name());
            }
            predicates.add(dynamicDateBetween);
        } else if (dynamicaPropertyFrom != null) {
            final MapJoin<Event, String, DateEventProperty> treat = builder.treat(propJoin, DateEventProperty.class);
            final Predicate dynamicDateBetween = builder.and(
                    builder.equal(treat.get(DateEventProperty_.key), uniqueDynamicDateParameter),
                    builder.greaterThanOrEqualTo(treat.get(DateEventProperty_.dateValue), dynamicaPropertyFrom));
            if (LOG.isDebugEnabled()) {
                LOG.debug("Add greater then date criteria key:[{}]>[{}] for alert type:[{}]", uniqueDynamicDateParameter, dynamicaPropertyFrom, alertCriteria.getAlertType().name());
            }
            predicates.add(dynamicDateBetween);
        } else if (dynamicaPropertyTo != null) {
            final MapJoin<Event, String, DateEventProperty> treat = builder.treat(propJoin, DateEventProperty.class);
            final Predicate dynamicDateBetween = builder.and(
                    builder.equal(treat.get(DateEventProperty_.key), uniqueDynamicDateParameter),
                    builder.lessThanOrEqualTo(treat.get(DateEventProperty_.dateValue), dynamicaPropertyTo));
            if (LOG.isDebugEnabled()) {
                LOG.debug("Add lesser then date criteria key:[{}]<[{}] for alert type:[{}]", uniqueDynamicDateParameter, dynamicaPropertyFrom, alertCriteria.getAlertType().name());
            }
            predicates.add(dynamicDateBetween);
        }
    }

    public Long countAlerts(AlertCriteria alertCriteria) {

        QueryInfo queryInfo = initQuery(alertCriteria, Long.class);

        queryInfo.getQuery().select(queryInfo.getBuilder().count(queryInfo.getRoot().get(Alert_.entityId))).distinct(true);

        final TypedQuery<Long> query = em.createQuery(queryInfo.getQuery());

        return query.getSingleResult();
    }

    private List<Predicate> getAlertPredicates(AlertCriteria alertCriteria, CriteriaBuilder cb, Root<Alert> alertRoot) {
        List<Predicate> predicates = Lists.newArrayList();
        if (alertCriteria.isProcessed() != null) {
            predicates.add(cb.equal(alertRoot.get(Alert_.processed), alertCriteria.isProcessed()));
        }

        if (alertCriteria.getAlertType() != null) {
            predicates.add(cb.equal(alertRoot.get(Alert_.alertType), alertCriteria.getAlertType()));
        }

        if (alertCriteria.getAlertStatus() != null) {
            predicates.add(cb.equal(alertRoot.get(Alert_.alertStatus), alertCriteria.getAlertStatus()));
        }

        if (alertCriteria.getAlertLevel() != null) {
            predicates.add(cb.equal(alertRoot.get(Alert_.alertLevel), alertCriteria.getAlertLevel()));
        }

        if (alertCriteria.getAlertID() != null) {
            predicates.add(cb.equal(alertRoot.get(Alert_.entityId), alertCriteria.getAlertID()));
        }

        if (alertCriteria.getCreationFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(alertRoot.get(Alert_.creationTime), alertCriteria.getCreationFrom()));
        }
        if (alertCriteria.getCreationTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(alertRoot.get(Alert_.creationTime), alertCriteria.getCreationTo()));
        }

        if (alertCriteria.getReportingFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(alertRoot.get(Alert_.reportingTime), alertCriteria.getReportingFrom()));
        }
        if (alertCriteria.getReportingTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(alertRoot.get(Alert_.reportingTime), alertCriteria.getReportingTo()));
        }
        return predicates;

    }

    public List<Alert> retrieveAlertsWithCreationDateSmallerThen(final Date alertLimitDate) {
        final TypedQuery<Alert> namedQuery = em.createNamedQuery("Alert.findAlertToClean", Alert.class);
        namedQuery.setParameter("ALERT_LIMIT_DATE", alertLimitDate);
        return namedQuery.getResultList();
    }

    public void updateAlertProcessed(final Long id, Boolean processed) {
        final Query namedQuery = em.createNamedQuery("Alert.updateProcess");
        namedQuery.setParameter("ALERT_ID", id);
        namedQuery.setParameter("PROCESSED", processed);
        namedQuery.executeUpdate();
    }

    protected <T> QueryInfo<T> initQuery(AlertCriteria alertCriteria, Class<T> clazz) {

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<T> criteria = builder.createQuery(clazz);

        //create root entity specifying that we want to eager fetch. (Avoid the N+1 hibernate problem)
        final Root<Alert> root = criteria.from(Alert.class);

        final Subquery<Long> subQuery = criteria.subquery(Long.class);
        Root<Alert> subRoot = subQuery.from(Alert.class);

        //Do first a subQuery to retrieve the filtered alerts id based on criteria.
        subQuery.select(subRoot.get(Alert_.entityId));

        List<Predicate> predicates = new ArrayList<>(getAlertPredicates(alertCriteria, builder, subRoot));
        addDynamicPredicates(alertCriteria, builder, subRoot, predicates);
        //add predicates to the sub query.
        subQuery.where(predicates.toArray(new Predicate[predicates.size()])).distinct(true);

        //create main query by retrieving alerts where ids are in the sub query selection.
        criteria.where(root.get(Alert_.entityId).in(subQuery)).distinct(true);

        return new QueryInfo(builder, criteria, root);
    }

    class QueryInfo<T> {
        private CriteriaBuilder builder;
        private CriteriaQuery<T> query;
        private Root<Alert> root;

        QueryInfo(CriteriaBuilder builder, CriteriaQuery<T> query, Root<Alert> root) {
            this.builder = builder;
            this.query = query;
            this.root = root;
        }

        public CriteriaBuilder getBuilder() {
            return builder;
        }

        public CriteriaQuery<T> getQuery() {
            return query;
        }

        public Root<Alert> getRoot() {
            return root;
        }
    }
}
