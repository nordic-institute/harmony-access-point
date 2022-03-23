package eu.domibus.ext.services;

/**
 * Service used for operations related with plugins
 *
 * @author Ion Perpegel
 * @since 5.0
 */
public interface BackendConnectorExtService {

    void ensureValidConfiguration();

    void validateConfiguration(String backendName, final String domainCode);
}
