package eu.domibus.plugin.ws.webservice.configuration;

import eu.domibus.plugin.environment.TomcatCondition;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * Class responsible for the configuration of the ws plugin for Tomcat
 *
 * @author François Gautier
 * @since 5.0
 */
@Conditional(TomcatCondition.class)
@Configuration
public class WebServiceTomcatConfiguration {

    @Bean("notifyBackendWebServiceQueue")
    public ActiveMQQueue notifyBackendWSQueue() {
        return new ActiveMQQueue("domibus.notification.webservice");
    }

}
