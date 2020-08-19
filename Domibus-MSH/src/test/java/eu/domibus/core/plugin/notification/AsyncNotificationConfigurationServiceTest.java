package eu.domibus.core.plugin.notification;

import eu.domibus.core.plugin.routing.RoutingService;
import eu.domibus.plugin.NotificationListener;
import eu.domibus.plugin.notification.AsyncNotificationConfiguration;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Cosmin Baciu
 * @since
 */
public class AsyncNotificationConfigurationServiceTest {

    @Tested
    AsyncNotificationConfigurationService asyncNotificationConfigurationService;

    @Test
    public void testGetNotificationListener(@Injectable final AsyncNotificationConfiguration notificationListener1,
                                            @Injectable final AsyncNotificationConfiguration notificationListener2) {
        final String backendName = "customPlugin";
        AsyncNotificationConfigurationService routingService = new AsyncNotificationConfigurationService();
        new Expectations(routingService) {{
            routingService.matches(notificationListener1, backendName);
            result = true;
        }};

        List<AsyncNotificationConfiguration> notificationListeners = new ArrayList<>();
        notificationListeners.add(notificationListener1);
        notificationListeners.add(notificationListener2);
        routingService.asyncNotificationConfigurations = notificationListeners;

        AsyncNotificationConfiguration notificationListener = routingService.getAsyncPluginConfiguration(backendName);
        assertEquals(notificationListener1, notificationListener);

    }

    @Test
    public void testGetNotificationListener_empty(@Injectable final NotificationListener notificationListener1,
                                                  @Injectable final NotificationListener notificationListener2) {
        AsyncNotificationConfigurationService routingService = new AsyncNotificationConfigurationService();
        final String backendName = "customPlugin";

        routingService.asyncNotificationConfigurations = new ArrayList<>();

        AsyncNotificationConfiguration notificationListener = routingService.getAsyncPluginConfiguration(backendName);
        assertNull(notificationListener);

    }

}