package eu.domibus.core.message.signal;

import eu.domibus.api.model.ActionEntity;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.SignalMessage;
import eu.domibus.core.dao.BasicDao;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;

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

    public SignalMessage findByUserMessageEntityId(final Long userMessageEntityId) {
        final TypedQuery<SignalMessage> query = em.createNamedQuery("SignalMessage.findSignalMessageByUserMessageEntityId", SignalMessage.class);
        query.setParameter("ENTITY_ID", userMessageEntityId);
        return DataAccessUtils.singleResult(query.getResultList());
    }

    public SignalMessage findByUserMessageIdWithUserMessage(String messageId, MSHRole mshRole) {
        final TypedQuery<SignalMessage> query = em.createNamedQuery("SignalMessage.findSignalMessageWithUserMessageByUserMessageIdAndRole", SignalMessage.class);
        query.setParameter("MESSAGE_ID", messageId);
        query.setParameter("MSH_ROLE", mshRole);
        return DataAccessUtils.singleResult(query.getResultList());
    }

    @Timer(clazz = SignalMessageDao.class, value = "deleteMessages")
    @Counter(clazz = SignalMessageDao.class, value = "deleteMessages")
    public int deleteMessages(List<Long> ids) {
        LOG.debug("deleteMessages [{}]", ids.size());
        final Query deleteQuery = em.createNamedQuery("SignalMessage.deleteMessages");
        deleteQuery.setParameter("IDS", ids);
        int result = deleteQuery.executeUpdate();
        LOG.debug("deleteMessages result [{}]", result);
        return result;
    }

    public SignalMessage findLastTestMessage(String partyId, ActionEntity actionEntity) {
        final TypedQuery<SignalMessage> query = this.em.createNamedQuery("SignalMessage.findTestMessageDesc", SignalMessage.class);
        query.setParameter("PARTY_ID", partyId);
        query.setParameter("ACTION_ID", actionEntity.getEntityId());
        query.setMaxResults(1);
        return DataAccessUtils.singleResult(query.getResultList());
    }

}
