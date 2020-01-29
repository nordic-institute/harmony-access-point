package eu.domibus.common.dao;

import com.codahale.metrics.MetricRegistry;
import eu.domibus.common.MessageStatus;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.logging.MDCKey;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Christian Koch, Stefan Mueller, Federico Martini
 * @since 3.0
 */

@Repository
public class MessagingDao extends BasicDao<Messaging> {

    private static final String FIND_MESSAGING_ON_STATUS_AND_RECEIVER = "select new eu.domibus.ebms3.common.model.MessagePullDto(ul.messageId,ul.received) from UserMessageLog ul where ul.messageId in (SELECT m.userMessage.messageInfo.messageId as id FROM  Messaging m left join m.userMessage.partyInfo.to.partyId as pids where UPPER(pids.value)=UPPER(:PARTY_ID) and m.userMessage.mpc=:MPC) and ul.messageStatus=:MESSAGE_STATUS ORDER BY ul.received";
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessagingDao.class);
    private static final String PARTY_ID = "PARTY_ID";
    private static final String MESSAGE_STATUS = "MESSAGE_STATUS";
    private static final String MPC = "MPC";
    private static final String MESSAGE_ID = "MESSAGE_ID";
    private static final String GROUP_ID = "GROUP_ID";

    @Autowired
    private MetricRegistry metricRegistry;

    public MessagingDao() {
        super(Messaging.class);
    }

    public List<UserMessage> findUserMessageByGroupId(final String groupId) {
        final TypedQuery<UserMessage> query = this.em.createNamedQuery("Messaging.findUserMessageByGroupId", UserMessage.class);
        query.setParameter(GROUP_ID, groupId);
        return query.getResultList();
    }

    public UserMessage findUserMessageByMessageId(final String messageId) {
        com.codahale.metrics.Timer.Context authorizeUserMessageMetric = metricRegistry.timer(MetricRegistry.name(MessagingDao.class, "findUserMessageByMessageId")).time();

        final TypedQuery<UserMessage> query = this.em.createNamedQuery("Messaging.findUserMessageByMessageId", UserMessage.class);
        query.setParameter(MESSAGE_ID, messageId);

        UserMessage userMessage = DataAccessUtils.singleResult(query.getResultList());
        authorizeUserMessageMetric.stop();
        return userMessage;
    }

    public SignalMessage findSignalMessageByMessageId(final String messageId) {
        com.codahale.metrics.Timer.Context authorizeUserMessageMetric = metricRegistry.timer(MetricRegistry.name(MessagingDao.class, "findSignalMessageByMessageId")).time();

        final TypedQuery<SignalMessage> query = this.em.createNamedQuery("Messaging.findSignalMessageByMessageId", SignalMessage.class);
        query.setParameter(MESSAGE_ID, messageId);

        SignalMessage signalMessage = DataAccessUtils.singleResult(query.getResultList());

        authorizeUserMessageMetric.stop();

        return signalMessage;
    }

    public Messaging findMessageByMessageId(final String messageId) {
        com.codahale.metrics.Timer.Context authorizeUserMessageMetric = metricRegistry.timer(MetricRegistry.name(MessagingDao.class, "findMessageByMessageId")).time();

        try {
            final TypedQuery<Messaging> query = em.createNamedQuery("Messaging.findMessageByMessageId", Messaging.class);
            query.setParameter(MESSAGE_ID, messageId);
            return query.getSingleResult();
        } catch (NoResultException nrEx) {
            LOG.debug("Could not find any message for message id[" + messageId + "]");
            return null;
        } finally {
            authorizeUserMessageMetric.stop();
        }
    }

    protected List<PartInfo> getPayloads(final UserMessage userMessage, Predicate<PartInfo> partInfoPredicate) {
        return userMessage.getPayloadInfo().getPartInfo().stream().filter(partInfoPredicate).collect(Collectors.toList());
    }

    protected Predicate<PartInfo> getFilenameEmptyPredicate() {
        return partInfo -> StringUtils.isBlank(partInfo.getFileName());
    }

    protected List<PartInfo> getDatabasePayloads(final UserMessage userMessage) {
        Predicate<PartInfo> filenameEmptyPredicate = getFilenameEmptyPredicate();
        return getPayloads(userMessage, filenameEmptyPredicate);
    }

    protected List<PartInfo> getFileSystemPayloads(final UserMessage userMessage) {
        Predicate<PartInfo> filenamePresentPredicate = getFilenameEmptyPredicate().negate();
        return getPayloads(userMessage, filenamePresentPredicate);
    }

    /**
     * Retrieves messages based STATUS and TO fields. The return is ordered by received date.
     *
     * @param partyIdentifier the party to which this message should be delivered.
     * @param messageStatus   the status of the message.
     * @param mpc             the message partition channel of the message.
     * @return a list of class containing the date and the messageId.
     */
    public List<MessagePullDto> findMessagingOnStatusReceiverAndMpc(final String partyIdentifier,
                                                                    final MessageStatus messageStatus, final String mpc) {
        TypedQuery<MessagePullDto> processQuery = em.createQuery(FIND_MESSAGING_ON_STATUS_AND_RECEIVER, MessagePullDto.class);
        processQuery.setParameter(PARTY_ID, partyIdentifier);
        processQuery.setParameter(MESSAGE_STATUS, messageStatus);
        processQuery.setParameter(MPC, mpc);
        return processQuery.getResultList();
    }
}

