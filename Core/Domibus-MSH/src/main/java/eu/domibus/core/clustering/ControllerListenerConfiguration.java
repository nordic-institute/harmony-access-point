package eu.domibus.core.clustering;

import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.DomibusJMSConstants;
import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.support.destination.JndiDestinationResolver;

import javax.jms.ConnectionFactory;
import javax.jms.Session;
import javax.jms.Topic;
import java.util.Optional;

import static eu.domibus.jms.spi.InternalJMSConstants.CLUSTER_COMMAND_TOPIC;

/**
 * JMS listener responsible of executing internal commands
 *
 * @author Cosmin Baciu
 * @since 4.2
 */
@Configuration
public class ControllerListenerConfiguration {
    private static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(ControllerListenerConfiguration.class);

    @Bean("controllerListener")
    public DefaultMessageListenerContainer createDefaultMessageListenerContainer(@Qualifier(DomibusJMSConstants.DOMIBUS_JMS_CONNECTION_FACTORY) ConnectionFactory connectionFactory,
                                                                                 @Qualifier(CLUSTER_COMMAND_TOPIC) Topic destination,
                                                                                 ControllerListenerService messageListener,
                                                                                 @Qualifier("internalDestinationResolver") Optional<JndiDestinationResolver> internalDestinationResolver,
                                                                                 DomibusPropertyProvider domibusPropertyProvider) {
        DefaultMessageListenerContainer messageListenerContainer = new DefaultMessageListenerContainer();
        messageListenerContainer.setConnectionFactory(connectionFactory);
        messageListenerContainer.setDestination(destination);
        messageListenerContainer.setMessageListener(messageListener);

        String concurrency = domibusPropertyProvider.getProperty(DomibusPropertyMetadataManagerSPI.DOMIBUS_JMS_INTERNAL_COMMAND_CONCURENCY);
        LOG.debug("Configured property [{}] with [{}]", DomibusPropertyMetadataManagerSPI.DOMIBUS_JMS_INTERNAL_COMMAND_CONCURENCY, concurrency);

        messageListenerContainer.setConcurrency(concurrency);
        messageListenerContainer.setSessionTransacted(true);
        messageListenerContainer.setSessionAcknowledgeMode(Session.SESSION_TRANSACTED);
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
