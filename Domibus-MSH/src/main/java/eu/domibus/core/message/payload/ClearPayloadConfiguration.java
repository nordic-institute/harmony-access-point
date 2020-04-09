package eu.domibus.core.message.payload;

import eu.domibus.api.jms.JMSConstants;
import eu.domibus.api.property.DomibusPropertyMetadataManager;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.destination.JndiDestinationResolver;
import org.springframework.transaction.PlatformTransactionManager;

import javax.jms.ConnectionFactory;
import javax.jms.Session;
import java.util.Optional;

/**
 * @author Catalin Enache
 * @since 4.2
 */
@Configuration
public class ClearPayloadConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(ClearPayloadConfiguration.class);

    @Bean("clearPayloadJmsListenerContainerFactory")
    public DefaultJmsListenerContainerFactory alertJmsListenerContainerFactory(@Qualifier(JMSConstants.DOMIBUS_JMS_XACONNECTION_FACTORY) ConnectionFactory connectionFactory,
                                                                               PlatformTransactionManager transactionManager,
                                                                               DomibusPropertyProvider domibusPropertyProvider,
                                                                               @Qualifier("jackson2MessageConverter") MappingJackson2MessageConverter jackson2MessageConverter,
                                                                               Optional<JndiDestinationResolver> internalDestinationResolver) {
        DefaultJmsListenerContainerFactory result = new DefaultJmsListenerContainerFactory();
        result.setConnectionFactory(connectionFactory);
        result.setTransactionManager(transactionManager);

        String concurrency = domibusPropertyProvider.getProperty(DomibusPropertyMetadataManager.DOMIBUS_JMS_QUEUE_CLEAR_PAYLOAD_CONCURRENCY);
        LOG.debug("Configured property [{}] with [{}]", DomibusPropertyMetadataManager.DOMIBUS_JMS_QUEUE_CLEAR_PAYLOAD_CONCURRENCY, concurrency);
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
