package eu.domibus.plugin.fs.configuration;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.environment.ApplicationServerCondition;
import eu.domibus.plugin.fs.property.FSPluginProperties;
import eu.domibus.plugin.fs.property.FSPluginPropertiesMetadataManagerImpl;
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
public class FSPluginApplicationServerConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSPluginApplicationServerConfiguration.class);

    @Bean(FSPluginConfiguration.NOTIFY_BACKEND_FS_QUEUE_NAME)
    public JndiObjectFactoryBean notifyBackendFSQueue() {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();

        String queueNotificationJndi = FSPluginConfiguration.NOTIFY_BACKEND_QUEUE_JNDI;
        LOG.debug("Using queue notification jndi for notifyBackendFSQueue [{}]", queueNotificationJndi);
        jndiObjectFactoryBean.setJndiName(queueNotificationJndi);

        jndiObjectFactoryBean.setExpectedType(Queue.class);
        return jndiObjectFactoryBean;
    }

    @Bean("fsPluginSendQueue")
    public JndiObjectFactoryBean sendMessageQueue(FSPluginProperties fsPluginProperties) {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();

        String sendQueueJndi = fsPluginProperties.getKnownPropertyValue(FSPluginPropertiesMetadataManagerImpl.PROPERTY_PREFIX + FSPluginPropertiesMetadataManagerImpl.OUT_QUEUE);
        LOG.debug("Using send queue jndi [{}]", sendQueueJndi);
        jndiObjectFactoryBean.setJndiName(sendQueueJndi);

        jndiObjectFactoryBean.setExpectedType(Queue.class);
        return jndiObjectFactoryBean;
    }
}
