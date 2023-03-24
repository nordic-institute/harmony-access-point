package eu.domibus.core.scheduler;

import eu.domibus.core.time.TimezoneOffsetService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Sebastian-Ion TINCU
 * @since 5.0
 */
@Configuration
public class SchedulerConfig {

    @Bean
    public ReprogrammableService reprogrammableService(TimezoneOffsetService timezoneOffsetService) {
        return new ReprogrammableServiceImpl(timezoneOffsetService);
    }
}
