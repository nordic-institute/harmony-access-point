package eu.domibus.core.message.dictionary;

import eu.domibus.api.model.AgreementRefEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.concurrent.Callable;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
public class AgreementDictionaryService extends AbstractDictionaryService{

    protected AgreementDao agreementDao;

    public AgreementDictionaryService(AgreementDao agreementDao) {
        this.agreementDao = agreementDao;
    }

    public AgreementRefEntity findOrCreateAgreement(String value, String type) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        Callable<AgreementRefEntity> findTask = () -> agreementDao.findExistingAgreement(value, type);
        Callable<AgreementRefEntity> findOrCreateTask = () -> agreementDao.findOrCreateAgreement(value, type);
        String entityDescription = "AgreementRefEntity value=[" + value + "] type=[" + type + "]";

        return this.findOrCreateEntity(findTask, findOrCreateTask, entityDescription);
    }

}
