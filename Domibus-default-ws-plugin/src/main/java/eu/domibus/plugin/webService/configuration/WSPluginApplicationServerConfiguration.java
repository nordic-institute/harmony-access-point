package eu.domibus.plugin.webService.configuration;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.environment.ApplicationServerCondition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.jndi.JndiObjectFactoryBean;

import javax.jms.Queue;

/**
 * Class responsible for the configuration of the plugin for an application server, WebLogic and WildFly
 *
 * @author Cosmin Baciu
 * @since 4.2
 */
@Conditional(ApplicationServerCondition.class)
@Configuration
public class WSPluginApplicationServerConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSPluginApplicationServerConfiguration.class);

    @Bean("notifyBackendWebServiceQueue")
    public JndiObjectFactoryBean notifyBackendWebServiceQueue() {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();

        String queueNotificationJndi = WSPluginConfiguration.NOTIFY_BACKEND_QUEUE_JNDI;
        LOG.debug("Using queue notification jndi for notifyBackendWebServiceQueue [{}]", queueNotificationJndi);
        jndiObjectFactoryBean.setJndiName(queueNotificationJndi);

        jndiObjectFactoryBean.setExpectedType(Queue.class);
        return jndiObjectFactoryBean;
    }

}
