package eu.domibus.api.plugin;

/**
 * Service used for operations related to plugins
 *
 * @author Ion Perpegel
 * @since 5.0
 */
public interface BackendConnectorNotificationService {

    void backendConnectorEnabled(String backendName, String domainCode);

    void backendConnectorDisabled(String backendName, String domainCode);

}
