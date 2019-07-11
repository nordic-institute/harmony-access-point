package eu.domibus.api.property;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
public interface PasswordEncryptionContext {

    boolean isPasswordEncryptionActive();

    String getProperty(String propertyName);

    String getConfigurationFileName();
}
