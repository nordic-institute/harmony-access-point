package eu.domibus.core.message;

import eu.domibus.api.util.DateUtil;
import eu.domibus.core.message.dictionary.*;
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
                                               ReprogrammableService reprogrammableService,
                                               MshRoleDao mshRoleDao,
                                               MpcDao mpcDao) {
        return new UserMessageLogDao(dateUtil, userMessageLogInfoFilter, messageStatusDao, notificationStatusDao, reprogrammableService, mshRoleDao, mpcDao);
    }

    @Bean
    public MessageStatusDao messageStatusDao() {
        return new MessageStatusDao();
    }

    @Bean
    public NotificationStatusDao notificationStatusDao() {
        return new NotificationStatusDao();
    }

    @Bean
    public UserMessageDao userMessageDao(MessageStatusDao messageStatusDao, MshRoleDao mshRoleDao, PartyIdDao partyIdDao) {
        return new UserMessageDao(messageStatusDao, mshRoleDao, partyIdDao);
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




