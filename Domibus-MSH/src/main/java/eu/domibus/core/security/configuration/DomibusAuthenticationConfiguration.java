package eu.domibus.core.security.configuration;

import eu.domibus.web.security.AuthenticationService;
import eu.domibus.web.security.AuthenticationServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/**
 *
 *
 * @author Ion Perpegel
 * @since 5.0
 */
@Conditional(SecurityInternalAuthProviderCondition.class)
@Configuration
//@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class DomibusAuthenticationConfiguration {

    @Bean(name = "authenticationService")
    public AuthenticationService authenticationService() {
        return new AuthenticationServiceImpl();
    }

}
