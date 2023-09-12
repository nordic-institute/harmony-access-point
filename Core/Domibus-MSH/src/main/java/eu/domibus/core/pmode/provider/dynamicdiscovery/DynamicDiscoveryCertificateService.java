package eu.domibus.core.pmode.provider.dynamicdiscovery;

import eu.domibus.api.dynamicdyscovery.DynamicDiscoveryCertificateEntity;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.pki.MultiDomainCryptoService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;

@Service
public class DynamicDiscoveryCertificateService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DynamicDiscoveryCertificateService.class);

    protected DynamicDiscoveryCertificateDao dynamicDiscoveryCertificateDao;
    protected CertificateService certificateService;
    protected MultiDomainCryptoService multiDomainCryptoService;
    protected DomainContextProvider domainProvider;

    public DynamicDiscoveryCertificateService(DynamicDiscoveryCertificateDao dynamicDiscoveryCertificateDao, CertificateService certificateService, MultiDomainCryptoService multiDomainCryptoService, DomainContextProvider domainProvider) {
        this.dynamicDiscoveryCertificateDao = dynamicDiscoveryCertificateDao;
        this.certificateService = certificateService;
        this.multiDomainCryptoService = multiDomainCryptoService;
        this.domainProvider = domainProvider;
    }

    /**
     * Saves the time when the certificate was discovered last time from SMP
     *
     * @param cn The X509 common name extracted from the subject
     */
    @Transactional
    public void saveCertificateDynamicDiscoveryDate(String cn, final X509Certificate certificate) {
        LOG.debug("Saving the certificate discovery date for certificate with alias [{}]", cn);

        DynamicDiscoveryCertificateEntity certificateEntity = dynamicDiscoveryCertificateDao.findByCommonName(cn);
        if (certificateEntity == null) {
            LOG.debug("Creating DDC certificate for [{}]", cn);
            certificateEntity = new DynamicDiscoveryCertificateEntity();
            certificateEntity.setCn(cn);
            certificateEntity.setSubject(certificate.getSubjectDN().getName());
            certificateEntity.setSerial(certificate.getSerialNumber() + "");
            certificateEntity.setIssuerSubject(certificate.getIssuerDN().getName());
            final String fingerprint = certificateService.extractFingerprints(certificate);
            certificateEntity.setFingerprint(fingerprint);
        }
        final Date dynamicDiscoveryUpdateTime = new Date();
        LOG.debug("Setting the certificate discovery date to [{}]", dynamicDiscoveryUpdateTime);
        certificateEntity.setDynamicDiscoveryTime(dynamicDiscoveryUpdateTime);
        try {
            dynamicDiscoveryCertificateDao.createOrUpdate(certificateEntity);
        } catch (DataIntegrityViolationException e) {
            //in a cluster environment, an entity associated for a DDC certificate can be created in parallel and a unique constraint is raised
            //in case a constraint violation occurs we don't do anything because the other node added the latest data in parallel
            LOG.warn("Could not create or update DDC certificate entity with entity id [{}]. It could be that another node updated the same entity in parallel", certificateEntity.getEntityId(), e);
        }
    }

    /**
     * Deletes from the truststore and from the database the dynamically discovered certificates which were not discovered during the specified hours
     */
    public void deleteDDCCertificatesNotDiscoveredInTheLastPeriod(int retentionInHours) {
        final List<DynamicDiscoveryCertificateEntity> certificatesNotDiscoveredInTheLastPeriod = dynamicDiscoveryCertificateDao.findCertificatesNotDiscoveredInTheLastPeriod(retentionInHours);
        for (DynamicDiscoveryCertificateEntity dynamicDiscoveryCertificateEntity : certificatesNotDiscoveredInTheLastPeriod) {
            final Domain currentDomain = domainProvider.getCurrentDomain();
            final String certificateCn = dynamicDiscoveryCertificateEntity.getCn();
            LOG.info("Deleting from truststore the certificate with alias [{}] for domain [{}]", certificateCn, currentDomain);
            final boolean removeCertificateFromTruststore = multiDomainCryptoService.removeCertificate(currentDomain, certificateCn);
            if (removeCertificateFromTruststore) {
                LOG.info("Deleted from truststore the certificate with alias [{}] for domain [{}]", certificateCn, currentDomain);
            }

            LOG.info("Deleting from database the dynamically discovered certificate with alias [{}]", certificateCn);
            final boolean deleted = dynamicDiscoveryCertificateDao.deleteCertificateByCn(certificateCn);
            if (deleted) {
                LOG.info("Deleted from database the dynamically discovered certificate with alias [{}]", certificateCn);
            }
        }
    }
}
