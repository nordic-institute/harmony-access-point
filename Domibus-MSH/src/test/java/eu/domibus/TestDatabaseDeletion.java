package eu.domibus;

import com.atomikos.icatch.config.UserTransactionServiceImp;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.mysql.cj.jdbc.MysqlXADataSource;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.error.ErrorLogDao;
import eu.domibus.core.error.ErrorLogEntryTruncateUtil;
import eu.domibus.core.jpa.DomibusJPAConfiguration;
import eu.domibus.core.message.*;
import eu.domibus.core.message.acknowledge.MessageAcknowledgementDao;
import eu.domibus.core.message.attempt.MessageAttemptDao;
import eu.domibus.core.message.retention.MessageRetentionDefaultService;
import eu.domibus.core.message.signal.SignalMessageDao;
import eu.domibus.core.message.signal.SignalMessageLogDao;
import eu.domibus.core.replication.UIMessageDao;
import eu.domibus.core.replication.UIMessageDaoImpl;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import net.ttddyy.dsproxy.listener.logging.SLF4JLogLevel;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.hibernate.cfg.Environment;
import org.mockito.Mockito;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * @author Thomas Dussart
 * @since 4.2
 */
public class TestDatabaseDeletion {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TestDatabaseDeletion.class);

    @Configuration
    public static class TomcatConfiguration{
        @Bean(value = "userTransactionService", initMethod = "init", destroyMethod = "shutdownWait")
        public UserTransactionServiceImp userTransactionServiceImp() {
            Properties properties = getAtomikosProperties();
            return new UserTransactionServiceImp(properties);
        }
        protected Properties getAtomikosProperties() {
            Properties properties = new Properties();
            properties.setProperty("com.atomikos.icatch.service", "com.atomikos.icatch.standalone.UserTransactionServiceFactory");

            properties.setProperty("com.atomikos.icatch.output_dir", "/tmp/atomikos/output");

            properties.setProperty("com.atomikos.icatch.log_base_dir", "/tmp/atomikos/log");

            properties.setProperty("com.atomikos.icatch.force_shutdown_on_vm_exit", "true");

            properties.setProperty("com.atomikos.icatch.default_jta_timeout", "60000");

            properties.setProperty("com.atomikos.icatch.max_timeout", "300000");

            properties.setProperty("com.atomikos.icatch.max_actives", "100");
            return properties;
        }



        @Bean(name = DomibusJPAConfiguration.DOMIBUS_JDBC_XA_DATA_SOURCE)
        @DependsOn("userTransactionService")
        public DataSource domibusXADatasource() throws SQLException {
            MysqlXADataSource mysqlXaDataSource = new MysqlXADataSource();
            mysqlXaDataSource.setUrl("jdbc:mysql://localhost:3306/domibus_dev_c2t?pinGlobalTxToPhysicalConnection=true");
            mysqlXaDataSource.setPinGlobalTxToPhysicalConnection(true);
            mysqlXaDataSource.setPassword("edelivery");
            mysqlXaDataSource.setUser("edelivery");


            AtomikosDataSourceBean dataSource = new AtomikosDataSourceBean();
            dataSource.setUniqueResourceName("domibusJDBC-XA");
            dataSource.setXaDataSource(mysqlXaDataSource);
            return ProxyDataSourceBuilder.create(dataSource).logQueryBySlf4j(SLF4JLogLevel.INFO).build();
        }


        @Bean
        public LocalContainerEntityManagerFactoryBean domibusJTA() throws SQLException {
            Properties jpaProperties = new Properties();
            //jpaProperties.put("hibernate.hbm2ddl.auto","update");
            jpaProperties.put("hibernate.show_sql","true");
            jpaProperties.put("hibernate.format_sql","true");
            jpaProperties.put("hibernate.id.new_generator_mappings","false");
            jpaProperties.put("hibernate.connection.driver_class","com.mysql.cj.jdbc.MysqlXADataSource");
            jpaProperties.put("hibernate.dialect","org.hibernate.dialect.MySQL5InnoDBDialect");
            jpaProperties.put("hibernate.order_inserts","true");
            jpaProperties.put("hibernate.order_updates","true");
            jpaProperties.put("hibernate.jdbc.batch_versioned_data","true");
            jpaProperties.put("hibernate.jdbc.batch_size","25");
            /**
             * #Connector/J 8.0.x
             * #domibus.entityManagerFactory.jpaProperty.hibernate.connection.driver_class=com.mysql.cj.jdbc.MysqlXADataSource
             * #Connector/J 5.4.x (deprecated)
             * #domibus.entityManagerFactory.jpaProperty.hibernate.connection.driver_class=com.mysql.jdbc.jdbc2.optional.MysqlXADataSource
             * #domibus.entityManagerFactory.jpaProperty.hibernate.dialect=org.hibernate.dialect.MySQL5InnoDBDialect
             * #domibus.entityManagerFactory.jpaProperty.hibernate.id.new_generator_mappings=false
             */

            LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
            localContainerEntityManagerFactoryBean.setPackagesToScan("eu.domibus");
            localContainerEntityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            localContainerEntityManagerFactoryBean.setJpaProperties(jpaProperties);
            localContainerEntityManagerFactoryBean.setDataSource(domibusXADatasource());
            return localContainerEntityManagerFactoryBean;
        }
    }

    @Configuration
    public static class TestDatabaseDeletionConfiguration{
        @Bean
        public  MessageDeletionService messageRetentionService(){
            return new MessageDeletionService();
        }
        @Bean
        public UserMessageLogDao userMessageLogDao(){
            return new UserMessageLogDao();
        }
        @Bean
        public MessagingDao messagingDao(){
            return new MessagingDao();
        }
        @Bean
        public  SignalMessageLogDao signalMessageLogDao(){
            return new SignalMessageLogDao();
        }
        @Bean
        public  MessageInfoDao messageInfoDao(){
            return new MessageInfoDao();
        }
        @Bean
        public  SignalMessageDao signalMessageDao(){
            return new SignalMessageDao();
        }
        @Bean
        public  MessageAttemptDao messageAttemptDao(){
            return new MessageAttemptDao();
        }
        @Bean
        public  ErrorLogDao errorLogDao(){
            return new ErrorLogDao();
        }
        @Bean
        public  UIMessageDao uiMessageDao(){
            return new UIMessageDaoImpl();
        }
        @Bean
        public  MessageAcknowledgementDao messageAcknowledgementDao(){
            return new MessageAcknowledgementDao();
        }

        /*@Bean
        public UserMessageLogInfoFilter userMessageLogInfoFilter(){
            return Mockito.mock(UserMessageLogInfoFilter.class);
        }*/

        @Bean
        public DomibusPropertyProvider domibusPropertyProvider(){
            return Mockito.mock(DomibusPropertyProvider.class);
        }

        @Bean
        public ErrorLogEntryTruncateUtil errorLogEntryTruncateUtil(){
            return new ErrorLogEntryTruncateUtil();
        }


    }

    public static void main(String[] args) {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(TestDatabaseDeletionConfiguration.class,TomcatConfiguration.class);
        MessageDeletionService bean = ctx.getBean(MessageDeletionService.class);
        bean.execute();



//        UserMessageLogDao userMessageLogDao = ctx.getBean(UserMessageLogDao.class);
//
//        Date currentDate = new Date(System.currentTimeMillis());
//        String mpc = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/defaultMPC";
//        Integer expiredDownloadedMessagesLimit = 10000;
//
//        long now = System.currentTimeMillis();
//        List<MessageDto> expiredMessages = userMessageLogDao.getSentUserMessagesOlderThan(currentDate, mpc, expiredDownloadedMessagesLimit, true);
//        long delay = System.currentTimeMillis() - now;
//
//        LOG.info("getDownloadedUserMessagesOlderThan [{}]", delay);
//
//        expiredMessages.stream().forEach(messageDto -> LOG.info("[{}], [{}], [{}], [{}]", messageDto.getUserMessageId(), messageDto.getSignalMessageId(), messageDto.getReceiptId(), messageDto.getBackend()));

        //bean.deleteMessages();

       /* HelloWorld helloWorld = ctx.getBean(HelloWorld.class);
        helloWorld.setMessage("Hello World!");
        helloWorld.getMessage();*/
    }

//    private static class ProxyDataSourceInterceptor implements MethodInterceptor {
//        private final DataSource dataSource;
//        public ProxyDataSourceInterceptor(final DataSource dataSource) {
//            this.dataSource = ProxyDataSourceBuilder.create(dataSource)
//                    .name("Batch-Insert-Logger")
//                    .asJson().countQuery().logQueryToSysOut().build();
//        }
//
//        Object intercept(Object var1, Method var2, Object[] var3, MethodProxy var4) throws Throwable {
//
//        };
//        // Other methods...
//    }
}
