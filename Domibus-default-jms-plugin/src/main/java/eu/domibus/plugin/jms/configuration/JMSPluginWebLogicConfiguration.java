package eu.domibus.plugin.jms.configuration;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.environment.WebLogicCondition;
import eu.domibus.plugin.jms.JMSMessageConstants;
import eu.domibus.plugin.jms.property.JmsPluginPropertyManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.jndi.JndiObjectFactoryBean;

import javax.jms.ConnectionFactory;

/**
 * Class responsible for the configuration of the plugin for WebLogic
 *
 * @author Cosmin Baciu
 * @since 4.2
 */
@Conditional(WebLogicCondition.class)
@Configuration
public class JMSPluginWebLogicConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(JMSPluginWebLogicConfiguration.class);

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
