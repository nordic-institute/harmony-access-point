package eu.domibus.plugin.jms;

import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.DomibusPropertyExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.handler.MessageRetriever;
import eu.domibus.plugin.jms.property.JmsPluginPropertyManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jndi.JndiObjectFactoryBean;

import javax.jms.ConnectionFactory;

import static eu.domibus.plugin.jms.JMSMessageConstants.CONNECTIONFACTORY;
import static eu.domibus.plugin.jms.JMSMessageConstants.JMS_PLUGIN_PROPERTY_PREFIX;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Configuration
public class BackendJMSConfiguration {

    public static final String DOMIBUS_JMS_PLUGIN_CONNECTION_FACTORY = "domibusJMSPlugin-ConnectionFactory";

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(BackendJMSConfiguration.class);

    @Bean
    public BackendJMSQueueService backendJMSQueueService(DomibusPropertyExtService domibusPropertyExtService,
                                                         DomainContextExtService domainContextExtService,
                                                         JmsPluginPropertyManager jmsPluginPropertyManager,
                                                         MessageRetriever messageRetriever) {
        LOG.warn("!!!!!!!!!!!~~~~~~~~~~~~~~~~~~~~~~~BEAN backendJMSQueueService backendJMSQueueService~~~~~~~~~~~~~~~~~~~~~~~~~~!!!!!!!!!!!!!!!!!");
        return new BackendJMSQueueService(domibusPropertyExtService, domainContextExtService, jmsPluginPropertyManager, messageRetriever);
    }

//    @Bean(DOMIBUS_JMS_PLUGIN_CONNECTION_FACTORY)
//    public JndiObjectFactoryBean jmsPluginConnectionFactory(DomibusPropertyExtService domibusPropertyExtService) {
//
//        LOG.warn("!!!!!!!!!!!~~~~~~~~~~~~~~~~~~~~~~~BEAN CONNECTION FACTORY~~~~~~~~~~~~~~~~~~~~~~~~~~!!!!!!!!!!!!!!!!!");
//
//        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
//        final String connectioFactoryJNDI = domibusPropertyExtService.getProperty(JMS_PLUGIN_PROPERTY_PREFIX + "." + CONNECTIONFACTORY);
//        LOG.warn("!!!!!!!!!!!~~~~~~~~~~~~~~~~~~~~~~~[{}]~~~~~~~~~~~~~~~~~~~~~~~~~~!!!!!!!!!!!!!!!!!", connectioFactoryJNDI);
//        jndiObjectFactoryBean.setJndiName(connectioFactoryJNDI);
//        jndiObjectFactoryBean.setLookupOnStartup(false);
//        jndiObjectFactoryBean.setExpectedType(ConnectionFactory.class);
//        return jndiObjectFactoryBean;
//    }
}
