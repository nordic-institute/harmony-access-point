package eu.domibus.core.message;

import eu.domibus.api.model.ReceiptEntity;
import eu.domibus.core.dao.BasicDao;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@Repository
public class ReceiptDao extends BasicDao<ReceiptEntity> {

    private static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(ReceiptDao.class);

    public ReceiptDao() {
        super(ReceiptEntity.class);
    }

    public ReceiptEntity findBySignalRefToMessageId(String refToMessageId) {
        final TypedQuery<ReceiptEntity> query = em.createNamedQuery("Receipt.findBySignalRefToMessageId", ReceiptEntity.class);
        query.setParameter("REF_TO_MESSAGE_ID", refToMessageId);
        return DataAccessUtils.singleResult(query.getResultList());
    }

    @Timer(clazz = ReceiptDao.class, value = "deleteMessages")
    @Counter(clazz = ReceiptDao.class, value = "deleteMessages")
    public int deleteReceipts(List<Long> ids) {
        LOG.debug("deleteMessages [{}]", ids.size());
        final Query deleteQuery = em.createNamedQuery("Receipt.deleteMessages");
        deleteQuery.setParameter("IDS", ids);
        int result = deleteQuery.executeUpdate();
        LOG.debug("deleteMessages result [{}]", result);
        return result;
    }
}
