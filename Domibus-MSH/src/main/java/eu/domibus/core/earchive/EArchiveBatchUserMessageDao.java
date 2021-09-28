package eu.domibus.core.earchive;

import eu.domibus.core.dao.BasicDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author François Gautier
 * @since 5.0
 */
@Repository
public class EArchiveBatchUserMessageDao extends BasicDao<EArchiveBatchUserMessage> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EArchiveBatchUserMessageDao.class);

    public EArchiveBatchUserMessageDao() {
        super(EArchiveBatchUserMessage.class);
    }

    @Transactional
    public void create(EArchiveBatch entity, List<Long> userMessageLogEntityIds) {
        LOG.debug("Create the EArchiveBatch [{}] of EArchiveBatchUserMessage with [{}] messages", entity.getEntityId(), userMessageLogEntityIds.size());
        for (Long userMessageLogEntityId : userMessageLogEntityIds) {
            EArchiveBatchUserMessage eArchiveBatchUserMessage = new EArchiveBatchUserMessage();
            eArchiveBatchUserMessage.seteArchiveBatch(entity);
            eArchiveBatchUserMessage.setUserMessageEntityId(userMessageLogEntityId);
            em.persist(eArchiveBatchUserMessage);
            LOG.trace("Create EArchiveBatchUserMessage [{}]", eArchiveBatchUserMessage.getEntityId());

        }
        em.flush();
        em.clear();
        LOG.debug("Finish the creation of the EArchiveBatch [{}]", entity.getEntityId());
    }
}
