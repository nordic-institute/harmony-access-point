package eu.domibus.sti;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlets.AdminServlet;
import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.NoArgGenerator;
import eu.domibus.plugin.webService.generated.BackendInterface;
import eu.domibus.plugin.webService.generated.BackendService11;
import eu.domibus.rest.client.ApiClient;
import eu.domibus.rest.client.api.UsermessageApi;
import oracle.jdbc.pool.OracleDataSource;
import oracle.jms.AQjmsFactory;
import oracle.jms.AQjmsQueueConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.sql.DataSource;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;

// [AQ] uncomment this when using AQ
// @org.springframework.context.annotation.Configuration
@EnableJms
@EnableAsync
public class ConfigurationAQ {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationAQ.class);

    // Name of the queue o topic to extract the message
    @Value("${jms.destinationName}")
    private String destinationName;

    @Value("${jms.in.queue}")
    private String jmsInDestination;

    // Number of consumers in the application
    @Value("${jms.concurrentConsumers}")
    private String concurrentConsumers;

    @Value("${ws.plugin.url}")
    private String wsdlUrl;


    @Value("${domibus.url}")
    private String domibusUrl;

    @Value("${session.cache.size}")
    private Integer cacheSize;

    @Value("${jms.session.transacted}")
    private Boolean sessionTransacted;

    @Value("${domibus.dbUrl}")
    private String dbUrl;

    @Value("${domibus.dbUser}")
    private String dbUser;

    @Value("${domibus.dbPasswd}")
    private String dbPasswd;

    @Bean
    public DefaultJmsListenerContainerFactory myFactoryAQ() {
        LOG.info("Initiating jms listener factory AQ");

        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory());
        factory.setConcurrency(concurrentConsumers);

        if(sessionTransacted) {
            LOG.info("Configuring DefaultJmsListenerContainerFactory: myFactory with session transacted");
            factory.setSessionTransacted(true);
            factory.setSessionAcknowledgeMode(0);
        }

        return factory;
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        try {
            return AQjmsFactory.getConnectionFactory(dataSource());
        } catch (JMSException | SQLException e) {
            LOG.error("Impossible to get connection factory: ", e);
        }
        return null;
    }

    @Bean
    DataSource dataSource() throws SQLException {
        OracleDataSource dataSource = new OracleDataSource();
        dataSource.setUser(dbUser);
        dataSource.setPassword(dbPasswd);
        dataSource.setURL(dbUrl);
        dataSource.setImplicitCachingEnabled(true);
        dataSource.setFastConnectionFailoverEnabled(true);
        return dataSource;
    }


    @Bean("stiUUIDGenerator")
    public NoArgGenerator createUUIDGenerator() {
        final EthernetAddress ethernetAddress = EthernetAddress.fromInterface();
        return Generators.timeBasedGenerator(ethernetAddress);
    }

    @Bean
    public JmsListener jmsListener() {
        return new JmsListener(senderService(), metricRegistry());
    }

    @Bean
    public JmsTemplate inQueueJmsTemplate() {
        LOG.info("Configuring jms template for");
        JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory());
        jmsTemplate.setDefaultDestinationName(jmsInDestination);
        jmsTemplate.setReceiveTimeout(5000);
        return jmsTemplate;
    }

    @Bean
    public BackendInterface backendInterface() {
        BackendService11 backendService = null;
        try {
            backendService = new BackendService11(new URL(wsdlUrl), new QName("http://org.ecodex.backend/1_1/", "BackendService_1_1"));
        } catch (MalformedURLException | WebServiceException e) {
            LOG.error("Could not instantiate backendService, sending message with ws plugin won't work.", e);
            return null;
        }
        BackendInterface backendPort = backendService.getBACKENDPORT();

        //enable chunking
        BindingProvider bindingProvider = (BindingProvider) backendPort;
        //comment the following lines if sending large files
        List<Handler> handlers = bindingProvider.getBinding().getHandlerChain();
        bindingProvider.getBinding().setHandlerChain(handlers);

        return backendPort;

    }

    @Bean
    public UsermessageApi usermessageApi() {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(domibusUrl);

        return new UsermessageApi(apiClient);
    }

    @Bean
    public SenderService senderService() {
        return new SenderService(inQueueJmsTemplate(), backendInterface(), metricRegistry(), usermessageApi(), createUUIDGenerator());
    }

    @Bean
    public HealthCheckRegistry healthCheckRegistry() {
        return new HealthCheckRegistry();
    }

    @Bean
    public MetricRegistry metricRegistry() {
        return new MetricRegistry();
    }

    @Bean
    public ConsoleReporter consoleReporter() {
        ConsoleReporter reporter = ConsoleReporter.forRegistry(metricRegistry())
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        reporter.start(30, TimeUnit.SECONDS);
        return reporter;
    }

    @Bean
    public ServletRegistrationBean servletRegistrationBean() {
        return new ServletRegistrationBean(new AdminServlet(), "/metrics/*");
    }
}