package eu.domibus.core.message;

import eu.domibus.api.model.AgreementRef;
import eu.domibus.core.dao.BasicDao;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@Repository
public class AgreementDao extends BasicDao<AgreementRef> {

    public AgreementDao() {
        super(AgreementRef.class);
    }

    public AgreementRef findOrCreateAgreement(String value, String type) {
        AgreementRef agreementRef = findByValue(value);
        if (agreementRef != null) {
            return agreementRef;
        }
        AgreementRef newAgreement = new AgreementRef();
        newAgreement.setValue(value);
        newAgreement.setType(type);
        create(newAgreement);
        return newAgreement;
    }

    public AgreementRef findByValue(final String value) {
        final TypedQuery<AgreementRef> query = this.em.createNamedQuery("AgreementRef.findByValue", AgreementRef.class);
        query.setParameter("VALUE", value);
        return DataAccessUtils.singleResult(query.getResultList());
    }
}
