package eu.domibus.core.time;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Sebastian-Ion TINCU
 * @since 5.0
 */
@Configuration
public class TimeConfig {

    @Bean
    public TimezoneOffsetDao timezoneOffsetDao() {
        return new TimezoneOffsetDao();
    }

    @Bean
    public TimezoneOffsetService timezoneOffsetService(TimezoneOffsetDao timezoneOffsetDao) {
        return new TimezoneOffsetService(timezoneOffsetDao);
    }
}
