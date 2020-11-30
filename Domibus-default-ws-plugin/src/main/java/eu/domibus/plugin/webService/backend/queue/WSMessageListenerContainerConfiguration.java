package eu.domibus.plugin.webService.backend.queue;

import eu.domibus.common.JMSConstants;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.plugin.webService.property.WSPluginPropertyManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.MessageListenerContainer;
import org.springframework.transaction.PlatformTransactionManager;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;

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

    @Autowired
    @Qualifier("wsPluginSendQueue")
    private Queue fsPluginSendQueue;

    @Qualifier("wsSendMessageListener")
    @Autowired
    private WSSendMessageListener fsSendMessageListener;

    @Autowired
    @Qualifier(JMSConstants.DOMIBUS_JMS_XACONNECTION_FACTORY)
    private ConnectionFactory connectionFactory;

    @Autowired
    protected PlatformTransactionManager transactionManager;

    @Autowired
    private WSPluginPropertyManager wsPluginPropertyManager;

    @Bean(name = "wsPluginOutContainer")
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public MessageListenerContainer createDefaultMessageListenerContainer(DomainDTO domain) {
        DefaultMessageListenerContainer messageListenerContainer = new DefaultMessageListenerContainer();

        final String messageSelector = MessageConstants.DOMAIN + "='" + domain.getCode() + "'";
        final String queueConcurrency = wsPluginPropertyManager.getKnownPropertyValue(domain.getCode(), DISPATCHER_SEND_QUEUE_CONCURRENCY);
        LOG.debug("wsPluginSendQueue concurrency set to: {}", queueConcurrency);

        messageListenerContainer.setMessageSelector(messageSelector);
        messageListenerContainer.setConnectionFactory(connectionFactory);
        messageListenerContainer.setDestination(fsPluginSendQueue);
        messageListenerContainer.setMessageListener(fsSendMessageListener);
        messageListenerContainer.setTransactionManager(transactionManager);
        messageListenerContainer.setConcurrency(queueConcurrency);
        messageListenerContainer.setSessionTransacted(true);
        messageListenerContainer.setSessionAcknowledgeMode(0);

        messageListenerContainer.afterPropertiesSet();

        return messageListenerContainer;
    }
}
