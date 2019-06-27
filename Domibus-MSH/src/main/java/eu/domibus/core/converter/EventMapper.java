package eu.domibus.core.converter;

import eu.domibus.api.audit.AuditLog;
import eu.domibus.api.cluster.Command;
import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.party.Party;
import eu.domibus.api.pmode.PModeArchiveInfo;
import eu.domibus.api.routing.BackendFilter;
import eu.domibus.api.routing.RoutingCriteria;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.api.user.User;
import eu.domibus.clustering.CommandEntity;
import eu.domibus.common.model.audit.Audit;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.common.model.logging.ErrorLogEntry;
import eu.domibus.common.model.logging.MessageLogInfo;
import eu.domibus.common.model.logging.SignalMessageLog;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.core.alerts.model.persist.Alert;
import eu.domibus.core.alerts.model.persist.DateEventProperty;
import eu.domibus.core.alerts.model.persist.Event;
import eu.domibus.core.alerts.model.persist.StringEventProperty;
import eu.domibus.core.alerts.model.service.DatePropertyValue;
import eu.domibus.core.alerts.model.service.StringPropertyValue;
import eu.domibus.core.crypto.api.CertificateEntry;
import eu.domibus.core.crypto.spi.CertificateEntrySpi;
import eu.domibus.core.crypto.spi.DomainSpi;
import eu.domibus.core.logging.LoggingEntry;
import eu.domibus.core.message.attempt.MessageAttemptEntity;
import eu.domibus.core.party.PartyResponseRo;
import eu.domibus.core.party.ProcessRo;
import eu.domibus.core.replication.UIMessageDiffEntity;
import eu.domibus.core.replication.UIMessageEntity;
import eu.domibus.core.security.AuthenticationEntity;
import eu.domibus.ebms3.common.model.PullRequest;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.ext.domain.PullRequestDTO;
import eu.domibus.ext.domain.UserMessageDTO;
import eu.domibus.plugin.routing.BackendFilterEntity;
import eu.domibus.plugin.routing.RoutingCriteriaEntity;
import eu.domibus.web.rest.ro.*;
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
    Event eventServiceToEventPersist(eu.domibus.core.alerts.model.service.Event event);

    @Mapping(target = "properties", ignore = true)
    eu.domibus.core.alerts.model.service.Event eventPersistToEventService(Event event);

    @Mapping(source = "value", target = "stringValue")
    StringEventProperty stringPropertyValueToStringEventProperty(StringPropertyValue propertyValue);

    @InheritInverseConfiguration
    StringPropertyValue stringEventPropertyToStringPropertyValue(StringEventProperty eventProperty);

    @Mapping(source = "value", target = "dateValue")
    DateEventProperty datePropertyValueToDateEventProperty(DatePropertyValue propertyValue);

    @InheritInverseConfiguration
    DatePropertyValue dateEventPropertyToDatePropertyValue(DateEventProperty eventProperty);
}
