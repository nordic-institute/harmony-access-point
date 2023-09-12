package eu.domibus.core.participant;

import eu.domibus.api.model.participant.FinalRecipientEntity;
import eu.domibus.core.dao.BasicDao;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;
import java.util.Date;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 5.0.2
 */

@Repository
public class FinalRecipientDao extends BasicDao<FinalRecipientEntity> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FinalRecipientDao.class);

    public FinalRecipientDao() {
        super(FinalRecipientEntity.class);
    }

    @Timer(clazz = FinalRecipientDao.class, value = "findEndpointUrl")
    @Counter(clazz = FinalRecipientDao.class, value = "findEndpointUrl")
    public FinalRecipientEntity findByFinalRecipient(String finalRecipient) {
        final TypedQuery<FinalRecipientEntity> query = em.createNamedQuery("FinalRecipientEntity.findByFinalRecipient", FinalRecipientEntity.class);
        query.setParameter("FINAL_RECIPIENT", finalRecipient);
        return DataAccessUtils.singleResult(query.getResultList());
    }

    @Transactional
    public void createOrUpdate(FinalRecipientEntity finalRecipientEntity) {
        if (finalRecipientEntity.getEntityId() > 0) {
            update(finalRecipientEntity);
            return;
        }
        //create
        create(finalRecipientEntity);
    }

    public List<FinalRecipientEntity> findFinalRecipientsOlderThan(int numberOfDays) {
        Date dateLimit = DateUtils.addDays(new Date(), numberOfDays  * -1);
        final TypedQuery<FinalRecipientEntity> query = em.createNamedQuery("FinalRecipientEntity.findFinalRecipientsModifiedBefore", FinalRecipientEntity.class);
        query.setParameter("MODIFICATION_DATE", dateLimit);
        return query.getResultList();
    }
}
