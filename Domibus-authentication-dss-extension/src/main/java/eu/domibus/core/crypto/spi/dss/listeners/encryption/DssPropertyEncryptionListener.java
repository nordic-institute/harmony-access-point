package eu.domibus.core.crypto.spi.dss.listeners.encryption;

import eu.domibus.core.crypto.spi.dss.DssConfiguration;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.DomainExtService;
import eu.domibus.ext.services.DomibusConfigurationExtService;
import eu.domibus.ext.services.DomibusSchedulerExtService;
import eu.domibus.ext.services.PasswordEncryptionExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.encryption.PluginPropertyEncryptionListener;
import org.springframework.stereotype.Service;

/**
 * @author Soumya Chandran
 * @since 5.0
 *
 * Listener for DSS Password encryption property change.
 */
public class DssPropertyEncryptionListener implements PluginPropertyEncryptionListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DssPropertyEncryptionListener.class);

    protected PasswordEncryptionExtService passwordEncryptionService;
    protected DssConfiguration dssConfiguration;
    protected DomibusConfigurationExtService domibusConfigurationExtService;
    protected DomainExtService domainExtService;

    public DssPropertyEncryptionListener(PasswordEncryptionExtService passwordEncryptionService, DssConfiguration dssConfiguration,
                                         DomibusConfigurationExtService domibusConfigurationExtService, DomainExtService domainExtService) {
        this.passwordEncryptionService = passwordEncryptionService;
        this.dssConfiguration = dssConfiguration;
        this.domibusConfigurationExtService = domibusConfigurationExtService;
        this.domainExtService = domainExtService;
        LOG.debug("In DssPropertyEncryptionListener constructor. ");
    }


    @Override
    public void encryptPasswords() {
        final boolean passwordEncryptionActive = dssConfiguration.isPasswordEncryptionActive();
        LOG.debug("Encrypting passwords is active in DSS configuration? [{}]", passwordEncryptionActive);

        if (!passwordEncryptionActive) {
            LOG.debug("No password encryption will be performed for DSS");
            return;
        }

        LOG.debug("Encrypting passwords");

        //We use the default domain to encrypt all the passwords. This is because there is no clear segregation between DSS properties per domain
        final DomainDTO domainDTO = domainExtService.getDomain(dssConfiguration.DEFAULT_DOMAIN);
        final DssPropertyPasswordEncryptionContext passwordEncryptionContext =
                new DssPropertyPasswordEncryptionContext(
                        dssConfiguration,
                        domibusConfigurationExtService,
                        passwordEncryptionService,
                        domainDTO);
        passwordEncryptionService.encryptPasswordsInFile(passwordEncryptionContext);

        LOG.debug("Finished encrypting passwords");
    }
}
