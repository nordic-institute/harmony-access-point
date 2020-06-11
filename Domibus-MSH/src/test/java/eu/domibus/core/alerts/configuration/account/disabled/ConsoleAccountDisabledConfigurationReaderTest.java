package eu.domibus.core.alerts.configuration.account.disabled;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.alerts.configuration.account.disabled.console.ConsoleAccountDisabledConfigurationReader;
import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.service.Alert;
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
public class ConsoleAccountDisabledConfigurationReaderTest {

    @Tested
    ConsoleAccountDisabledConfigurationReader configurationReader;

    @Injectable
    private DomainContextProvider domainContextProvider;

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    AlertConfigurationService alertConfigurationService;

    @Injectable
    private DomibusConfigurationService domibusConfigurationService;


    @Test
    public void readAccountDisabledConfigurationMainAlertModuleDisabled() {
        new Expectations() {{
            alertConfigurationService.isAlertModuleEnabled();
            this.result = false;
        }};

        final AccountDisabledModuleConfiguration accountDisabledConfiguration = configurationReader.readConfiguration();

        assertFalse(accountDisabledConfiguration.isActive());
    }

    @Test
    public void readAccountDisabledConfiguration() {

        final String mailSubject = "Accout disabled";
        new Expectations(configurationReader) {{
            alertConfigurationService.isAlertModuleEnabled();
            result = true;
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_ACTIVE);
            result = true;
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_LEVEL);
            result = "HIGH";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_MOMENT);
            result = "AT_LOGON";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_SUBJECT);
            this.result = mailSubject;
        }};

        final AccountDisabledModuleConfiguration accountDisabledConfiguration = configurationReader.readConfiguration();

        assertTrue(accountDisabledConfiguration.isActive());
        assertEquals(mailSubject, accountDisabledConfiguration.getMailSubject());
        Alert alert = new Alert();
        alert.setAlertType(AlertType.USER_ACCOUNT_DISABLED);
        assertEquals(AlertLevel.HIGH, accountDisabledConfiguration.getAlertLevel(alert));
        assertTrue(accountDisabledConfiguration.shouldTriggerAccountDisabledAtEachLogin());

    }

    @Test
    public void test_readAccountDisabledConfiguration_ExtAuthProviderEnabled() {
        new Expectations() {{
            domibusConfigurationService.isExtAuthProviderEnabled();
            result = true;
        }};

        final AccountDisabledModuleConfiguration accountDisabledConfiguration = configurationReader.readConfiguration();
        assertFalse(accountDisabledConfiguration.isActive());
    }

    @Test
    public void readAccountDisabledConfigurationMissconfigured() {

        new Expectations() {{
            alertConfigurationService.isAlertModuleEnabled();
            result = true;
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_ACTIVE);
            result = true;
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_LEVEL);
            result = "HIGHPP";
        }};
        final AccountDisabledModuleConfiguration accountDisabledConfiguration = configurationReader.readConfiguration();
        assertFalse(accountDisabledConfiguration.isActive());
    }

}