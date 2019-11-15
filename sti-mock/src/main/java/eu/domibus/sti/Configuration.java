package eu.domibus.sti;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.jms.support.destination.JndiDestinationResolver;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.jndi.JndiTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.jms.ConnectionFactory;
import java.util.Properties;
import java.util.concurrent.Executor;

@org.springframework.context.annotation.Configuration
@EnableJms
@EnableAsync
public class Configuration {

    private static final Logger LOG = LoggerFactory.getLogger(Configuration.class);
    // Url to access to the queue o topic
    @Value("${jms.providerUrl}")
    private String providerUrl;

    // Jms connection type.
    @Value("${jms.connectionType}")
    private String connectionType;

    // Name of the queue o topic to extract the message
    @Value("${jms.destinationName}")
    private String destinationName;

    @Value("${jms.in.queue}")
    private String jmsInDestination;

    // Number of consumers in the application
    @Value("${jms.concurrentConsumers}")
    private String concurrentConsumers;


    //@Autowired
    @Bean
    public DefaultJmsListenerContainerFactory myFactory(ConnectionFactory connectionFactory, DestinationResolver destination) {
        LOG.info("Initiating jms listener factory");
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setDestinationResolver(destination);
        factory.setConcurrency(concurrentConsumers);
        return factory;
    }

    @Bean
    public JndiTemplate provider() {
        Properties env = new Properties();
        env.put("java.naming.factory.initial", "weblogic.jndi.WLInitialContextFactory");
        env.put("java.naming.provider.url", providerUrl);
        return new JndiTemplate(env);
    }

    //@Autowired
    @Bean
    public JndiObjectFactoryBean connectionFactory(JndiTemplate provider) {
        JndiObjectFactoryBean factory = new JndiObjectFactoryBean();
        factory.setJndiTemplate(provider);
        factory.setJndiName(connectionType);
        factory.setProxyInterface(ConnectionFactory.class);
        return factory;
    }

    //@Autowired
    @Bean
    public JndiDestinationResolver jmsDestinationResolver(JndiTemplate provider) {
        JndiDestinationResolver destResolver = new JndiDestinationResolver();
        destResolver.setJndiTemplate(provider);
        return destResolver;
    }

    //@Autowired
    @Bean
    public JndiObjectFactoryBean inQueueDestination(JndiTemplate provider) {
        JndiObjectFactoryBean dest = new JndiObjectFactoryBean();
        dest.setJndiTemplate(provider);
        dest.setJndiName(jmsInDestination);
        return dest;
    }

    @Bean
    public JmsListener jmsListener(SenderService senderService) {
        return new JmsListener(senderService);
    }

    @Bean
    public JmsTemplate inQueueJmsTemplate(ConnectionFactory connectionFactory, DestinationResolver destination) {
        JmsTemplate jmsTemplate =
                new JmsTemplate(connectionFactory);
        jmsTemplate.setDefaultDestinationName(jmsInDestination);
        jmsTemplate.setDestinationResolver(destination);
        jmsTemplate.setReceiveTimeout(5000);
        return jmsTemplate;
    }

    @Bean(name = "threadPoolTaskExecutor")
    public Executor threadPoolTaskExecutor() {
        return new ThreadPoolTaskExecutor();
    }

    @Bean
    public SenderService senderService(JmsTemplate inQueueJmsTemplate) {
        return new SenderService(inQueueJmsTemplate);
    }
}