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

}
