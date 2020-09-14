package eu.domibus.plugin.jms.configuration;

import eu.domibus.common.JMSConstants;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.environment.WildFlyCondition;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.JndiDestinationResolver;

import javax.jms.ConnectionFactory;
import javax.jms.Session;
import java.util.Optional;

/**
 * Class responsible for the configuration of the plugin for WildFly
 *
 * @author Cosmin Baciu
 * @since 4.2
 */
@Conditional(WildFlyCondition.class)
@Configuration
public class JMSPluginWildFlyConfiguration extends JMSPluginApplicationServerConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(JMSPluginWildFlyConfiguration.class);

    @Bean("mshToBackendTemplate")
    public JmsTemplate mshToBackendTemplate(@Qualifier(JMSConstants.DOMIBUS_JMS_CACHING_XACONNECTION_FACTORY) ConnectionFactory connectionFactory,
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


}
