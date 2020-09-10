package eu.domibus.plugin.jms.configuration;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.environment.TomcatCondition;
import eu.domibus.plugin.jms.JMSMessageConstants;
import eu.domibus.plugin.jms.property.JmsPluginPropertyManager;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * Class responsible for the configuration of the plugin for Tomcat
 *
 * @author Cosmin Baciu
 * @since 4.2
 */
@Conditional(TomcatCondition.class)
@Configuration
public class BackendJMSTomcatConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(BackendJMSTomcatConfiguration.class);

    @Bean("notifyBackendJmsQueue")
    public ActiveMQQueue notifyBackendQueue(JmsPluginPropertyManager jmsPluginPropertyManager) {
        String backendQueueName = jmsPluginPropertyManager.getKnownPropertyValue(JMSMessageConstants.QUEUE_NOTIFICATION);
        LOG.debug("Using backend queue name [{}]", backendQueueName);
        return new ActiveMQQueue(backendQueueName);
    }
}
