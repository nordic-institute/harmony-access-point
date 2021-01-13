package eu.domibus.plugin.webService.backend.reliability.queue;

import eu.domibus.common.JMSConstants;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.webService.property.WSPluginPropertyManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.transaction.PlatformTransactionManager;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;

import static eu.domibus.plugin.webService.backend.reliability.queue.WSSendMessageListener.WS_SEND_MESSAGE_LISTENER;
import static eu.domibus.plugin.webService.property.WSPluginPropertyManager.DISPATCHER_SEND_QUEUE_CONCURRENCY;

/**
 * Configuration class for JMS queues used in WS Plugin
 * <p>
 * It will contain all configuration for all defined queues
 *
 * @author François Gautier
 * @since 5.0
 */
@Configuration
public class WSMessageListenerContainerConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSMessageListenerContainerConfiguration.class);
    public static final String WS_PLUGIN_SEND_QUEUE = "wsPluginSendQueue";
    public static final String WS_PLUGIN_OUT_CONTAINER = "wsPluginOutContainer";

    @Autowired
    @Qualifier(WS_PLUGIN_SEND_QUEUE)
    private Queue wsPluginSendQueue;

    @Qualifier(WS_SEND_MESSAGE_LISTENER)
    @Autowired
    private WSSendMessageListener wsSendMessageListener;

    @Autowired
    @Qualifier(JMSConstants.DOMIBUS_JMS_XACONNECTION_FACTORY)
    private ConnectionFactory connectionFactory;

    @Autowired
    protected PlatformTransactionManager transactionManager;

    @Autowired
    private WSPluginPropertyManager wsPluginPropertyManager;

    @Bean(name = WS_PLUGIN_OUT_CONTAINER)
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public DefaultMessageListenerContainer createDefaultMessageListenerContainer() {
        DefaultMessageListenerContainer messageListenerContainer = new DefaultMessageListenerContainer();

        final String queueConcurrency = wsPluginPropertyManager.getKnownPropertyValue(DISPATCHER_SEND_QUEUE_CONCURRENCY);
        LOG.debug("wsPluginSendQueue concurrency set to: {}", queueConcurrency);

        messageListenerContainer.setConnectionFactory(connectionFactory);
        messageListenerContainer.setDestination(wsPluginSendQueue);
        messageListenerContainer.setMessageListener(wsSendMessageListener);
        messageListenerContainer.setTransactionManager(transactionManager);
        messageListenerContainer.setConcurrency(queueConcurrency);
        messageListenerContainer.setSessionTransacted(true);
        messageListenerContainer.setSessionAcknowledgeMode(0);

        messageListenerContainer.afterPropertiesSet();

        return messageListenerContainer;
    }
}
