package eu.domibus.core.pmode.provider.dynamicdiscovery;

import eu.domibus.api.dynamicdyscovery.DynamicDiscoveryCertificateEntity;
import eu.domibus.core.dao.BasicDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;
import java.util.Date;
import java.util.List;

@Repository
public class DynamicDiscoveryCertificateDao extends BasicDao<DynamicDiscoveryCertificateEntity> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DynamicDiscoveryCertificateDao.class);

    public DynamicDiscoveryCertificateDao() {
        super(DynamicDiscoveryCertificateEntity.class);
    }

    public DynamicDiscoveryCertificateEntity findByCommonName(String cn) {
        if (StringUtils.isBlank(cn)) {
            return null;
        }

        final TypedQuery<DynamicDiscoveryCertificateEntity> query = this.em.createNamedQuery("DynamicDiscoveryCertificateEntity.findByCertificateCN", DynamicDiscoveryCertificateEntity.class);
        query.setParameter("CERT_CN", cn);
        return DataAccessUtils.singleResult(query.getResultList());
    }

    public List<DynamicDiscoveryCertificateEntity> findCertificatesNotUsed(int numberOfHours) {
        Date dateLimit = DateUtils.addHours(new Date(), numberOfHours * -1);
        final TypedQuery<DynamicDiscoveryCertificateEntity> query = em.createNamedQuery("DynamicDiscoveryCertificateEntity.findCertificatesNotUsed", DynamicDiscoveryCertificateEntity.class);
        query.setParameter("DDC_TIME", dateLimit);
        return query.getResultList();
    }

    @Transactional
    public void createOrUpdate(DynamicDiscoveryCertificateEntity certificateEntity) {
        if (certificateEntity.getEntityId() > 0) {
            LOG.debug("Updating certificate entry with entity id [{}]", certificateEntity.getEntityId());
            update(certificateEntity);
            return;
        }
        //create
        LOG.debug("Creating certificate entry [{}]", certificateEntity);
        create(certificateEntity);
    }
}
