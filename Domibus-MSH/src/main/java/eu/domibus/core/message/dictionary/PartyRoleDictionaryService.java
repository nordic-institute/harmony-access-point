package eu.domibus.core.message.dictionary;

import eu.domibus.api.model.PartyRole;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.Callable;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
public class PartyRoleDictionaryService extends AbstractDictionaryService {

    protected PartyRoleDao partyRoleDao;

    public PartyRoleDictionaryService(PartyRoleDao partyRoleDao) {
        this.partyRoleDao = partyRoleDao;
    }

    @Transactional
    public PartyRole findOrCreateRole(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        Callable<PartyRole> findTask = () -> partyRoleDao.findRoleByValue(value);
        Callable<PartyRole> findOrCreateTask = () -> partyRoleDao.findOrCreateRole(value);
        String entityDescription = "PartyRole value=[" + value + "]";

        return this.findOrCreateEntity(findTask, findOrCreateTask, entityDescription);
    }

}
