package eu.domibus.core.message;

import eu.domibus.api.model.MessageStatus;
import eu.domibus.core.dao.BasicDao;
import eu.domibus.core.message.pull.MessagePullDto;
import eu.domibus.api.model.Messaging;
import eu.domibus.api.model.PartInfo;
import eu.domibus.api.model.SignalMessage;
import eu.domibus.api.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.logging.MDCKey;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

    private static final String FIND_MESSAGING_ON_STATUS_AND_RECEIVER = "select new eu.domibus.core.message.pull.MessagePullDto(ul.messageId,ul.received) from UserMessageLog ul where ul.messageId in (SELECT m.userMessage.messageInfo.messageId as id FROM  Messaging m left join m.userMessage.partyInfo.to.partyId as pids where UPPER(pids.value)=UPPER(:PARTY_ID) and m.userMessage.mpc=:MPC) and ul.messageStatus=:MESSAGE_STATUS ORDER BY ul.received";
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessagingDao.class);
    private static final String PARTY_ID = "PARTY_ID";
    private static final String MESSAGE_STATUS = "MESSAGE_STATUS";
    private static final String MPC = "MPC";
    private static final String MESSAGE_ID = "MESSAGE_ID";

    public MessagingDao() {
        super(Messaging.class);
    }

    public SignalMessage findSignalMessageByUserMessageId(final String messageId) {
        final TypedQuery<SignalMessage> query = this.em.createNamedQuery("Messaging.findSignalMessageByUserMessageId", SignalMessage.class);
        query.setParameter(MESSAGE_ID, messageId);

        return DataAccessUtils.singleResult(query.getResultList());
    }


    /**
     * Deletes the payloads saved on the file system
     *
     * @param userMessage
     */
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

