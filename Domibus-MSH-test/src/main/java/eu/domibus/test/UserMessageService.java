package eu.domibus.test;

import eu.domibus.api.model.UserMessage;
import eu.domibus.core.message.UserMessageDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author François Gautier
 * @since 5.0
 */
@Service
public class UserMessageService {

    @Autowired
    UserMessageDao userMessageDao;

    @Transactional
    public void saveUserMessage(UserMessage userMessage) {
        userMessageDao.merge(userMessage);
    }

}
