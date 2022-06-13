package eu.domibus.core.property.encryption;

import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.property.encryption.PasswordEncryptionService;
import eu.domibus.core.property.DomibusRawPropertyProvider;
import mockit.Expectations;
import mockit.Injectable;
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
public class PasswordEncryptionContextDomainTest {

    @Injectable
    Domain domain;

    @Injectable
    protected PasswordEncryptionService passwordEncryptionService;

    @Injectable
    protected DomibusRawPropertyProvider domibusPropertyProvider;

    @Injectable
    protected DomibusConfigurationService domibusConfigurationService;

    @Tested
    PasswordEncryptionContextDomain passwordEncryptionContextDomain;

    @Test
    public void isPasswordEncryptionActive() {
        new Expectations() {{
            domibusConfigurationService.isPasswordEncryptionActive(domain);
            result = true;
        }};

        Assert.assertTrue(passwordEncryptionContextDomain.isPasswordEncryptionActive());
    }

    @Test
    public void getProperty() {
        String myProperty = "myProperty";
        passwordEncryptionContextDomain.getProperty(myProperty);

        new Verifications() {{
            domibusPropertyProvider.getRawPropertyValue(domain, myProperty);
        }};
    }

    @Test
    public void getConfigurationFileName() {
        String myConfFile = "myConfFile";

        new Expectations() {{
            domibusConfigurationService.getConfigurationFileName(domain);
            result = myConfFile;
        }};

        Assert.assertEquals(myConfFile, passwordEncryptionContextDomain.getConfigurationFileName());
    }
}
