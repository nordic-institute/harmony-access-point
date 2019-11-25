package eu.domibus.clustering;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.support.destination.JndiDestinationResolver;
import org.springframework.transaction.PlatformTransactionManager;

import javax.jms.ConnectionFactory;
import javax.jms.Topic;
import java.util.Optional;

@Configuration
public class ControllerListenerConfiguration {

    private static final String CONTROLLER_LISTENER_CONCURRENCY = "1-1";

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(ControllerListenerConfiguration.class);

    @Bean("controllerListener")
    public DefaultMessageListenerContainer createDefaultMessageListenerContainer(@Qualifier("domibusJMS-XAConnectionFactory") ConnectionFactory connectionFactory,
                                                                                 @Qualifier("clusterCommandTopic") Topic destination,
                                                                                 ControllerListenerService messageListener,
                                                                                 PlatformTransactionManager transactionManager,
                                                                                 Optional<JndiDestinationResolver> internalDestinationResolver,
                                                                                 DomibusPropertyProvider domibusPropertyProvider) {
        DefaultMessageListenerContainer messageListenerContainer = new DefaultMessageListenerContainer();
        messageListenerContainer.setConnectionFactory(connectionFactory);
        messageListenerContainer.setDestination(destination);
        messageListenerContainer.setMessageListener(messageListener);
        messageListenerContainer.setTransactionManager(transactionManager);

        final String concurrency = CONTROLLER_LISTENER_CONCURRENCY;
        messageListenerContainer.setConcurrency(CONTROLLER_LISTENER_CONCURRENCY);
        messageListenerContainer.setSessionTransacted(true);
        messageListenerContainer.setSessionAcknowledgeMode(0);
        messageListenerContainer.setPubSubDomain(true);
        messageListenerContainer.setSubscriptionDurable(false);

        messageListenerContainer.afterPropertiesSet();

        if (internalDestinationResolver.isPresent()) {
            messageListenerContainer.setDestinationResolver(internalDestinationResolver.get());
        }

        LOG.debug("DefaultMessageListenerContainer created for ControllerListener with concurrency=[{}]", concurrency);
        return messageListenerContainer;
    }
}
