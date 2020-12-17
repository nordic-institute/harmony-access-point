package eu.domibus.core.plugin.notification;

import eu.domibus.common.DeliverMessageEvent;
import eu.domibus.common.NotificationType;
import eu.domibus.core.plugin.delegate.BackendConnectorDelegate;
import eu.domibus.plugin.BackendConnector;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@RunWith(JMockit.class)
public class PluginMessageReceivedNotifierTest {

    @Tested
    PluginMessageReceivedNotifier pluginMessageReceivedNotifier;

    @Injectable
    protected BackendConnectorDelegate backendConnectorDelegate;


    @Test
    public void canHandle() {
        assertTrue(pluginMessageReceivedNotifier.canHandle(NotificationType.MESSAGE_RECEIVED));
    }

    @Test
    public void notifyPlugin(@Injectable BackendConnector backendConnector) {
        String messageId = "123";
        Map<String, String> properties = new HashMap<>();


        pluginMessageReceivedNotifier.notifyPlugin(backendConnector, messageId, properties);

        new Verifications() {{
            DeliverMessageEvent event = null;
            backendConnectorDelegate.deliverMessage(backendConnector, event = withCapture());
            assertEquals(messageId, event.getMessageId());
        }};
    }
}