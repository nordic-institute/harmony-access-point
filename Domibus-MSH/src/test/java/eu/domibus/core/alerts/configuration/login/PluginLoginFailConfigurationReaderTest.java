package eu.domibus.core.alerts.configuration.login;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
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
public class PluginLoginFailConfigurationReaderTest {

    @Tested
    PluginLoginFailConfigurationReader configurationReader;

    @Injectable
    private DomibusConfigurationService domibusConfigurationService;

    @Injectable
    private DomainContextProvider domainContextProvider;

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    AlertConfigurationService alertConfigurationService;

    @Test
    public void readLoginFailureConfigurationMainModuleInactive() {

        new Expectations() {{
            alertConfigurationService.isAlertModuleEnabled();
            result = false;
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_PLUGIN_USER_LOGIN_FAILURE_ACTIVE);
            result = true;
        }};

        final LoginFailureModuleConfiguration loginFailureConfiguration = configurationReader.readConfiguration();

        assertFalse(loginFailureConfiguration.isActive());
    }

    @Test
    public void readLoginFailureConfigurationModuleInactive() {
        new Expectations() {
            {
                alertConfigurationService.isAlertModuleEnabled();
                result = true;
                domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_PLUGIN_USER_LOGIN_FAILURE_ACTIVE);
                result = false;
            }
        };
        final LoginFailureModuleConfiguration loginFailureConfiguration = configurationReader.readConfiguration();
        assertFalse(loginFailureConfiguration.isActive());
    }

    @Test
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
        final LoginFailureModuleConfiguration loginFailureConfiguration = configurationReader.readConfiguration();
        assertTrue(loginFailureConfiguration.isActive());
        Alert alert = new Alert();
        alert.setAlertType(AlertType.PLUGIN_USER_LOGIN_FAILURE);
        assertEquals(AlertLevel.MEDIUM, loginFailureConfiguration.getAlertLevel(alert));
        assertEquals(mailSubject, loginFailureConfiguration.getMailSubject());
    }

    @Test
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
        final LoginFailureModuleConfiguration loginFailureConfiguration = configurationReader.readConfiguration();
        assertFalse(loginFailureConfiguration.isActive());
    }
}