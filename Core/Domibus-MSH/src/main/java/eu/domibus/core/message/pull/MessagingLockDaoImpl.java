package eu.domibus.core.message.pull;

import eu.domibus.api.model.MessageState;
import eu.domibus.api.property.DataBaseEngine;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.util.DateUtil;
import eu.domibus.common.JPAConstants;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.BooleanUtils;
import org.hibernate.procedure.ProcedureOutputs;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import static eu.domibus.core.message.pull.PullMessageState.EXPIRED;
import static eu.domibus.core.message.pull.PullMessageState.RETRY;

/**
 * @author Thomas Dussart
 * @since 3.3.4
 */
@Repository
public class MessagingLockDaoImpl implements MessagingLockDao {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessagingLockDaoImpl.class);

    private static final String MESSAGE_ID = "MESSAGE_ID";

    protected static final String IDPK = "idpk";

    private static final String MPC = "MPC";

    private static final String INITIATOR = "INITIATOR";

    protected static final String MESSAGE_STATE = "MESSAGE_STATE";

    private static final String CURRENT_TIMESTAMP = "CURRENT_TIMESTAMP";

    @PersistenceContext(unitName = JPAConstants.PERSISTENCE_UNIT_NAME)
    private EntityManager entityManager;

    private final DateUtil dateUtil;

    private final DomibusConfigurationService domibusConfigurationService;

    public MessagingLockDaoImpl(DateUtil dateUtil, DomibusConfigurationService domibusConfigurationService) {
        this.dateUtil = dateUtil;
        this.domibusConfigurationService = domibusConfigurationService;
    }


    @Override
    @Transactional
    public PullMessageId getNextPullMessageToProcess(final String initiator, final String mpc) {
        if (DataBaseEngine.ORACLE == domibusConfigurationService.getDataBaseEngine()) {
            return getNextPullMessageToProcessOracle(initiator, mpc);
        } else {
            return getNextPullMessageToProcessMySql(initiator, mpc);
        }
    }

    private PullMessageId getNextPullMessageToProcessMySql(final String initiator, final String mpc) {
        try {
            Query q = entityManager.createNamedQuery("MessagingLock.lockQuerySkipBlocked_MySQL", MessagingLock.class);
            q.setParameter(MPC, mpc);
            q.setParameter(INITIATOR, initiator);
            q.setParameter(CURRENT_TIMESTAMP, dateUtil.getUtcDate());
            final MessagingLock messagingLock = (MessagingLock) q.getSingleResult();


            return buildPullMessageId(messagingLock);
        } catch (NoResultException ne) {
            LOG.trace("No message to lock found for mpc=[{}], initiator=[{}]", mpc, initiator, ne);
            return null;
        } catch (Exception e) {
            LOG.error("MessageLock lock could not be acquired for mpc=[{}], initiator=[{}]", mpc, initiator, e);
            return null;
        }
    }

    private PullMessageId getNextPullMessageToProcessOracle(final String initiator, final String mpc) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("LOCK_MESSAGE_SKIP_BLOCKED")
                .registerStoredProcedureParameter("P_MPC", String.class, ParameterMode.IN)
                .setParameter("P_MPC", mpc)
                .registerStoredProcedureParameter("P_INITIATOR", String.class, ParameterMode.IN)
                .setParameter("P_INITIATOR", initiator)
                .registerStoredProcedureParameter("P_CURRENT_TIMESTAMP", Date.class, ParameterMode.IN)
                .setParameter("P_CURRENT_TIMESTAMP", dateUtil.getUtcDate())
                .registerStoredProcedureParameter("P_ROW", Object.class, ParameterMode.REF_CURSOR);

        try {
            Boolean outcome = query.execute();
            if (BooleanUtils.isFalse(outcome)) {
                LOG.warn("Locking procedure did not execute successfully");
                return null;
            } else {
                LOG.trace("Locking procedure executed successfully");
                ResultSet resultSet = (ResultSet) query.getOutputParameterValue("P_ROW");
                if (resultSet == null || !resultSet.next()) {
                    LOG.trace("No message to lock found for mpc=[{}], initiator=[{}]", mpc, initiator);
                    return null;
                }
                final MessagingLock messagingLock = new MessagingLock();

                messagingLock.setEntityId(resultSet.getLong("ID_PK"));
                messagingLock.setMessageState(MessageState.valueOf(resultSet.getString("MESSAGE_STATE")));
                messagingLock.setMessageId(resultSet.getString("MESSAGE_ID"));
                messagingLock.setInitiator(resultSet.getString("INITIATOR"));
                messagingLock.setMpc(resultSet.getString("MPC"));
                messagingLock.setSendAttempts(resultSet.getInt("SEND_ATTEMPTS"));
                messagingLock.setSendAttemptsMax(resultSet.getInt("SEND_ATTEMPTS_MAX"));
                messagingLock.setNextAttempt(resultSet.getTimestamp("NEXT_ATTEMPT"));
                messagingLock.setStaled(resultSet.getTimestamp("MESSAGE_STALED"));
                messagingLock.setReceived(resultSet.getTimestamp("MESSAGE_RECEIVED"));
                messagingLock.setMessageType(resultSet.getString("MESSAGE_TYPE"));
                messagingLock.setCreatedBy(resultSet.getString("CREATED_BY"));
                messagingLock.setCreationTime(resultSet.getTimestamp("CREATION_TIME"));
                messagingLock.setModifiedBy(resultSet.getString("MODIFIED_BY"));
                messagingLock.setModificationTime(resultSet.getTimestamp("MODIFICATION_TIME"));

                return buildPullMessageId(messagingLock);
            }
        } catch (Exception ex) {
            LOG.error("Error executing locking procedure", ex);
            return null;
        } finally {
            try {
                query.unwrap(ProcedureOutputs.class).release();
                LOG.trace("Finished releasing locking procedure outputs");
            } catch (Exception ex) {
                LOG.error("Error when releasing locking procedure outputs", ex);
            }
        }
    }

    private PullMessageId buildPullMessageId(MessagingLock messagingLock) {
        LOG.debug("[getNextPullMessageToProcess]:id[{}] locked", messagingLock.getEntityId());
        final String messageId = messagingLock.getMessageId();
        final int sendAttempts = messagingLock.getSendAttempts();
        final int sendAttemptsMax = messagingLock.getSendAttemptsMax();
        final Date messageStaled = messagingLock.getStaled();

        final Timestamp currentDate = new Timestamp(System.currentTimeMillis());
        LOG.debug("expiration date[{}], current date[{}] ", messageStaled, currentDate);
        if (messageStaled.compareTo(currentDate) < 0) {
            messagingLock.setMessageState(MessageState.DEL);
            merge(messagingLock);
            return new PullMessageId(messageId, EXPIRED, String.format("Maximum time to send the message has been reached:[%tc]", messageStaled));
        }
        LOG.debug("sendattempts[{}], sendattemptsmax[{}]", sendAttempts, sendAttemptsMax);
        if (sendAttempts >= sendAttemptsMax) {
            messagingLock.setMessageState(MessageState.DEL);
            merge(messagingLock);
            return new PullMessageId(messageId, EXPIRED, String.format("Maximum number of attempts to send the message has been reached:[%d]", sendAttempts));
        }
        if (sendAttempts >= 0) {
            messagingLock.setMessageState(MessageState.PROCESS);
            merge(messagingLock);
        }
        if (sendAttempts > 0) {
            return new PullMessageId(messageId, RETRY);
        }
        return new PullMessageId(messageId);
    }

    public MessagingLock getLock(final String messageId) {
        try {
            LOG.debug("Message[{}] Getting lock", messageId);
            TypedQuery<MessagingLock> q = entityManager.createNamedQuery("MessagingLock.lockByMessageId", MessagingLock.class);
            q.setParameter(1, messageId);
            return q.getSingleResult();
        } catch (NoResultException nr) {
            LOG.trace("Message:[{}] lock not found. It has been removed by another process.", messageId, nr);
            return null;
        } catch (Exception ex) {
            LOG.warn("Message:[{}] lock could not be acquired. It is probably handled by another process.", messageId, ex);
            return null;
        }
    }

    protected void merge(final MessagingLock messagingLock) {
        entityManager.merge(messagingLock);
    }

    @Override
    public void save(final MessagingLock messagingLock) {
        entityManager.persist(messagingLock);
    }

    @Override
    public void delete(final String messageId) {
        Query query = entityManager.createNamedQuery("MessagingLock.delete");
        query.setParameter(MESSAGE_ID, messageId);
        query.executeUpdate();
    }

    @Override
    public void delete(final MessagingLock messagingLock) {
        entityManager.remove(messagingLock);
    }

    @Override
    public MessagingLock findMessagingLockForMessageId(final String messageId) {
        TypedQuery<MessagingLock> namedQuery = entityManager.createNamedQuery("MessagingLock.findForMessageId", MessagingLock.class);
        namedQuery.setParameter(MESSAGE_ID, messageId);
        try {
            return namedQuery.getSingleResult();
        } catch (NoResultException nr) {
            return null;
        }
    }

    @Override
    public List<MessagingLock> findStaledMessages() {
        TypedQuery<MessagingLock> query = entityManager.createNamedQuery("MessagingLock.findStalledMessages", MessagingLock.class);
        query.setParameter(CURRENT_TIMESTAMP, dateUtil.getUtcDate());
        return query.getResultList();
    }

    @Override
    public List<MessagingLock> findDeletedMessages() {
        TypedQuery<MessagingLock> query = entityManager.createNamedQuery("MessagingLock.findDeletedMessages", MessagingLock.class);
        return query.getResultList();
    }

    @Override
    public List<MessagingLock> findWaitingForReceipt() {
        final TypedQuery<MessagingLock> namedQuery = entityManager.createNamedQuery("MessagingLock.findWaitingForReceipt", MessagingLock.class);
        namedQuery.setParameter(CURRENT_TIMESTAMP, dateUtil.getUtcDate());
        return namedQuery.getResultList();
    }


}
