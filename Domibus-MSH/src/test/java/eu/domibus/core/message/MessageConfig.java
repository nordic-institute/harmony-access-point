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
    public UserMessageLogDao userMessageLogDao(DateUtil dateUtil,
                                               UserMessageLogInfoFilter userMessageLogInfoFilter,
                                               MessageStatusDao messageStatusDao,
                                               NotificationStatusDao notificationStatusDao,
                                               ReprogrammableService reprogrammableService) {
        return new UserMessageLogDao(dateUtil, userMessageLogInfoFilter, messageStatusDao, notificationStatusDao, reprogrammableService);
    }

    @Bean
    public MessageInfoDao messageInfoDao() {
        return new MessageInfoDao();
    }

    @Bean
    public MessagePropertyDao propertyDao() {
        return new MessagePropertyDao();
    }

    @Bean
    public UserMessageLogInfoFilter userMessageLogInfoFilter() {
        return new UserMessageLogInfoFilter();
    }
}




