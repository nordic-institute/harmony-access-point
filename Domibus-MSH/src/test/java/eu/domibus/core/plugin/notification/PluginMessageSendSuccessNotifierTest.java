package eu.domibus.core.plugin.notification;

import eu.domibus.common.MessageSendSuccessEvent;
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
public class PluginMessageSendSuccessNotifierTest {

    @Tested
    PluginMessageSendSuccessNotifier pluginMessageSendSuccessNotifier;

    @Injectable
    protected BackendConnectorDelegate backendConnectorDelegate;

    @Test
    public void canHandle() {
        assertTrue(pluginMessageSendSuccessNotifier.canHandle(NotificationType.MESSAGE_SEND_SUCCESS));
    }

    @Test
    public void notifyPlugin(@Injectable BackendConnector backendConnector) {
        String messageId = "123";
        Map<String, Object> properties = new HashMap<>();


        pluginMessageSendSuccessNotifier.notifyPlugin(backendConnector, messageId, properties);

        new Verifications() {{
            MessageSendSuccessEvent event = null;
            backendConnectorDelegate.messageSendSuccess(backendConnector, event = withCapture());
            assertEquals(messageId, event.getMessageId());
        }};
    }
}