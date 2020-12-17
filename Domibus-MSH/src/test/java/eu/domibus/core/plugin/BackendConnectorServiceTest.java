package eu.domibus.core.plugin;

import eu.domibus.core.plugin.notification.AsyncNotificationConfigurationService;
import eu.domibus.plugin.AbstractBackendConnector;
import eu.domibus.plugin.BackendConnector;
import eu.domibus.plugin.NotificationListenerService;
import eu.domibus.plugin.notification.AsyncNotificationConfiguration;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@RunWith(JMockit.class)
public class BackendConnectorServiceTest {

    @Tested
    BackendConnectorService backendConnectorService;

    @Injectable
    protected BackendConnectorProvider backendConnectorProvider;

    @Injectable
    protected AsyncNotificationConfigurationService asyncNotificationConfigurationService;

    @Test
    public void getRequiredNotificationTypeList(@Injectable BackendConnector<?, ?> backendConnector,
                                                @Injectable NotificationListenerService notificationListenerService) {
        new Expectations(backendConnectorService) {{
            asyncNotificationConfigurationService.getAsyncPluginConfiguration(backendConnector.getName());
            result = notificationListenerService;

            backendConnectorService.isInstanceOfNotificationListener(notificationListenerService);
            result = true;
        }};

        backendConnectorService.getRequiredNotificationTypeList(backendConnector);

        new Verifications() {{
            notificationListenerService.getRequiredNotificationTypeList();

            backendConnector.getRequiredNotifications();
            times = 0;
        }};
    }

    @Test
    public void isInstanceOfNotificationListener(@Injectable NotificationListenerService notificationListenerService) {
        assertTrue(backendConnectorService.isInstanceOfNotificationListener(notificationListenerService));
    }

    @Test
    public void isInstanceOfNotificationListener(@Injectable AsyncNotificationConfiguration asyncNotificationConfiguration) {
        assertFalse(backendConnectorService.isInstanceOfNotificationListener(asyncNotificationConfiguration));
    }

    @Test
    public void isAbstractBackendConnector(@Injectable AbstractBackendConnector abstractBackendConnector) {
        assertTrue(backendConnectorService.isAbstractBackendConnector(abstractBackendConnector));
    }

    @Test
    public void isAbstractBackendConnector(@Injectable BackendConnector backendConnector) {
        assertFalse(backendConnectorService.isAbstractBackendConnector(backendConnector));
    }

    @Test
    public void isListerAnInstanceOfAsyncPluginConfiguration(@Injectable AbstractBackendConnector abstractBackendConnector,
                                                             @Injectable NotificationListenerService notificationListenerService) {
        new Expectations(backendConnectorService) {{
            backendConnectorService.isAbstractBackendConnector(abstractBackendConnector);
            result = true;

            abstractBackendConnector.getLister();
            result = notificationListenerService;
        }};

        assertTrue(backendConnectorService.isListerAnInstanceOfAsyncPluginConfiguration(abstractBackendConnector));
    }
}