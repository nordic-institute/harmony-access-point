package eu.domibus.plugin.fs.property.encryption;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.DomainExtService;
import eu.domibus.ext.services.DomibusConfigurationExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.ext.services.PasswordEncryptionExtService;
import eu.domibus.plugin.encryption.PluginPropertyEncryptionListener;
import eu.domibus.plugin.fs.property.FSPluginProperties;
import eu.domibus.plugin.fs.worker.FSSendMessagesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
@Service
public class FSPluginPropertyEncryptionListener implements PluginPropertyEncryptionListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSPluginPropertyEncryptionListener.class);

    @Autowired
    protected PasswordEncryptionExtService pluginPasswordEncryptionService;

    @Autowired
    protected FSPluginProperties fsPluginProperties;

    @Autowired
    protected DomibusConfigurationExtService domibusConfigurationExtService;

    @Autowired
    protected DomainExtService domainExtService;

    @Override
    public void encryptPasswords() {
        final boolean passwordEncryptionActive = fsPluginProperties.isPasswordEncryptionActive();
        LOG.debug("Encrypting passwords is active in the FS Plugin? [{}]", passwordEncryptionActive);

        if (!passwordEncryptionActive) {
            LOG.info("No password encryption will be performed for FSPlugin");
            return;
        }

        LOG.debug("Encrypting passwords");

        //We use the default domain to encrypt all the passwords. This is because there is no clear segregation between FS Plugin properties per domain
        final DomainDTO domainDTO = domainExtService.getDomain(FSSendMessagesService.DEFAULT_DOMAIN);
        final FSPluginPasswordEncryptionContext passwordEncryptionContext =
                new FSPluginPasswordEncryptionContext(
                        fsPluginProperties,
                        domibusConfigurationExtService,
                        pluginPasswordEncryptionService,
                        domainDTO);
        pluginPasswordEncryptionService.encryptPasswordsInFile(passwordEncryptionContext);

        LOG.debug("Finished encrypting passwords");
    }
}
