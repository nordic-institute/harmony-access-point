package eu.domibus.plugin.ws.backend.reliability.queue;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.environment.TomcatCondition;
import eu.domibus.plugin.ws.property.WSPluginPropertyManager;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import static eu.domibus.plugin.ws.backend.reliability.queue.WSMessageListenerContainerConfiguration.WS_PLUGIN_SEND_QUEUE;

/**
 * Class responsible for the configuration of the plugin for Tomcat
 *
 * @author Cosmin Baciu
 * @since 4.2
 */
@Conditional(TomcatCondition.class)
@Configuration
public class WSBackendTomcatConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSBackendTomcatConfiguration.class);

    @Bean(WS_PLUGIN_SEND_QUEUE)
    public ActiveMQQueue wsPluginSendQueue(WSPluginPropertyManager wsPluginPropertyManager) {
        String queueName = wsPluginPropertyManager.getKnownPropertyValue(WSPluginPropertyManager.DISPATCHER_SEND_QUEUE_NAME);
        LOG.debug("Using ws plugin send queue name [{}]", queueName);
        return new ActiveMQQueue(queueName);
    }

}
