package eu.domibus.core.message.nonrepudiation;

import eu.domibus.api.model.RawEnvelopeDto;
import eu.domibus.api.model.UserMessageRaw;
import eu.domibus.core.dao.BasicDao;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.common.util.CollectionUtils;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * @author idragusa
 * @since 3.2.5
 */
//@thom test this class
@Repository
public class UserMessageRawEnvelopeDao extends BasicDao<UserMessageRaw> {

    private final static IDomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageRawEnvelopeDao.class);

    public static final String MESSAGE_ID = "MESSAGE_ID";
    public static final String MESSAGE_ENTITY_ID = "MESSAGE_ENTITY_ID";

    public UserMessageRawEnvelopeDao() {
        super(UserMessageRaw.class);
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

    public RawEnvelopeDto findRawXmlByEntityId(final long entityId) {
        TypedQuery<RawEnvelopeDto> namedQuery = em.createNamedQuery("RawDto.findByEntityId", RawEnvelopeDto.class);
        namedQuery.setParameter("ENTITY_ID", entityId);

        List<RawEnvelopeDto> resultList = namedQuery.getResultList();
        if (CollectionUtils.isEmpty(resultList)) {
            LOG.trace("The message with entity id[{}] has no associated raw xml saved in the database.", entityId);
            return null;
        }
        LOG.debug("[findRawXmlByEntityId][Message]:[{}]", entityId);
        return resultList.get(0);
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
     * @param entityId the id of the message.
     */
    public void deleteUserMessageRawEnvelope(final long entityId) {
        Query query = em.createNamedQuery("Raw.deleteByMessageID");
        query.setParameter(MESSAGE_ENTITY_ID, entityId);
        query.executeUpdate();
    }

    @Timer(clazz = UserMessageRawEnvelopeDao.class, value = "deleteMessages")
    @Counter(clazz = UserMessageRawEnvelopeDao.class, value = "deleteMessages")
    public int deleteMessages(List<Long> ids) {
        LOG.debug("deleteMessages [{}]", ids.size());
        final Query deleteQuery = em.createNamedQuery("UserMessageRaw.deleteMessages");
        deleteQuery.setParameter("IDS", ids);
        int result = deleteQuery.executeUpdate();
        LOG.debug("deleteMessages result [{}]", result);
        return result;
    }
}
