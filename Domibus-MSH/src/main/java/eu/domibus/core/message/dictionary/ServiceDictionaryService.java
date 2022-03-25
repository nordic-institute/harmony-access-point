package eu.domibus.core.message.dictionary;

import eu.domibus.api.model.ServiceEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.Callable;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
public class ServiceDictionaryService extends AbstractDictionaryService {

    protected ServiceDao serviceDao;

    public ServiceDictionaryService(ServiceDao serviceDao) {
        this.serviceDao = serviceDao;
    }

    public ServiceEntity findOrCreateService(String value, String type) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        Callable<ServiceEntity> findTask = () -> serviceDao.findExistingService(value, type);
        Callable<ServiceEntity> findOrCreateTask = () -> serviceDao.findOrCreateService(value, type);
        String entityDescription = "ServiceEntity value=[" + value + "] type=[" + type + "]";

        return this.findOrCreateEntity(findTask, findOrCreateTask, entityDescription);
    }

}
