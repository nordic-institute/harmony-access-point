package eu.domibus.core.security.configuration;

import eu.domibus.web.filter.CookieFilter;
import eu.domibus.web.filter.SetDomainFilter;
import eu.domibus.web.matcher.URLCsrfMatcher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.ConcurrentSessionControlAuthenticationStrategy;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.Arrays;

/**
 * Spring Security configuration which contains common beans to be instantiated
 *
 * @author Catalin Enache
 * @since 4.1
 */
@Configuration
public class SecurityBaseConfiguration {

    @Bean
    public CsrfTokenRepository tokenRepository() {
        CookieCsrfTokenRepository csrfTokenRepository = new CookieCsrfTokenRepository();
        csrfTokenRepository.setCookieHttpOnly(false);
        return csrfTokenRepository;
    }

    @Bean
    public RequestMatcher csrfURLMatcher() {
        URLCsrfMatcher requestMatcher = new URLCsrfMatcher();
        requestMatcher.setIgnoreUrl("/rest/security/authentication");
        return requestMatcher;
    }

    @Bean
    public SetDomainFilter setDomainFilter() {
        return new SetDomainFilter();
    }

    @Bean
    public CookieFilter coockieFilter() {
        return new CookieFilter();
    }

    @Bean
    public Http403ForbiddenEntryPoint http403ForbiddenEntryPoint() {
        return new Http403ForbiddenEntryPoint();
    }

    @Bean
    public BCryptPasswordEncoder bcryptEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public SessionAuthenticationStrategy csas(SessionRegistry sessionRegistry) {
        ConcurrentSessionControlAuthenticationStrategy csas = new ConcurrentSessionControlAuthenticationStrategy(sessionRegistry);
        csas.setMaximumSessions(1);
        return csas;
    }

    @Bean
    public SessionAuthenticationStrategy rsas(SessionRegistry sessionRegistry) {
        RegisterSessionAuthenticationStrategy rsas = new RegisterSessionAuthenticationStrategy(sessionRegistry);
        return rsas;
    }

    @Bean
    public CompositeSessionAuthenticationStrategy sas(SessionRegistry sessionRegistry) {
        CompositeSessionAuthenticationStrategy sas = new CompositeSessionAuthenticationStrategy(Arrays.asList(csas(sessionRegistry), rsas(sessionRegistry)));
        return sas;
    }
}
