//package eu.domibus.sti;
//
//import com.codahale.metrics.ConsoleReporter;
//import com.codahale.metrics.MetricRegistry;
//import com.codahale.metrics.health.HealthCheckRegistry;
//import com.codahale.metrics.servlets.AdminServlet;
//import com.fasterxml.uuid.EthernetAddress;
//import com.fasterxml.uuid.Generators;
//import com.fasterxml.uuid.NoArgGenerator;
//import eu.domibus.plugin.webService.generated.BackendInterface;
//import eu.domibus.plugin.webService.generated.BackendService11;
//import eu.domibus.rest.client.ApiClient;
//import eu.domibus.rest.client.api.UsermessageApi;
//import oracle.jdbc.datasource.OraclePooledConnection;
//import oracle.jdbc.pool.OracleConnectionPoolDataSource;
//import oracle.jdbc.pool.OracleDataSource;
//import oracle.jms.AQjmsFactory;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.web.servlet.ServletRegistrationBean;
//import org.springframework.context.annotation.Bean;
//import org.springframework.jms.annotation.EnableJms;
//import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
//import org.springframework.jms.connection.CachingConnectionFactory;
//import org.springframework.jms.core.JmsTemplate;
//import org.springframework.jms.listener.DefaultMessageListenerContainer;
//import org.springframework.scheduling.annotation.EnableAsync;
//
//import javax.jms.Connection;
//import javax.jms.ConnectionFactory;
//import javax.jms.JMSException;
//import javax.jms.QueueConnectionFactory;
//import javax.naming.Context;
//import javax.naming.InitialContext;
//import javax.naming.NamingException;
//import javax.rmi.PortableRemoteObject;
//import javax.sql.DataSource;
//import javax.xml.namespace.QName;
//import javax.xml.ws.BindingProvider;
//import javax.xml.ws.WebServiceException;
//import javax.xml.ws.handler.Handler;
//import java.io.File;
//import java.lang.reflect.Method;
//import java.net.MalformedURLException;
//import java.net.URI;
//import java.net.URL;
//import java.net.URLClassLoader;
//import java.sql.SQLException;
//import java.util.Hashtable;
//import java.util.List;
//import java.util.Properties;
//import java.util.concurrent.TimeUnit;
//
//// [AQ] uncomment this when using AQ
////@org.springframework.context.annotation.Configuration
//@EnableJms
//@EnableAsync
//public class ConfigurationAQ {
//
//    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationAQ.class);
//
//    // Url to access to the queue o topic
//    @Value("${jms.providerUrl}")
//    private String providerUrl;
//
//    // Name of the queue o topic to extract the message
//    @Value("${jms.destinationName}")
//    private String destinationName;
//
//    @Value("${jms.in.queue}")
//    private String jmsInDestination;
//
//    // Number of consumers in the application
//    @Value("${jms.concurrentConsumers}")
//    private String concurrentConsumers;
//
//    @Value("${ws.plugin.url}")
//    private String wsdlUrl;
//
//
//    @Value("${domibus.url}")
//    private String domibusUrl;
//
//    @Value("${session.cache.size}")
//    private Integer cacheSize;
//
//    @Value("${jms.session.transacted}")
//    private Boolean sessionTransacted;
//
//    @Value("${domibus.dbUrl}")
//    private String dbUrl;
//
//    @Value("${domibus.dbUser}")
//    private String dbUser;
//
//    @Value("${domibus.dbPasswd}")
//    private String dbPasswd;
//
//    @Bean
//    public DefaultJmsListenerContainerFactory myFactoryAQ() {
//        LOG.info("Initiating jms listener factory AQ");
//
//        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
//        factory.setConnectionFactory(connectionFactory());
//        factory.setConcurrency(concurrentConsumers);
//        factory.setCacheLevel(DefaultMessageListenerContainer.CACHE_CONNECTION);
//
//        if(sessionTransacted) {
//            LOG.info("Configuring DefaultJmsListenerContainerFactory: myFactory with session transacted");
//            factory.setSessionTransacted(true);
//            factory.setSessionAcknowledgeMode(0);
//        }
//
//        return factory;
//    }
//
//    @Bean
//    public ConnectionFactory connectionFactory() {
//        try {
//            ConnectionFactory conn = AQjmsFactory.getConnectionFactory(dataSource());
//            LOG.info("Connection [{}]", conn);
//            CachingConnectionFactory factory = new CachingConnectionFactory(conn);
//            factory.setSessionCacheSize(cacheSize);
//            return factory;
//        } catch (JMSException | SQLException e) {
//            LOG.error("Impossible to get connection factory: ", e);
//        }
//        return null;
//    }
//
//    @Bean
//    DataSource dataSource() throws SQLException {
//        OracleDataSource dataSource = new OracleDataSource();
//        dataSource.setUser(dbUser);
//        dataSource.setPassword(dbPasswd);
//        dataSource.setURL(dbUrl);
//        dataSource.setImplicitCachingEnabled(true);
//        dataSource.setFastConnectionFailoverEnabled(true);
//
//        Properties props = new Properties();
//        props.put("driverType", "thin");
//        props.put("MaxStatementsLimit", "25");
//
//        dataSource.setConnectionProperties(props);
//
//        Properties cacheProps = new Properties();
//        cacheProps.setProperty("MinLimit", "1");
//        cacheProps.setProperty("MaxLimit", "10");
//        cacheProps.setProperty("InitialLimit", "1");
//        cacheProps.setProperty("ConnectionWaitTimeout", "5");
//        cacheProps.setProperty("ValidateConnection", "true");
//
//        dataSource.setConnectionCacheProperties(cacheProps);
//
//        return dataSource;
//    }
//
//    // TODO - Lookup using jndi name. This way we use the connection pool mechanism provided by WebLogic.
//    /*
//    @Bean
//    DataSource dataSource() throws SQLException {
//        try {
//            addPath("/home/edelivery/sti_mock_jars/wlclient.jar");
//            addPath("/home/edelivery/sti_mock_jars/aqapi.jar");
//            addPath("/home/edelivery/sti_mock_jars/ojdbc8.jar");
//            addPath("/home/edelivery/sti_mock_jars/orai18n.jar");
//            addPath("/home/edelivery/sti_mock_jars/wlthint3client-12.1.1.jar");
//        } catch (Exception e) {
//            System.out.println("Exception!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//        }
//        DataSource ds=null;
//        Hashtable env = new Hashtable<>();
//        //env.put( Context.INITIAL_CONTEXT_FACTORY, "oracle.jms.AQjmsInitialContextFactory" );
//        //env.put( "datasource", "jdbc/cipaeDeliveryDs" );
//        env.put( Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory" );
//        env.put(Context.PROVIDER_URL, providerUrl);
//        env.put(Context.SECURITY_PRINCIPAL, "weblogic");
//        env.put(Context.SECURITY_CREDENTIALS, "weblogic1");
//
//        try {
//            Context context = new InitialContext(env);
//            ds = (DataSource) context.lookup("jdbc/jmsdatasource");
//
////            Object dsObj = (Object) context.lookup("jdbc/jmsdatasource");
////            ds = (DataSource) PortableRemoteObject.narrow(dsObj,
////                    javax.sql.DataSource.class);
////            LOG.info("dsObj [{}]", dsObj);
//            LOG.info("ds [{}]", ds);
//
//            QueueConnectionFactory cf = (QueueConnectionFactory) context.lookup("jms/ConnectionFactory");
//
//            LOG.info("cf [{}]", cf);
//
//            ConnectionFactory cfSti = (ConnectionFactory)context.lookup("jms/StiCF");
//
////            LOG.info("cfObj [{}]", cfObj);
//            LOG.info("cfSti [{}]", cfSti);
//
////            Connection conn = cf.createConnection();
////            conn.start();
////            LOG.info("cfObj [{}]", cfObj);
////            LOG.info("cf [{}]", cf);
////            conn.close();
//        //} catch(JMSException | NamingException ne){
//        } catch(NamingException ne){
//            LOG.info("Datasource error", ne);
//        }
//        return ds;
//    }
//
//    public static void addPath(String s) throws Exception {
//        File f = new File(s);
//        URI u = f.toURI();
//        URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
//        Class<URLClassLoader> urlClass = URLClassLoader.class;
//        Method method = urlClass.getDeclaredMethod("addURL", new Class[]{URL.class});
//        method.setAccessible(true);
//        method.invoke(urlClassLoader, new Object[]{u.toURL()});
//    }
//    */
//
//    @Bean("stiUUIDGenerator")
//    public NoArgGenerator createUUIDGenerator() {
//        final EthernetAddress ethernetAddress = EthernetAddress.fromInterface();
//        return Generators.timeBasedGenerator(ethernetAddress);
//    }
//
//    @Bean
//    public JmsListener jmsListener() {
//        return new JmsListener(senderService(), metricRegistry());
//    }
//
//    @Bean
//    public JmsTemplate inQueueJmsTemplate() {
//        LOG.info("Configuring jms template for");
//        JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory());
//        jmsTemplate.setDefaultDestinationName(jmsInDestination);
//        jmsTemplate.setReceiveTimeout(5000);
//        return jmsTemplate;
//    }
//
////    @Bean
////    public BackendInterface backendInterface() {
////        BackendService11 backendService = null;
////        try {
////            backendService = new BackendService11(new URL(wsdlUrl), new QName("http://org.ecodex.backend/1_1/", "BackendService_1_1"));
////        } catch (MalformedURLException | WebServiceException e) {
////            LOG.error("Could not instantiate backendService, sending message with ws plugin won't work.", e);
////            return null;
////        }
////        BackendInterface backendPort = backendService.getBACKENDPORT();
////
////        //enable chunking
////        BindingProvider bindingProvider = (BindingProvider) backendPort;
////        //comment the following lines if sending large files
////        List<Handler> handlers = bindingProvider.getBinding().getHandlerChain();
////        bindingProvider.getBinding().setHandlerChain(handlers);
////
////        return backendPort;
////
////    }
//
//    @Bean
//    public UsermessageApi usermessageApi() {
//        ApiClient apiClient = new ApiClient();
//        apiClient.setBasePath(domibusUrl);
//
//        return new UsermessageApi(apiClient);
//    }
//
//    @Bean
//    public SenderService senderService() {
//        return new SenderService(inQueueJmsTemplate(), metricRegistry(), createUUIDGenerator());
//    }
//
//    @Bean
//    public HealthCheckRegistry healthCheckRegistry() {
//        return new HealthCheckRegistry();
//    }
//
//    @Bean
//    public MetricRegistry metricRegistry() {
//        return new MetricRegistry();
//    }
//
//    @Bean
//    public ConsoleReporter consoleReporter() {
//        ConsoleReporter reporter = ConsoleReporter.forRegistry(metricRegistry())
//                .convertRatesTo(TimeUnit.SECONDS)
//                .convertDurationsTo(TimeUnit.MILLISECONDS)
//                .build();
//        reporter.start(30, TimeUnit.SECONDS);
//        return reporter;
//    }
//
//    @Bean
//    public ServletRegistrationBean servletRegistrationBean() {
//        return new ServletRegistrationBean(new AdminServlet(), "/metrics/*");
//    }
//}