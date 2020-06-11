package eu.domibus.core.alerts.service;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.alerts.configuration.AlertConfigurationManager;
import eu.domibus.core.alerts.configuration.AlertModuleConfiguration;
import eu.domibus.core.alerts.configuration.common.CommonConfigurationManager;
import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.service.Alert;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_ALERT_ACTIVE;

@RunWith(JMockit.class)
public class AlertConfigurationServiceImplTest {

    @Tested
    private AlertConfigurationServiceImpl configurationService;


    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    List<AlertConfigurationManager> alertConfigurationManagers;

    @Injectable
    private CommonConfigurationManager commonConfigurationManager;

    @Test
    public void resetAll(@Mocked AlertConfigurationManager alertConfigurationManager) {
        new Expectations(configurationService) {{
            configurationService.getModuleConfigurationManager((AlertType) any);
            result = alertConfigurationManager;
        }};

        configurationService.resetAll();

        new Verifications() {{
            commonConfigurationManager.reset();
            configurationService.getModuleConfigurationManager((AlertType) any).reset();
            times = AlertType.values().length;
        }};
    }

    @Test
    public void getAlertLevel(@Mocked Alert alert, @Mocked AlertModuleConfiguration alertModuleConfiguration) {
        new Expectations(configurationService) {{
            alert.getAlertType();
            result = AlertType.MSG_STATUS_CHANGED;
            configurationService.getModuleConfiguration(AlertType.MSG_STATUS_CHANGED);
            result = alertModuleConfiguration;
            alertModuleConfiguration.getAlertLevel(alert);
            result = AlertLevel.HIGH;
        }};

        AlertLevel res = configurationService.getAlertLevel(alert);

        Assert.assertTrue(res == AlertLevel.HIGH);
    }

    @Test
    public void getMailSubject(@Mocked AlertType alertType, @Mocked AlertModuleConfiguration alertModuleConfiguration) {
        new Expectations(configurationService) {{
            configurationService.getModuleConfiguration(alertType);
            result = alertModuleConfiguration;
            alertModuleConfiguration.getMailSubject();
            result = "email subject";
        }};

        String res = configurationService.getMailSubject(alertType);

        Assert.assertTrue(res.equals("email subject"));
    }

    @Test
    public void isAlertModuleEnabled() {
        new Expectations(configurationService) {{
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
            result = true;
        }};

        boolean res = configurationService.isAlertModuleEnabled();

        Assert.assertTrue(res);
    }

    @Test
    public void getModuleConfiguration(@Mocked AlertType alertType,
                                       @Mocked AlertConfigurationManager alertConfigurationManager,
                                       @Mocked AlertModuleConfiguration alertModuleConfiguration) {

        new Expectations(configurationService) {{
            configurationService.getModuleConfigurationManager(alertType);
            result = alertConfigurationManager;
            alertConfigurationManager.getConfiguration();
            result = alertModuleConfiguration;
        }};

        AlertModuleConfiguration res = configurationService.getModuleConfiguration(alertType);

        Assert.assertTrue(res == alertModuleConfiguration);
    }

    @Test
    public void getModuleConfigurationManager(@Mocked AlertType alertType1, @Mocked AlertType alertType2, @Mocked AlertType alertType3,
                                              @Mocked AlertConfigurationManager alertConfigurationManager1,
                                              @Mocked AlertConfigurationManager alertConfigurationManager2) {
        configurationService.alertConfigurationManagers = Arrays.asList(alertConfigurationManager2, alertConfigurationManager1);
        new Expectations(configurationService) {{
            alertConfigurationManager1.getAlertType();
            result = alertType1;
            alertConfigurationManager2.getAlertType();
            result = alertType2;
        }};

        AlertConfigurationManager res = configurationService.getModuleConfigurationManager(alertType1);
        Assert.assertTrue(res == alertConfigurationManager1);

        try {
            AlertConfigurationManager res2 = configurationService.getModuleConfigurationManager(alertType3);
            Assert.fail();
        } catch (IllegalArgumentException ex) {
        }
    }
}
