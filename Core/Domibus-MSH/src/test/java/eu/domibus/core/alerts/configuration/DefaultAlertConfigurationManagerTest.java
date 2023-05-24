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
    DefaultConfigurationManager configurationReader = new DefaultConfigurationManager(AlertType.ARCHIVING_NOTIFICATION_FAILED);

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
    public void readLoginFailureConfigurationMainModuleInactive() {

        new Expectations() {{
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
            result = false;
        }};

        final AlertModuleConfiguration loginFailureConfiguration = configurationReader.readConfiguration();

        assertFalse(loginFailureConfiguration.isActive());
    }

    @Test
    public void readLoginFailureConfigurationModuleInactive() {
        new Expectations() {
            {
                domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
                result = true;
            }
        };
        final AlertModuleConfiguration loginFailureConfiguration = configurationReader.readConfiguration();
        assertFalse(loginFailureConfiguration.isActive());
    }

    @Test
    public void readLoginFailureConfiguration() {
        final String mailSubject = "mailSubject";
        new Expectations() {
            {
                domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
                result = true;
                domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_EARCHIVING_NOTIFICATION_FAILED_ACTIVE);
                result = true;
                domibusPropertyProvider.getProperty(DOMIBUS_ALERT_EARCHIVING_NOTIFICATION_FAILED_LEVEL);
                result = "MEDIUM";
                domibusPropertyProvider.getProperty(DOMIBUS_ALERT_EARCHIVING_NOTIFICATION_FAILED_MAIL_SUBJECT);
                this.result = mailSubject;
            }
        };
        final AlertModuleConfiguration configuration = configurationReader.readConfiguration();
        assertTrue(configuration.isActive());
        Event event = new Event(EventType.ARCHIVING_NOTIFICATION_FAILED);
        assertEquals(AlertLevel.MEDIUM, configuration.getAlertLevel(event));
        assertEquals(mailSubject, configuration.getMailSubject());
    }

    @Test
    public void readLoginFailureConfigurationWrongAlertLevelConfig() {

        new Expectations() {
            {
                domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
                result = true;
                domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_EARCHIVING_NOTIFICATION_FAILED_ACTIVE);
                result = true;
                domibusPropertyProvider.getProperty(DOMIBUS_ALERT_EARCHIVING_NOTIFICATION_FAILED_LEVEL);
                result = "WHAT?";
            }
        };
        final AlertModuleConfiguration loginFailureConfiguration = configurationReader.readConfiguration();
        assertFalse(loginFailureConfiguration.isActive());
    }
}
