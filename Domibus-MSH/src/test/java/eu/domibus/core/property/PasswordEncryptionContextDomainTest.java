package eu.domibus.core.property;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Cosmin Baciu
 * @since
 */
public class PasswordEncryptionContextDomainTest {

    @Injectable
    Domain domain;

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

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
            domibusPropertyProvider.getProperty(domain, myProperty);
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