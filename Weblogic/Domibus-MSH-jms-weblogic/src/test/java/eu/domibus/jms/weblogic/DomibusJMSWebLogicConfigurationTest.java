package eu.domibus.jms.weblogic;

import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.ConnectionFactory;
import javax.jms.Session;

@RunWith(JMockit.class)
public class DomibusJMSWebLogicConfigurationTest {

    @Tested
    DomibusJMSWebLogicConfiguration domibusJMSWebLogicConfiguration;

    @Test
    public void jmsSender(@Injectable ConnectionFactory connectionFactory,
                          @Mocked JmsTemplate jmsTemplate) {
        domibusJMSWebLogicConfiguration.jmsSender(connectionFactory);

        new Verifications() {{
            jmsTemplate.setSessionTransacted(true);
            jmsTemplate.setSessionAcknowledgeMode(Session.SESSION_TRANSACTED);
            jmsTemplate.setConnectionFactory(connectionFactory);
        }};
    }
}