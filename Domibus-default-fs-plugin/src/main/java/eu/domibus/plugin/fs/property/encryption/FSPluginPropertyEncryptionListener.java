package eu.domibus.plugin.fs.property.encryption;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.DomibusConfigurationExtService;
import eu.domibus.ext.services.PasswordEncryptionExtService;
import eu.domibus.ext.services.PluginPasswordEncryptionContext;
import eu.domibus.ext.services.PluginPropertyEncryptionListenerAbstract;
import eu.domibus.plugin.encryption.PluginPropertyEncryptionListener;
import eu.domibus.plugin.fs.property.FSPluginProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
@Service
public class FSPluginPropertyEncryptionListener extends PluginPropertyEncryptionListenerAbstract
        implements PluginPropertyEncryptionListener {

    protected FSPluginProperties propertyProvider;

    public FSPluginPropertyEncryptionListener(FSPluginProperties propertyProvider,
                                              PasswordEncryptionExtService pluginPasswordEncryptionService,
                                              DomibusConfigurationExtService domibusConfigurationExtService) {
        super(pluginPasswordEncryptionService, domibusConfigurationExtService);

        this.propertyProvider = propertyProvider;
    }

    @Override
    protected PluginPasswordEncryptionContext getDomainPasswordEncryptionContextDomain(DomainDTO domain) {
        return new FSPluginDomainPasswordEncryptionContext(propertyProvider, domibusConfigurationExtService, pluginPasswordEncryptionService, domain);
    }

    @Override
    protected PluginPasswordEncryptionContext getGlobalPasswordEncryptionContext() {
        return new FSPluginGlobalPasswordEncryptionContext(propertyProvider, domibusConfigurationExtService, pluginPasswordEncryptionService);
    }

//    @Override
//    public void encryptPasswords() {
//        //check inside context,like in core
////        final boolean passwordEncryptionActive = propertyProvider.isPasswordEncryptionActive();
////        LOG.debug("Encrypting passwords is active in the FS Plugin? [{}]", passwordEncryptionActive);
////        if (!passwordEncryptionActive) {
////            LOG.info("No password encryption will be performed for FSPlugin");
////            return;
////        }
//
//        LOG.debug("Encrypting passwords");
//
//        // global context
//        domainContextProvider.clearCurrentDomain();
//        final PluginPasswordEncryptionContext passwordEncryptionContext = getGlobalPasswordEncryptionContext();
//        pluginPasswordEncryptionService.encryptPasswordsInFile(passwordEncryptionContext);
//
//        // domain context
//        if (domibusConfigurationExtService.isMultiTenantAware()) {
//            final List<DomainDTO> domains = domainsExtService.getDomains();
//            for (DomainDTO domain : domains) {
//                domainContextProvider.setCurrentDomain(domain);
//                PluginPasswordEncryptionContext passwordEncryptionContextDomain = getDomainPasswordEncryptionContextDomain(domain);
//                pluginPasswordEncryptionService.encryptPasswordsInFile(passwordEncryptionContextDomain);
//                domainContextProvider.clearCurrentDomain();
//            }
//        }
//
//        //We use the default domain to encrypt all the passwords. This is because there is no clear segregation between FS Plugin properties per domain
////        final DomainDTO domainDTO = domainExtService.getDomain(FSSendMessagesService.DEFAULT_DOMAIN);
////        final FSPluginDomainPasswordEncryptionContext passwordEncryptionContext =
////                new FSPluginDomainPasswordEncryptionContext(
////                        propertyProvider,
////                        domibusConfigurationExtService,
////                        pluginPasswordEncryptionService,
////                        domainDTO);
////        pluginPasswordEncryptionService.encryptPasswordsInFile(passwordEncryptionContext);
//
//        LOG.debug("Finished encrypting passwords");
//    }

}
