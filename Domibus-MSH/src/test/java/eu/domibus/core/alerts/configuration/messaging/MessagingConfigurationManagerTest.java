package eu.domibus.core.alerts.configuration.messaging;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.MessageStatus;
import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.service.ConfigurationLoader;
import eu.domibus.core.alerts.service.AlertConfigurationService;
import eu.domibus.core.alerts.service.ConfigurationReader;

import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;
import static org.junit.Assert.*;

@RunWith(JMockit.class)
public class MessagingConfigurationManagerTest  {

    @Tested
    MessagingConfigurationManager configurationManager;

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    private DomainContextProvider domainContextProvider;

    @Injectable
    private ConfigurationLoader<MessagingModuleConfiguration> loader;

    @Injectable
    AlertConfigurationService alertConfigurationService;

    @Test
    public void getAlertType() {
        AlertType res = configurationManager.getAlertType();
        assertEquals(res,  AlertType.MSG_STATUS_CHANGED);
    }

    @Test
    public void getConfiguration(@Mocked MessagingModuleConfiguration configuration) {
        new Expectations() {{
            loader.getConfiguration((ConfigurationReader<MessagingModuleConfiguration>) any);
            result = configuration;
        }};
        MessagingModuleConfiguration res = configurationManager.getConfiguration();
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
    public void readConfigurationEachMessagetStatusItsOwnAlertLevel() {
        final String mailSubject = "Messsage status changed";
        new Expectations() {{
            alertConfigurationService.isAlertModuleEnabled();
            result = true;
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_ACTIVE);
            result = true;
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_STATES);
            result = "SEND_FAILURE,ACKNOWLEDGED";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_LEVEL);
            result = "HIGH,LOW";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_MAIL_SUBJECT);
            this.result = mailSubject;
        }};

        final MessagingModuleConfiguration messagingConfiguration = configurationManager.readConfiguration();

        assertEquals(mailSubject, messagingConfiguration.getMailSubject());
        assertEquals(AlertLevel.HIGH, messagingConfiguration.getAlertLevel(MessageStatus.SEND_FAILURE));
        assertEquals(AlertLevel.LOW, messagingConfiguration.getAlertLevel(MessageStatus.ACKNOWLEDGED));
        assertTrue(messagingConfiguration.isActive());
    }

    @Test
    public void readConfigurationEachMessagetStatusHasTheSameAlertLevel() {
        final String mailSubject = "Messsage status changed";
        new Expectations() {{
            alertConfigurationService.isAlertModuleEnabled();
            result = true;
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_ACTIVE);
            result = true;
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_STATES);
            result = "SEND_FAILURE,ACKNOWLEDGED";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_LEVEL);
            result = "HIGH";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_MAIL_SUBJECT);
            this.result = mailSubject;
        }};

        final MessagingModuleConfiguration messagingConfiguration = configurationManager.readConfiguration();

        assertEquals(mailSubject, messagingConfiguration.getMailSubject());
        assertEquals(AlertLevel.HIGH, messagingConfiguration.getAlertLevel(MessageStatus.SEND_FAILURE));
        assertEquals(AlertLevel.HIGH, messagingConfiguration.getAlertLevel(MessageStatus.ACKNOWLEDGED));
        assertTrue(messagingConfiguration.isActive());
    }

    @Test
    public void readConfigurationIncorrectProperty() {
        new Expectations() {{
            alertConfigurationService.isAlertModuleEnabled();
            result = true;
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_ACTIVE);
            result = true;
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_STATES);
            result = "SEND_FLOP";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_LEVEL);
            result = "HIGH";
        }};
        final MessagingModuleConfiguration messagingConfiguration = configurationManager.readConfiguration();
        assertFalse(messagingConfiguration.isActive());
    }

    @Test
    public void readMessageConfigurationActiveFalse() {
        new Expectations() {{
            alertConfigurationService.isAlertModuleEnabled();
            this.result = true;
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_ACTIVE);
            result = false;
        }};
        final MessagingModuleConfiguration messagingConfiguration = configurationManager.readConfiguration();
        assertFalse(messagingConfiguration.isActive());
    }

    @Test
    public void readMessageConfigurationEmptyStatus() {
        new Expectations() {{
            alertConfigurationService.isAlertModuleEnabled();
            this.result = true;
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_ACTIVE);
            result = true;
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_STATES);
            result = "";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_LEVEL);
            result = "";
        }};
        final MessagingModuleConfiguration messagingConfiguration = configurationManager.readConfiguration();
        assertFalse(messagingConfiguration.isActive());
    }

}