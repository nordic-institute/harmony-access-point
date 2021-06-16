package eu.domibus.core.message.nonrepudiation;

import eu.domibus.api.model.RawEnvelopeDto;
import eu.domibus.api.model.SignalMessageRaw;
import eu.domibus.core.dao.BasicDao;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@Repository
public class SignalMessageRawEnvelopeDao extends BasicDao<SignalMessageRaw> {

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(SignalMessageRawEnvelopeDao.class);

    public SignalMessageRawEnvelopeDao() {
        super(SignalMessageRaw.class);
    }

    public RawEnvelopeDto findRawXmlByMessageEntityId(final Long messageId) {
        TypedQuery<RawEnvelopeDto> namedQuery = em.createNamedQuery("SignalMessageRaw.findByMessageEntityId", RawEnvelopeDto.class);
        namedQuery.setParameter("ENTITY_ID", messageId);
        return DataAccessUtils.singleResult(namedQuery.getResultList());
    }

    public RawEnvelopeDto findSignalMessageByUserMessageId(final String userMessageId) {
        TypedQuery<RawEnvelopeDto> namedQuery = em.createNamedQuery("SignalMessageRaw.findByUserMessageId", RawEnvelopeDto.class);
        namedQuery.setParameter("MESSAGE_ID", userMessageId);
        return DataAccessUtils.singleResult(namedQuery.getResultList());
    }

    @Timer(clazz = SignalMessageRawEnvelopeDao.class, value = "deleteMessages")
    @Counter(clazz = SignalMessageRawEnvelopeDao.class, value = "deleteMessages")
    public int deleteMessages(List<Long> ids) {
        LOG.debug("deleteMessages [{}]", ids.size());
        final Query deleteQuery = em.createNamedQuery("SignalMessageRaw.deleteMessages");
        deleteQuery.setParameter("IDS", ids);
        int result = deleteQuery.executeUpdate();
        LOG.debug("deleteMessages result [{}]", result);
        return result;
    }

    /**
     * Delete all the raw entries related to a given UserMessage id.
     *
     * @param messageId the id of the message.
     */
    public void deleteUserMessageRawEnvelope(final String messageId) {
        /*Query query = em.createNamedQuery("Raw.deleteByMessageID");
        query.setParameter(MESSAGE_ID, messageId);
        query.executeUpdate();*/
    }

}
