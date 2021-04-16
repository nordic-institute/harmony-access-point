package eu.domibus.core.message;

import eu.domibus.api.model.PartyId;
import eu.domibus.core.dao.BasicDao;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@Repository
public class PartyIdDao extends BasicDao<PartyId> {

    public PartyIdDao() {
        super(PartyId.class);
    }

    public PartyId findPartyByValue(final String value) {
        final TypedQuery<PartyId> query = this.em.createNamedQuery("PartyId.findByValue", PartyId.class);
        query.setParameter("VALUE", value);
        return DataAccessUtils.singleResult(query.getResultList());
    }

    @Transactional
    public PartyId findOrCreateParty(String value, String type) {
        PartyId party = findPartyByValue(value);
        if (party != null) {
            return party;
        }
        PartyId newParty = new PartyId();
        newParty.setValue(value);
        newParty.setType(type);
        create(newParty);
        return newParty;
    }
}
