package eu.domibus.core.message;

import eu.domibus.api.model.ReceiptEntity;
import eu.domibus.core.dao.BasicDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@Repository
public class ReceiptDao extends BasicDao<ReceiptEntity> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(ReceiptDao.class);

    public ReceiptDao() {
        super(ReceiptEntity.class);
    }

    public ReceiptEntity findBySignalRefToMessageId(String refToMessageId) {
        final TypedQuery<ReceiptEntity> query = em.createNamedQuery("Receipt.findBySignalRefToMessageId", ReceiptEntity.class);
        query.setParameter("REF_TO_MESSAGE_ID", refToMessageId);
        return DataAccessUtils.singleResult(query.getResultList());
    }
}
