package eu.domibus.core.message.nonrepudiation;

import eu.domibus.core.dao.BasicDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

/**
 * @author idragusa
 * @since 3.2.5
 */
//@thom test this class
@Repository
public class RawEnvelopeLogDao extends BasicDao<RawEnvelopeLog> {

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(RawEnvelopeLogDao.class);

    public static final String MESSAGE_ID = "MESSAGE_ID";

    public RawEnvelopeLogDao() {
        super(RawEnvelopeLog.class);
    }

    public RawEnvelopeDto findRawXmlByMessageId(final String messageId) {
        TypedQuery<RawEnvelopeDto> namedQuery = em.createNamedQuery("RawDto.findByMessageId", RawEnvelopeDto.class);
        namedQuery.setParameter("MESSAGE_ID", messageId);
        try {
            LOG.debug("[findRawXmlByMessageIdMessage][Message]:[{}]", messageId);
            return namedQuery.getSingleResult();
        } catch (NoResultException nr) {
            LOG.trace("The message with id[{}] has no associated raw xml saved in the database.", messageId, nr);
            return null;
        }
    }

    public RawEnvelopeDto findUserMessageEnvelopeById(final long userMessageId) {
        TypedQuery<RawEnvelopeDto> namedQuery = em.createNamedQuery("RawDto.findByUserMessageId", RawEnvelopeDto.class);
        namedQuery.setParameter("USER_MESSAGE_ID", userMessageId);
        LOG.debug("[findUserMessageEnvelopeById][Message]:[{}]", userMessageId);
        return DataAccessUtils.singleResult(namedQuery.getResultList());
    }

    /**
     * Delete all the raw entries related to a given UserMessage id.
     *
     * @param messageId the id of the message.
     */
    public void deleteUserMessageRawEnvelope(final String messageId) {
        Query query = em.createNamedQuery("Raw.deleteByMessageID");
        query.setParameter(MESSAGE_ID, messageId);
        query.executeUpdate();
    }

}
