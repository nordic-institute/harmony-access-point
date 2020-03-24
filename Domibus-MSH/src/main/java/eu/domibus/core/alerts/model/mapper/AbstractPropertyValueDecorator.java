package eu.domibus.core.alerts.model.mapper;

import eu.domibus.core.alerts.model.persist.AbstractEventProperty;
import eu.domibus.core.alerts.model.persist.DateEventProperty;
import eu.domibus.core.alerts.model.persist.Event;
import eu.domibus.core.alerts.model.persist.StringEventProperty;
import eu.domibus.core.alerts.model.service.AbstractPropertyValue;
import eu.domibus.core.alerts.model.service.DatePropertyValue;
import eu.domibus.core.alerts.model.service.StringPropertyValue;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;



/**
 *  This decorator class is used to enhance the EventMapper on mapping the event properties depending on their
 *  type, StringPropertyValue and DatePropertyValue. Initially ignored in the EventMapper, the properties are converted here.
 *
 * @author Ioana Dragusanu (idragusa)
 * @since 4.1
 */
 public abstract class AbstractPropertyValueDecorator implements EventMapper {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AbstractPropertyValueDecorator.class);


    @Autowired
    @Qualifier("delegate")
    protected EventMapper delegate;

    public AbstractPropertyValueDecorator() {
    }


    public void setDelegate(EventMapper delegate) {
        this.delegate = delegate;
    }

    @Override
    public Event eventServiceToEventPersist(eu.domibus.core.alerts.model.service.Event event) {
        Event event1 = delegate.eventServiceToEventPersist(event);
        event.getProperties().forEach((ke, propertyValue) -> event1.addProperty(ke, convert(propertyValue)));
        return event1;
    }

    @Override
    public eu.domibus.core.alerts.model.service.Event eventPersistToEventService(Event event) {
        eu.domibus.core.alerts.model.service.Event event1 = delegate.eventPersistToEventService(event);
        event.getProperties().forEach((ke, eventProperty) -> event1.addProperty(ke, convert(eventProperty)));
        return event1;
    }

    protected AbstractEventProperty convert(AbstractPropertyValue propertyValue) {
        LOG.debug("AbstractPropertyValue convert: [{}]", propertyValue.getClass());
        if (propertyValue instanceof StringPropertyValue) {
            return delegate.stringPropertyValueToStringEventProperty((StringPropertyValue) propertyValue);
        }
        if (propertyValue instanceof DatePropertyValue) {
            return delegate.datePropertyValueToDateEventProperty((DatePropertyValue) propertyValue);
        }
        LOG.warn("Invalid type for AbstractPropertyValue: [{}]", propertyValue.getClass());
        return null;
    }

    protected AbstractPropertyValue convert(AbstractEventProperty eventProperty) {
        LOG.debug("AbstractEventProperty convert: [{}]", eventProperty.getClass());
        if (eventProperty instanceof StringEventProperty) {
            return delegate.stringEventPropertyToStringPropertyValue((StringEventProperty) eventProperty);
        }
        if (eventProperty instanceof DateEventProperty) {
            return delegate.dateEventPropertyToDatePropertyValue((DateEventProperty) eventProperty);
        }
        LOG.warn("Invalid type for AbstractPropertyValue: [{}]", eventProperty.getClass());
        return null;
    }
}