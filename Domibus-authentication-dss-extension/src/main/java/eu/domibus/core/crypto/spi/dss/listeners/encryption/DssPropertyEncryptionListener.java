package eu.domibus.core.crypto.spi.dss.listeners.encryption;

import eu.domibus.core.crypto.spi.dss.DssExtensionPropertyManager;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.DomainExtService;
import eu.domibus.ext.services.DomibusConfigurationExtService;
import eu.domibus.ext.services.PasswordEncryptionExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.encryption.PluginPropertyEncryptionListener;

/**
 * @author Soumya Chandran
 * @since 5.0
 *
 * Listener for DSS Password encryption property change.
 */
public class DssPropertyEncryptionListener implements PluginPropertyEncryptionListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DssPropertyEncryptionListener.class);

    protected PasswordEncryptionExtService passwordEncryptionService;
    protected DssExtensionPropertyManager propertyProvider;
    protected DomibusConfigurationExtService domibusConfigurationExtService;
    protected DomainExtService domainExtService;

    public DssPropertyEncryptionListener(PasswordEncryptionExtService passwordEncryptionService,
                                         DomibusConfigurationExtService domibusConfigurationExtService,
                                         DomainExtService domainExtService,
                                         DssExtensionPropertyManager propertyProvider) {
        this.passwordEncryptionService = passwordEncryptionService;
        this.domibusConfigurationExtService = domibusConfigurationExtService;
        this.domainExtService = domainExtService;
        this.propertyProvider = propertyProvider;
        LOG.debug("In DssPropertyEncryptionListener constructor. ");
    }


    @Override
    public void encryptPasswords() {
//        final boolean passwordEncryptionActive = propertyProvider.isPasswordEncryptionActive();
//        LOG.debug("Encrypting passwords is active in DSS configuration? [{}]", passwordEncryptionActive);
//
//        if (!passwordEncryptionActive) {
//            LOG.debug("No password encryption will be performed for DSS");
//            return;
//        }

        LOG.debug("Encrypting passwords");


        //We use the default domain to encrypt all the passwords. This is because there is no clear segregation between DSS properties per domain
        final DomainDTO domainDTO = domainExtService.getDomain("default");
        final DssDomainPasswordEncryptionContext passwordEncryptionContext =
                new DssDomainPasswordEncryptionContext(
                        propertyProvider,
                        domibusConfigurationExtService,
                        passwordEncryptionService,
                        domainDTO);
        passwordEncryptionService.encryptPasswordsInFile(passwordEncryptionContext);

        LOG.debug("Finished encrypting passwords");
    }
}
