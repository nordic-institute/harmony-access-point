package eu.domibus.core.message.signal;

import eu.domibus.core.dao.BasicDao;
import eu.domibus.ebms3.common.model.SignalMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * @author Christian Koch, Stefan Mueller
 * @since 3.0
 */

@Repository
public class SignalMessageDao extends BasicDao<SignalMessage> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SignalMessageDao.class);

    public SignalMessageDao() {
        super(SignalMessage.class);
    }

    public List<SignalMessage> findSignalMessagesByRefMessageId(final String originalMessageId) {
        final TypedQuery<SignalMessage> query = em.createNamedQuery("SignalMessage.findSignalMessageByRefMessageId", SignalMessage.class);
        query.setParameter("ORI_MESSAGE_ID", originalMessageId);
        return query.getResultList();
    }

    public List<String> findSignalMessageIdsByRefMessageId(final String originalMessageId) {
        final TypedQuery<String> query = em.createNamedQuery("SignalMessage.findSignalMessageIdByRefMessageId", String.class);
        query.setParameter("ORI_MESSAGE_ID", originalMessageId);
        return query.getResultList();
    }

    public List<Long> findReceiptIdsByMessageIds(List<String> messageIds) {
        TypedQuery<Long> query = em.createNamedQuery("SignalMessage.findReceiptIdsByMessageIds", Long.class);
        query.setParameter("MESSAGEIDS", messageIds);
        return query.getResultList();
    }

    public int deleteReceipts(List<Long> receiptIds) {
        final Query deleteQuery = em.createNamedQuery("Receipt.deleteReceipts");
        deleteQuery.setParameter("RECEIPTIDS", receiptIds);
        int result  = deleteQuery.executeUpdate();
        LOG.trace("deleteReceipts result [{}]", result);
        return result;
    }

    /**
     * Clear receipts of the Signal Message.
     *
     * @param signalMessage the signal message
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void clear(final SignalMessage signalMessage) {
        if (signalMessage.getReceipt() != null) {
            signalMessage.getReceipt().getAny().clear();
        }
        signalMessage.setReceipt(null);
        update(signalMessage);
        LOG.debug("Xml data for signal message [" + signalMessage.getMessageInfo().getMessageId() + "] have been cleared");
    }


}
