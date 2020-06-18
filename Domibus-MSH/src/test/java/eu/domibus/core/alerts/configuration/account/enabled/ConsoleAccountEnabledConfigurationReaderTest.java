package eu.domibus.core.alerts.configuration.account.enabled;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.alerts.configuration.AlertModuleConfigurationBase;
import eu.domibus.core.alerts.configuration.account.enabled.console.ConsoleAccountEnabledConfigurationReader;
import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.core.alerts.model.service.Event;
import eu.domibus.core.alerts.service.AlertConfigurationService;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;
import static org.junit.Assert.*;

@RunWith(JMockit.class)
public class ConsoleAccountEnabledConfigurationReaderTest {

    @Tested
    ConsoleAccountEnabledConfigurationReader configurationReader;

    @Injectable
    private DomainContextProvider domainContextProvider;

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    AlertConfigurationService alertConfigurationService;

    @Test
    public void readAccountEnabledConfigurationMainAlertModuleEnabled() {
        new Expectations() {{
            alertConfigurationService.isAlertModuleEnabled();
            this.result = false;
        }};
        final AlertModuleConfigurationBase accountEnabledConfiguration = configurationReader.readConfiguration();
        assertFalse(accountEnabledConfiguration.isActive());
    }

    @Test
    public void readAccountEnabledConfiguration() {

        final String mailSubject = "Accout enabled";
        new Expectations() {{
            alertConfigurationService.isAlertModuleEnabled();
            result = true;
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_USER_ACCOUNT_ENABLED_ACTIVE);
            result = true;
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_USER_ACCOUNT_ENABLED_LEVEL);
            result = "HIGH";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_USER_ACCOUNT_ENABLED_SUBJECT);
            this.result = mailSubject;
        }};

        final AlertModuleConfigurationBase accountEnabledConfiguration = configurationReader.readConfiguration();

        assertTrue(accountEnabledConfiguration.isActive());
        assertEquals(mailSubject, accountEnabledConfiguration.getMailSubject());
        Event event = new Event(EventType.USER_ACCOUNT_ENABLED);
        assertEquals(AlertLevel.HIGH, accountEnabledConfiguration.getAlertLevel(event));
    }

    @Test
    public void readAccountEnabledConfigurationMisconfigured() {

        new Expectations() {{
            alertConfigurationService.isAlertModuleEnabled();
            result = true;
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_USER_ACCOUNT_ENABLED_ACTIVE);
            result = true;
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_USER_ACCOUNT_ENABLED_LEVEL);
            result = "HIGHPP";
        }};
        final AlertModuleConfigurationBase accountDisabledConfiguration = configurationReader.readConfiguration();
        assertFalse(accountDisabledConfiguration.isActive());
    }

}