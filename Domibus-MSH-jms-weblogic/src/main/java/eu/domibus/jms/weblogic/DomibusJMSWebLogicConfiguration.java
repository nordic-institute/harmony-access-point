package eu.domibus.jms.weblogic;

import eu.domibus.api.jms.JMSConstants;
import eu.domibus.jms.spi.helper.PriorityJmsTemplate;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.ConnectionFactory;
import javax.jms.Session;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Configuration
public class DomibusJMSWebLogicConfiguration {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DomibusJMSWebLogicConfiguration.class);

    @Bean("jmsSender")
    public JmsTemplate jmsSender(@Qualifier(JMSConstants.DOMIBUS_JMS_XACONNECTION_FACTORY) ConnectionFactory connectionFactory) {
        PriorityJmsTemplate result = new PriorityJmsTemplate();
        result.setSessionTransacted(true);
        result.setSessionAcknowledgeMode(Session.SESSION_TRANSACTED);
        result.setConnectionFactory(connectionFactory);

        return result;
    }
}
