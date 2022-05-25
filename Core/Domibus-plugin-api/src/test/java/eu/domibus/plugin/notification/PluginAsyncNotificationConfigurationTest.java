package eu.domibus.plugin.notification;

import eu.domibus.plugin.BackendConnector;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.JMSException;
import javax.jms.Queue;

/**
 * @author Soumya Chandran
 * @since 4.2
 */
@RunWith(JMockit.class)
public class PluginAsyncNotificationConfigurationTest {

    @Tested
    PluginAsyncNotificationConfiguration pluginAsyncNotificationConfiguration;

    @Injectable
    BackendConnector backendConnector;

    @Injectable
    Queue queue;

    final String queueName = "jms";

    @Test
    public void getQueueName() throws JMSException {

        pluginAsyncNotificationConfiguration.getQueueName();

        new FullVerifications() {{
            queue.getQueueName();
        }};
    }

    @Test
    public void getQueueNameNotEmpty() throws JMSException {

        pluginAsyncNotificationConfiguration.getQueueName();

        new FullVerifications() {{
            queue.getQueueName();
        }};
    }

    @Test
    public void getBackendConnector() {

        pluginAsyncNotificationConfiguration.getBackendConnector();

        new FullVerifications() {{
        }};
    }

    @Test
    public void getBackendNotificationQueue() {

        pluginAsyncNotificationConfiguration.getBackendNotificationQueue();

        new FullVerifications() {{
        }};
    }

    @Test
    public void setQueueName() {
        pluginAsyncNotificationConfiguration.setQueueName(queueName);

        new FullVerifications() {{
        }};
    }
}