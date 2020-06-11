package eu.domibus.core.alerts.configuration.certificate.imminent;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.alerts.configuration.certificate.imminent.ImminentExpirationCertificateConfigurationManager;
import eu.domibus.core.alerts.configuration.certificate.imminent.ImminentExpirationCertificateModuleConfiguration;
import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.service.Alert;
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
public class ImminentExpirationCertificateConfigurationManagerTest {

    @Tested
    ImminentExpirationCertificateConfigurationManager configurationManager;

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    private DomainContextProvider domainContextProvider;

    @Injectable
    private ConfigurationLoader<ImminentExpirationCertificateModuleConfiguration> loader;

    @Injectable
    AlertConfigurationService alertConfigurationService;

    @Test
    public void getAlertType() {
        AlertType res = configurationManager.getAlertType();
        assertEquals(res, AlertType.CERT_IMMINENT_EXPIRATION);
    }

    @Test
    public void getConfiguration(@Mocked ImminentExpirationCertificateModuleConfiguration configuration) {
        new Expectations() {{
            loader.getConfiguration((ConfigurationReader<ImminentExpirationCertificateModuleConfiguration>) any);
            result = configuration;
        }};
        ImminentExpirationCertificateModuleConfiguration res = configurationManager.getConfiguration();
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
    public void readImminentExpirationCertificateConfigurationMainModuleDisabled() {
        new Expectations() {{
            alertConfigurationService.isAlertModuleEnabled();
            result = false;
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_ACTIVE);
            result = true;
        }};
        final ImminentExpirationCertificateModuleConfiguration imminentExpirationCertificateConfiguration = configurationManager.readConfiguration();
        assertFalse(imminentExpirationCertificateConfiguration.isActive());
    }

    @Test
    public void readImminentExpirationCertificateConfigurationModuleDisabled() {
        new Expectations() {{
            alertConfigurationService.isAlertModuleEnabled();
            result = true;
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_ACTIVE);
            result = false;
        }};
        final ImminentExpirationCertificateModuleConfiguration imminentExpirationCertificateConfiguration = configurationManager.readConfiguration();
        assertFalse(imminentExpirationCertificateConfiguration.isActive());

    }

    @Test
    public void readImminentExpirationCertificateConfigurationModuleTest() {

        final String mailSubject = "Certificate imminent expiration";
        new Expectations() {{
            alertConfigurationService.isAlertModuleEnabled();
            result = true;
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_ACTIVE);
            result = true;
            domibusPropertyProvider.getIntegerProperty(DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_DELAY_DAYS);
            result = 60;
            domibusPropertyProvider.getIntegerProperty(DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_FREQUENCY_DAYS);
            result = 10;
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_LEVEL);
            result = "MEDIUM";
            domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_MAIL_SUBJECT);
            this.result = mailSubject;
        }};
        final ImminentExpirationCertificateModuleConfiguration imminentExpirationCertificateConfiguration = configurationManager.readConfiguration();
        assertTrue(imminentExpirationCertificateConfiguration.isActive());
        assertEquals(mailSubject, imminentExpirationCertificateConfiguration.getMailSubject());
        assertEquals(60, imminentExpirationCertificateConfiguration.getImminentExpirationDelay(), 0);
        assertEquals(10, imminentExpirationCertificateConfiguration.getImminentExpirationFrequency(), 0);
        Alert alert = new Alert();
        alert.setAlertType(AlertType.CERT_IMMINENT_EXPIRATION);
        assertEquals(AlertLevel.MEDIUM, imminentExpirationCertificateConfiguration.getAlertLevel(alert));

    }
}