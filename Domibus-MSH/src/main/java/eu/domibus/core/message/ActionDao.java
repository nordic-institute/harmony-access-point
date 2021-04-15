package eu.domibus.core.message;


import eu.domibus.api.model.ActionEntity;
import eu.domibus.core.dao.BasicDao;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;


/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@Repository
public class ActionDao extends BasicDao<ActionEntity> {

    public ActionDao() {
        super(ActionEntity.class);
    }

    @Transactional
    public ActionEntity findOrCreateAction(String value) {
        ActionEntity mpc = findByValue(value);
        if (mpc != null) {
            return mpc;
        }
        ActionEntity newMpc = new ActionEntity();
        newMpc.setValue(value);
        create(newMpc);
        return newMpc;
    }

    public ActionEntity findByValue(final String value) {
        final TypedQuery<ActionEntity> query = this.em.createNamedQuery("Action.findByValue", ActionEntity.class);
        query.setParameter("VALUE", value);
        return DataAccessUtils.singleResult(query.getResultList());
    }
}
