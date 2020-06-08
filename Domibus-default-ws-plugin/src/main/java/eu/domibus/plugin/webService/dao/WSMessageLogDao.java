package eu.domibus.plugin.webService.dao;

import eu.domibus.common.MessageStatus;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.webService.entity.WSMessageLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;


@Repository
public class WSMessageLogDao extends WSBasicDao<WSMessageLog> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSMessageLogDao.class);

    public WSMessageLogDao() {
        super(WSMessageLog.class);
    }


    public WSMessageLog findByMessageId(String messageId) {
        TypedQuery<WSMessageLog> query = em.createNamedQuery("WSMessageLog.findByMessageId", WSMessageLog.class);
        query.setParameter("MESSAGE_ID", messageId);
        return query.getSingleResult();
    }

}
