package eu.domibus.plugin.fs.configuration;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.environment.TomcatCondition;
import eu.domibus.plugin.fs.property.FSPluginProperties;
import eu.domibus.plugin.fs.property.FSPluginPropertiesMetadataManagerImpl;
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
public class FSPluginTomcatConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSPluginTomcatConfiguration.class);

    @Bean(FSPluginConfiguration.NOTIFY_BACKEND_FS_QUEUE_NAME)
    public ActiveMQQueue notifyBackendFSQueue() {
        return new ActiveMQQueue("domibus.notification.filesystem");
    }

    @Bean("fsPluginSendQueue")
    public ActiveMQQueue fsPluginSendQueue(FSPluginProperties fsPluginProperties) {
        String queueName = fsPluginProperties.getKnownPropertyValue(FSPluginPropertiesMetadataManagerImpl.PROPERTY_PREFIX + FSPluginPropertiesMetadataManagerImpl.OUT_QUEUE);
        LOG.debug("Using fs plugin send queue name [{}]", queueName);
        return new ActiveMQQueue(queueName);
    }
}
