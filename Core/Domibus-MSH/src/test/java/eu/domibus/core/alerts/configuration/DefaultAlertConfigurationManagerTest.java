package eu.domibus.core.alerts.configuration;

import eu.domibus.api.alerts.AlertLevel;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.alerts.configuration.account.AccountDisabledModuleConfiguration;
import eu.domibus.core.alerts.configuration.common.AlertConfigurationService;
import eu.domibus.core.alerts.configuration.common.AlertModuleConfiguration;
import eu.domibus.core.alerts.configuration.generic.DefaultConfigurationManager;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.core.alerts.model.service.ConfigurationLoader;
import eu.domibus.core.alerts.model.service.ConfigurationLoaderTest;
import eu.domibus.core.alerts.model.service.Event;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;
import static org.junit.Assert.*;

@RunWith(JMockit.class)
public class DefaultAlertConfigurationManagerTest {

    @Tested
    DefaultConfigurationManager configurationReader = new DefaultConfigurationManager(AlertType.USER_ACCOUNT_DISABLED);

    @Injectable
    ConfigurationLoader<AccountDisabledModuleConfiguration> loader;

    @Injectable
    private DomibusConfigurationService domibusConfigurationService;

    @Injectable
    private DomainContextProvider domainContextProvider;

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    AlertConfigurationService alertConfigurationService;

    @Test
    @Ignore
    public void readLoginFailureConfigurationMainModuleInactive() {

        new Expectations() {{
            alertConfigurationService.isAlertModuleEnabled();
            result = false;
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_PLUGIN_USER_LOGIN_FAILURE_ACTIVE);
            result = true;
        }};

        final AlertModuleConfiguration loginFailureConfiguration = configurationReader.readConfiguration();

        assertFalse(loginFailureConfiguration.isActive());
    }

    @Test
    @Ignore
    public void readLoginFailureConfigurationModuleInactive() {
        new Expectations() {
            {
                alertConfigurationService.isAlertModuleEnabled();
                result = true;
                domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_PLUGIN_USER_LOGIN_FAILURE_ACTIVE);
                result = false;
            }
        };
        final AlertModuleConfiguration loginFailureConfiguration = configurationReader.readConfiguration();
        assertFalse(loginFailureConfiguration.isActive());
    }

    @Test
    @Ignore
    public void readLoginFailureConfiguration() {
        final String mailSubject = "Login failure";
        new Expectations() {
            {
                alertConfigurationService.isAlertModuleEnabled();
                result = true;
                domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_PLUGIN_USER_LOGIN_FAILURE_ACTIVE);
                result = true;
                domibusPropertyProvider.getProperty(DOMIBUS_ALERT_PLUGIN_USER_LOGIN_FAILURE_LEVEL);
                result = "MEDIUM";
                domibusPropertyProvider.getProperty(DOMIBUS_ALERT_PLUGIN_USER_LOGIN_FAILURE_MAIL_SUBJECT);
                this.result = mailSubject;
            }
        };
        final AlertModuleConfiguration loginFailureConfiguration = configurationReader.readConfiguration();
        assertTrue(loginFailureConfiguration.isActive());
        Event event = new Event(EventType.PLUGIN_USER_LOGIN_FAILURE);
        assertEquals(AlertLevel.MEDIUM, loginFailureConfiguration.getAlertLevel(event));
        assertEquals(mailSubject, loginFailureConfiguration.getMailSubject());
    }

    @Test
    @Ignore
    public void readLoginFailureConfigurationWrongAlertLevelConfig() {

        new Expectations() {
            {
                alertConfigurationService.isAlertModuleEnabled();
                result = true;
                domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_PLUGIN_USER_LOGIN_FAILURE_ACTIVE);
                result = true;
                domibusPropertyProvider.getProperty(DOMIBUS_ALERT_PLUGIN_USER_LOGIN_FAILURE_LEVEL);
                result = "WHAT?";
            }
        };
        final AlertModuleConfiguration loginFailureConfiguration = configurationReader.readConfiguration();
        assertFalse(loginFailureConfiguration.isActive());
    }
}
