package eu.domibus.core.message.signal;

import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MSHRoleEntity;
import eu.domibus.api.model.SignalMessageLog;
import eu.domibus.core.message.MessageLogDao;
import eu.domibus.core.message.MessageLogInfoFilter;
import eu.domibus.core.message.dictionary.MshRoleDao;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * @author Federico Martini
 * @since 3.2
 */
@Repository
public class SignalMessageLogDao extends MessageLogDao<SignalMessageLog> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SignalMessageLogDao.class);

    private final SignalMessageLogInfoFilter signalMessageLogInfoFilter;

    private final MshRoleDao mshRoleDao;

    public SignalMessageLogDao(SignalMessageLogInfoFilter signalMessageLogInfoFilter, MshRoleDao mshRoleDao) {
        super(SignalMessageLog.class);
        this.signalMessageLogInfoFilter = signalMessageLogInfoFilter;
        this.mshRoleDao = mshRoleDao;
    }

    public SignalMessageLog findByMessageId(String messageId, MSHRole mshRole) {
        TypedQuery<SignalMessageLog> query = em.createNamedQuery("SignalMessageLog.findByMessageIdAndRole", SignalMessageLog.class);
        query.setParameter("MESSAGE_ID", messageId);

        MSHRoleEntity roleEntity = mshRoleDao.findByRole(mshRole);
        query.setParameter("MSH_ROLE", roleEntity);

        try {
            return query.getSingleResult();
        } catch (NoResultException nrEx) {
            LOG.debug("Query SignalMessageLog.findByMessageId did not find any result for message with id [" + messageId + "] and MSH role [" + mshRole + "]");
            return null;
        }
    }

    @Timer(clazz = SignalMessageLogDao.class, value = "deleteMessages.deleteMessageLogs")
    @Counter(clazz = SignalMessageLogDao.class, value = "deleteMessages.deleteMessageLogs")
    public int deleteMessageLogs(List<Long> ids) {
        final Query deleteQuery = em.createNamedQuery("SignalMessageLog.deleteMessageLogs");
        deleteQuery.setParameter("IDS", ids);
        int result = deleteQuery.executeUpdate();
        LOG.trace("deleteSignalMessageLogs result [{}]", result);
        return result;
    }

    protected MessageLogInfoFilter getMessageLogInfoFilter() {
        return signalMessageLogInfoFilter;
    }

}
