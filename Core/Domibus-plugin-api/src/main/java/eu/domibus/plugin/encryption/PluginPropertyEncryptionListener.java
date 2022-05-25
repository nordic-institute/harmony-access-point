package eu.domibus.plugin.encryption;

/**
 * This interface should be implemented by plugins that want to support password encryption. <p/>
 * If password encryption is active, Domibus will generate the secret keys that are used to encrypt the passwords and notify afterwards the subscribed plugin listeners to encrypt their own passwords.
 * Plugins must use the password encryption services {@link eu.domibus.ext.services.PasswordEncryptionExtService} to handle the password encryption.
 *
 * @author Cosmin Baciu
 * @since 4.1.2
 */
public interface PluginPropertyEncryptionListener {

    void encryptPasswords();
}
