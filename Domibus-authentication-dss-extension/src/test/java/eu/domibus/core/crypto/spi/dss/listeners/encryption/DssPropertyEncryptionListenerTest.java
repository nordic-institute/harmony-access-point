package eu.domibus.core.crypto.spi.dss.listeners.encryption;

import eu.domibus.core.crypto.spi.dss.DssConfiguration;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.DomainExtService;
import eu.domibus.ext.services.DomibusConfigurationExtService;
import eu.domibus.ext.services.DomibusPropertyManagerExt;
import eu.domibus.ext.services.PasswordEncryptionExtService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Soumya Chandran
 * @since 5.0
 */
@RunWith(JMockit.class)
public class DssPropertyEncryptionListenerTest {

    @Injectable
    protected PasswordEncryptionExtService pluginPasswordEncryptionService;

    @Injectable
    protected DssConfiguration dssConfiguration;

    @Injectable
    protected DomibusConfigurationExtService domibusConfigurationExtService;

    @Injectable
    protected DomainExtService domainExtService;

    @Injectable
    DomibusPropertyManagerExt propertyProvider;

    @Tested
    DssPropertyEncryptionListener dssPropertyEncryptionListener;

    @Test
    public void encryptPasswords(@Injectable DomainDTO domainDTO,
                                 @Mocked DssDomainPasswordEncryptionContext dssPropertyPasswordEncryptionContext) {
        new Expectations() {{
//            propertyProvider.isPasswordEncryptionActive();
//            result = true;

            domainExtService.getDomain(dssConfiguration.DEFAULT_DOMAIN);
            result = domainDTO;

            new DssDomainPasswordEncryptionContext(propertyProvider, domibusConfigurationExtService, pluginPasswordEncryptionService, domainDTO);
            result = dssPropertyPasswordEncryptionContext;
        }};

        dssPropertyEncryptionListener.encryptPasswords();

        new Verifications() {{
            pluginPasswordEncryptionService.encryptPasswordsInFile(dssPropertyPasswordEncryptionContext);
        }};
    }

}
