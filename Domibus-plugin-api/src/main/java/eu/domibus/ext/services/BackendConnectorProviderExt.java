package eu.domibus.ext.services;

/**
 * @author Ion Perpegel
 * @since 5.0
 *
 * Interface needed by plugins
 */
public interface BackendConnectorProviderExt {

    /**
     * Checks that at least one plugin is enabled on each domain; if not, takes one and enables it
     * At this point, only default plugins are enable-aware and only the FSPlugin can actually be disabled
     */
    void ensureValidConfiguration();

    /**
     * Checks that at least one plugin is enabled on the specified domain; if not, throws a ConfigurationException
     * @param domainCode the domain on which the validation is done
     */
    void validateConfiguration(final String domainCode);
}
