//package eu.domibus.core.alerts.service;
//
//import eu.domibus.api.multitenancy.DomainContextProvider;
//import eu.domibus.api.property.DomibusConfigurationService;
//import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
//import eu.domibus.api.property.DomibusPropertyProvider;
//import eu.domibus.core.alerts.configuration.AlertConfigurationManager;
//import eu.domibus.core.alerts.configuration.AlertModuleConfigurationBase;
//import eu.domibus.core.alerts.configuration.account.disabled.*;
//import eu.domibus.core.alerts.configuration.account.enabled.ConsoleAccountEnabledConfigurationManager;
//import eu.domibus.core.alerts.configuration.account.enabled.ConsoleAccountEnabledConfigurationReader;
//import eu.domibus.core.alerts.configuration.account.enabled.PluginAccountEnabledConfigurationManager;
//import eu.domibus.core.alerts.configuration.account.enabled.PluginAccountEnabledConfigurationReader;
//import eu.domibus.core.alerts.configuration.certificate.ExpiredCertificateConfigurationManager;
//import eu.domibus.core.alerts.configuration.certificate.ExpiredCertificateModuleConfiguration;
//import eu.domibus.core.alerts.configuration.certificate.ImminentExpirationCertificateConfigurationManager;
//import eu.domibus.core.alerts.configuration.certificate.ImminentExpirationCertificateModuleConfiguration;
//import eu.domibus.core.alerts.configuration.common.CommonConfigurationManager;
//import eu.domibus.core.alerts.configuration.login.ConsoleLoginFailConfigurationManager;
//import eu.domibus.core.alerts.configuration.login.ConsoleLoginFailConfigurationReader;
//import eu.domibus.core.alerts.configuration.login.LoginFailureModuleConfiguration;
//import eu.domibus.core.alerts.configuration.login.PluginLoginFailConfigurationManager;
//import eu.domibus.core.alerts.configuration.messaging.MessagingConfigurationManager;
//import eu.domibus.core.alerts.configuration.messaging.MessagingModuleConfiguration;
//import eu.domibus.core.alerts.configuration.password.expired.ConsolePasswordExpiredAlertConfigurationReader;
//import eu.domibus.core.alerts.configuration.password.PasswordExpirationAlertModuleConfiguration;
//import eu.domibus.core.alerts.model.common.AlertLevel;
//import eu.domibus.core.alerts.model.common.AlertType;
//import eu.domibus.core.alerts.model.service.*;
//import mockit.*;
//import mockit.integration.junit4.JMockit;
//import org.junit.Assert;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.util.List;
//
//import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;
//import static org.junit.Assert.*;
//
///**
// * @author Thomas Dussart
// * @since 4.0
// */
//@SuppressWarnings("ResultOfMethodCallIgnored")
//@RunWith(JMockit.class)
//public class AlertModuleConfigurationServiceImplTest {
//    @Tested
//    private AlertConfigurationService configurationService;
//
// 
//    @Test
//    public void readLoginFailureConfigurationMainModuleInactive() {
//
//        new Expectations() {
//            {
//                domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
//                result = false;
//                domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_USER_LOGIN_FAILURE_ACTIVE);
//                result = true;
//            }
//        };
//        final LoginFailureModuleConfiguration loginFailureConfiguration = new ConsoleLoginFailConfigurationReader().readConfiguration();
//        assertFalse(loginFailureConfiguration.isActive());
//    }
//
//    @Test
//    public void readLoginFailureConfigurationModuleInactive() {
//        new Expectations() {
//            {
//                domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
//                result = true;
//                domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_USER_LOGIN_FAILURE_ACTIVE);
//                result = false;
//            }
//        };
//        final LoginFailureModuleConfiguration loginFailureConfiguration = new ConsoleLoginFailConfigurationReader().readConfiguration();
//        assertFalse(loginFailureConfiguration.isActive());
//    }
//
//    @Test
//    public void test_readLoginFailureConfigurationExtAuthProviderEnabled() {
//        new Expectations() {
//            {
//                domibusConfigurationService.isExtAuthProviderEnabled();
//                result = true;
//            }
//        };
//        final LoginFailureModuleConfiguration loginFailureConfiguration = new ConsoleLoginFailConfigurationReader().readConfiguration();
//        assertFalse(loginFailureConfiguration.isActive());
//    }
//
//
//    @Test
//    public void readLoginFailureConfiguration() {
//        final String mailSubject = "Login failure";
//        new Expectations() {
//            {
//                domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
//                result = true;
//                domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_USER_LOGIN_FAILURE_ACTIVE);
//                result = true;
//                domibusPropertyProvider.getProperty(DOMIBUS_ALERT_USER_LOGIN_FAILURE_LEVEL);
//                result = "MEDIUM";
//                domibusPropertyProvider.getProperty(DOMIBUS_ALERT_USER_LOGIN_FAILURE_MAIL_SUBJECT);
//                this.result = mailSubject;
//            }
//        };
//        final LoginFailureModuleConfiguration loginFailureConfiguration = new ConsoleLoginFailConfigurationReader().readConfiguration();
//        assertTrue(loginFailureConfiguration.isActive());
//        Alert alert = new Alert();
//        alert.setAlertType(AlertType.USER_LOGIN_FAILURE);
//        assertEquals(AlertLevel.MEDIUM, loginFailureConfiguration.getAlertLevel(alert));
//        assertEquals(mailSubject, loginFailureConfiguration.getMailSubject());
//    }
//
//    @Test
//    public void readLoginFailureConfigurationWrongAlertLevelConfig() {
//
//        new Expectations() {
//            {
//                domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
//                result = true;
//                domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_USER_LOGIN_FAILURE_ACTIVE);
//                result = true;
//                domibusPropertyProvider.getProperty(DOMIBUS_ALERT_USER_LOGIN_FAILURE_LEVEL);
//                result = "WHAT?";
//            }
//        };
//        final LoginFailureModuleConfiguration loginFailureConfiguration = new ConsoleLoginFailConfigurationReader().readConfiguration();
//        assertFalse(loginFailureConfiguration.isActive());
//    }
//
////    @Test
////    public void readImminentExpirationCertificateConfigurationMainModuleDisabled() {
////        new Expectations() {{
////            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
////            result = false;
////            domibusPropertyProvider.getBooleanProperty( DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_ACTIVE);
////            result = true;
////        }};
////        final ImminentExpirationCertificateModuleConfiguration imminentExpirationCertificateConfiguration = configurationService.readImminentExpirationCertificateConfiguration();
////        assertFalse(imminentExpirationCertificateConfiguration.isActive());
////    }
//
////    @Test
////    public void readImminentExpirationCertificateConfigurationModuleDisabled() {
////        new Expectations() {{
////            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
////            result = true;
////            domibusPropertyProvider.getBooleanProperty( DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_ACTIVE);
////            result = false;
////        }};
////        final ImminentExpirationCertificateModuleConfiguration imminentExpirationCertificateConfiguration = configurationService.readImminentExpirationCertificateConfiguration();
////        assertFalse(imminentExpirationCertificateConfiguration.isActive());
////
////    }
//
////    @Test
////    public void readImminentExpirationCertificateConfigurationModuleTest() {
////
////        final String mailSubject = "Certificate imminent expiration";
////        new Expectations() {{
////            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
////            result = true;
////            domibusPropertyProvider.getBooleanProperty( DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_ACTIVE);
////            result = true;
////            domibusPropertyProvider.getIntegerProperty( DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_DELAY_DAYS);
////            result = 60;
////            domibusPropertyProvider.getIntegerProperty( DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_FREQUENCY_DAYS);
////            result = 10;
////            domibusPropertyProvider.getProperty( DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_LEVEL);
////            result = "MEDIUM";
////            domibusPropertyProvider.getProperty( DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_MAIL_SUBJECT);
////            this.result = mailSubject;
////        }};
////        final ImminentExpirationCertificateModuleConfiguration imminentExpirationCertificateConfiguration = configurationService.readImminentExpirationCertificateConfiguration();
////        assertTrue(imminentExpirationCertificateConfiguration.isActive());
////        assertEquals(mailSubject, imminentExpirationCertificateConfiguration.getMailSubject());
////        assertEquals(60, imminentExpirationCertificateConfiguration.getImminentExpirationDelay(), 0);
////        assertEquals(10, imminentExpirationCertificateConfiguration.getImminentExpirationFrequency(), 0);
////        Alert alert = new Alert();
////        alert.setAlertType(AlertType.CERT_IMMINENT_EXPIRATION);
////        assertEquals(AlertLevel.MEDIUM, imminentExpirationCertificateConfiguration.getAlertLevel(alert));
////
////    }
//
////    @Test
////    public void readExpiredCertificateConfigurationMainModuleInactiveTest() {
////
////        new Expectations() {{
////            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
////            result = false;
////            domibusPropertyProvider.getBooleanProperty( DOMIBUS_ALERT_CERT_EXPIRED_ACTIVE);
////            result = true;
////        }};
////        final ExpiredCertificateModuleConfiguration expiredCertificateConfiguration = configurationService.readExpiredCertificateConfiguration();
////        assertFalse(expiredCertificateConfiguration.isActive());
////    }
//
////    @Test
////    public void readExpiredCertificateConfigurationModuleInactiveTest() {
////
////        new Expectations() {{
////            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
////            result = true;
////            domibusPropertyProvider.getBooleanProperty( DOMIBUS_ALERT_CERT_EXPIRED_ACTIVE);
////            result = false;
////        }};
////        final ExpiredCertificateModuleConfiguration expiredCertificateConfiguration = configurationService.readExpiredCertificateConfiguration();
////        assertFalse(expiredCertificateConfiguration.isActive());
////    }
//
////    @Test
////    public void readExpiredCertificateConfigurationTest() {
////        final String mailSubject = "Certificate expired";
////        new Expectations() {{
////            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
////            result = true;
////            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_CERT_EXPIRED_ACTIVE);
////            result = true;
////            domibusPropertyProvider.getIntegerProperty(DOMIBUS_ALERT_CERT_EXPIRED_FREQUENCY_DAYS);
////            result = 20;
////            domibusPropertyProvider.getIntegerProperty(DOMIBUS_ALERT_CERT_EXPIRED_DURATION_DAYS);
////            result = 10;
////            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CERT_EXPIRED_LEVEL);
////            result = "LOW";
////            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CERT_EXPIRED_MAIL_SUBJECT);
////            this.result = mailSubject;
////        }};
////
////        final ExpiredCertificateModuleConfiguration expiredCertificateConfiguration = configurationService.readExpiredCertificateConfiguration();
////
////        assertTrue(expiredCertificateConfiguration.isActive());
////        assertEquals(20, expiredCertificateConfiguration.getExpiredFrequency(), 0);
////        assertEquals(10, expiredCertificateConfiguration.getExpiredDuration(), 0);
////        Alert alert = new Alert();
////        alert.setAlertType(AlertType.CERT_EXPIRED);
////        assertEquals(AlertLevel.LOW, expiredCertificateConfiguration.getAlertLevel(alert));
////        assertEquals(mailSubject, expiredCertificateConfiguration.getMailSubject());
////    }
//
//    @Test
//    public void getRepetitiveAlertConfigurationTest() {
//        String property = "domibus.alert.password.expired";
//        new Expectations() {
//            {
//                domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
//                result = true;
//
//                domibusPropertyProvider.getProperty(property + ".active");
//                result = "true";
//
//                domibusPropertyProvider.getProperty(property + ".delay_days");
//                result = "15";
//
//                domibusPropertyProvider.getProperty(property + ".frequency_days");
//                result = "5";
//
//                domibusPropertyProvider.getProperty(property + ".level");
//                result = AlertLevel.MEDIUM.name();
//
//                domibusPropertyProvider.getProperty(property + ".mail.subject");
//                result = "my subjects";
//            }
//        };
//        final PasswordExpirationAlertModuleConfiguration conf = new ConsolePasswordExpiredAlertConfigurationReader().readConfiguration();
//
//        assertTrue(conf.isActive());
//        assertEquals(15, (long) conf.getEventDelay());
//        Alert a = new Alert() {{
//            setAlertType(AlertType.PASSWORD_EXPIRED);
//        }};
//        assertEquals(AlertLevel.MEDIUM, conf.getAlertLevel(a));
//
//    }
//
//    @Test
//    public void test_getRepetitiveAlertConfiguration_ExtAuthProviderEnabled() {
//        new Expectations() {
//            {
//                domibusConfigurationService.isExtAuthProviderEnabled();
//                result = true;
//            }
//        };
//        final PasswordExpirationAlertModuleConfiguration conf = new ConsolePasswordExpiredAlertConfigurationReader().readConfiguration();
//        assertFalse(conf.isActive());
//    }
//
//}