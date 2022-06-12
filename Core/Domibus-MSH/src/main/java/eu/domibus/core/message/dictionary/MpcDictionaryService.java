package eu.domibus.core.message.dictionary;

import eu.domibus.api.model.MpcEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.Callable;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
public class MpcDictionaryService extends AbstractDictionaryService {

    protected MpcDao mpcDao;

    public MpcDictionaryService(MpcDao mpcDao) {
        this.mpcDao = mpcDao;
    }

    public MpcEntity findOrCreateMpc(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        Callable<MpcEntity> findTask = () -> mpcDao.findMpc(value);
        Callable<MpcEntity> findOrCreateTask = () -> mpcDao.findOrCreateMpc(value);
        String entityDescription = "MpcEntity value=[" + value + "]";

        return this.findOrCreateEntity(findTask, findOrCreateTask, entityDescription);
    }

}
