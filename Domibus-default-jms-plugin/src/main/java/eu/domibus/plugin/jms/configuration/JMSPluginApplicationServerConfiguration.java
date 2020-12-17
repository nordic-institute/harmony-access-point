package eu.domibus.plugin.jms.configuration;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.environment.ApplicationServerCondition;
import eu.domibus.plugin.jms.JMSMessageConstants;
import eu.domibus.plugin.jms.property.JmsPluginPropertyManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.destination.JndiDestinationResolver;
import org.springframework.jndi.JndiObjectFactoryBean;

import javax.jms.Queue;

/**
 * Class responsible for the configuration of the plugin for an application server, WebLogic and WildFly
 *
 * @author Cosmin Baciu
 * @since 4.2
 */
public abstract class JMSPluginApplicationServerConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(JMSPluginApplicationServerConfiguration.class);

    @Bean(name = "jndiDestinationResolver")
    public JndiDestinationResolver jndiDestinationResolver() {
        LOG.debug("Creating jndiDestinationResolver bean");
        JndiDestinationResolver jndiDestinationResolver = new JndiDestinationResolver();
        jndiDestinationResolver.setFallbackToDynamicDestination(true);
        return jndiDestinationResolver;
    }

    @Bean("notifyBackendJmsQueue")
    public JndiObjectFactoryBean notifyBackendQueue(JmsPluginPropertyManager jmsPluginPropertyManager) {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();

        String queueNotificationJndi = jmsPluginPropertyManager.getKnownPropertyValue(JMSMessageConstants.QUEUE_NOTIFICATION);
        LOG.debug("Using queue notification jndi [{}]", queueNotificationJndi);
        jndiObjectFactoryBean.setJndiName(queueNotificationJndi);

        jndiObjectFactoryBean.setExpectedType(Queue.class);
        return jndiObjectFactoryBean;
    }
}
