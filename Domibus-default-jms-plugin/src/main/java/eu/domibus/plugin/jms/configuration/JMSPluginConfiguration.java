package eu.domibus.plugin.jms.configuration;

import com.codahale.metrics.MetricRegistry;
import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.DomibusPropertyExtService;
import eu.domibus.ext.services.JMSExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.environment.DomibusEnvironmentUtil;
import eu.domibus.plugin.handler.MessageRetriever;
import eu.domibus.plugin.jms.JMSPluginImpl;
import eu.domibus.plugin.jms.JMSPluginQueueService;
import eu.domibus.plugin.jms.JMSMessageConstants;
import eu.domibus.plugin.jms.JMSMessageTransformer;
import eu.domibus.plugin.jms.property.JmsPluginPropertyManager;
import eu.domibus.plugin.notification.PluginAsyncNotificationConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsOperations;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.JndiDestinationResolver;
import org.springframework.transaction.PlatformTransactionManager;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.jms.Session;
import java.util.Optional;

/**
 * Class responsible for the configuration of the plugin, independent of any server
 *
 * @author Cosmin Baciu
 * @since 4.2
 */
@Configuration
public class JMSPluginConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(JMSPluginConfiguration.class);

    public static final String JMSPLUGIN_QUEUE_IN_CONCURRENCY = "jmsplugin.queue.in.concurrency";

    @Bean
    public JMSPluginQueueService backendJMSQueueService(DomibusPropertyExtService domibusPropertyExtService,
                                                        DomainContextExtService domainContextExtService,
                                                        MessageRetriever messageRetriever) {
        return new JMSPluginQueueService(domibusPropertyExtService, domainContextExtService, messageRetriever);
    }

    @Bean("backendJms")
    public JMSPluginImpl createBackendJMSImpl(MetricRegistry metricRegistry,
                                              JMSExtService jmsExtService,
                                              DomainContextExtService domainContextExtService,
                                              JMSPluginQueueService JMSPluginQueueService,
                                              @Qualifier(value = "mshToBackendTemplate") JmsOperations mshToBackendTemplate,
                                              JMSMessageTransformer jmsMessageTransformer) {
        return new JMSPluginImpl(metricRegistry, jmsExtService, domainContextExtService, JMSPluginQueueService, mshToBackendTemplate, jmsMessageTransformer);
    }

    @Bean("jmsAsyncPluginConfiguration")
    public PluginAsyncNotificationConfiguration pluginAsyncNotificationConfiguration(@Qualifier("notifyBackendJmsQueue") Queue notifyBackendJmsQueue,
                                                                                     JMSPluginImpl backendJMS,
                                                                                     Environment environment,
                                                                                     JmsPluginPropertyManager jmsPluginPropertyManager) {
        PluginAsyncNotificationConfiguration pluginAsyncNotificationConfiguration = new PluginAsyncNotificationConfiguration(backendJMS, notifyBackendJmsQueue);
        if (DomibusEnvironmentUtil.INSTANCE.isApplicationServer(environment)) {
            String queueNotificationJndi = jmsPluginPropertyManager.getKnownPropertyValue(JMSMessageConstants.QUEUE_NOTIFICATION);
            LOG.debug("Domibus is running inside an application server. Setting the queue name to [{}]", queueNotificationJndi);
            pluginAsyncNotificationConfiguration.setQueueName(queueNotificationJndi);
        }
        return pluginAsyncNotificationConfiguration;
    }

    @Bean("backendJmsListenerContainerFactory")
    public DefaultJmsListenerContainerFactory defaultJmsListenerContainerFactory(@Qualifier("domibusJMS-XAConnectionFactory") ConnectionFactory connectionFactory,
                                                                                 PlatformTransactionManager transactionManager,
                                                                                 JmsPluginPropertyManager jmsPluginPropertyManager,
                                                                                 Optional<JndiDestinationResolver> jndiDestinationResolver) {
        DefaultJmsListenerContainerFactory result = new DefaultJmsListenerContainerFactory();
        result.setConnectionFactory(connectionFactory);
        result.setTransactionManager(transactionManager);
        String queueInConcurrency = jmsPluginPropertyManager.getKnownPropertyValue(JMSPLUGIN_QUEUE_IN_CONCURRENCY);
        LOG.debug("Using jms in-queue concurrency [{}]", queueInConcurrency);
        result.setConcurrency(queueInConcurrency);
        result.setSessionTransacted(true);
        result.setSessionAcknowledgeMode(Session.SESSION_TRANSACTED);

        if (jndiDestinationResolver.isPresent()) {
            LOG.debug("Setting jndi destination resolver to the defaultJmsListenerContainerFactory");
            result.setDestinationResolver(jndiDestinationResolver.get());
        }

        return result;
    }

    @Bean("mshToBackendTemplate")
    public JmsTemplate mshToBackendTemplate(@Qualifier("domibusJMS-CachingXAConnectionFactory") ConnectionFactory connectionFactory,
                                            Optional<JndiDestinationResolver> jndiDestinationResolver) {
        JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
        jmsTemplate.setSessionTransacted(true);
        jmsTemplate.setSessionAcknowledgeMode(Session.SESSION_TRANSACTED);

        if (jndiDestinationResolver.isPresent()) {
            LOG.debug("Setting jndi destination resolver to the mshToBackendTemplate");
            jmsTemplate.setDestinationResolver(jndiDestinationResolver.get());
        }

        return jmsTemplate;
    }

}
