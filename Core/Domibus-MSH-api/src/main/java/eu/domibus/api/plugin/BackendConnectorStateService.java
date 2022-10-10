package eu.domibus.api.plugin;

/**
 * Service used by plugins to notify domibus that the enabled state has changed
 *
 * @author Ion Perpegel
 * @since 5.0
 */
public interface BackendConnectorStateService {

    void backendConnectorEnabled(String backendName, String domainCode);

    void backendConnectorDisabled(String backendName, String domainCode);

}
