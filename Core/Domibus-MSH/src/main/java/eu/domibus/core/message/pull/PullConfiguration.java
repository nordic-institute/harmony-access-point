package eu.domibus.core.message.pull;

import eu.domibus.api.multitenancy.Domain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.internal.Function;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * @author Thomas Dussart
 * @since 4.1
 * <p>
 * Configuration class for the pull frequency control components.
 * Act as a factory for domain specific pull frequency configuration.
 */
@Configuration
public class PullConfiguration {

    @Bean
    public Function<Domain, DomainPullFrequencyHelper> beanFactory() {
        return this::prototypeBeanWithParam;
    }

    @Bean
    @Scope(value = "prototype")
    public DomainPullFrequencyHelper prototypeBeanWithParam(@Autowired(required = false) Domain domain) {
        return new DomainPullFrequencyHelper(domain);
    }

    @Bean
    public PullFrequencyHelper pullFrequencyHelper() {
        return new PullFrequencyHelper();
    }

}
