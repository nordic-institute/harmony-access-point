package eu.domibus.weblogic.security;

import eu.domibus.core.security.configuration.AbstractWebSecurityConfigurerAdapter;
import eu.domibus.core.security.configuration.SecurityExternalAuthProviderCondition;
import eu.domibus.web.security.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/**
 * Spring security configuration file for EU Login.
 *
 * @author Catalin Enache
 * @since 4.1
 */
@Conditional(SecurityExternalAuthProviderCondition.class)
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Configuration
/*
 * Please ensure updating eu.domibus.core.spring.DomibusSessionInitializer whenever renaming or moving this class to a
 * different package. The Spring session should not bootstrap in EU Login because of the issues it causes with the
 * existing infrastructure (i.e. SNET's reverse proxy mappings/load balancer don't work with the cookie serializer
 * writing the JSESSIONID cookie to the client).
 */
public class ECASSecurityConfiguration extends AbstractWebSecurityConfigurerAdapter {

    @Autowired
    ECASUserDetailsService ecasUserDetailsService;

    @Bean(name = "authenticationService")
    public AuthenticationService authenticationService() {
        return new ECASAuthenticationServiceImpl();
    }

    @Override
    public void configureWebSecurity(WebSecurity web) throws Exception {
        web
                .ignoring().antMatchers("/logout/**");
    }

    @Override
    public void configureHttpSecurity(HttpSecurity http) throws Exception {
        http
                .jee().authenticatedUserDetailsService(ecasUserDetailsService)
                .and()
                .sessionManagement().sessionFixation().none()
                .and()
                .authorizeRequests()
                .antMatchers( "/rest/security/user/domain").authenticated();

    }

    @Autowired
    @Override
    protected void configureAuthenticationManagerBuilder(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(ecasUserDetailsService);
    }


}
