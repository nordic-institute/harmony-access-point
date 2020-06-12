package eu.domibus.core.alerts.configuration.certificate.expired;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.core.alerts.model.service.ConfigurationLoader;
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
public class ExpiredCertificateConfigurationManagerTest {

    @Tested
    ExpiredCertificateConfigurationManager configurationManager;

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    private DomainContextProvider domainContextProvider;

    @Injectable
    private ConfigurationLoader<ExpiredCertificateModuleConfiguration> loader;

    @Injectable
    AlertConfigurationService alertConfigurationService;

    @Test
    public void readExpiredCertificateConfigurationMainModuleInactiveTest() {

        new Expectations() {{
            alertConfigurationService.isAlertModuleEnabled();
            result = false;
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_CERT_EXPIRED_ACTIVE);
            result = true;
        }};
        final ExpiredCertificateModuleConfiguration expiredCertificateConfiguration = configurationManager.readConfiguration();
        assertFalse(expiredCertificateConfiguration.isActive());
    }

    @Test
    public void readExpiredCertificateConfigurationModuleInactiveTest() {

        new Expectations() {{
            alertConfigurationService.isAlertModuleEnabled();
            result = true;
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_CERT_EXPIRED_ACTIVE);
            result = false;
        }};
        final ExpiredCertificateModuleConfiguration expiredCertificateConfiguration = configurationManager.readConfiguration();
        assertFalse(expiredCertificateConfiguration.isActive());
    }

    @Test
    public void readExpiredCertificateConfigurationTest() {
        final String mailSubject = "Certificate expired";
        new Expectations() {{
            alertConfigurationService.isAlertModuleEnabled();
            result = true;
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_CERT_EXPIRED_ACTIVE);
            result = true;
            domibusPropertyProvider.getIntegerProperty(DOMIBUS_ALERT_CERT_EXPIRED_FREQUENCY_DAYS);
            result = 20;
            domibusPropertyProvider.getIntegerProperty(DOMIBUS_ALERT_CERT_EXPIRED_DURATION_DAYS);
            result = 10;
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CERT_EXPIRED_LEVEL);
            result = "LOW";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CERT_EXPIRED_MAIL_SUBJECT);
            this.result = mailSubject;
        }};

        final ExpiredCertificateModuleConfiguration expiredCertificateConfiguration = configurationManager.readConfiguration();

        assertTrue(expiredCertificateConfiguration.isActive());
        assertEquals(20, expiredCertificateConfiguration.getExpiredFrequency(), 0);
        assertEquals(10, expiredCertificateConfiguration.getExpiredDuration(), 0);
        Event event = new Event(EventType.CERT_EXPIRED);
        assertEquals(AlertLevel.LOW, expiredCertificateConfiguration.getAlertLevel(event));
        assertEquals(mailSubject, expiredCertificateConfiguration.getMailSubject());
    }

}