package eu.domibus;

import eu.domibus.user.MockAuthenticationService;
import eu.domibus.web.security.AuthenticationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author Ion Perpegel
 * @since 4.2
 */
@Configuration
public class DomibusTestMocksConfiguration {

    @Primary
    @Bean()
    public AuthenticationService authenticationService() {
        return new MockAuthenticationService();
    }

}
