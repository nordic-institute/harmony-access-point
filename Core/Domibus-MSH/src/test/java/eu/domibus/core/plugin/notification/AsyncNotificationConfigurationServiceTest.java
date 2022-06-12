package eu.domibus.core.plugin.notification;

import eu.domibus.plugin.notification.AsyncNotificationConfiguration;
import mockit.Expectations;
import mockit.Injectable;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
public class AsyncNotificationConfigurationServiceTest {

    @Test
    public void testGetNotificationListener(@Injectable final AsyncNotificationConfiguration notificationListener1,
                                            @Injectable final AsyncNotificationConfiguration notificationListener2) {
        final String backendName = "customPlugin";
        AsyncNotificationConfigurationService asyncNotificationConfigurationService = new AsyncNotificationConfigurationService();
        new Expectations(asyncNotificationConfigurationService) {{
            asyncNotificationConfigurationService.matches(notificationListener1, backendName);
            result = true;
        }};

        List<AsyncNotificationConfiguration> notificationListeners = new ArrayList<>();
        notificationListeners.add(notificationListener1);
        notificationListeners.add(notificationListener2);
        asyncNotificationConfigurationService.asyncNotificationConfigurations = notificationListeners;

        AsyncNotificationConfiguration notificationListener = asyncNotificationConfigurationService.getAsyncPluginConfiguration(backendName);
        assertEquals(notificationListener1, notificationListener);

    }

    @Test
    public void testGetNotificationListener_empty() {
        AsyncNotificationConfigurationService asyncNotificationConfigurationService = new AsyncNotificationConfigurationService();
        final String backendName = "customPlugin";

        asyncNotificationConfigurationService.asyncNotificationConfigurations = new ArrayList<>();

        AsyncNotificationConfiguration notificationListener = asyncNotificationConfigurationService.getAsyncPluginConfiguration(backendName);
        assertNull(notificationListener);

    }

}