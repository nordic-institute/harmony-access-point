package eu.domibus.core.message;

import eu.domibus.api.model.Action;
import eu.domibus.core.dao.BasicDao;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@Repository
public class ActionDao extends BasicDao<Action> {

    public ActionDao() {
        super(Action.class);
    }

    public Action findOrCreateAction(String value) {
        Action mpc = findByValue(value);
        if (mpc != null) {
            return mpc;
        }
        Action newMpc = new Action();
        newMpc.setValue(value);
        create(newMpc);
        return newMpc;
    }

    public Action findByValue(final String value) {
        final TypedQuery<Action> query = this.em.createNamedQuery("Action.findByValue", Action.class);
        query.setParameter("VALUE", value);
        return DataAccessUtils.singleResult(query.getResultList());
    }
}
