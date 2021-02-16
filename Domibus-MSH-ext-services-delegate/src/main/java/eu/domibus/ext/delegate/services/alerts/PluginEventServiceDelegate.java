package eu.domibus.ext.delegate.services.alerts;

import eu.domibus.api.alerts.PluginEventService;
import eu.domibus.ext.services.PluginEventExtService;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * {@inheritDoc}
 *
 * @author François Gautier
 * @since 5.0
 */
@Service
public class PluginEventServiceDelegate implements PluginEventExtService {

    private final PluginEventService pluginEventService;

    public PluginEventServiceDelegate(PluginEventService pluginEventService) {
        this.pluginEventService = pluginEventService;
    }

    public void enqueueMessageEvent(Map<String, String> properties) {
        pluginEventService.enqueueMessageEvent(properties);
    }
}
