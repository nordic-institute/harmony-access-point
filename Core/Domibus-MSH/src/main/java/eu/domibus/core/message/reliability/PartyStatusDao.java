package eu.domibus.core.message.reliability;

import eu.domibus.core.dao.BasicDao;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

/**
 * @author Razvan Cretu
 * @since 5.1
 */
@Repository
public class PartyStatusDao extends BasicDao<PartyStatusEntity> {

    public PartyStatusDao() {
        super(PartyStatusEntity.class);
    }

    public PartyStatusEntity findByName(String name) {
        TypedQuery<PartyStatusEntity> q = em.createNamedQuery("PartyStatus.findByName", PartyStatusEntity.class);
        q.setParameter("PARTY_NAME", name);
        return q.getSingleResult();
    }

    public PartyStatusEntity findByNameSafely(String name) {
        try {
            return findByName(name);
        } catch (NoResultException ex) {
            return null;
        }
    }

    public boolean existsWithName(String name) {
        Query q = em.createNamedQuery("PartyStatus.countByName", Long.class);
        q.setParameter("PARTY_NAME", name);
        return (Long) q.getSingleResult() > 0;
    }

}
