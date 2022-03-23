package eu.domibus.ext.services;

/**
 * @author Ion Perpegel
 * @since 5.0
 *
 * Interface needed by plugins
 */
public interface BackendConnectorExtService {

    void ensureValidConfiguration();

    void validateConfiguration(String backendName, final String domainCode);
}
