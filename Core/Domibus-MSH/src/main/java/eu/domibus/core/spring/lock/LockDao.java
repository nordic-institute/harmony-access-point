package eu.domibus.core.spring.lock;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.dao.BasicDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_SYNCHRONIZATION_TIMEOUT;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Repository
public class LockDao extends BasicDao<LockEntity> {

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    public LockDao() {
        super(LockEntity.class);
    }

    public LockEntity findByLockKeyWithLock(String lockKey) {
        Integer timeout = domibusPropertyProvider.getIntegerProperty(DOMIBUS_SYNCHRONIZATION_TIMEOUT);

        CriteriaBuilder builder = this.em.getCriteriaBuilder();
        CriteriaQuery<LockEntity> criteria = builder.createQuery(LockEntity.class);
        Root<LockEntity> from = criteria.from(LockEntity.class);
        criteria.where(builder.in(from.get("lockKey")).value(lockKey));
        Query query = this.em.createQuery(criteria);
        query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        query.setHint("javax.persistence.lock.timeout", timeout.toString());
        LockEntity res = (LockEntity) query.getSingleResult();
        return res;
    }
}
