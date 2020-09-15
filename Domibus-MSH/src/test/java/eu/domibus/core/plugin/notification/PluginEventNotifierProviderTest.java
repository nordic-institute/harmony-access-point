package eu.domibus.core.plugin.notification;

import eu.domibus.common.NotificationType;
import mockit.Expectations;
import mockit.Injectable;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@RunWith(JMockit.class)
public class PluginEventNotifierProviderTest {

    @Test
    public void getPluginEventNotifier(@Injectable PluginEventNotifier pluginEventNotifier1,
                                       @Injectable PluginEventNotifier pluginEventNotifier2) {

        NotificationType notificationType = NotificationType.MESSAGE_RECEIVED;
        List<PluginEventNotifier> list = new ArrayList<>();
        list.add(pluginEventNotifier1);
        list.add(pluginEventNotifier2);

        PluginEventNotifierProvider pluginEventNotifierProvider = new PluginEventNotifierProvider(list);


        new Expectations() {{
            pluginEventNotifier1.canHandle(notificationType);
            result = false;

            pluginEventNotifier2.canHandle(notificationType);
            result = true;
        }};

        PluginEventNotifier pluginEventNotifier = pluginEventNotifierProvider.getPluginEventNotifier(notificationType);
        assertEquals(pluginEventNotifier2, pluginEventNotifier);
    }
}