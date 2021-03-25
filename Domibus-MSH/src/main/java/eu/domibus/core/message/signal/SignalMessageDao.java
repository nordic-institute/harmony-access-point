package eu.domibus.core.message.signal;

import eu.domibus.api.model.MessageType;
import eu.domibus.core.dao.BasicDao;
import eu.domibus.api.model.SignalMessage;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.dao.support.DataAccessUtils;
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

    public SignalMessage findSignalMessageByUserMessageEntityId(final Long userMessageEntityId) {
        final TypedQuery<SignalMessage> query = em.createNamedQuery("SignalMessage.findSignalMessageByUserMessageEntityId", SignalMessage.class);
        query.setParameter("ENTITY_ID", userMessageEntityId);
        return DataAccessUtils.singleResult(query.getResultList());
    }

    public List<SignalMessage> findSignalMessagesByRefMessageId(final String originalMessageId) {
        final TypedQuery<SignalMessage> query = em.createNamedQuery("SignalMessage.findSignalMessageByRefMessageId", SignalMessage.class);
        query.setParameter("ORI_MESSAGE_ID", originalMessageId);
        return query.getResultList();
    }

    public SignalMessage findSignalMessageWithUserMessageByMessageId(final String messageId) {
        final TypedQuery<SignalMessage> query = em.createNamedQuery("SignalMessage.findSignalMessageWithUserMessageByUserMessageId", SignalMessage.class);
        query.setParameter("MESSAGE_ID", messageId);
        return DataAccessUtils.singleResult(query.getResultList());
    }

    public List<String> findSignalMessageIds(List<String> userMessageIds) {
        final TypedQuery<String> query = em.createNamedQuery("SignalMessage.findMessageIdsWithRefToMessageIds", String.class);
        query.setParameter("MESSAGEIDS", userMessageIds);
        List<String> messageIds = query.getResultList();
        LOG.debug("Found ids [{}]", messageIds);
        return messageIds;
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

    @Timer(clazz = SignalMessageDao.class,value = "deleteMessages.deleteReceipts")
    @Counter(clazz = SignalMessageDao.class,value = "deleteMessages.deleteReceipts")
    public int deleteReceipts(List<Long> receiptIds) {
        final Query deleteQuery = em.createNamedQuery("Receipt.deleteReceipts");
        deleteQuery.setParameter("RECEIPTIDS", receiptIds);
        int result  = deleteQuery.executeUpdate();
        LOG.trace("deleteReceipts result [{}]", result);
        return result;
    }

    @Timer(clazz = SignalMessageDao.class, value = "deleteMessages")
    @Counter(clazz = SignalMessageDao.class, value = "deleteMessages")
    public int deleteMessages(List<String> messageIds) {
        LOG.debug("deleteMessages [{}]", messageIds.size());
        final Query deleteQuery = em.createNamedQuery("SignalMessage.deleteMessages");
        deleteQuery.setParameter("MESSAGEIDS", messageIds);
        int result = deleteQuery.executeUpdate();
        LOG.debug("deleteMessages result [{}]", result);
        return result;
    }

}
