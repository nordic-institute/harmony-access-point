package eu.domibus.core.security.configuration;

import eu.domibus.web.security.AuthenticationService;
import eu.domibus.web.security.AuthenticationServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/**
 * Spin-off from SecurityAdminConsoleConfiguration to avoid cyclic dependency due to authenticationService
 *
 * @author Ion Perpegel
 * @since 5.0
 */
@Conditional(SecurityInternalAuthProviderCondition.class)
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class DomibusAuthenticationConfiguration {

    @Bean(name = "authenticationService")
    public AuthenticationService authenticationService() {
        return new AuthenticationServiceImpl();
    }

}
