package eu.domibus.core.crypto.spi.dss.listeners.encryption;

import eu.domibus.core.crypto.spi.dss.DssConfiguration;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.DomainExtService;
import eu.domibus.ext.services.DomibusConfigurationExtService;
import eu.domibus.ext.services.PasswordEncryptionExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.encryption.PluginPropertyEncryptionListener;
import org.springframework.stereotype.Service;

/**
 * @author Soumya Chandran
 * @since 5.0
 */
@Service
public class DssPropertyEncryptionListener implements PluginPropertyEncryptionListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DssPropertyEncryptionListener.class);

    protected PasswordEncryptionExtService pluginPasswordEncryptionService;
    protected DssConfiguration dssConfiguration;

    public DssPropertyEncryptionListener(PasswordEncryptionExtService pluginPasswordEncryptionService, DssConfiguration dssConfiguration,
                                         DomibusConfigurationExtService domibusConfigurationExtService, DomainExtService domainExtService) {
        this.pluginPasswordEncryptionService = pluginPasswordEncryptionService;
        this.dssConfiguration = dssConfiguration;
        this.domibusConfigurationExtService = domibusConfigurationExtService;
        this.domainExtService = domainExtService;
    }

    protected DomibusConfigurationExtService domibusConfigurationExtService;
    protected DomainExtService domainExtService;

    @Override
    public void encryptPasswords() {
        LOG.debug("In DssPropertyEncryptionListener encryptPasswords. ");
        final boolean passwordEncryptionActive = dssConfiguration.isPasswordEncryptionActive();
        LOG.debug("Encrypting passwords is active in the FS Plugin? [{}]", passwordEncryptionActive);

        if (!passwordEncryptionActive) {
            LOG.info("No password encryption will be performed for FSPlugin");
            return;
        }

        LOG.debug("Encrypting passwords");

        //We use the default domain to encrypt all the passwords. This is because there is no clear segregation between FS Plugin properties per domain
        final DomainDTO domainDTO = domainExtService.getDomain(dssConfiguration.DEFAULT_DOMAIN);
        final DssPropertyPasswordEncryptionContext passwordEncryptionContext =
                new DssPropertyPasswordEncryptionContext(
                        dssConfiguration,
                        domibusConfigurationExtService,
                        pluginPasswordEncryptionService,
                        domainDTO);
        pluginPasswordEncryptionService.encryptPasswordsInFile(passwordEncryptionContext);

        LOG.debug("Finished encrypting passwords");
    }
}
