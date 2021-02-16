package eu.domibus.ext.services;

import java.util.Map;

/**
 * Service used for creating event and alert from plugins.
 *
 * @author François Gautier
 * @since 5.0
 */
public interface PluginEventExtService {

    void enqueueMessageEvent(Map<String, String> properties);

}
