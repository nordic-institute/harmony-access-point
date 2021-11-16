package eu.domibus.plugin.fs.property.encryption;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static eu.domibus.ext.services.DomibusPropertyManagerExt.PLUGINS_CONFIG_HOME;
import static eu.domibus.plugin.fs.property.FSPluginProperties.PLUGIN_PROPERTIES_FILE_NAME;
import static eu.domibus.plugin.fs.property.FSPluginPropertiesMetadataManagerImpl.FSPLUGIN_PASSWORD_ENCRYPTION_PROPERTIES;
import static eu.domibus.plugin.fs.property.FSPluginPropertiesMetadataManagerImpl.PASSWORD_ENCRYPTION_ACTIVE;
import static eu.domibus.plugin.fs.worker.FSSendMessagesService.DEFAULT_DOMAIN;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
public class FSPluginDomainPasswordEncryptionContext extends PluginPasswordEncryptionContextAbstract implements PluginPasswordEncryptionContext {

    protected DomainDTO domain;

    public FSPluginDomainPasswordEncryptionContext(DomibusPropertyManagerExt propertyProvider,
                                                   DomibusConfigurationExtService domibusConfigurationExtService,
                                                   PasswordEncryptionExtService pluginPasswordEncryptionService,
                                                   DomainDTO domain) {
        super(propertyProvider, domibusConfigurationExtService, pluginPasswordEncryptionService);
        this.domain = domain;
    }

    @Override
    public DomainDTO getDomain() {
        return domain;
    }

    @Override
    public String getProperty(String propertyName) {
        return propertyProvider.getKnownPropertyValue(domain.getCode(), propertyName);
    }

    @Override
    public boolean isEncryptionActive() {
        final String passwordEncryptionActive = propertyProvider.getKnownPropertyValue(domain.getCode(), PASSWORD_ENCRYPTION_ACTIVE);
        return BooleanUtils.toBoolean(passwordEncryptionActive);
    }

    @Override
    protected String getEncryptedPropertyNames() {
        return FSPLUGIN_PASSWORD_ENCRYPTION_PROPERTIES;
    }

    @Override
    protected Optional<String> getConfigurationFileName() {
        return propertyProvider.getConfigurationFileName(domain);
    }
}
