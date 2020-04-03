package eu.domibus.ext.rest.spring;

import eu.domibus.ext.web.interceptor.AuthenticationInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Ion Perpegel
 * @since 4.2
 */
@EnableWebMvc
@Configuration("domibusExtWebConfiguration")
@ComponentScan(basePackages = "eu.domibus.ext.rest")
public class DomibusExtWebConfiguration implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(authenticationInterceptor())
                .addPathPatterns("/ext/**");

    }

    @Bean
    AuthenticationInterceptor authenticationInterceptor () {
        return new AuthenticationInterceptor();
    }

}
