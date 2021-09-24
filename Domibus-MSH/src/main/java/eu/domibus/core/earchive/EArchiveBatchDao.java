package eu.domibus.core.earchive;

import eu.domibus.core.dao.BasicDao;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

/**
 * @author François Gautier
 * @since 5.0
 */
@Repository
public class EArchiveBatchDao extends BasicDao<EArchiveBatch> {

    public EArchiveBatchDao() {
        super(EArchiveBatch.class);
    }

    public EArchiveBatch findEArchiveBatchByBatchId(long entityId) {
        TypedQuery<EArchiveBatch> query = this.em.createNamedQuery("EArchiveBatch.findByBatchId", EArchiveBatch.class);
        query.setParameter("BATCH_ENTITY_ID", entityId);

        List<EArchiveBatch> resultList = query.getResultList();
        if (isEmpty(resultList)) {
            return null;
        }
        return resultList.get(0);
    }

    public Long findLastEntityIdArchived() {
        TypedQuery<Long> query = this.em.createNamedQuery("EArchiveBatch.findLastEntityIdArchived", Long.class);
        List<Long> resultList = query.getResultList();
        if (isEmpty(resultList)) {
            return null;
        }
        return resultList.get(0);
    }
}
