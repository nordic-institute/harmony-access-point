package eu.domibus.ext.delegate;

import eu.domibus.ext.delegate.mapper.DomibusExtMapperImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Configuration("domibusExtMapperConfiguration")
public class DomibusExtMapperConfiguration {

    @Bean("domibusExtMapper")
    public DomibusExtMapperImpl domibusExtMapper() {
        return new DomibusExtMapperImpl();
    }
}
