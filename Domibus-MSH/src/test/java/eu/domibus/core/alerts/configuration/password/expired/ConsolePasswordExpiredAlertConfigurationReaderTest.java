package eu.domibus.core.alerts.configuration.password.expired;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.alerts.configuration.password.PasswordExpirationAlertModuleConfiguration;
import eu.domibus.core.alerts.configuration.password.expired.console.ConsolePasswordExpiredAlertConfigurationReader;
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

import static org.junit.Assert.*;

@RunWith(JMockit.class)
public class ConsolePasswordExpiredAlertConfigurationReaderTest {

    @Tested
    ConsolePasswordExpiredAlertConfigurationReader configurationManager;

    @Injectable
    private DomibusConfigurationService domibusConfigurationService;

    @Injectable
    private DomainContextProvider domainContextProvider;

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    AlertConfigurationService alertConfigurationService;

    @Test
    public void getRepetitiveAlertConfigurationTest() {
        String property = "domibus.alert.password.expired";
        new Expectations() {
            {
                alertConfigurationService.isAlertModuleEnabled();
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
        final PasswordExpirationAlertModuleConfiguration conf = configurationManager.readConfiguration();

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
        final PasswordExpirationAlertModuleConfiguration conf = configurationManager.readConfiguration();
        assertFalse(conf.isActive());
    }

}