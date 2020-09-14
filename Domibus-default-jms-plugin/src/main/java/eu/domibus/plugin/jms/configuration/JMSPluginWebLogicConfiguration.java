package eu.domibus.plugin.jms.configuration;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.environment.WebLogicCondition;
import eu.domibus.plugin.jms.JMSMessageConstants;
import eu.domibus.plugin.jms.property.JmsPluginPropertyManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.JndiDestinationResolver;
import org.springframework.jndi.JndiObjectFactoryBean;

import javax.jms.ConnectionFactory;
import javax.jms.Session;
import java.util.Optional;

/**
 * Class responsible for the configuration of the plugin for WebLogic
 *
 * @author Cosmin Baciu
 * @since 4.2
 */
@Conditional(WebLogicCondition.class)
@Configuration
public class JMSPluginWebLogicConfiguration extends JMSPluginApplicationServerConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(JMSPluginWebLogicConfiguration.class);

    @Bean("mshToBackendTemplate")
    public JmsTemplate mshToBackendTemplate(@Qualifier(JMSMessageConstants.CACHING_CONNECTION_FACTORY_NAME) ConnectionFactory connectionFactory,
                                            Optional<JndiDestinationResolver> jndiDestinationResolver) {
        JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
        jmsTemplate.setSessionTransacted(true);
        jmsTemplate.setSessionAcknowledgeMode(Session.SESSION_TRANSACTED);

        if (jndiDestinationResolver.isPresent()) {
            LOG.debug("Setting jndi destination resolver to the mshToBackendTemplate");
            jmsTemplate.setDestinationResolver(jndiDestinationResolver.get());
        }

        return jmsTemplate;
    }

    @Bean(JMSMessageConstants.CACHING_CONNECTION_FACTORY_NAME)
    public CachingConnectionFactory cachingConnectionFactory(JmsPluginPropertyManager jmsPluginPropertyManager,
                                                             @Qualifier(JMSMessageConstants.CONNECTION_FACTORY_NAME) ConnectionFactory connectionFactory) {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();

        Integer sessionCacheSize = jmsPluginPropertyManager.getKnownIntegerPropertyValue(JMSMessageConstants.CACHING_CONNECTION_FACTORY_SESSION_CACHE_SIZE);
        LOG.debug("Using caching connection factory session cache size [{}]", sessionCacheSize);
        cachingConnectionFactory.setSessionCacheSize(sessionCacheSize);
        cachingConnectionFactory.setTargetConnectionFactory(connectionFactory);

        return cachingConnectionFactory;
    }

    @Bean(JMSMessageConstants.CONNECTION_FACTORY_NAME)
    public JndiObjectFactoryBean connectionFactory(JmsPluginPropertyManager jmsPluginPropertyManager) {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();

        String connectionFactoryJndi = jmsPluginPropertyManager.getKnownPropertyValue(JMSMessageConstants.CONNECTION_FACTORY);
        LOG.debug("Using connection factory jndi [{}]", connectionFactoryJndi);
        jndiObjectFactoryBean.setJndiName(connectionFactoryJndi);

        jndiObjectFactoryBean.setLookupOnStartup(false);
        jndiObjectFactoryBean.setExpectedType(ConnectionFactory.class);
        return jndiObjectFactoryBean;
    }
}