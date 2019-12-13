package eu.domibus.ext.services;

import eu.domibus.ext.domain.DomainDTO;

import java.io.File;
import java.util.List;

/**
 * Password encryption context used when encrypting passwords configured in property file
 *
 * @author Cosmin Baciu
 * @since 4.1.2
 */
public interface PluginPasswordEncryptionContext {

    /**
     * The domain in which the property has been configured
     * @return The domain
     */
    DomainDTO getDomain();

    /**
     *  Gets the property value based on the property name
     *
     * @param propertyName The property name
     * @return The property value
     */
    String getProperty(String propertyName);

    /**
     * Gets the configuration file containing the properties to be encrypted
     *
     * @return The property file
     */
    File getConfigurationFile();

    /**
     * Gets the list of property names to be encrypted
     *
     * @return The property list
     */
    List<String> getPropertiesToEncrypt();
}
