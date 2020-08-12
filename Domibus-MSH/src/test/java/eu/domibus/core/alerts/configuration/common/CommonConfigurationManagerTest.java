package eu.domibus.core.alerts.configuration.common;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.alerts.model.service.ConfigurationLoader;
import eu.domibus.core.alerts.service.AlertConfigurationService;
import eu.domibus.core.alerts.service.ConfigurationReader;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.mail.internet.InternetAddress;
import java.util.ArrayList;
import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(JMockit.class)
public class CommonConfigurationManagerTest {

    @Tested
    CommonConfigurationManager configurationManager;

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    private DomainContextProvider domainContextProvider;

    @Injectable
    private ConfigurationLoader<CommonConfiguration> loader;

    @Injectable
    private AlertConfigurationService configurationService;

    @Test
    public void getConfiguration(@Mocked CommonConfiguration configuration) {
        new Expectations() {{
            loader.getConfiguration((ConfigurationReader<CommonConfiguration>) any);
            result = configuration;
        }};
        CommonConfiguration res = configurationManager.getConfiguration();
        assertEquals(res, configuration);
    }

    @Test
    public void reset() {
        configurationManager.reset();
        new Verifications() {{
            loader.resetConfiguration();
        }};
    }

    @Test
    public void readConfiguration() {
        final String sender = "thomas.dussart@ec.eur.europa.com";
        final String receiver = "f.f@f.com";
        new Expectations() {{
            configurationService.isSendEmailActive();
            result = true;
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_SENDER_EMAIL);
            result = sender;
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_RECEIVER_EMAIL);
            result = receiver;
            domibusPropertyProvider.getIntegerProperty(DOMIBUS_ALERT_CLEANER_ALERT_LIFETIME);
            result = 20;
        }};

        final CommonConfiguration commonConfiguration = configurationManager.readConfiguration();

        assertEquals(sender, commonConfiguration.getSendFrom());
        assertEquals(receiver, commonConfiguration.getSendTo());
        assertEquals(20, commonConfiguration.getAlertLifeTimeInDays(), 0);
    }

    @Test
    public void readDomainEmptyEmailConfiguration() {

        final String sender = "";
        final String receiver = "abc@gmail.com";
        new Expectations() {{

            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_SENDER_EMAIL);
            result = sender;
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_RECEIVER_EMAIL);
            result = receiver;
        }};
        try {
            configurationManager.readDomainEmailConfiguration(1);
            Assert.fail();
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals(ex.getMessage(), "Empty sender/receiver email address configured for the alert module.");
        }
    }

    @Test
    public void readDomainInvalidEmailConfiguration() {

        final String sender = "abc.def@mail#g.c";
        final String receiver = "abcd@gmail.com";
        List<String> emailsToValidate = new ArrayList<>();
        emailsToValidate.add(sender);
        emailsToValidate.add(receiver);
        new Expectations(configurationManager) {{

            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_SENDER_EMAIL);
            result = sender;
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_RECEIVER_EMAIL);
            result = receiver;
            configurationManager.isValidEmail(sender);
            result = false;
        }};
        try {
            configurationManager.readDomainEmailConfiguration(1);
            Assert.fail();
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals(ex.getMessage(), "Invalid sender/receiver email address configured for the alert module: abc.def@mail#g.c");
        }
    }

    @Test
    public void isValidEmail(@Injectable InternetAddress address) {

        final String email = "abc.def@gmail.com";
        assertTrue(configurationManager.isValidEmail(email));
    }
}