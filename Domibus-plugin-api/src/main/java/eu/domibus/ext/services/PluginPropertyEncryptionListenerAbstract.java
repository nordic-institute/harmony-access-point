package eu.domibus.ext.services;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.encryption.PluginPropertyEncryptionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
public abstract class PluginPropertyEncryptionListenerAbstract implements PluginPropertyEncryptionListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PluginPropertyEncryptionListenerAbstract.class);

    @Autowired
    protected DomainExtService domainsExtService;

    @Autowired
    protected DomainContextExtService domainContextProvider;

    protected PasswordEncryptionExtService pluginPasswordEncryptionService;
    protected DomibusConfigurationExtService domibusConfigurationExtService;

    public PluginPropertyEncryptionListenerAbstract(PasswordEncryptionExtService pluginPasswordEncryptionService,
                                                    DomibusConfigurationExtService domibusConfigurationExtService) {
        this.pluginPasswordEncryptionService = pluginPasswordEncryptionService;
        this.domibusConfigurationExtService = domibusConfigurationExtService;
    }

    protected abstract PluginPasswordEncryptionContext getDomainPasswordEncryptionContextDomain(DomainDTO domain);

    protected abstract PluginPasswordEncryptionContext getGlobalPasswordEncryptionContext();

    @Override
    public void encryptPasswords() {
        LOG.debug("Encrypting passwords");

        // operate on the global context, without a current domain
        domainContextProvider.clearCurrentDomain();
        final PluginPasswordEncryptionContext passwordEncryptionContext = getGlobalPasswordEncryptionContext();
        pluginPasswordEncryptionService.encryptPasswordsInFile(passwordEncryptionContext);

        // operate on each domain context
        if (domibusConfigurationExtService.isMultiTenantAware()) {
            final List<DomainDTO> domains = domainsExtService.getDomains();
            for (DomainDTO domain : domains) {
                domainContextProvider.setCurrentDomain(domain);
                PluginPasswordEncryptionContext passwordEncryptionContextDomain = getDomainPasswordEncryptionContextDomain(domain);
                pluginPasswordEncryptionService.encryptPasswordsInFile(passwordEncryptionContextDomain);
                domainContextProvider.clearCurrentDomain();
            }
        }

        LOG.debug("Finished encrypting passwords");
    }
}
