package eu.domibus.core.earchive;

import eu.domibus.core.dao.BasicDao;
import eu.domibus.core.util.QueryUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;
import java.util.Date;
import java.util.List;

/**
 * @author François Gautier
 * @since 5.0
 */
@Repository
public class EArchiveBatchUserMessageDao extends BasicDao<EArchiveBatchUserMessage> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EArchiveBatchUserMessageDao.class);

    private final QueryUtil queryUtil;

    public EArchiveBatchUserMessageDao(QueryUtil queryUtil) {
        super(EArchiveBatchUserMessage.class);
        this.queryUtil = queryUtil;
    }

    @Transactional
    public void create(EArchiveBatchEntity entity, List<EArchiveBatchUserMessage> batchUserMessages) {
        LOG.debug("Create the EArchiveBatch [{}] of EArchiveBatchUserMessage with [{}] messages", entity.getEntityId(), batchUserMessages.size());
        for (EArchiveBatchUserMessage batchUserMessage : batchUserMessages) {
            batchUserMessage.seteArchiveBatch(entity);
            create(batchUserMessage);
            LOG.trace("Create EArchiveBatchUserMessage [{}]", batchUserMessage.getEntityId());
        }
        LOG.debug("Finish the creation of the EArchiveBatch [{}]", entity.getEntityId());
    }

    public List<EArchiveBatchUserMessage> getBatchMessageList(String batchId, Integer pageStart, Integer pageSize) {
        TypedQuery<EArchiveBatchUserMessage> query = em.createNamedQuery("EArchiveBatchUserMessage.findByArchiveBatchId", EArchiveBatchUserMessage.class);
        query.setParameter("batchId", batchId);
        queryUtil.setPaginationParametersToQuery(query, pageStart, pageSize);
        return query.getResultList();
    }

    public Date getReceivedTime(String entityId) {
        TypedQuery<Date> query = em.createNamedQuery("UserMessageLog.findReceivedTimeForEArchiveBatch", Date.class);
        LOG.debug("EArchiveBatch ReceivedTime query:[{}]", query.toString());
        query.setParameter("ENTITY_ID", Long.parseLong(entityId));
        LOG.debug("EArchiveBatch ReceivedTime:[{}]", query.getSingleResult());
        return query.getSingleResult();
    }
}
