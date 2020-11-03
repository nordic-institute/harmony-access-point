package eu.domibus.plugin.webService.backend;

import eu.domibus.plugin.webService.dao.WSBasicDao;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

/**
 * @author idragusa
 * @since 4.2
 */
@Repository
public class WSBackendMessageLogDao extends WSBasicDao<WSBackendMessageLogEntity> {

    private static final String MESSAGE_ID = "MESSAGE_ID";

    public WSBackendMessageLogDao() {
        super(WSBackendMessageLogEntity.class);
    }

    /**
     * Find the entry based on a given MessageId.
     *
     * @param messageId the id of the message.
     */
    public WSBackendMessageLogEntity findByMessageId(String messageId) {
        TypedQuery<WSBackendMessageLogEntity> query = em.createNamedQuery("WSBackendMessageLogEntity.findByMessageId", WSBackendMessageLogEntity.class);
        query.setParameter(MESSAGE_ID, messageId);
        WSBackendMessageLogEntity wsBackendMessageLogEntity;
        try {
            wsBackendMessageLogEntity = query.getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
        return wsBackendMessageLogEntity;
    }
}
