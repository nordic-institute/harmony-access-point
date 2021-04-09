package eu.domibus.core.message;

import eu.domibus.api.util.DateUtil;
import eu.domibus.core.scheduler.ReprogrammableService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author François Gautier
 * @since 5.0
 */
@Configuration
public class MessageConfig {
    @Bean
    public UserMessageLogDao userMessageLogDao(DateUtil dateUtil, UserMessageLogInfoFilter userMessageLogInfoFilter, ReprogrammableService reprogrammableService) {
        return new UserMessageLogDao(dateUtil, userMessageLogInfoFilter, reprogrammableService);
    }

    @Bean
    public MessageInfoDao messageInfoDao() {
        return new MessageInfoDao();
    }

    @Bean
    public PropertyDao propertyDao() {
        return new PropertyDao();
    }

    @Bean
    public UserMessageLogInfoFilter userMessageLogInfoFilter() {
        return new UserMessageLogInfoFilter();
    }
}




