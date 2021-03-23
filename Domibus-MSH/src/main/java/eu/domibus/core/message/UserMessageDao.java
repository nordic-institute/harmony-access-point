package eu.domibus.core.message;

import eu.domibus.api.model.Mpc;
import eu.domibus.api.model.UserMessage;
import eu.domibus.core.dao.BasicDao;
import eu.domibus.core.dao.ListDao;
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
public class UserMessageDao extends BasicDao<UserMessage> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageDao.class);

    public UserMessageDao() {
        super(UserMessage.class);
    }

    public UserMessage findByMessageId(String messageId) {
        final TypedQuery<UserMessage> query = this.em.createNamedQuery("UserMessage.findByMessageId", UserMessage.class);
        query.setParameter("MESSAGE_ID", messageId);
        return DataAccessUtils.singleResult(query.getResultList());
    }

    public UserMessage findByGroupEntityId(Long groupEntityId) {
        final TypedQuery<UserMessage> query = this.em.createNamedQuery("UserMessage.findByGroupEntityId", UserMessage.class);
        query.setParameter("ENTITY_ID", groupEntityId);
        return DataAccessUtils.singleResult(query.getResultList());
    }
}
