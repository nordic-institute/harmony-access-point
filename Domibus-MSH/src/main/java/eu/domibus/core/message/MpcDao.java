package eu.domibus.core.message;

import eu.domibus.api.model.MpcEntity;
import eu.domibus.core.dao.BasicDao;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@Repository
public class MpcDao extends BasicDao<MpcEntity> {

    public MpcDao() {
        super(MpcEntity.class);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public MpcEntity findOrCreateMpc(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        MpcEntity mpc = findMpc(value);
        if (mpc != null) {
            return mpc;
        }
        MpcEntity newMpc = new MpcEntity();
        newMpc.setValue(value);
        create(newMpc);
        return newMpc;
    }

    public MpcEntity findMpc(final String mpc) {
        final TypedQuery<MpcEntity> query = this.em.createNamedQuery("Mpc.findByValue", MpcEntity.class);
        query.setParameter("MPC", mpc);
        return DataAccessUtils.singleResult(query.getResultList());
    }
}
