package eu.domibus.core.message;

import eu.domibus.api.model.PartyId;
import eu.domibus.api.model.PartyRole;
import eu.domibus.core.dao.BasicDao;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;

/**
 * @author François Gautier
 * @since 5.0
 */
@Repository
public class PartyRoleDao extends BasicDao<PartyRole> {

    public PartyRoleDao() {
        super(PartyRole.class);
    }

    public PartyRole findRoleByValue(final String value) {
        final TypedQuery<PartyRole> query = this.em.createNamedQuery("PartyRole.findByValue", PartyRole.class);
        query.setParameter("VALUE", value);
        return DataAccessUtils.singleResult(query.getResultList());
    }

    public PartyRole findOrCreateParty(String value) {
        PartyRole role = findRoleByValue(value);
        if (role != null) {
            return role;
        }
        PartyRole newRole = new PartyRole();
        newRole.setRole(value);
        create(newRole);
        return newRole;
    }
}
