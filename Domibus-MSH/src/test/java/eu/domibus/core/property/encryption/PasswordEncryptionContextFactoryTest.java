package eu.domibus.core.property.encryption;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.property.encryption.PasswordEncryptionContext;
import eu.domibus.api.property.encryption.PasswordEncryptionService;
import eu.domibus.core.property.DomibusRawPropertyProvider;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
@RunWith(JMockit.class)
public class PasswordEncryptionContextFactoryTest {

    @Injectable
    protected DomibusConfigurationService domibusConfigurationService;

    @Injectable
    protected PasswordEncryptionService passwordEncryptionService;

    @Injectable
    protected DomibusRawPropertyProvider domibusRawPropertyProvider;

    @Tested
    PasswordEncryptionContextFactory passwordEncryptionContextFactory;

    @Test
    public void getPasswordEncryptionContextNoDomain() {
        final PasswordEncryptionContext passwordEncryptionContext = passwordEncryptionContextFactory.getPasswordEncryptionContext(null);
        Assert.assertTrue(passwordEncryptionContext instanceof PasswordEncryptionContextDefault);

        new Verifications() {{
            new PasswordEncryptionContextDefault(passwordEncryptionService, domibusRawPropertyProvider, domibusConfigurationService);
        }};
    }

    @Test
    public void getPasswordEncryptionContextWithDomain(@Injectable Domain domain) {
        final PasswordEncryptionContext passwordEncryptionContext = passwordEncryptionContextFactory.getPasswordEncryptionContext(domain);
        Assert.assertTrue(passwordEncryptionContext instanceof PasswordEncryptionContextDomain);

        new Verifications() {{
            new PasswordEncryptionContextDomain(passwordEncryptionService, domibusRawPropertyProvider, domibusConfigurationService, domain);
        }};
    }
}
