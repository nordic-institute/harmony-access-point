package eu.domibus.core.alerts.configuration.common;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.alerts.model.service.ConfigurationLoader;
import eu.domibus.core.alerts.service.AlertConfigurationService;
import eu.domibus.core.alerts.service.ConfigurationReader;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;
import static org.junit.Assert.assertEquals;

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
            configurationService.getSendEmailActivePropertyName();
            result = DOMIBUS_ALERT_MAIL_SENDING_ACTIVE;
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_SENDER_EMAIL);
            result = sender;
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_RECEIVER_EMAIL);
            result = receiver;
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_MAIL_SENDING_ACTIVE);
            result = true;
            domibusPropertyProvider.getIntegerProperty(DOMIBUS_ALERT_CLEANER_ALERT_LIFETIME);
            result = 20;
        }};

        final CommonConfiguration commonConfiguration = configurationManager.readConfiguration();

        assertEquals(sender, commonConfiguration.getSendFrom());
        assertEquals(receiver, commonConfiguration.getSendTo());
        assertEquals(20, commonConfiguration.getAlertLifeTimeInDays(), 0);
    }

    @Test
    public void readDomainEmailConfiguration() {
    }

    @Test
    public void isValidEmail() {
    }
}