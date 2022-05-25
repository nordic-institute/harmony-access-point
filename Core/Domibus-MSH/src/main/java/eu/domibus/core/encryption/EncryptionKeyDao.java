package eu.domibus.core.encryption;

import eu.domibus.core.dao.BasicDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */

@Repository
public class EncryptionKeyDao extends BasicDao<EncryptionKeyEntity> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EncryptionKeyDao.class);

    public EncryptionKeyDao() {
        super(EncryptionKeyEntity.class);
    }

    @Cacheable(value = "encryptionKey", key = "#domain + #encryptionUsage")
    public EncryptionKeyEntity findByUsageCacheable(final String domain, final EncryptionUsage encryptionUsage) {
        LOG.trace("Getting the EncryptionKey for usage [{}]", encryptionUsage);

        return findByUsage(encryptionUsage);
    }

    public EncryptionKeyEntity findByUsage(final EncryptionUsage encryptionUsage) {
        LOG.trace("Getting the EncryptionKey for usage from the database [{}]", encryptionUsage);

        final TypedQuery<EncryptionKeyEntity> query = this.em.createNamedQuery("EncryptionKeyEntity.findByUsage", EncryptionKeyEntity.class);
        query.setParameter("USAGE", encryptionUsage);
        return DataAccessUtils.singleResult(query.getResultList());
    }
}

