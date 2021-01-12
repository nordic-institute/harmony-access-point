package eu.domibus.core.message;

import eu.domibus.api.model.MessageType;
import eu.domibus.api.model.Messaging;
import eu.domibus.core.dao.BasicDao;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * @author idragusa
 * @since 4.2
 */

@Repository
public class MessageInfoDao extends BasicDao<Messaging> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageInfoDao.class);

    public MessageInfoDao() {
        super(Messaging.class);
    }

    public List<String> findUserMessageIds(List<String> userMessageIds) {
        final TypedQuery<String> query = em.createNamedQuery("MessageInfo.findUserMessageIds", String.class);
        query.setParameter("MESSAGEIDS", userMessageIds);
        List<String> messageIds = query.getResultList();
        LOG.debug("Found ids [{}]", messageIds);
        return messageIds;
    }

    @Timer(clazz = MessageInfoDao.class,value = "findSignalMessageIds")
    @Counter(clazz = MessageInfoDao.class,value = "findSignalMessageIds")
    public List<String> findSignalMessageIds(List<String> userMessageIds) {
        final TypedQuery<String> query = em.createNamedQuery("MessageInfo.findMessageIdsWithRefToMessageIds", String.class);
        query.setParameter("MESSAGEIDS", userMessageIds);
        query.setParameter("MESSAGE_TYPE", MessageType.SIGNAL_MESSAGE);
        List<String> messageIds = query.getResultList();
        LOG.debug("Found ids [{}]", messageIds);
        return messageIds;
    }

    @Timer(clazz = MessageInfoDao.class,value = "deleteMessages")
    @Counter(clazz = MessageInfoDao.class,value = "deleteMessages")
    public int deleteMessages(List<String> messageIds) {
        LOG.debug("deleteMessages [{}]", messageIds.size());
        final Query deleteQuery = em.createNamedQuery("MessageInfo.deleteMessages");
        deleteQuery.setParameter("MESSAGEIDS", messageIds);
        int result  = deleteQuery.executeUpdate();
        LOG.debug("deleteMessages result [{}]", result);
        return result;
    }
}

