package eu.domibus.core.plugin.notification;

import eu.domibus.common.NotificationType;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@RunWith(JMockit.class)
public class PluginMessageReceivedNotifierTest {

    @Tested
    PluginMessageReceivedNotifier pluginMessageReceivedNotifier;

    @Test
    public void canHandle() {
        assertTrue(pluginMessageReceivedNotifier.canHandle(NotificationType.MESSAGE_RECEIVED));
    }

    @Test
    public void notifyPlugin() {
    }
}