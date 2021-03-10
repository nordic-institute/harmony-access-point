package eu.domibus.ext.delegate.services.alerts;

import eu.domibus.api.alerts.AlertEvent;
import eu.domibus.api.alerts.PluginEventService;
import eu.domibus.ext.delegate.mapper.AlertExtMapper;
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
    private final AlertExtMapper alertMapper;

    public PluginEventServiceDelegate(PluginEventService pluginEventService, AlertExtMapper alertMapper) {
        this.alertMapper = alertMapper;
        this.pluginEventService = pluginEventService;
    }

    @Override
    public void enqueueMessageEvent(AlertEventDTO alertEventDTO) {
        final AlertEvent alertEvent = alertMapper.alertEventDTOToAlertEvent(alertEventDTO);
        pluginEventService.enqueueMessageEvent(alertEvent);
    }
}
