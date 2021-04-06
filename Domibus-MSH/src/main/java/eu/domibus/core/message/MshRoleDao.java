package eu.domibus.core.message;

import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MSHRoleEntity;
import eu.domibus.core.dao.BasicDao;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@Repository
public class MshRoleDao extends BasicDao<MSHRoleEntity> {

    public MshRoleDao() {
        super(MSHRoleEntity.class);
    }

    public MSHRoleEntity findByRole(final MSHRole role) {
        final TypedQuery<MSHRoleEntity> query = this.em.createNamedQuery("MSHRoleEntity.findByValue", MSHRoleEntity.class);
        query.setParameter("ROLE", role);
        return DataAccessUtils.singleResult(query.getResultList());
    }
}
