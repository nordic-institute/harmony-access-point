package eu.domibus.core.jms;

import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.DomibusJMSConstants;
import eu.domibus.logging.IDomibusLogger;
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
 * Class responsible for configuring the internal JMS factory
 *
 * @author Cosmin Baciu
 * @since 4.2
 */
@Configuration
public class InternalJmsListenerContainerFactoryConfiguration {

    private static final IDomibusLogger LOGGER = DomibusLoggerFactory.getLogger(InternalJmsListenerContainerFactoryConfiguration.class);


    @Bean("internalJmsListenerContainerFactory")
    public DefaultJmsListenerContainerFactory internalJmsListenerContainerFactory(@Qualifier(DomibusJMSConstants.DOMIBUS_JMS_CONNECTION_FACTORY) ConnectionFactory connectionFactory,
                                                                                  DomibusPropertyProvider domibusPropertyProvider,
                                                                                  @Qualifier("jackson2MessageConverter") MappingJackson2MessageConverter jackson2MessageConverter,
                                                                                  @Qualifier("internalDestinationResolver") Optional<JndiDestinationResolver> internalDestinationResolver,
                                                                                  @Qualifier("taskExecutor") SchedulingTaskExecutor schedulingTaskExecutor) {
        DefaultJmsListenerContainerFactory result = new DefaultJmsListenerContainerFactory();
        result.setConnectionFactory(connectionFactory);
        result.setTaskExecutor(schedulingTaskExecutor);

        String concurrency = domibusPropertyProvider.getProperty(DomibusPropertyMetadataManagerSPI.DOMIBUS_INTERNAL_QUEUE_CONCURENCY);
        LOGGER.debug("Configured property [{}] with [{}]", DomibusPropertyMetadataManagerSPI.DOMIBUS_INTERNAL_QUEUE_CONCURENCY, concurrency);
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
