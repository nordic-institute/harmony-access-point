package eu.domibus.plugin;

import eu.domibus.ext.services.JMSExtService;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import javax.jms.Queue;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Configuration
public class QueueMessageListerConfiguration {

    protected JMSExtService jmsExtService;

    public QueueMessageListerConfiguration(JMSExtService jmsExtService) {
        this.jmsExtService = jmsExtService;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public QueueMessageLister createQueueMessageLister(Queue backendNotificationQueue, String backendName) {
        return new QueueMessageLister(jmsExtService, backendNotificationQueue, backendName);
    }
}
