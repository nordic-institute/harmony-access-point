package eu.domibus.core.earchive;

import eu.domibus.core.dao.BasicDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;
import java.util.List;

import static eu.domibus.core.earchive.RequestType.CONTINUOUS;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

/**
 * @author François Gautier
 * @since 5.0
 */
@Repository
public class EArchiveBatchDao extends BasicDao<EArchiveBatchEntity> {

    public EArchiveBatchDao() {
        super(EArchiveBatchEntity.class);
    }

    public EArchiveBatchEntity findEArchiveBatchByBatchId(long entityId) {
        TypedQuery<EArchiveBatchEntity> query = this.em.createNamedQuery("EArchiveBatchEntity.findByBatchId", EArchiveBatchEntity.class);
        query.setParameter("BATCH_ENTITY_ID", entityId);

        List<EArchiveBatchEntity> resultList = query.getResultList();
        if (isEmpty(resultList)) {
            return null;
        }
        return resultList.get(0);
    }

    public Long findLastEntityIdArchived() {
        TypedQuery<Long> query = this.em.createNamedQuery("EArchiveBatchEntity.findLastEntityIdArchived", Long.class);
        query.setParameter("REQUEST_TYPE", CONTINUOUS);
        List<Long> resultList = query.getResultList();
        if (isEmpty(resultList)) {
            return null;
        }
        return resultList.get(0);
    }

    @Transactional
    public void setStatus(EArchiveBatchEntity eArchiveBatchByBatchId, EArchiveBatchStatus status) {
        eArchiveBatchByBatchId.seteArchiveBatchStatus(status);
        em.merge(eArchiveBatchByBatchId);
    }
}
