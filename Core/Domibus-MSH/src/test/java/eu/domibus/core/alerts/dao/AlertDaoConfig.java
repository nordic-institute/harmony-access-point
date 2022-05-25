package eu.domibus.core.alerts.dao;

import eu.domibus.api.util.DateUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AlertDaoConfig {

    @Bean
    public AlertDao alertDao(DateUtil dateUtil) {
        return new AlertDao(dateUtil);
    }

    @Bean
    public EventDao eventDao(){
        return new EventDao();
    }

}
