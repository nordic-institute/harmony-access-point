package eu.domibus.plugin.webService.dao;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.webService.entity.WSMessageLog;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;


@Repository
public class WSMessageLogDao extends WSBasicDao<WSMessageLog> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSMessageLogDao.class);

    public static final String MESSAGE_ID = "MESSAGE_ID";

    public WSMessageLogDao() {
        super(WSMessageLog.class);
    }


    /**
     * Find the entry based on a given MessageId.
     *
     * @param messageId the id of the message.
     */
    public WSMessageLog findByMessageId(String messageId) {
        TypedQuery<WSMessageLog> query = em.createNamedQuery("WSMessageLog.findByMessageId", WSMessageLog.class);
        query.setParameter(MESSAGE_ID, messageId);
        WSMessageLog wsMessageLog;
        try {
            wsMessageLog = query.getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
        return wsMessageLog;
    }

    /**
     * Fins all entries in the plugin table.
     */
    public List<WSMessageLog> findAll() {
        TypedQuery<WSMessageLog> query = em.createNamedQuery("WSMessageLog.findAll", WSMessageLog.class);
        return query.getResultList();
    }

    /**
     * Delete the entry related to a given MessageId.
     *
     * @param messageId the id of the message.
     */
    public void deleteByMessageId(final String messageId) {
        Query query = em.createNamedQuery("WSMessageLog.deleteByMessageId");
        query.setParameter(MESSAGE_ID, messageId);
        query.executeUpdate();
    }
}
