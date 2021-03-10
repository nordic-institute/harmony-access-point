package eu.domibus.ext.delegate.mapper;

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
public class MapperContextConfiguration {

    @Bean
    public AlertExtMapper alertExtMapper() {
        return new AlertExtMapperImpl();
    }

    @Bean
    public MessageExtMapper messageExtMapper() {
        return new MessageExtMapperImpl();
    }

    @Bean
    public PModeExtMapper pModeExtMapper() {
        return new PModeExtMapperImpl();
    }
}
