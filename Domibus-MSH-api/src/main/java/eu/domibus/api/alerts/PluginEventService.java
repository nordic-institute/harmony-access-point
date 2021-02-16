package eu.domibus.api.alerts;

import java.util.Map;

/**
 * Interface for the creation of plugin events
 *
 * @author François Gautier
 * @since 5.0
 */
public interface PluginEventService {

    void enqueueMessageEvent(Map<String, String> properties);

}
