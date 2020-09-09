package eu.domibus.core.security.configuration;

import eu.domibus.api.security.AuthRole;
import eu.domibus.core.security.UserDetailServiceImpl;
import eu.domibus.web.security.AuthenticationService;
import eu.domibus.web.security.AuthenticationServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Default Spring security config for Domibus
 *
 * @author Catalin Enache
 * @since 4.1
 */
@Conditional(SecurityInternalAuthProviderCondition.class)
@Configuration
@EnableWebSecurity
@EnableAspectJAutoProxy
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityAdminConsoleConfiguration extends AbstractWebSecurityConfigurerAdapter {

    @Autowired
    UserDetailServiceImpl userDetailService;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    SessionRegistry sessionRegistry;

    @Autowired
    ExpiredSessionStrategy expiredSessionStrategy;

    @Bean(name = "authenticationManagerForAdminConsole")
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean(name = "authenticationService")
    public AuthenticationService authenticationService() {
        return new AuthenticationServiceImpl();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailService);
        provider.setPasswordEncoder(bCryptPasswordEncoder);
        return provider;
    }

    @Override
    public void configureHttpSecurity(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .authorizeRequests()
                .antMatchers("/rest/security/user/domain").hasAnyAuthority(AuthRole.ROLE_USER.name(), AuthRole.ROLE_ADMIN.name(), AuthRole.ROLE_AP_ADMIN.name())
                .and()
                .sessionManagement()
                .maximumSessions(10)
                .maxSessionsPreventsLogin(false)
                .expiredSessionStrategy(expiredSessionStrategy)
                .sessionRegistry(sessionRegistry)
        ;
    }

    @Override
    protected void configureWebSecurity(WebSecurity web) throws Exception {
        //nothing here
    }

    @Autowired
    @Override
    protected void configureAuthenticationManagerBuilder(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(daoAuthenticationProvider());
    }

}
