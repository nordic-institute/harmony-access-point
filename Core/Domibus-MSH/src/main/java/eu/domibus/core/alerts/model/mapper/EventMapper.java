package eu.domibus.core.alerts.model.mapper;

import eu.domibus.core.alerts.model.persist.DateEventProperty;
import eu.domibus.core.alerts.model.persist.Event;
import eu.domibus.core.alerts.model.persist.StringEventProperty;
import eu.domibus.core.alerts.model.service.DatePropertyValue;
import eu.domibus.core.alerts.model.service.StringPropertyValue;
import eu.domibus.core.converter.WithoutAuditAndEntityId;
import eu.domibus.core.converter.WithoutAudit;
import org.mapstruct.DecoratedWith;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author Ioana Dragusanu (idragusa)
 * @since 4.1
 */
@Mapper(componentModel = "spring")
@DecoratedWith(AbstractPropertyValueDecorator.class)
public interface EventMapper {

    @Mapping(target = "properties", ignore = true)
    @Mapping(target = "alerts", ignore = true)
    @WithoutAudit
    Event eventServiceToEventPersist(eu.domibus.core.alerts.model.service.Event event);

    @Mapping(target = "properties", ignore = true)
    eu.domibus.core.alerts.model.service.Event eventPersistToEventService(Event event);

    @WithoutAuditAndEntityId
    @Mapping(target = "stringValue", source = "value")
    @Mapping(target = "event", ignore = true)
    StringEventProperty stringPropertyValueToStringEventProperty(StringPropertyValue propertyValue);

    @InheritInverseConfiguration
    StringPropertyValue stringEventPropertyToStringPropertyValue(StringEventProperty eventProperty);

    @WithoutAuditAndEntityId
    @Mapping(target = "dateValue", source = "value")
    @Mapping(target = "event", ignore = true)
    DateEventProperty datePropertyValueToDateEventProperty(DatePropertyValue propertyValue);

    @InheritInverseConfiguration
    DatePropertyValue dateEventPropertyToDatePropertyValue(DateEventProperty eventProperty);
}
