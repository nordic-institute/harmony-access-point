package eu.domibus.core.util;

import eu.domibus.api.util.DateUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Sebastian-Ion TINCU
 * @since 5.0
 */
@Configuration
public class UtilConfig {

    @Bean
    public DateUtil dateUtil() {
        return new DateUtilImpl();
    }
}
