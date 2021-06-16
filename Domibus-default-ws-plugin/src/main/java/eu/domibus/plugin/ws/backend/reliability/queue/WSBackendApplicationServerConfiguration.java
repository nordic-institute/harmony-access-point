package eu.domibus.plugin.ws.backend.reliability.queue;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.environment.ApplicationServerCondition;
import eu.domibus.plugin.ws.property.WSPluginPropertyManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.jndi.JndiObjectFactoryBean;

import javax.jms.Queue;

import static eu.domibus.plugin.ws.backend.reliability.queue.WSMessageListenerContainerConfiguration.WS_PLUGIN_SEND_QUEUE;

/**
 * Class responsible for the configuration of the plugin for an application server, WebLogic and WildFly
 *
 * @author Cosmin Baciu
 * @since 4.2
 */
@Conditional(ApplicationServerCondition.class)
@Configuration
public class WSBackendApplicationServerConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSBackendApplicationServerConfiguration.class);

    @Bean(WS_PLUGIN_SEND_QUEUE)
    public JndiObjectFactoryBean sendMessageQueue(WSPluginPropertyManager wsPluginPropertyManager) {
        String queueName = wsPluginPropertyManager.getKnownPropertyValue(WSPluginPropertyManager.DISPATCHER_SEND_QUEUE_NAME);
        LOG.debug("Using ws plugin send queue name [{}]", queueName);
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();

        jndiObjectFactoryBean.setJndiName(queueName);

        jndiObjectFactoryBean.setExpectedType(Queue.class);
        return jndiObjectFactoryBean;
    }

}
