package eu.domibus.core.plugin.notification;

import eu.domibus.common.MessageStatus;
import eu.domibus.common.MessageStatusChangeEvent;
import eu.domibus.common.NotificationType;
import eu.domibus.core.plugin.delegate.BackendConnectorDelegate;
import eu.domibus.messaging.MessageConstants;
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
    public void notifyPlugin(@Injectable BackendConnector backendConnector) {
        String messageId = "123";
        Map<String, String> properties = new HashMap<>();

        String service = "myservice";
        String endpoint = "myendpoint";
        String action = "myaction";
        String serviceType = "servicetype";
        properties.put(MessageConstants.SERVICE, service);
        properties.put(MessageConstants.SERVICE_TYPE, serviceType);
        properties.put(MessageConstants.ACTION, action);
        properties.put(MessageConstants.ENDPOINT, endpoint);
        properties.put(MessageConstants.STATUS_TO, MessageStatus.ACKNOWLEDGED.toString());
        properties.put(MessageConstants.CHANGE_TIMESTAMP, String.valueOf(System.currentTimeMillis()));

        pluginMessageStatusChangeNotifier.notifyPlugin(backendConnector, messageId, properties);

        new Verifications() {{
            MessageStatusChangeEvent event = null;
            backendConnectorDelegate.messageStatusChanged(backendConnector, event = withCapture());
            assertEquals(service, event.getProperties().get(MessageConstants.SERVICE));
            assertEquals(serviceType, event.getProperties().get(MessageConstants.SERVICE_TYPE));
        }};
    }
}