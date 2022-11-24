package eu.domibus.ext.services;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.domain.PasswordEncryptionResultDTO;

import java.util.List;
import java.util.function.Function;

/**
 * An interface containing utility operations for encrypting passwords.
 *
 * @author Cosmin Baciu
 * @since 4.1.2
 */
public interface PasswordEncryptionExtService {

    /**
     * Encrypts passwords configured in a properties file
     *
     * @param pluginPasswordEncryptionContext password context used for encrypting passwords. For more info see {@link PluginPasswordEncryptionContext}
     */
    void encryptPasswordsInFile(PluginPasswordEncryptionContext pluginPasswordEncryptionContext);

    /**
     * Checks if a specific property value is encrypted
     *
     * @param propertyValue The property value to check
     * @return true if the value is encrypted
     */
    boolean isValueEncrypted(final String propertyValue);

    /**
     * Encrypts a property value configured for a specific domain
     *
     * @param domain The domain in which the property has been configured
     * @param propertyName The property name
     * @param propertyValue The property value to be encrypted
     * @return The encrypted property result
     */
    PasswordEncryptionResultDTO encryptProperty(DomainDTO domain, String propertyName, String propertyValue);

    /**
     *
     * @param encryptedProperties names of the properties to encrypt
     * @param getPropertyFn function receiving a property name and returning its value
     * @return the list of property values that have been successfully encrypted
     * @deprecated use {@link #getPropertiesToEncrypt(String, Function, Function)} instead.
     */
    @Deprecated
    List<String> getPropertiesToEncrypt(String encryptedProperties, Function<String, String> getPropertyFn);

    /**
     * Returns a sublist of encryted property names passed in as a comma-separated value that can be actually encrypted.
     *
     * @param encryptedProperties names of the properties to encrypt
     * @param handlesPropertyFn function receiving a property name and returning whether the property can be encrypted or not
     * @param getPropertyFn function receiving a property name and returning its value
     * @return the list of property names that have can be encrypted
     */
    List<String> getPropertiesToEncrypt(String encryptedProperties, Function<String, Boolean> handlesPropertyFn, Function<String, String> getPropertyFn);
}
