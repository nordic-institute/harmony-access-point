package eu.domibus.ext.delegate.services.alerts;

import eu.domibus.api.alerts.AlertEvent;
import eu.domibus.api.alerts.PluginEventService;
import eu.domibus.ext.delegate.converter.DomainExtConverter;
import eu.domibus.ext.domain.AlertEventDTO;
import eu.domibus.ext.services.PluginEventExtService;
import org.springframework.stereotype.Service;

/**
 * {@inheritDoc}
 *
 * @author François Gautier
 * @since 5.0
 */
@Service
public class PluginEventServiceDelegate implements PluginEventExtService {

    private final PluginEventService pluginEventService;
    private final DomainExtConverter domainExtConverter;

    public PluginEventServiceDelegate(PluginEventService pluginEventService, DomainExtConverter domainExtConverter) {
        this.domainExtConverter = domainExtConverter;
        this.pluginEventService = pluginEventService;
    }

    @Override
    public void enqueueMessageEvent(AlertEventDTO alertEventDTO) {
        final AlertEvent alertEvent = domainExtConverter.convert(alertEventDTO, AlertEvent.class);
        pluginEventService.enqueueMessageEvent(alertEvent);
    }
}
