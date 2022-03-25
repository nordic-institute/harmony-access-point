package eu.domibus.core.message.dictionary;

import eu.domibus.api.model.MessageProperty;
import org.springframework.stereotype.Service;

import java.util.concurrent.Callable;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
public class MessagePropertyDictionaryService extends AbstractDictionaryService {
    protected MessagePropertyDao messagePropertyDao;

    public MessagePropertyDictionaryService(MessagePropertyDao messagePropertyDao) {
        this.messagePropertyDao = messagePropertyDao;
    }

    public MessageProperty findOrCreateMessageProperty(final String name, String value, String type) {
        Callable<MessageProperty> findTask = () -> messagePropertyDao.findExistingProperty(name, value, type);
        Callable<MessageProperty> findOrCreateTask = () -> messagePropertyDao.findOrCreateProperty(name, value, type);
        String entityDescription = "MessageProperty name=[" + name + "] value=[" + value + "] type=[" + type + "]";

        return this.findOrCreateEntity(findTask, findOrCreateTask, entityDescription);
    }

}
