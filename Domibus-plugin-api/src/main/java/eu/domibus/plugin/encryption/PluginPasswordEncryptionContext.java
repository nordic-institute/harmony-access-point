package eu.domibus.plugin.encryption;

import eu.domibus.ext.domain.DomainDTO;

import java.io.File;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
public interface PluginPasswordEncryptionContext {

    DomainDTO getDomain();

    String getProperty(String propertyName);

    File getConfigurationFile();

    List<String> getPropertiesToEncrypt();
}
