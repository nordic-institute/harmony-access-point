package eu.domibus.plugin.encryption;

/**
 * This interface should be implemented by plugins that want to support password encryption. <p/>
 * If password encryption is active Domibus will generate the secret keys that are used to encrypt the password and notify the subscribed listeners to encrypt their plugin passwords.
 * Plugins must use the password encryption exposed services to handle the password encryption.
 *
 * @author Cosmin Baciu
 * @since 4.1.1
 */
public interface PluginPropertyEncryptionListener {

    void encryptPasswords();
}
