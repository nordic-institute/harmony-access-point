package eu.domibus.core.alerts.service;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.alerts.configuration.AlertConfigurationManager;
import eu.domibus.core.alerts.configuration.AlertModuleConfigurationBase;
import eu.domibus.core.alerts.configuration.account.disabled.*;
import eu.domibus.core.alerts.configuration.account.enabled.ConsoleAccountEnabledConfigurationManager;
import eu.domibus.core.alerts.configuration.account.enabled.ConsoleAccountEnabledConfigurationReader;
import eu.domibus.core.alerts.configuration.account.enabled.PluginAccountEnabledConfigurationManager;
import eu.domibus.core.alerts.configuration.account.enabled.PluginAccountEnabledConfigurationReader;
import eu.domibus.core.alerts.configuration.certificate.ExpiredCertificateConfigurationManager;
import eu.domibus.core.alerts.configuration.certificate.ExpiredCertificateModuleConfiguration;
import eu.domibus.core.alerts.configuration.certificate.ImminentExpirationCertificateConfigurationManager;
import eu.domibus.core.alerts.configuration.certificate.ImminentExpirationCertificateModuleConfiguration;
import eu.domibus.core.alerts.configuration.common.CommonConfigurationManager;
import eu.domibus.core.alerts.configuration.login.ConsoleLoginFailConfigurationManager;
import eu.domibus.core.alerts.configuration.login.ConsoleLoginFailConfigurationReader;
import eu.domibus.core.alerts.configuration.login.LoginFailureModuleConfiguration;
import eu.domibus.core.alerts.configuration.login.PluginLoginFailConfigurationManager;
import eu.domibus.core.alerts.configuration.messaging.MessagingConfigurationManager;
import eu.domibus.core.alerts.configuration.messaging.MessagingModuleConfiguration;
import eu.domibus.core.alerts.configuration.password.expired.ConsolePasswordExpiredAlertConfigurationReader;
import eu.domibus.core.alerts.configuration.password.PasswordExpirationAlertModuleConfiguration;
import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.service.*;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;
import static org.junit.Assert.*;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(JMockit.class)
public class MultiDomainAlertModuleConfigurationServiceImplTest {

    @Tested
    private MessagingConfigurationManager messagingConfigurationManager;

    @Tested
    private AlertConfigurationService configurationService;

    @Injectable
    ConsoleAccountDisabledConfigurationManager consoleAccountDisabledConfigurationManager;

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    private DomainContextProvider domainContextProvider;

    @Injectable
    DomibusConfigurationService domibusConfigurationService;

    @Injectable
    List<AlertConfigurationManager> alertConfigurationManagers;

    @Injectable
    private PluginAccountDisabledConfigurationManager pluginAccountDisabledConfigurationManager;

    @Injectable
    private ConsoleAccountEnabledConfigurationManager consoleAccountEnabledConfigurationManager;

    @Injectable
    private PluginAccountEnabledConfigurationManager pluginAccountEnabledConfigurationManager;

    @Injectable
    private ConsoleLoginFailConfigurationManager consoleLoginFailConfigurationManager;

    @Injectable
    private PluginLoginFailConfigurationManager pluginLoginFailConfigurationManager;

    @Injectable
    private ImminentExpirationCertificateConfigurationManager imminentExpirationCertificateConfigurationManager;

    @Injectable
    private ExpiredCertificateConfigurationManager expiredCertificateConfigurationManager;

    @Injectable
    private CommonConfigurationManager commonConfigurationManager;

    @Test
    public void getAlertLevelForMessage(final @Mocked MessagingModuleConfiguration messagingConfiguration) {
        final Alert alert = new Alert();
        alert.setAlertType(AlertType.MSG_STATUS_CHANGED);
        new Expectations(configurationService) {{
            messagingConfigurationManager.getConfiguration();
            result = messagingConfiguration;
        }};
        configurationService.getAlertLevel(alert);
        new Verifications() {{
            messagingConfiguration.getAlertLevel(alert);
            times = 1;
        }};
    }

    @Test
    public void getAlertLevelForAccountDisabled(final @Mocked AccountDisabledModuleConfiguration accountDisabledConfiguration) {
        final Alert alert = new Alert();
        alert.setAlertType(AlertType.USER_ACCOUNT_DISABLED);
        new Expectations(configurationService) {{
            consoleAccountDisabledConfigurationManager.getConfiguration();
            this.result = accountDisabledConfiguration;
        }};
        configurationService.getAlertLevel(alert);
        new Verifications() {{
            accountDisabledConfiguration.getAlertLevel(alert);
            times = 1;
        }};
    }


    @Test
    public void getAlertLevelForLoginFailure(final @Mocked LoginFailureModuleConfiguration loginFailureConfiguration) {
        final Alert alert = new Alert();
        alert.setAlertType(AlertType.USER_LOGIN_FAILURE);
        new Expectations(configurationService) {{
            configurationService.getConsoleLoginFailureConfiguration();
            this.result = loginFailureConfiguration;
        }};
        configurationService.getAlertLevel(alert);
        new Verifications() {{
            loginFailureConfiguration.getAlertLevel(alert);
            times = 1;
        }};
    }

    @Test
    public void getAlertLevelCertificateForImminentExpiration(final @Mocked ImminentExpirationCertificateModuleConfiguration imminentExpirationCertificateConfiguration) {
        final Alert alert = new Alert();
        alert.setAlertType(AlertType.CERT_IMMINENT_EXPIRATION);
        new Expectations(configurationService) {{
            configurationService.getImminentExpirationCertificateConfiguration();
            this.result = imminentExpirationCertificateConfiguration;
        }};
        configurationService.getAlertLevel(alert);
        new Verifications() {{
            imminentExpirationCertificateConfiguration.getAlertLevel(alert);
            times = 1;
        }};
    }

    @Test
    public void getAlertLevelForCertificateExpired(final @Mocked ExpiredCertificateModuleConfiguration expiredCertificateConfiguration) {
        final Alert alert = new Alert();
        alert.setAlertType(AlertType.CERT_EXPIRED);
        new Expectations(configurationService) {{
            configurationService.getExpiredCertificateConfiguration();
            this.result = expiredCertificateConfiguration;
        }};
        configurationService.getAlertLevel(alert);
        new Verifications() {{
            expiredCertificateConfiguration.getAlertLevel(alert);
            times = 1;
        }};
    }

    @Test
    public void getMailSubjectForMessage(final @Mocked MessagingModuleConfiguration messagingConfiguration) {
        new Expectations(messagingConfigurationManager) {{
            messagingConfigurationManager.getConfiguration();
            result = messagingConfiguration;

            messagingConfiguration.getMailSubject();
            result = DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_MAIL_SUBJECT;
        }};

        final String mailSubject = configurationService.getMailSubject(AlertType.MSG_STATUS_CHANGED);
        Assert.assertNotNull(mailSubject);
        Assert.assertTrue(mailSubject.contains(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_PREFIX));

        new Verifications() {{
            messagingConfiguration.getMailSubject();
            times = 1;
        }};
    }

    @Test
    public void getMailSubjectForAccountDisabled(final @Mocked AccountDisabledModuleConfiguration accountDisabledConfiguration) {
        new Expectations(configurationService) {{
            consoleAccountDisabledConfigurationManager.getConfiguration();
            this.result = accountDisabledConfiguration;

            accountDisabledConfiguration.getMailSubject();
            result = DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_SUBJECT;
        }};

        final String mailSubject = configurationService.getMailSubject(AlertType.USER_ACCOUNT_DISABLED);
        Assert.assertNotNull(mailSubject);
        Assert.assertTrue(mailSubject.contains(DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_PREFIX));

        new Verifications() {{
            accountDisabledConfiguration.getMailSubject();
            times = 1;
        }};
    }

    @Test
    public void getMailSubjectForAccountEnabled(final @Mocked AlertModuleConfigurationBase accountEnabledConfiguration) {
        new Expectations(configurationService) {{
            configurationService.getConsoleAccountEnabledConfiguration();
            this.result = accountEnabledConfiguration;

            accountEnabledConfiguration.getMailSubject();
            result = DOMIBUS_ALERT_USER_ACCOUNT_ENABLED_SUBJECT;
        }};

        final String mailSubject = configurationService.getMailSubject(AlertType.USER_ACCOUNT_ENABLED);
        Assert.assertNotNull(mailSubject);
        Assert.assertTrue(mailSubject.contains(DOMIBUS_ALERT_USER_ACCOUNT_ENABLED_PREFIX));

        new Verifications() {{
            accountEnabledConfiguration.getMailSubject();
            times = 1;
        }};
    }


    @Test
    public void getMailSubjectForLoginFailure(final @Mocked LoginFailureModuleConfiguration loginFailureConfiguration) {
        new Expectations(configurationService) {{
            configurationService.getConsoleLoginFailureConfiguration();
            this.result = loginFailureConfiguration;

            loginFailureConfiguration.getMailSubject();
            result = DOMIBUS_ALERT_USER_LOGIN_FAILURE_MAIL_SUBJECT;
        }};

        final String mailSubject = configurationService.getMailSubject(AlertType.USER_LOGIN_FAILURE);
        Assert.assertNotNull(mailSubject);
        Assert.assertTrue(mailSubject.contains(DOMIBUS_ALERT_USER_LOGIN_FAILURE_PREFIX));

        new Verifications() {{
            loginFailureConfiguration.getMailSubject();
            times = 1;
        }};
    }

    @Test
    public void getMailSubjectForCertificateImminentExpiration(final @Mocked ImminentExpirationCertificateModuleConfiguration imminentExpirationCertificateConfiguration) {
        new Expectations(configurationService) {{
            configurationService.getImminentExpirationCertificateConfiguration();
            this.result = imminentExpirationCertificateConfiguration;

            imminentExpirationCertificateConfiguration.getMailSubject();
            result = DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_MAIL_SUBJECT;
        }};
        final String mailSubject = configurationService.getMailSubject(AlertType.CERT_IMMINENT_EXPIRATION);
        Assert.assertNotNull(mailSubject);
        Assert.assertTrue(mailSubject.contains(DomibusPropertyMetadataManagerSPI.DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_PREFIX));
        new Verifications() {{
            imminentExpirationCertificateConfiguration.getMailSubject();
            times = 1;
        }};
    }

    @Test
    public void getMailSubjectForCertificateExpired(final @Mocked ExpiredCertificateModuleConfiguration expiredCertificateConfiguration) {
        new Expectations(configurationService) {{
            configurationService.getExpiredCertificateConfiguration();
            this.result = expiredCertificateConfiguration;

            expiredCertificateConfiguration.getMailSubject();
            result = DOMIBUS_ALERT_CERT_EXPIRED_MAIL_SUBJECT;
        }};

        final String mailSubject = configurationService.getMailSubject(AlertType.CERT_EXPIRED);
        Assert.assertNotNull(mailSubject);
        Assert.assertTrue(mailSubject.contains(DOMIBUS_ALERT_CERT_EXPIRED_PREFIX));
        new Verifications() {{
            expiredCertificateConfiguration.getMailSubject();
            times = 1;
        }};
    }


//    @Test
//    public void readCommonConfiguration() {
//        final String sender = "thomas.dussart@ec.eur.europa.com";
//        final String receiver = "f.f@f.com";
//        new Expectations() {{
//            domibusPropertyProvider.getProperty( DOMIBUS_ALERT_SENDER_EMAIL);
//            result = sender;
//            domibusPropertyProvider.getProperty( DOMIBUS_ALERT_RECEIVER_EMAIL);
//            result = receiver;
//            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_MAIL_SENDING_ACTIVE);
//            result = true;
//            domibusPropertyProvider.getIntegerProperty(DOMIBUS_ALERT_CLEANER_ALERT_LIFETIME);
//            result = 20;
//        }};
//        final CommonConfiguration commonConfiguration = configurationService.readCommonConfiguration();
//        assertEquals(sender, commonConfiguration.getSendFrom());
//        assertEquals(receiver, commonConfiguration.getSendTo());
//        assertEquals(20, commonConfiguration.getAlertLifeTimeInDays(), 0);
//    }


    @Test
    public void isAlertModuleEnabled() {
        new Expectations() {{
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
            this.result = true;
        }};
        assertEquals(true, configurationService.isAlertModuleEnabled());
    }

//    @Test
//    public void readMessageConfigurationEachMessagetStatusItsOwnAlertLevel() {
//        final String mailSubject = "Messsage status changed";
//        new Expectations() {{
//            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
//            result = true;
//            domibusPropertyProvider.getBooleanProperty( DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_ACTIVE);
//            result = true;
//            domibusPropertyProvider.getProperty( DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_STATES);
//            result = "SEND_FAILURE,ACKNOWLEDGED";
//            domibusPropertyProvider.getProperty( DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_LEVEL);
//            result = "HIGH,LOW";
//            domibusPropertyProvider.getProperty( DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_MAIL_SUBJECT);
//            this.result = mailSubject;
//        }};
//        final MessagingModuleConfiguration messagingConfiguration = configurationService.readMessageConfiguration();
//        assertEquals(mailSubject, messagingConfiguration.getMailSubject());
//        assertEquals(AlertLevel.HIGH, messagingConfiguration.getAlertLevel(MessageStatus.SEND_FAILURE));
//        assertEquals(AlertLevel.LOW, messagingConfiguration.getAlertLevel(MessageStatus.ACKNOWLEDGED));
//        assertTrue(messagingConfiguration.isActive());
//    }

//    @Test
//    public void readMessageConfigurationEachMessagetStatusHasTheSameAlertLevel() {
//
//        final String mailSubject = "Messsage status changed";
//        new Expectations() {{
//            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
//            result = true;
//            domibusPropertyProvider.getBooleanProperty( DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_ACTIVE);
//            result = true;
//            domibusPropertyProvider.getProperty( DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_STATES);
//            result = "SEND_FAILURE,ACKNOWLEDGED";
//            domibusPropertyProvider.getProperty( DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_LEVEL);
//            result = "HIGH";
//            domibusPropertyProvider.getProperty( DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_MAIL_SUBJECT);
//            this.result = mailSubject;
//        }};
//        final MessagingModuleConfiguration messagingConfiguration = configurationService.readMessageConfiguration();
//        assertEquals(mailSubject, messagingConfiguration.getMailSubject());
//        assertEquals(AlertLevel.HIGH, messagingConfiguration.getAlertLevel(MessageStatus.SEND_FAILURE));
//        assertEquals(AlertLevel.HIGH, messagingConfiguration.getAlertLevel(MessageStatus.ACKNOWLEDGED));
//        assertTrue(messagingConfiguration.isActive());
//
//    }

//    @Test
//    public void readMessageConfigurationIncorrectProperty() {
//        new Expectations() {{
//            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
//            result = true;
//            domibusPropertyProvider.getBooleanProperty( DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_ACTIVE);
//            result = true;
//            domibusPropertyProvider.getProperty( DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_STATES);
//            result = "SEND_FLOP";
//            domibusPropertyProvider.getProperty( DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_LEVEL);
//            result = "HIGH";
//        }};
//        final MessagingModuleConfiguration messagingConfiguration = configurationService.readMessageConfiguration();
//        assertFalse(messagingConfiguration.isActive());
//    }

//    @Test
//    public void readMessageConfigurationActiveFalse() {
//
//        new Expectations() {{
//            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
//            this.result = true;
//            domibusPropertyProvider.getBooleanProperty( DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_ACTIVE);
//            result = false;
//        }};
//        final MessagingModuleConfiguration messagingConfiguration = configurationService.readMessageConfiguration();
//        assertFalse(messagingConfiguration.isActive());
//
//    }

//    @Test
//    public void readMessageConfigurationEmptyStatus() {
//
//        new Expectations() {{
//            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
//            this.result = true;
//            domibusPropertyProvider.getBooleanProperty( DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_ACTIVE);
//            result = true;
//            domibusPropertyProvider.getProperty( DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_STATES);
//            result = "";
//            domibusPropertyProvider.getProperty( DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_LEVEL);
//            result = "";
//        }};
//        final MessagingModuleConfiguration messagingConfiguration = configurationService.readMessageConfiguration();
//        assertFalse(messagingConfiguration.isActive());
//
//    }

    @Test
    public void readAccountDisabledConfigurationMainAlertModuleDisabled() {

        new Expectations() {{
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
            this.result = false;
        }};
        final AccountDisabledModuleConfiguration accountDisabledConfiguration = new ConsoleAccountDisabledConfigurationReader().readConfiguration();
        assertFalse(accountDisabledConfiguration.isActive());

    }

    @Test
    public void readAccountEnabledConfigurationMainAlertModuleEnabled() {

        new Expectations() {{
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
            this.result = false;
        }};
        final AlertModuleConfigurationBase accountEnabledConfiguration = new ConsoleAccountEnabledConfigurationReader().readConfiguration();
        assertFalse(accountEnabledConfiguration.isActive());

    }

    @Test
    public void readAccountDisabledConfiguration() {

        final String mailSubject = "Accout disabled";
        new Expectations() {{
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
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
        final AccountDisabledModuleConfiguration accountDisabledConfiguration = new ConsoleAccountDisabledConfigurationReader().readConfiguration();
        assertTrue(accountDisabledConfiguration.isActive());
        assertEquals(mailSubject, accountDisabledConfiguration.getMailSubject());
        Alert alert = new Alert();
        alert.setAlertType(AlertType.USER_ACCOUNT_DISABLED);
        assertEquals(AlertLevel.HIGH, accountDisabledConfiguration.getAlertLevel(alert));
        assertTrue(accountDisabledConfiguration.shouldTriggerAccountDisabledAtEachLogin());

    }

    @Test
    public void readAccountEnabledConfiguration() {

        final String mailSubject = "Accout enabled";
        new Expectations() {{
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
            result = true;
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_USER_ACCOUNT_ENABLED_ACTIVE);
            result = true;
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_USER_ACCOUNT_ENABLED_LEVEL);
            result = "HIGH";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_USER_ACCOUNT_ENABLED_SUBJECT);
            this.result = mailSubject;
        }};
        final AlertModuleConfigurationBase accountEnabledConfiguration = new ConsoleAccountEnabledConfigurationReader().readConfiguration();
        assertTrue(accountEnabledConfiguration.isActive());
        assertEquals(mailSubject, accountEnabledConfiguration.getMailSubject());
        Alert alert = new Alert();
        alert.setAlertType(AlertType.USER_ACCOUNT_ENABLED);
        assertEquals(AlertLevel.HIGH, accountEnabledConfiguration.getAlertLevel(alert));

    }

    @Test
    public void test_readAccountDisabledConfiguration_ExtAuthProviderEnabled() {
        new Expectations() {{
            domibusConfigurationService.isExtAuthProviderEnabled();
            result = true;
        }};

        final AccountDisabledModuleConfiguration accountDisabledConfiguration = new ConsoleAccountDisabledConfigurationReader().readConfiguration();
        assertFalse(accountDisabledConfiguration.isActive());
    }

    @Test
    public void readPluginAccountDisabledConfigurationTest() {

        final String mailSubject = "Plugin accout disabled";
        new Expectations() {{
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
            result = true;
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_DISABLED_ACTIVE);
            result = true;
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_DISABLED_LEVEL);
            result = "HIGH";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_DISABLED_MOMENT);
            result = "AT_LOGON";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_DISABLED_SUBJECT);
            this.result = mailSubject;
        }};
        final AccountDisabledModuleConfiguration accountDisabledConfiguration = new PluginAccountDisabledConfigurationReader().readConfiguration();

        assertTrue(accountDisabledConfiguration.isActive());
        assertEquals(mailSubject, accountDisabledConfiguration.getMailSubject());
        Alert alert = new Alert();
        alert.setAlertType(AlertType.PLUGIN_USER_ACCOUNT_DISABLED);
        assertEquals(AlertLevel.HIGH, accountDisabledConfiguration.getAlertLevel(alert));
        assertTrue(accountDisabledConfiguration.shouldTriggerAccountDisabledAtEachLogin());

    }

    @Test
    public void readPluginAccountEnabledConfigurationTest() {

        final String mailSubject = "Plugin accout enabled";
        new Expectations() {{
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
            result = true;
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_ENABLED_ACTIVE);
            result = true;
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_ENABLED_LEVEL);
            result = "HIGH";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_ENABLED_SUBJECT);
            this.result = mailSubject;
        }};
        final AlertModuleConfigurationBase accountEnabledConfiguration = new PluginAccountEnabledConfigurationReader().readConfiguration();

        assertTrue(accountEnabledConfiguration.isActive());
        assertEquals(mailSubject, accountEnabledConfiguration.getMailSubject());
        Alert alert = new Alert();
        alert.setAlertType(AlertType.PLUGIN_USER_ACCOUNT_ENABLED);
        assertEquals(AlertLevel.HIGH, accountEnabledConfiguration.getAlertLevel(alert));

    }

    @Test
    public void readAccountDisabledConfigurationMissconfigured() {

        new Expectations() {{
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
            result = true;
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_ACTIVE);
            result = true;
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_LEVEL);
            result = "HIGHPP";
        }};
        final AccountDisabledModuleConfiguration accountDisabledConfiguration = new ConsoleAccountDisabledConfigurationReader().readConfiguration();
        assertFalse(accountDisabledConfiguration.isActive());
    }

    @Test
    public void readAccountEnabledConfigurationMissconfigured() {

        new Expectations() {{
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
            result = true;
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_USER_ACCOUNT_ENABLED_ACTIVE);
            result = true;
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_USER_ACCOUNT_ENABLED_LEVEL);
            result = "HIGHPP";
        }};
        final AlertModuleConfigurationBase accountDisabledConfiguration = new ConsoleAccountEnabledConfigurationReader().readConfiguration();
        assertFalse(accountDisabledConfiguration.isActive());
    }

    @Test
    public void readLoginFailureConfigurationMainModuleInactive() {

        new Expectations() {
            {
                domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
                result = false;
                domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_USER_LOGIN_FAILURE_ACTIVE);
                result = true;
            }
        };
        final LoginFailureModuleConfiguration loginFailureConfiguration = new ConsoleLoginFailConfigurationReader().readConfiguration();
        assertFalse(loginFailureConfiguration.isActive());
    }

    @Test
    public void readLoginFailureConfigurationModuleInactive() {
        new Expectations() {
            {
                domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
                result = true;
                domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_USER_LOGIN_FAILURE_ACTIVE);
                result = false;
            }
        };
        final LoginFailureModuleConfiguration loginFailureConfiguration = new ConsoleLoginFailConfigurationReader().readConfiguration();
        assertFalse(loginFailureConfiguration.isActive());
    }

    @Test
    public void test_readLoginFailureConfigurationExtAuthProviderEnabled() {
        new Expectations() {
            {
                domibusConfigurationService.isExtAuthProviderEnabled();
                result = true;
            }
        };
        final LoginFailureModuleConfiguration loginFailureConfiguration = new ConsoleLoginFailConfigurationReader().readConfiguration();
        assertFalse(loginFailureConfiguration.isActive());
    }


    @Test
    public void readLoginFailureConfiguration() {
        final String mailSubject = "Login failure";
        new Expectations() {
            {
                domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
                result = true;
                domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_USER_LOGIN_FAILURE_ACTIVE);
                result = true;
                domibusPropertyProvider.getProperty(DOMIBUS_ALERT_USER_LOGIN_FAILURE_LEVEL);
                result = "MEDIUM";
                domibusPropertyProvider.getProperty(DOMIBUS_ALERT_USER_LOGIN_FAILURE_MAIL_SUBJECT);
                this.result = mailSubject;
            }
        };
        final LoginFailureModuleConfiguration loginFailureConfiguration = new ConsoleLoginFailConfigurationReader().readConfiguration();
        assertTrue(loginFailureConfiguration.isActive());
        Alert alert = new Alert();
        alert.setAlertType(AlertType.USER_LOGIN_FAILURE);
        assertEquals(AlertLevel.MEDIUM, loginFailureConfiguration.getAlertLevel(alert));
        assertEquals(mailSubject, loginFailureConfiguration.getMailSubject());
    }

    @Test
    public void readLoginFailureConfigurationWrongAlertLevelConfig() {

        new Expectations() {
            {
                domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
                result = true;
                domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_USER_LOGIN_FAILURE_ACTIVE);
                result = true;
                domibusPropertyProvider.getProperty(DOMIBUS_ALERT_USER_LOGIN_FAILURE_LEVEL);
                result = "WHAT?";
            }
        };
        final LoginFailureModuleConfiguration loginFailureConfiguration = new ConsoleLoginFailConfigurationReader().readConfiguration();
        assertFalse(loginFailureConfiguration.isActive());
    }

//    @Test
//    public void readImminentExpirationCertificateConfigurationMainModuleDisabled() {
//        new Expectations() {{
//            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
//            result = false;
//            domibusPropertyProvider.getBooleanProperty( DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_ACTIVE);
//            result = true;
//        }};
//        final ImminentExpirationCertificateModuleConfiguration imminentExpirationCertificateConfiguration = configurationService.readImminentExpirationCertificateConfiguration();
//        assertFalse(imminentExpirationCertificateConfiguration.isActive());
//    }

//    @Test
//    public void readImminentExpirationCertificateConfigurationModuleDisabled() {
//        new Expectations() {{
//            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
//            result = true;
//            domibusPropertyProvider.getBooleanProperty( DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_ACTIVE);
//            result = false;
//        }};
//        final ImminentExpirationCertificateModuleConfiguration imminentExpirationCertificateConfiguration = configurationService.readImminentExpirationCertificateConfiguration();
//        assertFalse(imminentExpirationCertificateConfiguration.isActive());
//
//    }

//    @Test
//    public void readImminentExpirationCertificateConfigurationModuleTest() {
//
//        final String mailSubject = "Certificate imminent expiration";
//        new Expectations() {{
//            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
//            result = true;
//            domibusPropertyProvider.getBooleanProperty( DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_ACTIVE);
//            result = true;
//            domibusPropertyProvider.getIntegerProperty( DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_DELAY_DAYS);
//            result = 60;
//            domibusPropertyProvider.getIntegerProperty( DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_FREQUENCY_DAYS);
//            result = 10;
//            domibusPropertyProvider.getProperty( DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_LEVEL);
//            result = "MEDIUM";
//            domibusPropertyProvider.getProperty( DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_MAIL_SUBJECT);
//            this.result = mailSubject;
//        }};
//        final ImminentExpirationCertificateModuleConfiguration imminentExpirationCertificateConfiguration = configurationService.readImminentExpirationCertificateConfiguration();
//        assertTrue(imminentExpirationCertificateConfiguration.isActive());
//        assertEquals(mailSubject, imminentExpirationCertificateConfiguration.getMailSubject());
//        assertEquals(60, imminentExpirationCertificateConfiguration.getImminentExpirationDelay(), 0);
//        assertEquals(10, imminentExpirationCertificateConfiguration.getImminentExpirationFrequency(), 0);
//        Alert alert = new Alert();
//        alert.setAlertType(AlertType.CERT_IMMINENT_EXPIRATION);
//        assertEquals(AlertLevel.MEDIUM, imminentExpirationCertificateConfiguration.getAlertLevel(alert));
//
//    }

//    @Test
//    public void readExpiredCertificateConfigurationMainModuleInactiveTest() {
//
//        new Expectations() {{
//            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
//            result = false;
//            domibusPropertyProvider.getBooleanProperty( DOMIBUS_ALERT_CERT_EXPIRED_ACTIVE);
//            result = true;
//        }};
//        final ExpiredCertificateModuleConfiguration expiredCertificateConfiguration = configurationService.readExpiredCertificateConfiguration();
//        assertFalse(expiredCertificateConfiguration.isActive());
//    }

//    @Test
//    public void readExpiredCertificateConfigurationModuleInactiveTest() {
//
//        new Expectations() {{
//            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
//            result = true;
//            domibusPropertyProvider.getBooleanProperty( DOMIBUS_ALERT_CERT_EXPIRED_ACTIVE);
//            result = false;
//        }};
//        final ExpiredCertificateModuleConfiguration expiredCertificateConfiguration = configurationService.readExpiredCertificateConfiguration();
//        assertFalse(expiredCertificateConfiguration.isActive());
//    }

//    @Test
//    public void readExpiredCertificateConfigurationTest() {
//        final String mailSubject = "Certificate expired";
//        new Expectations() {{
//            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
//            result = true;
//            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_CERT_EXPIRED_ACTIVE);
//            result = true;
//            domibusPropertyProvider.getIntegerProperty(DOMIBUS_ALERT_CERT_EXPIRED_FREQUENCY_DAYS);
//            result = 20;
//            domibusPropertyProvider.getIntegerProperty(DOMIBUS_ALERT_CERT_EXPIRED_DURATION_DAYS);
//            result = 10;
//            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CERT_EXPIRED_LEVEL);
//            result = "LOW";
//            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CERT_EXPIRED_MAIL_SUBJECT);
//            this.result = mailSubject;
//        }};
//
//        final ExpiredCertificateModuleConfiguration expiredCertificateConfiguration = configurationService.readExpiredCertificateConfiguration();
//
//        assertTrue(expiredCertificateConfiguration.isActive());
//        assertEquals(20, expiredCertificateConfiguration.getExpiredFrequency(), 0);
//        assertEquals(10, expiredCertificateConfiguration.getExpiredDuration(), 0);
//        Alert alert = new Alert();
//        alert.setAlertType(AlertType.CERT_EXPIRED);
//        assertEquals(AlertLevel.LOW, expiredCertificateConfiguration.getAlertLevel(alert));
//        assertEquals(mailSubject, expiredCertificateConfiguration.getMailSubject());
//    }

    @Test
    public void getRepetitiveAlertConfigurationTest() {
        String property = "domibus.alert.password.expired";
        new Expectations() {
            {
                domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
                result = true;

                domibusPropertyProvider.getProperty(property + ".active");
                result = "true";

                domibusPropertyProvider.getProperty(property + ".delay_days");
                result = "15";

                domibusPropertyProvider.getProperty(property + ".frequency_days");
                result = "5";

                domibusPropertyProvider.getProperty(property + ".level");
                result = AlertLevel.MEDIUM.name();

                domibusPropertyProvider.getProperty(property + ".mail.subject");
                result = "my subjects";
            }
        };
        final PasswordExpirationAlertModuleConfiguration conf = new ConsolePasswordExpiredAlertConfigurationReader().readConfiguration();

        assertTrue(conf.isActive());
        assertEquals(15, (long) conf.getEventDelay());
        Alert a = new Alert() {{
            setAlertType(AlertType.PASSWORD_EXPIRED);
        }};
        assertEquals(AlertLevel.MEDIUM, conf.getAlertLevel(a));

    }

    @Test
    public void test_getRepetitiveAlertConfiguration_ExtAuthProviderEnabled() {
        new Expectations() {
            {
                domibusConfigurationService.isExtAuthProviderEnabled();
                result = true;
            }
        };
        final PasswordExpirationAlertModuleConfiguration conf = new ConsolePasswordExpiredAlertConfigurationReader().readConfiguration();
        assertFalse(conf.isActive());
    }

}