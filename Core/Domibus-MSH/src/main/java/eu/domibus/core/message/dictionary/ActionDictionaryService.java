package eu.domibus.core.message.dictionary;

import eu.domibus.api.model.ActionEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.Callable;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
public class ActionDictionaryService extends AbstractDictionaryService {

    protected ActionDao actionDao;

    public ActionDictionaryService(ActionDao actionDao) {
        this.actionDao = actionDao;
    }

    public ActionEntity findOrCreateAction(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        Callable<ActionEntity> findTask = () -> actionDao.findByValue(value);
        Callable<ActionEntity> findOrCreateTask = () -> actionDao.findOrCreateAction(value);
        String entityDescription = "ActionEntity value=[" + value + "]";

        return this.findOrCreateEntity(findTask, findOrCreateTask, entityDescription);
    }

}
