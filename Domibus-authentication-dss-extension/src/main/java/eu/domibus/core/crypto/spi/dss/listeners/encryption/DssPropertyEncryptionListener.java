package eu.domibus.core.crypto.spi.dss.listeners.encryption;

import eu.domibus.core.crypto.spi.dss.DssExtensionPropertyManager;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.*;
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
@Service
public class DssPropertyEncryptionListener extends PluginPropertyEncryptionListenerAbstract
        implements PluginPropertyEncryptionListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DssPropertyEncryptionListener.class);

    protected DssExtensionPropertyManager propertyProvider;

    public DssPropertyEncryptionListener(DssExtensionPropertyManager propertyProvider,
                                              PasswordEncryptionExtService pluginPasswordEncryptionService,
                                              DomibusConfigurationExtService domibusConfigurationExtService) {
        super(pluginPasswordEncryptionService, domibusConfigurationExtService);

        this.propertyProvider = propertyProvider;
    }

    @Override
    protected PluginPasswordEncryptionContext getDomainPasswordEncryptionContextDomain(DomainDTO domain) {
        return new DssDomainPasswordEncryptionContext(propertyProvider, domibusConfigurationExtService, pluginPasswordEncryptionService, domain);
    }

    @Override
    protected PluginPasswordEncryptionContext getGlobalPasswordEncryptionContext() {
        return new DssGlobalPasswordEncryptionContext(propertyProvider, domibusConfigurationExtService, pluginPasswordEncryptionService);
    }

//    @Override
//    public void encryptPasswords() {
////        final boolean passwordEncryptionActive = propertyProvider.isPasswordEncryptionActive();
////        LOG.debug("Encrypting passwords is active in DSS configuration? [{}]", passwordEncryptionActive);
////
////        if (!passwordEncryptionActive) {
////            LOG.debug("No password encryption will be performed for DSS");
////            return;
////        }
//
//        LOG.debug("Encrypting passwords");
//
//
//        //We use the default domain to encrypt all the passwords. This is because there is no clear segregation between DSS properties per domain
//        final DomainDTO domainDTO = domainExtService.getDomain("default");
//        final DssDomainPasswordEncryptionContext passwordEncryptionContext =
//                new DssDomainPasswordEncryptionContext(
//                        propertyProvider,
//                        domibusConfigurationExtService,
//                        passwordEncryptionService,
//                        domainDTO);
//        passwordEncryptionService.encryptPasswordsInFile(passwordEncryptionContext);
//
//        LOG.debug("Finished encrypting passwords");
//    }
}
