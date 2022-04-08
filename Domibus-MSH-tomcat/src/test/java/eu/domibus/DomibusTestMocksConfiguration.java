package eu.domibus;

import eu.domibus.core.plugin.BackendConnectorProvider;
import eu.domibus.web.security.AuthenticationService;
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
}
