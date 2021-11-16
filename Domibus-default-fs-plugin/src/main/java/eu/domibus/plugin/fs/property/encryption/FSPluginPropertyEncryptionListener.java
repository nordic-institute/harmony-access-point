package eu.domibus.plugin.fs.property.encryption;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.DomainExtService;
import eu.domibus.ext.services.DomibusConfigurationExtService;
import eu.domibus.ext.services.PasswordEncryptionExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.encryption.PluginPropertyEncryptionListener;
import eu.domibus.plugin.fs.property.FSPluginProperties;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
@Service
public class FSPluginPropertyEncryptionListener implements PluginPropertyEncryptionListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSPluginPropertyEncryptionListener.class);

    protected PasswordEncryptionExtService pluginPasswordEncryptionService;
    protected FSPluginProperties propertyProvider;
    protected DomibusConfigurationExtService domibusConfigurationExtService;
    protected DomainExtService domainsExtService;
    protected final DomainContextExtService domainContextProvider;

    public FSPluginPropertyEncryptionListener(PasswordEncryptionExtService pluginPasswordEncryptionService,
                                              FSPluginProperties propertyProvider,
                                              DomibusConfigurationExtService domibusConfigurationExtService,
                                              DomainExtService domainsExtService,
                                              DomainContextExtService domainContextProvider) {
        this.pluginPasswordEncryptionService = pluginPasswordEncryptionService;
        this.propertyProvider = propertyProvider;
        this.domibusConfigurationExtService = domibusConfigurationExtService;
        this.domainsExtService = domainsExtService;
        this.domainContextProvider = domainContextProvider;
    }

    @Override
    public void encryptPasswords() {
        //check inside context,like in core
//        final boolean passwordEncryptionActive = propertyProvider.isPasswordEncryptionActive();
//        LOG.debug("Encrypting passwords is active in the FS Plugin? [{}]", passwordEncryptionActive);
//        if (!passwordEncryptionActive) {
//            LOG.info("No password encryption will be performed for FSPlugin");
//            return;
//        }

        LOG.debug("Encrypting passwords");

        // global context
        domainContextProvider.clearCurrentDomain();
        final FSPluginGlobalPasswordEncryptionContext passwordEncryptionContext
                = new FSPluginGlobalPasswordEncryptionContext(propertyProvider, domibusConfigurationExtService, pluginPasswordEncryptionService);
        pluginPasswordEncryptionService.encryptPasswordsInFile(passwordEncryptionContext);

        // domain context
        if (domibusConfigurationExtService.isMultiTenantAware()) {
            final List<DomainDTO> domains = domainsExtService.getDomains();
            for (DomainDTO domain : domains) {
                domainContextProvider.setCurrentDomain(domain);
                FSPluginDomainPasswordEncryptionContext passwordEncryptionContextDomain
                        = new FSPluginDomainPasswordEncryptionContext(propertyProvider, domibusConfigurationExtService, pluginPasswordEncryptionService, domain);
                pluginPasswordEncryptionService.encryptPasswordsInFile(passwordEncryptionContextDomain);
                domainContextProvider.clearCurrentDomain();
            }
        }

        //We use the default domain to encrypt all the passwords. This is because there is no clear segregation between FS Plugin properties per domain
//        final DomainDTO domainDTO = domainExtService.getDomain(FSSendMessagesService.DEFAULT_DOMAIN);
//        final FSPluginDomainPasswordEncryptionContext passwordEncryptionContext =
//                new FSPluginDomainPasswordEncryptionContext(
//                        propertyProvider,
//                        domibusConfigurationExtService,
//                        pluginPasswordEncryptionService,
//                        domainDTO);
//        pluginPasswordEncryptionService.encryptPasswordsInFile(passwordEncryptionContext);

        LOG.debug("Finished encrypting passwords");
    }
}
