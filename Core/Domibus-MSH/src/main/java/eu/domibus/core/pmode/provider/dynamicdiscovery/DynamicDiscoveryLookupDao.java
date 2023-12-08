package eu.domibus.core.pmode.provider.dynamicdiscovery;

import eu.domibus.api.dynamicdyscovery.DynamicDiscoveryLookupEntity;
import eu.domibus.core.dao.BasicDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;
import java.util.Date;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 5.1.1
 */
@Repository
public class DynamicDiscoveryLookupDao extends BasicDao<DynamicDiscoveryLookupEntity> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DynamicDiscoveryLookupDao.class);

    public DynamicDiscoveryLookupDao() {
        super(DynamicDiscoveryLookupEntity.class);
    }

    public DynamicDiscoveryLookupEntity findByFinalRecipient(String finalRecipient) {
        if (StringUtils.isBlank(finalRecipient)) {
            return null;
        }

        final TypedQuery<DynamicDiscoveryLookupEntity> query = this.em.createNamedQuery("DynamicDiscoveryLookupEntity.findByFinalRecipient", DynamicDiscoveryLookupEntity.class);
        query.setParameter("FINAL_RECIPIENT", finalRecipient);
        return DataAccessUtils.singleResult(query.getResultList());
    }

    @Transactional
    public void createOrUpdate(DynamicDiscoveryLookupEntity discoveryLookupEntity) {
        if (discoveryLookupEntity.getEntityId() > 0) {
            LOG.debug("Updating DDC lookup entry with entity id [{}]", discoveryLookupEntity.getEntityId());
            update(discoveryLookupEntity);
            return;
        }
        //create
        LOG.debug("Creating DDC lookup entry [{}]", discoveryLookupEntity);
        create(discoveryLookupEntity);
    }

    /**
     * Finds certificates which were not discovered more recent than the specified date
     * It gets the entity which has the latest discovery time(grouped per certificate)
     */
    public List<String> findCertificatesNotDiscoveredInTheLastPeriod(Date dateLimit) {
        final TypedQuery<String> query = em.createNamedQuery("DynamicDiscoveryLookupEntity.findCertificatesNotDiscoveredInTheLastPeriod", String.class);
        query.setParameter("DDC_TIME", dateLimit);
        return query.getResultList();
    }

    /**
     * Finds parties which were not discovered more recent than the specified date
     * It gets the entity which has the latest discovery time(grouped per party name)
     */
    public List<String> findPartiesNotDiscoveredInTheLastPeriod(Date dateLimit) {
        final TypedQuery<String> query = em.createNamedQuery("DynamicDiscoveryLookupEntity.findPartiesNotDiscoveredInTheLastPeriod", String.class);
        query.setParameter("DDC_TIME", dateLimit);
        return query.getResultList();
    }

    /**
     * Finds final recipients which were not discovered more recent than the specified date
     */
    public List<DynamicDiscoveryLookupEntity> findFinalRecipientsNotDiscoveredInTheLastPeriod(Date dateLimit) {
        final TypedQuery<DynamicDiscoveryLookupEntity> query = em.createNamedQuery("DynamicDiscoveryLookupEntity.findFinalRecipientsNotDiscoveredInTheLastPeriod", DynamicDiscoveryLookupEntity.class);
        query.setParameter("DDC_TIME", dateLimit);
        return query.getResultList();
    }
}
