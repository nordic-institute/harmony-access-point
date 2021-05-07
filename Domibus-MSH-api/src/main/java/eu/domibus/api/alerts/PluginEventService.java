package eu.domibus.api.alerts;

/**
 * Interface for the creation of plugin events
 *
 * @author François Gautier
 * @since 5.0
 */
public interface PluginEventService {

    void enqueueMessageEvent(AlertEvent alertEvent);

}
