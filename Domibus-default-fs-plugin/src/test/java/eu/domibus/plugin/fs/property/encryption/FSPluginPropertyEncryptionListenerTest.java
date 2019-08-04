package eu.domibus.plugin.fs.property.encryption;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.DomainExtService;
import eu.domibus.ext.services.DomibusConfigurationExtService;
import eu.domibus.ext.services.PasswordEncryptionExtService;
import eu.domibus.plugin.fs.property.FSPluginProperties;
import eu.domibus.plugin.fs.worker.FSSendMessagesService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
@RunWith(JMockit.class)
public class FSPluginPropertyEncryptionListenerTest {

    @Injectable
    protected PasswordEncryptionExtService pluginPasswordEncryptionService;

    @Injectable
    protected FSPluginProperties fsPluginProperties;

    @Injectable
    protected DomibusConfigurationExtService domibusConfigurationExtService;

    @Injectable
    protected DomainExtService domainExtService;

    @Tested
    FSPluginPropertyEncryptionListener fsPluginPropertyEncryptionListener;


    @Test
    public void encryptPasswords(@Injectable DomainDTO domainDTO,
                                 @Mocked FSPluginPasswordEncryptionContext fsPluginPasswordEncryptionContext) {
        new Expectations() {{
            fsPluginProperties.isPasswordEncryptionActive();
            result = true;

            domainExtService.getDomain(FSSendMessagesService.DEFAULT_DOMAIN);
            result = domainDTO;

            new FSPluginPasswordEncryptionContext(fsPluginProperties, domibusConfigurationExtService, pluginPasswordEncryptionService, domainDTO);
            result = fsPluginPasswordEncryptionContext;
        }};

        fsPluginPropertyEncryptionListener.encryptPasswords();

        new Verifications() {{
            pluginPasswordEncryptionService.encryptPasswordsInFile(fsPluginPasswordEncryptionContext);
        }};
    }
}