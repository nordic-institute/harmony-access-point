package eu.domibus.core.alerts.service;

import eu.domibus.api.property.DomibusPropertyChangeNotifier;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.alerts.configuration.common.AlertConfigurationManager;
import eu.domibus.core.alerts.configuration.common.AlertConfigurationService;
import eu.domibus.core.alerts.configuration.common.AlertConfigurationServiceImpl;
import eu.domibus.core.alerts.configuration.common.AlertModuleConfiguration;
import eu.domibus.core.alerts.configuration.global.CommonConfigurationManager;
import eu.domibus.core.alerts.model.common.AlertType;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_ALERT_ACTIVE;

@RunWith(JMockit.class)
public class AlertConfigurationServiceImplTest {

    @Tested
    private AlertConfigurationServiceImpl alertConfigurationService;


    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    List<AlertConfigurationManager> alertConfigurationManagers;

    @Injectable
    private CommonConfigurationManager commonConfigurationManager;

    @Injectable
    ApplicationContext applicationContext;

    @Injectable
    DomibusPropertyChangeNotifier domibusPropertyChangeNotifier;

    @Test
    public void resetAll(@Mocked AlertConfigurationManager alertConfigurationManager) {
        new Expectations(alertConfigurationService) {{
            alertConfigurationService.getConfigurationManager((AlertType) any);
            result = alertConfigurationManager;
        }};

        alertConfigurationService.resetAll();

        new Verifications() {{
            commonConfigurationManager.reset();
            alertConfigurationService.getConfigurationManager((AlertType) any).reset();
            times = AlertType.values().length;
        }};
    }

    @Test
    public void getMailSubject(@Mocked AlertType alertType, @Mocked AlertModuleConfiguration alertModuleConfiguration) {
        new Expectations(alertConfigurationService) {{
            alertConfigurationService.getConfiguration(alertType);
            result = alertModuleConfiguration;
            alertModuleConfiguration.getMailSubject();
            result = "email subject";
        }};

        String res = alertConfigurationService.getMailSubject(alertType);

        Assert.assertTrue(res.equals("email subject"));
    }

    @Test
    public void isAlertModuleEnabled() {
        new Expectations(alertConfigurationService) {{
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
            result = true;
        }};

        boolean res = alertConfigurationService.isAlertModuleEnabled();

        Assert.assertTrue(res);
    }

    @Test
    public void getModuleConfiguration(@Mocked AlertType alertType,
                                       @Mocked AlertConfigurationManager alertConfigurationManager,
                                       @Mocked AlertModuleConfiguration alertModuleConfiguration) {

        new Expectations(alertConfigurationService) {{
            alertConfigurationService.getConfigurationManager(alertType);
            result = alertConfigurationManager;
            alertConfigurationManager.getConfiguration();
            result = alertModuleConfiguration;
        }};

        AlertModuleConfiguration res = alertConfigurationService.getConfiguration(alertType);

        Assert.assertTrue(res == alertModuleConfiguration);
    }

//    @Test
//    public void getModuleConfigurationManager(@Mocked AlertType alertType1, @Mocked AlertType alertType2, @Mocked AlertType alertType3,
//                                              @Mocked AlertConfigurationManager alertConfigurationManager1,
//                                              @Mocked AlertConfigurationManager alertConfigurationManager2) {
//        configurationService.alertConfigurationManagers = Arrays.asList(alertConfigurationManager2, alertConfigurationManager1);
//        new Expectations(configurationService) {{
//            alertConfigurationManager1.getAlertType();
//            result = alertType1;
//            alertConfigurationManager2.getAlertType();
//            result = alertType2;
//        }};
//
//        AlertConfigurationManager res = configurationService.getModuleConfigurationManager(alertType1);
//        Assert.assertTrue(res == alertConfigurationManager1);
//
//        AlertConfigurationManager res3 = configurationService.getModuleConfigurationManager(alertType3);
//        Assert.assertNull(res3);
//    }
}
