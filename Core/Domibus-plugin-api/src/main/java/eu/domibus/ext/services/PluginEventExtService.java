package eu.domibus.ext.services;

import eu.domibus.ext.domain.AlertEventDTO;

/**
 * Service used for creating event and alert from plugins.
 *
 * @author François Gautier
 * @since 5.0
 */
public interface PluginEventExtService {

    void enqueueMessageEvent(AlertEventDTO alertEvent);

}
