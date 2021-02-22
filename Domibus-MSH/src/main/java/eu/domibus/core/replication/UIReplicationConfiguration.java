package eu.domibus.core.replication;

import eu.domibus.common.JMSConstants;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.destination.JndiDestinationResolver;
import org.springframework.scheduling.SchedulingTaskExecutor;

import javax.jms.ConnectionFactory;
import javax.jms.Session;
import java.util.Optional;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Configuration
public class UIReplicationConfiguration {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(UIReplicationConfiguration.class);

    @Bean("uiReplicationJmsListenerContainerFactory")
    public DefaultJmsListenerContainerFactory uiReplicationJmsListenerContainerFactory(@Qualifier(JMSConstants.DOMIBUS_JMS_CONNECTION_FACTORY) ConnectionFactory connectionFactory,
                                                                                       DomibusPropertyProvider domibusPropertyProvider,
                                                                                       @Qualifier("jackson2MessageConverter") MappingJackson2MessageConverter jackson2MessageConverter,
                                                                                       Optional<JndiDestinationResolver> internalDestinationResolver,
                                                                                       @Qualifier("taskExecutor") SchedulingTaskExecutor schedulingTaskExecutor) {
        DefaultJmsListenerContainerFactory result = new DefaultJmsListenerContainerFactory();
        result.setConnectionFactory(connectionFactory);
        result.setTaskExecutor(schedulingTaskExecutor);

        String concurrency = domibusPropertyProvider.getProperty(DomibusPropertyMetadataManagerSPI.DOMIBUS_UI_REPLICATION_QUEUE_CONCURENCY);
        LOGGER.debug("Configured property [{}] with [{}]", DomibusPropertyMetadataManagerSPI.DOMIBUS_UI_REPLICATION_QUEUE_CONCURENCY, concurrency);
        result.setConcurrency(concurrency);
        result.setSessionTransacted(true);
        result.setSessionAcknowledgeMode(Session.SESSION_TRANSACTED);
        result.setMessageConverter(jackson2MessageConverter);

        if (internalDestinationResolver.isPresent()) {
            result.setDestinationResolver(internalDestinationResolver.get());
        }

        return result;
    }
}
