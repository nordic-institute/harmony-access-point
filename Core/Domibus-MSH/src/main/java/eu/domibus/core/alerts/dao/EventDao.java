package eu.domibus.core.alerts.dao;

import eu.domibus.core.dao.BasicDao;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.core.alerts.model.persist.Event;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Repository
public class EventDao extends BasicDao<Event> {

    public EventDao() {
        super(Event.class);
    }

    @Transactional(readOnly = true)
    public Event findWithTypeAndPropertyValue(EventType type, String property, String value) {
        TypedQuery<Event> namedQuery = em.createNamedQuery("AbstractEventProperty.findWithTypeAndPropertyValue", Event.class);
        namedQuery.setParameter("TYPE", type);
        namedQuery.setParameter("PROPERTY", property);
        namedQuery.setParameter("VALUE", value);
        return namedQuery.getResultList().stream().findFirst().map(EventDao::init).orElse(null);
    }

    private static Event init(Event event) {
        Hibernate.initialize(event.getProperties());
        Hibernate.initialize(event.getAlerts());
        return event;
    }

}
