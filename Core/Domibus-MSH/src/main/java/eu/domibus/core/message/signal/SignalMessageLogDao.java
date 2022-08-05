package eu.domibus.core.message.signal;

import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.SignalMessageLog;
import eu.domibus.core.message.MessageLogDao;
import eu.domibus.core.message.MessageLogInfo;
import eu.domibus.core.message.MessageLogInfoFilter;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Federico Martini
 * @since 3.2
 */
@Repository
public class SignalMessageLogDao extends MessageLogDao<SignalMessageLog> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SignalMessageLogDao.class);

    private final SignalMessageLogInfoFilter signalMessageLogInfoFilter;

    public SignalMessageLogDao(SignalMessageLogInfoFilter signalMessageLogInfoFilter) {
        super(SignalMessageLog.class);
        this.signalMessageLogInfoFilter = signalMessageLogInfoFilter;
    }

    public SignalMessageLog findByMessageId(String messageId, MSHRole mshRole) {
        TypedQuery<SignalMessageLog> query = em.createNamedQuery("SignalMessageLog.findByMessageIdAndRole", SignalMessageLog.class);
        query.setParameter("MESSAGE_ID", messageId);
        query.setParameter("MSH_ROLE", mshRole);

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
