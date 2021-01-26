package eu.domibus.core.message;

import eu.domibus.core.dao.BasicDao;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.ebms3.common.model.SignalMessage;
import eu.domibus.ebms3.common.model.UserMessage;
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
public class UserMessageDao extends BasicDao<UserMessage> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageDao.class);

    public UserMessageDao() {
        super(UserMessage.class);
    }

    @Timer(clazz = UserMessageDao.class,value = "findUserMessage")
    @Counter(clazz = UserMessageDao.class,value = "findUserMessage")
    public List<UserMessage> findUserMessages(List<String> userMessageIds) {
        final TypedQuery<UserMessage> query = em.createNamedQuery("UserMessage.find", UserMessage.class);
        query.setParameter("MESSAGEIDS", userMessageIds);
        List<UserMessage> userMessages = query.getResultList();
        LOG.debug("Number of signal messages Found ids [{}]", userMessages.size());
        return userMessages;
    }


}
