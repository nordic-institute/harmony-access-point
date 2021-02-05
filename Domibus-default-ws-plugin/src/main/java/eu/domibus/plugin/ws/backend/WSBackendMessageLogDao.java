package eu.domibus.plugin.ws.backend;

import eu.domibus.plugin.ws.util.WSBasicDao;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.Date;
import java.util.List;

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
        TypedQuery<WSBackendMessageLogEntity> query = em.createNamedQuery(
                "WSBackendMessageLogEntity.findByMessageId",
                WSBackendMessageLogEntity.class);
        query.setParameter(MESSAGE_ID, messageId);
        WSBackendMessageLogEntity wsBackendMessageLogEntity;
        try {
            wsBackendMessageLogEntity = query.getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
        return wsBackendMessageLogEntity;
    }

    /**
     * Find the backend messages with:
     * <p>
     * {@link WSBackendMessageLogEntity#getMessageStatus()} ()} = {@code WSBackendMessageStatus.WAITING_FOR_RETRY}
     * {@link WSBackendMessageLogEntity#getNextAttempt()} < {@code CURRENT_TIMESTAMP}
     * 0 < {@link WSBackendMessageLogEntity#getSendAttempts()} < {@link WSBackendMessageLogEntity#getSendAttemptsMax()}
     * {@link WSBackendMessageLogEntity#getScheduled()} is {@code false} or {@code null}
     *
     * @return list of backend messages available for retry now
     */
    public List<WSBackendMessageLogEntity> findRetryMessages() {
        TypedQuery<WSBackendMessageLogEntity> query = em.createNamedQuery(
                "WSBackendMessageLogEntity.findRetryMessages",
                WSBackendMessageLogEntity.class);
        query.setParameter("CURRENT_TIMESTAMP", new Date(System.currentTimeMillis()));
        query.setParameter("MESSAGE_STATUS", WSBackendMessageStatus.WAITING_FOR_RETRY);

        return query.getResultList();
    }

    public WSBackendMessageLogEntity getById(long backendMessageEntityId) {
        return em.find(typeOfT, backendMessageEntityId);
    }
}
