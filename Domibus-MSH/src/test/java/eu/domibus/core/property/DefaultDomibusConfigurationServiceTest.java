package eu.domibus.core.property;

import eu.domibus.api.property.DataBaseEngine;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.property.DefaultDomibusConfigurationService;
import eu.domibus.logging.DomibusLoggerFactory;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.ext.LoggerWrapper;

import java.io.File;

import static eu.domibus.api.property.DomibusConfigurationService.PASSWORD_ENCRYPTION_ACTIVE_PROPERTY;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
@RunWith(JMockit.class)
public class DefaultDomibusConfigurationServiceTest {

    @Injectable
    private DataBaseEngine dataBaseEngine;

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Mocked
    DomibusLoggerFactory domibusLoggerFactory;

    @Tested
    DefaultDomibusConfigurationService defaultDomibusConfigurationService;

    @Test
    public void isPasswordEncryptionActive() {
        new Expectations() {{
            domibusPropertyProvider.getBooleanProperty(PASSWORD_ENCRYPTION_ACTIVE_PROPERTY);
            result = true;
        }};

        Assert.assertTrue(defaultDomibusConfigurationService.isPasswordEncryptionActive());


    }

    @Test
    public void isPasswordEncryptionActive1(@Injectable Domain domain) {
        new Expectations() {{
            domibusPropertyProvider.getBooleanProperty(domain, PASSWORD_ENCRYPTION_ACTIVE_PROPERTY);
            result = true;
        }};

        Assert.assertTrue(defaultDomibusConfigurationService.isPasswordEncryptionActive(domain));
    }

    @Test
    public void getConfigurationFileName() {
        Assert.assertEquals(DomibusPropertyProvider.DOMIBUS_PROPERTY_FILE, defaultDomibusConfigurationService.getConfigurationFileName());
    }

    @Test
    public void getConfigurationFileNameDefaultDomain(@Mocked File file) {
        String defaultConfigLocation = "/home";
        String domainConfigFile = "default.key";

        new Expectations(defaultDomibusConfigurationService) {{
            defaultDomibusConfigurationService.getConfigLocation();
            result = defaultConfigLocation;

            defaultDomibusConfigurationService.getDomainConfigurationFileName(DomainService.DEFAULT_DOMAIN);
            result = domainConfigFile;


            new File(anyString).exists();
            result = true;

        }};

        final String configurationFileName = defaultDomibusConfigurationService.getConfigurationFileName(DomainService.DEFAULT_DOMAIN);
        Assert.assertEquals(domainConfigFile, configurationFileName);
    }

    @Test
    public void getConfigurationFileNameCustomDomain(@Injectable Domain domain,
                                                     @Mocked DomibusLoggerFactory domibusLoggerFactory,
                                                     @Mocked LoggerWrapper loggerWrapper) {

        String domainConfigFile = "/homecustom.key";

        new Expectations(defaultDomibusConfigurationService) {{
            defaultDomibusConfigurationService.getDomainConfigurationFileName(domain);
            result = domainConfigFile;
        }};

        final String configurationFileName = defaultDomibusConfigurationService.getConfigurationFileName(domain);
        Assert.assertEquals(domainConfigFile, configurationFileName);
    }

    @Test
    public void getDomainConfigurationFileName(@Injectable Domain domain) {
        String myDomain = "myDomain";
        new Expectations() {{
            domain.getCode();
            result = myDomain;
        }};

        final String domainConfigurationFileName = defaultDomibusConfigurationService.getDomainConfigurationFileName(domain);
        Assert.assertEquals("myDomain-" + DomibusPropertyProvider.DOMIBUS_PROPERTY_FILE, domainConfigurationFileName);
    }
}