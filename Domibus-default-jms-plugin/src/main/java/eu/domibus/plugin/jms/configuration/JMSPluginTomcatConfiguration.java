package eu.domibus.plugin.jms.configuration;

import eu.domibus.common.JMSConstants;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.environment.TomcatCondition;
import eu.domibus.plugin.jms.JMSMessageConstants;
import eu.domibus.plugin.jms.property.JmsPluginPropertyManager;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.ConnectionFactory;
import javax.jms.Session;

/**
 * Class responsible for the configuration of the plugin for Tomcat
 *
 * @author Cosmin Baciu
 * @since 4.2
 */
@Conditional(TomcatCondition.class)
@Configuration
public class JMSPluginTomcatConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(JMSPluginTomcatConfiguration.class);

    @Bean("notifyBackendJmsQueue")
    public ActiveMQQueue notifyBackendQueue(JmsPluginPropertyManager jmsPluginPropertyManager) {
        String backendQueueName = jmsPluginPropertyManager.getKnownPropertyValue(JMSMessageConstants.QUEUE_NOTIFICATION);
        LOG.debug("Using backend queue name [{}]", backendQueueName);
        return new ActiveMQQueue(backendQueueName);
    }

    @Bean("mshToBackendTemplate")
    public JmsTemplate mshToBackendTemplate(@Qualifier(JMSConstants.DOMIBUS_JMS_CACHING_XACONNECTION_FACTORY) ConnectionFactory connectionFactory) {
        JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
        jmsTemplate.setSessionTransacted(true);
        jmsTemplate.setSessionAcknowledgeMode(Session.SESSION_TRANSACTED);
        return jmsTemplate;
    }
}
