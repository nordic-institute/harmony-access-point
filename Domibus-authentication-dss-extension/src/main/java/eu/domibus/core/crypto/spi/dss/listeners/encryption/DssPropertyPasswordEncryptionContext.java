package eu.domibus.core.crypto.spi.dss.listeners.encryption;

import eu.domibus.core.crypto.spi.dss.DssConfiguration;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.DomibusConfigurationExtService;
import eu.domibus.ext.services.PasswordEncryptionExtService;
import eu.domibus.ext.services.PluginPasswordEncryptionContext;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DssPropertyPasswordEncryptionContext implements PluginPasswordEncryptionContext {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DssPropertyPasswordEncryptionContext.class);
    private static final String DSS_PASSWORD_ENCRYPTION_PROPERTIES = "domibus.authentication.dss.password.encryption.properties";

    protected DssConfiguration dssConfiguration;

    protected DomibusConfigurationExtService domibusConfigurationExtService;

    protected PasswordEncryptionExtService pluginPasswordEncryptionService;

    protected DomainDTO domain;

    public DssPropertyPasswordEncryptionContext(DssConfiguration dssConfiguration,
            DomibusConfigurationExtService domibusConfigurationExtService,
            PasswordEncryptionExtService pluginPasswordEncryptionService,
            DomainDTO domain) {
        this.dssConfiguration = dssConfiguration;
        this.domibusConfigurationExtService = domibusConfigurationExtService;
        this.pluginPasswordEncryptionService = pluginPasswordEncryptionService;
        this.domain = domain;
    }

    @Override
    public DomainDTO getDomain() {
        return domain;
    }

    @Override
    public String getProperty(String propertyName) {
        return dssConfiguration.getDomainProperty(domain.getCode(), propertyName);
    }

    @Override
    public File getConfigurationFile() {
        final File configurationFile = new File(domibusConfigurationExtService.getConfigLocation() + File.separator + "plugins/config/fs-plugin.properties");
        LOG.debug("Using FS Plugin configuration file [{}]", configurationFile);
        return configurationFile;
    }

    @Override
    public List<String> getPropertiesToEncrypt() {
        final String propertiesToEncryptString = dssConfiguration.getDomainProperty(domain.getCode(), DSS_PASSWORD_ENCRYPTION_PROPERTIES);

        if (StringUtils.isEmpty(propertiesToEncryptString)) {
            LOG.debug("No properties to encrypt");
            return new ArrayList<>();
        }
        final String[] propertiesToEncrypt = StringUtils.split(propertiesToEncryptString, ",");
        LOG.debug("The following properties are configured for encryption [{}]", Arrays.asList(propertiesToEncrypt));

        List<String> result = Arrays.stream(propertiesToEncrypt).filter(propertyName -> {
            propertyName = StringUtils.trim(propertyName);
            final String propertyValue = getProperty(propertyName);
            if (StringUtils.isBlank(propertyValue)) {
                return false;
            }

            if (!pluginPasswordEncryptionService.isValueEncrypted(propertyValue)) {
                LOG.debug("Property [{}] is not encrypted", propertyName);
                return true;
            }
            LOG.debug("Property [{}] is already encrypted", propertyName);
            return false;
        }).collect(Collectors.toList());

        LOG.debug("The following properties are not encrypted [{}]", result);

        return result;
    }
}
