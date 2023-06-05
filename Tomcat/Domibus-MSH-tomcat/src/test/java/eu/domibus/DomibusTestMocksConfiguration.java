package eu.domibus;

import eu.domibus.core.ebms3.sender.ResponseHandler;
import eu.domibus.core.ebms3.sender.client.MSHDispatcher;
import eu.domibus.core.message.reliability.ReliabilityChecker;
import eu.domibus.core.plugin.BackendConnectorHelper;
import eu.domibus.core.plugin.BackendConnectorProvider;
import eu.domibus.plugin.notification.PluginAsyncNotificationConfiguration;
import eu.domibus.web.security.AuthenticationService;
import org.apache.activemq.command.ActiveMQQueue;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Primary;

/**
 * @author Ion Perpegel
 * @since 4.2
 */
@Configuration
@ImportResource({
        "classpath:config/commonsTestContext.xml"
})
public class DomibusTestMocksConfiguration {

    @Primary
    @Bean()
    public AuthenticationService authenticationService() {
        return new MockAuthenticationService();
    }

    @Primary
    @Bean
    public BackendConnectorProvider backendConnectorProvider() {
        return Mockito.mock(BackendConnectorProvider.class);
    }

    @Primary
    @Bean
    public BackendConnectorHelper backendConnectorHelper() {
        return Mockito.mock(BackendConnectorHelper.class);
    }

    @Primary
    @Bean
    PluginAsyncNotificationConfiguration pluginAsyncNotificationConfiguration() {
        return Mockito.mock(PluginAsyncNotificationConfiguration.class);
    }

    @Primary
    @Bean("notifyBackendWebServiceQueue")
    public ActiveMQQueue notifyBackendWSQueue() {
        return new ActiveMQQueue("domibus.notification.webservice");
    }

    @Primary
    @Bean
    MSHDispatcher mshDispatcher() {
        return Mockito.mock(MSHDispatcher.class);
    }

    @Primary
    @Bean
    ResponseHandler responseHandler() {
        return Mockito.mock(ResponseHandler.class);
    }

    @Primary
    @Bean
    ReliabilityChecker reliabilityChecker() {
        return Mockito.mock(ReliabilityChecker.class);
    }

}
