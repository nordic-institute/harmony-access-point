package eu.domibus.ext.delegate.mapper;

import eu.domibus.api.earchive.DomibusEArchiveService;
import eu.domibus.ext.delegate.services.earchive.DomibusEArchiveServiceDelegate;
import eu.domibus.ext.rest.DomibusEArchiveExtResource;
import eu.domibus.ext.services.DomibusEArchiveExtService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/**
 * @author François Gautier
 * @since 5.0
 */
@Configuration
@ImportResource({
        "classpath:config/commonsTestContext.xml"
})
public class TestMapperContextConfiguration {

    @Bean
    public AlertExtMapper alertExtMapper() {
        return new AlertExtMapperImpl();
    }

    @Bean
    public MessageExtMapper messageExtMapper() {
        return new MessageExtMapperImpl();
    }

    @Bean
    public EArchiveExtMapper eArchiveExtMapper() {
        return new EArchiveExtMapperImpl();
    }

    @Bean
    public MonitoringExtMapper monitoringExtMapper() {
        return new MonitoringExtMapperImpl();
    }

    @Bean
    public MonitoringExtMapperImpl_ monitoringExtMapperDelegate() {
        return new MonitoringExtMapperImpl_();
    }

    @Bean
    public DomibusExtMapper domibusExtMapper() {
        return new DomibusExtMapperImpl();
    }

    @Bean
    public DomibusExtMapperImpl_ domibusExtMapperDelegate() {
        return new DomibusExtMapperImpl_();
    }
}
