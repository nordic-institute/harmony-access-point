package eu.domibus.core.message;

import eu.domibus.api.model.AgreementRefEntity;
import eu.domibus.core.dao.BasicDao;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@Repository
public class AgreementDao extends BasicDao<AgreementRefEntity> {

    public AgreementDao() {
        super(AgreementRefEntity.class);
    }

    @Transactional
    public AgreementRefEntity findOrCreateAgreement(String value, String type) {
        if(StringUtils.isEmpty(value)) {
            return null;
        }

        AgreementRefEntity agreementRef = findByValue(value);
        if (agreementRef != null) {
            return agreementRef;
        }
        AgreementRefEntity newAgreement = new AgreementRefEntity();
        newAgreement.setValue(value);
        newAgreement.setType(type);
        create(newAgreement);
        return newAgreement;
    }

    public AgreementRefEntity findByValue(final String value) {
        final TypedQuery<AgreementRefEntity> query = this.em.createNamedQuery("AgreementRef.findByValue", AgreementRefEntity.class);
        query.setParameter("VALUE", value);
        return DataAccessUtils.singleResult(query.getResultList());
    }
}
