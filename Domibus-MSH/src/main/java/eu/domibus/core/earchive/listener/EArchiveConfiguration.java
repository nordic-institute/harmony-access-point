package eu.domibus.core.earchive.listener;

import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.DomibusJMSConstants;
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
 * @author François Gautier
 * @since 5.0
 */
@Configuration
public class EArchiveConfiguration {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(EArchiveConfiguration.class);

    @Bean("eArchiveJmsListenerContainerFactory")
    public DefaultJmsListenerContainerFactory uiReplicationJmsListenerContainerFactory(@Qualifier(DomibusJMSConstants.DOMIBUS_JMS_CONNECTION_FACTORY) ConnectionFactory connectionFactory,
                                                                                       DomibusPropertyProvider domibusPropertyProvider,
                                                                                       @Qualifier("jackson2MessageConverter") MappingJackson2MessageConverter jackson2MessageConverter,
                                                                                       Optional<JndiDestinationResolver> internalDestinationResolver,
                                                                                       @Qualifier("taskExecutor") SchedulingTaskExecutor schedulingTaskExecutor) {
        DefaultJmsListenerContainerFactory result = new DefaultJmsListenerContainerFactory();
        result.setConnectionFactory(connectionFactory);
        result.setTaskExecutor(schedulingTaskExecutor);

        String concurrency = domibusPropertyProvider.getProperty(DomibusPropertyMetadataManagerSPI.DOMIBUS_JMS_QUEUE_EARCHIVE_CONCURRENCY);
        LOGGER.debug("Configured property [{}] with [{}]", DomibusPropertyMetadataManagerSPI.DOMIBUS_JMS_QUEUE_EARCHIVE_CONCURRENCY, concurrency);
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
