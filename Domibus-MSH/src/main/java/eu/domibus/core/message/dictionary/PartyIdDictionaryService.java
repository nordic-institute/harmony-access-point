package eu.domibus.core.message.dictionary;

import eu.domibus.api.model.PartyId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.Callable;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
public class PartyIdDictionaryService extends AbstractDictionaryService {

    protected PartyIdDao partyIdDao;

    public PartyIdDictionaryService(PartyIdDao partyIdDao) {
        this.partyIdDao = partyIdDao;
    }

    @Transactional
    public PartyId findOrCreateParty(String value, String type) {
        Callable<PartyId> findTask = () -> partyIdDao.findExistingPartyId(value, type);
        Callable<PartyId> findOrCreateTask = () -> partyIdDao.findOrCreateParty(value, type);
        String entityDescription = "PartyId value=[" + value + "] type=[" + type + "]";

        return this.findOrCreateEntity(findTask, findOrCreateTask, entityDescription);
    }

}
