package eu.domibus.core.message.dictionary;

import eu.domibus.api.model.PartProperty;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.Callable;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
public class PartPropertyDictionaryService extends AbstractDictionaryService {

    protected PartPropertyDao partPropertyDao;

    public PartPropertyDictionaryService(PartPropertyDao partPropertyDao) {
        this.partPropertyDao = partPropertyDao;
    }

    public PartProperty findOrCreatePartProperty(final String name, String value, String type) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        Callable<PartProperty> findTask = () -> partPropertyDao.findExistingProperty(name, value, type);
        Callable<PartProperty> findOrCreateTask = () -> partPropertyDao.findOrCreateProperty(name, value, type);
        String entityDescription = "PartProperty name=[" + name + "] value=[" + value + "] type=[" + type + "]";

        return this.findOrCreateEntity(findTask, findOrCreateTask, entityDescription);
    }

}
