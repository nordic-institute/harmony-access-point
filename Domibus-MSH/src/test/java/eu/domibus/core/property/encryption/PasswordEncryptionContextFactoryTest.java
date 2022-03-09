package eu.domibus.core.property.encryption;

import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.property.encryption.PasswordEncryptionContext;
import eu.domibus.api.property.encryption.PasswordEncryptionService;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
public class PasswordEncryptionContextFactoryTest {


    @Injectable
    protected DomibusConfigurationService domibusConfigurationService;

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    protected PasswordEncryptionService passwordEncryptionService;

    @Tested
    PasswordEncryptionContextFactory passwordEncryptionContextFactory;

    @Test
    public void getPasswordEncryptionContextNoDomain(@Mocked PasswordEncryptionContextDefault passwordEncryptionContextDefaultMock) {
        final PasswordEncryptionContext passwordEncryptionContext = passwordEncryptionContextFactory.getPasswordEncryptionContext(null);
        Assert.assertTrue(passwordEncryptionContext instanceof PasswordEncryptionContextDefault);

        new Verifications() {{
            new PasswordEncryptionContextDefault(passwordEncryptionService, domibusPropertyProvider, domibusConfigurationService);
        }};
    }

    @Test
    public void getPasswordEncryptionContextWithDomain(@Injectable Domain domain,
                                                       @Mocked PasswordEncryptionContextDomain passwordEncryptionContextDomainMock) {
        final PasswordEncryptionContext passwordEncryptionContext = passwordEncryptionContextFactory.getPasswordEncryptionContext(domain);
        Assert.assertTrue(passwordEncryptionContext instanceof PasswordEncryptionContextDomain);

        new Verifications() {{
            new PasswordEncryptionContextDomain(passwordEncryptionService, domibusPropertyProvider, domibusConfigurationService, domain);
        }};
    }
}