package eu.domibus.core.plugin.notification;

import eu.domibus.common.MessageStatus;
import eu.domibus.common.MessageStatusChangeEvent;
import eu.domibus.common.NotificationType;
import eu.domibus.core.plugin.delegate.BackendConnectorDelegate;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.plugin.BackendConnector;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.Tested;
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
public class PluginMessageStatusChangeNotifierTest {

    @Tested
    PluginMessageStatusChangeNotifier pluginMessageStatusChangeNotifier;

    @Injectable
    protected BackendConnectorDelegate backendConnectorDelegate;

    @Test
    public void canHandle() {
        assertTrue(pluginMessageStatusChangeNotifier.canHandle(NotificationType.MESSAGE_STATUS_CHANGE));
    }

    @Test
    public void notifyPlugin(@Injectable BackendConnector<?,?> backendConnector) {
        String messageId = "123";
        Map<String, String> properties = new HashMap<>();

        String finalRecipient = "finalRecipient";
        String originalSender = "originalSender";
        String service = "myservice";
        String endpoint = "myendpoint";
        String action = "myaction";
        String serviceType = "servicetype";
        properties.put(MessageConstants.FINAL_RECIPIENT, finalRecipient);
        properties.put(MessageConstants.ORIGINAL_SENDER, originalSender);
        properties.put(MessageConstants.SERVICE, service);
        properties.put(MessageConstants.SERVICE_TYPE, serviceType);
        properties.put(MessageConstants.ACTION, action);
        properties.put(MessageConstants.ENDPOINT, endpoint);
        properties.put(MessageConstants.STATUS_FROM,  MessageStatus.DOWNLOADED.toString());
        properties.put(MessageConstants.STATUS_TO, MessageStatus.ACKNOWLEDGED.toString());
        properties.put(MessageConstants.CHANGE_TIMESTAMP, String.valueOf(System.currentTimeMillis()));

        pluginMessageStatusChangeNotifier.notifyPlugin(backendConnector, messageId, properties);

        new FullVerifications() {{
            MessageStatusChangeEvent event;
            backendConnectorDelegate.messageStatusChanged(backendConnector, event = withCapture());
            assertEquals(service, event.getProps().get(MessageConstants.SERVICE));
            assertEquals(serviceType, event.getProps().get(MessageConstants.SERVICE_TYPE));
            assertEquals(action, event.getProps().get(MessageConstants.ACTION));
            assertEquals(finalRecipient, event.getProps().get(MessageConstants.FINAL_RECIPIENT));
            assertEquals(originalSender, event.getProps().get(MessageConstants.ORIGINAL_SENDER));
        }};
    }
}