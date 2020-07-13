package eu.domibus.core.alerts.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import eu.domibus.api.jms.JMSConstants;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import freemarker.cache.ClassTemplateLoader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.destination.JndiDestinationResolver;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;

import javax.jms.ConnectionFactory;
import javax.jms.Session;

import java.util.Optional;

import static org.springframework.jms.support.converter.MessageType.TEXT;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Configuration
public class AlertContextConfiguration {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(AlertContextConfiguration.class);

    @Bean("jackson2MessageConverter")
    public MappingJackson2MessageConverter jackson2MessageConverter() {
        final MappingJackson2MessageConverter mappingJackson2MessageConverter = new MappingJackson2MessageConverter();
        mappingJackson2MessageConverter.setTargetType(TEXT);
        mappingJackson2MessageConverter.setTypeIdPropertyName("_type");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        mappingJackson2MessageConverter.setObjectMapper(objectMapper);
        return mappingJackson2MessageConverter;
    }

    @Bean
    public JmsTemplate jsonJmsTemplate(@Qualifier(JMSConstants.DOMIBUS_JMS_CACHING_XACONNECTION_FACTORY) ConnectionFactory connectionFactory,
                                       MappingJackson2MessageConverter jackson2MessageConverter) {
        JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
        jmsTemplate.setSessionTransacted(true);
        jmsTemplate.setSessionAcknowledgeModeName("AUTO_ACKNOWLEDGE");
        jmsTemplate.setMessageConverter(jackson2MessageConverter);
        return jmsTemplate;
    }

    @Bean
    public FreeMarkerConfigurationFactoryBean freeMarkerConfigurationFactoryBean() {
        final FreeMarkerConfigurationFactoryBean freeMarkerConfigurationFactoryBean =
                new FreeMarkerConfigurationFactoryBean();
        freeMarkerConfigurationFactoryBean.setPreTemplateLoaders(new ClassTemplateLoader(AlertContextConfiguration.class, "/templates"));
        return freeMarkerConfigurationFactoryBean;
    }

    @Bean
    public JavaMailSenderImpl javaMailSender() {
        return new JavaMailSenderImpl();
    }

    @Bean("alertJmsListenerContainerFactory")
    public DefaultJmsListenerContainerFactory alertJmsListenerContainerFactory(@Qualifier(JMSConstants.DOMIBUS_JMS_XACONNECTION_FACTORY) ConnectionFactory connectionFactory,
                                                                               PlatformTransactionManager transactionManager,
                                                                               DomibusPropertyProvider domibusPropertyProvider,
                                                                               @Qualifier("jackson2MessageConverter") MappingJackson2MessageConverter jackson2MessageConverter,
                                                                               Optional<JndiDestinationResolver> internalDestinationResolver) {
        DefaultJmsListenerContainerFactory result = new DefaultJmsListenerContainerFactory();
        result.setConnectionFactory(connectionFactory);
        result.setTransactionManager(transactionManager);

        String concurrency = domibusPropertyProvider.getProperty(DomibusPropertyMetadataManagerSPI.DOMIBUS_ALERT_QUEUE_CONCURRENCY);
        LOGGER.debug("Configured property [{}] with [{}]", DomibusPropertyMetadataManagerSPI.DOMIBUS_ALERT_QUEUE_CONCURRENCY, concurrency);
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
