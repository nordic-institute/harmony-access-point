package eu.domibus.core.certificate;

import eu.domibus.AbstractIT;
import eu.domibus.api.util.DateUtil;
import eu.domibus.core.pmode.provider.dynamicdiscovery.DynamicDiscoveryLookupService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.test.common.PKIUtil;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;
import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.*;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Transactional
public class CertificateDaoImplIT extends AbstractIT {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CertificateDaoImplIT.class);

    @Autowired
    private CertificateDaoImpl certificateDao;

    @Autowired
    private DateUtil dateUtil;

    @Autowired
    protected DynamicDiscoveryLookupService dynamicDiscoveryLookupService;

    @Before
    public void setup() {
        LOG.putMDC(DomibusLogger.MDC_USER, "test_user");

        final LocalDateTime localDateTime = LocalDateTime.of(0, 1, 1, 0, 0);
        final LocalDateTime offset = localDateTime.minusDays(15);
        final LocalDateTime notification = localDateTime.minusDays(7);
        List<Certificate> certs2 = certificateDao.findExpiredToNotifyAsAlert(getDate(notification), getDate(offset));
        certificateDao.deleteAll(certs2);
        certs2 = certificateDao.findExpiredToNotifyAsAlert(getDate(notification), getDate(offset));
    }

    /**
     * In this scenario, notAfter is outside of the start/end range so
     * the method should not return the certificate.
     */
    @Test
    @Transactional
    public void create() {
        Certificate certificate = new Certificate();
        String firstCertificateName = "firstCertificateName";
        certificate.setAlias(firstCertificateName);
        certificate.setNotBefore(new Date());
        certificate.setCertificateStatus(CertificateStatus.SOON_REVOKED);
        certificate.setCertificateType(CertificateType.PRIVATE);
        certificate.setNotAfter(new Date());
        certificateDao.saveOrUpdate(certificate);
        List<Certificate> unNotifiedRevoked = certificateDao.getUnNotifiedSoonRevoked();
        assertEquals(1, unNotifiedRevoked.size());
        Certificate certificate1 = unNotifiedRevoked.get(0);
        assertNotNull(certificate1.getCreationTime());
        assertNotNull(certificate1.getCreatedBy());
        assertNotNull(certificate1.getModificationTime());
        assertNotNull(certificate1.getModifiedBy());

        assertEquals(certificate1.getCreationTime(), certificate1.getModificationTime());
    }

    @Test(expected = javax.validation.ConstraintViolationException.class)
    @Transactional
    public void saveWithNullDates() {
        Certificate firstCertificate = new Certificate();
        firstCertificate.setAlias("whatEver");
        firstCertificate.setNotBefore(null);
        firstCertificate.setNotAfter(null);
        firstCertificate.setCertificateType(CertificateType.PUBLIC);
        firstCertificate.setCertificateStatus(CertificateStatus.OK);
        certificateDao.saveOrUpdate(firstCertificate);
        em.flush();
    }

    @Test
    @Transactional
    public void saveOrUpdate() {

        Date notBefore = new Date();
        Date notAfter = new Date(notBefore.getTime() + 10000);

        Date notBeforeChanged = new Date(notAfter.getTime() + 20000);
        Date notAfterChanged = new Date(notBeforeChanged.getTime() + 100000);

        String firstCertificateName = "firstCertificateName";
        String secondCertificateName = "secondCertificateName";

        Certificate firstCertificate = new Certificate();
        firstCertificate.setAlias(firstCertificateName);
        firstCertificate.setNotBefore(notBefore);
        firstCertificate.setNotAfter(notAfter);
        firstCertificate.setCertificateType(CertificateType.PUBLIC);
        firstCertificate.setCertificateStatus(CertificateStatus.OK);

        certificateDao.saveOrUpdate(firstCertificate);

        Certificate secondCertificate = new Certificate();
        secondCertificate.setAlias(secondCertificateName);
        secondCertificate.setNotBefore(notBefore);
        secondCertificate.setNotAfter(notAfter);
        secondCertificate.setCertificateType(CertificateType.PUBLIC);
        secondCertificate.setCertificateStatus(CertificateStatus.SOON_REVOKED);
        secondCertificate.setLastNotification(notBefore);
        secondCertificate.setAlertImminentNotificationDate(notBefore);
        secondCertificate.setAlertExpiredNotificationDate(notBefore);

        certificateDao.saveOrUpdate(secondCertificate);

        secondCertificate = new Certificate();
        secondCertificate.setAlias(secondCertificateName);
        secondCertificate.setNotBefore(notBeforeChanged);
        secondCertificate.setNotAfter(notAfterChanged);
        secondCertificate.setCertificateType(CertificateType.PUBLIC);
        secondCertificate.setCertificateStatus(CertificateStatus.SOON_REVOKED);

        certificateDao.saveOrUpdate(secondCertificate);

        Certificate certificate = certificateDao.getByAliasAndType(firstCertificateName, CertificateType.PUBLIC);
        assertEquals(notBefore, certificate.getNotBefore());
        assertEquals(notAfter, certificate.getNotAfter());

        certificate = certificateDao.getByAliasAndType(secondCertificateName, CertificateType.PUBLIC);
        assertEquals(notBeforeChanged, certificate.getNotBefore());
        assertEquals(notAfterChanged, certificate.getNotAfter());
        assertEquals(notBefore, certificate.getLastNotification());
        assertEquals(notBefore, certificate.getAlertExpiredNotificationDate());
        assertEquals(notBefore, certificate.getAlertImminentNotificationDate());

    }

    @Test
    @Transactional
    public void findByAlias() {

        Certificate firstCertificate = new Certificate();
        String firstCertificateName = "firstCertificateName";
        firstCertificate.setAlias(firstCertificateName);
        firstCertificate.setNotBefore(new Date());
        firstCertificate.setNotAfter(new Date());
        firstCertificate.setCertificateType(CertificateType.PUBLIC);
        firstCertificate.setCertificateStatus(CertificateStatus.SOON_REVOKED);

        Certificate secondCertificate = new Certificate();
        String secondCertificateName = "secondCertificateName";
        secondCertificate.setAlias(secondCertificateName);
        secondCertificate.setNotBefore(new Date());
        secondCertificate.setNotAfter(new Date());
        secondCertificate.setCertificateType(CertificateType.PUBLIC);
        secondCertificate.setCertificateStatus(CertificateStatus.SOON_REVOKED);

        certificateDao.saveOrUpdate(firstCertificate);
        certificateDao.saveOrUpdate(secondCertificate);

        assertEquals(firstCertificate, certificateDao.getByAliasAndType(firstCertificateName, CertificateType.PUBLIC));
        assertEquals(secondCertificate, certificateDao.getByAliasAndType(secondCertificateName, CertificateType.PUBLIC));
        assertNull(certificateDao.getByAliasAndType("wrongAlias", CertificateType.PUBLIC));

    }

    /**
     * In this scenario, notAfter is outside of the start/end range so
     * the method should not return the certificate.
     */
    @Test
    @Transactional
    public void getUnNotifiedSoonRevoked() {
        Certificate certificate = new Certificate();
        String firstCertificateName = "firstCertificateName";
        certificate.setAlias(firstCertificateName);
        certificate.setNotBefore(new Date());
        certificate.setCertificateStatus(CertificateStatus.SOON_REVOKED);
        certificate.setCertificateType(CertificateType.PRIVATE);
        certificate.setNotAfter(new Date());
        certificateDao.saveOrUpdate(certificate);
        List<Certificate> unNotifiedRevoked = certificateDao.getUnNotifiedSoonRevoked();
        assertEquals(1, unNotifiedRevoked.size());

        certificate.setLastNotification(dateUtil.getStartOfDay());
        certificateDao.saveOrUpdate(certificate);
        unNotifiedRevoked = certificateDao.getUnNotifiedSoonRevoked();
        assertEquals(0, unNotifiedRevoked.size());
    }

    /**
     * In this scenario, notAfter is between start / end date and notification date is null, so
     * the method should return the certificate.
     */
    @Test
    @Transactional
    public void getUnNotifiedRevoked() {

        Certificate certificate = new Certificate();
        String firstCertificateName = "firstCertificateName";
        certificate.setAlias(firstCertificateName);
        certificate.setNotBefore(new Date());
        certificate.setCertificateStatus(CertificateStatus.REVOKED);
        certificate.setCertificateType(CertificateType.PRIVATE);
        certificate.setNotAfter(new Date());
        certificateDao.saveOrUpdate(certificate);
        List<Certificate> unNotifiedRevoked = certificateDao.getUnNotifiedRevoked();
        assertEquals(1, unNotifiedRevoked.size());

        certificate.setLastNotification(dateUtil.getStartOfDay());
        certificateDao.saveOrUpdate(certificate);
        unNotifiedRevoked = certificateDao.getUnNotifiedRevoked();
        assertEquals(0, unNotifiedRevoked.size());
    }

    @Test
    @Transactional
    public void findImminentExpirationToNotifyWithNullNotificationDate() {
        final LocalDateTime localDateTime = LocalDateTime.of(0, 1, 1, 0, 0);
        final LocalDateTime offset = localDateTime.plusDays(15);
        final LocalDateTime notification = localDateTime.plusDays(7);
        Certificate certificate = new Certificate();
        String firstCertificateName = "firstCertificateName";
        certificate.setAlias(firstCertificateName);
        certificate.setNotBefore(new Date());
        certificate.setCertificateStatus(CertificateStatus.SOON_REVOKED);
        certificate.setCertificateType(CertificateType.PRIVATE);
        certificate.setNotAfter(getDate(offset.minusDays(1)));
        certificateDao.saveOrUpdate(certificate);
        final List<Certificate> imminentExpirationToNotify = certificateDao.findImminentExpirationToNotifyAsAlert(getDate(notification), getDate(localDateTime), getDate(offset));
        assertEquals(1, imminentExpirationToNotify.size());
    }

    @Test
    @Transactional
    public void findImminentExpirationToNotifyForDynamicallyDiscoveredCertificates() {
        //we make sure there are no leftovers from other tests
        certificateDao.deleteAll(certificateDao.findAll());
        assertEquals(0, certificateDao.findAll().size());

        final LocalDateTime localDateTime = LocalDateTime.of(0, 1, 1, 0, 0);
        final LocalDateTime offset = localDateTime.plusDays(15);
        final LocalDateTime notification = localDateTime.plusDays(7);

        final String certificateAlias1 = "certificate1";
        final Certificate publicCertificate1 = persistSoonRevokedCertificate(offset, certificateAlias1, CertificateType.PUBLIC);
        //we save the dynamically discovered certificate
        final PKIUtil pkiUtil = new PKIUtil();
        final X509Certificate certificateParty1 = pkiUtil.createCertificateWithSubject(BigInteger.valueOf(111), "CN=" + certificateAlias1 + ",OU=Domibus,O=eDelivery,C=EU");
        dynamicDiscoveryLookupService.saveDynamicDiscoveryLookupTime("finalRecipient1", "endpointUrl1", certificateAlias1, "partyType", Arrays.asList("processes"), certificateAlias1, certificateParty1);

        final String certificateAlias2 = "certificate2";
        final Certificate publicCertificate2 = persistSoonRevokedCertificate(offset, certificateAlias2, CertificateType.PUBLIC);
        final String privateCertificateAlias1 = "privateCertificate1";
        final Certificate privateCertificate1 = persistSoonRevokedCertificate(offset, privateCertificateAlias1, CertificateType.PRIVATE);

        //we make sure all 3 certificates are saved
        assertEquals(3, certificateDao.findAll().size());

        //the certificate1 should not be retrieved because it was dynamically discovered
        final List<Certificate> imminentExpirationCertificates = certificateDao.findImminentExpirationToNotifyAsAlert(getDate(notification), getDate(localDateTime), getDate(offset));
        assertEquals(2, imminentExpirationCertificates.size());

        assertTrue(containsCertificateWithAlias(imminentExpirationCertificates, certificateAlias2));
        assertTrue(containsCertificateWithAlias(imminentExpirationCertificates, privateCertificateAlias1));
    }

    private boolean containsCertificateWithAlias(List<Certificate> imminentExpirationCertificates, String myAlias) {
        return imminentExpirationCertificates.stream().filter(certificate -> certificate.getAlias().equals(myAlias)).count() > 0;
    }

    private Certificate persistSoonRevokedCertificate(LocalDateTime offset, String alias, CertificateType certificateType) {
        Certificate certificate = new Certificate();
        certificate.setAlias(alias);
        certificate.setNotBefore(new Date());
        certificate.setNotAfter(getDate(offset.minusDays(1)));
        certificate.setCertificateStatus(CertificateStatus.SOON_REVOKED);
        certificate.setCertificateType(certificateType);
        certificateDao.saveOrUpdate(certificate);
        return certificate;
    }

    private Date getDate(LocalDateTime localDateTime1) {
        return Date.from(localDateTime1.atZone(ZoneOffset.UTC).toInstant());
    }

    @Test
    @Transactional
    public void findImminentExpirationToNotifyButToSoonToNotify() {
        final LocalDateTime localDateTime = LocalDateTime.of(0, 1, 1, 0, 0);
        final LocalDateTime offset = localDateTime.plusDays(15);
        final LocalDateTime notification = localDateTime.plusDays(7);
        Certificate certificate = new Certificate();
        String firstCertificateName = "firstCertificateName";
        certificate.setAlias(firstCertificateName);
        certificate.setNotBefore(new Date());
        certificate.setCertificateStatus(CertificateStatus.OK);
        certificate.setCertificateType(CertificateType.PRIVATE);
        certificate.setNotAfter(getDate(offset.minusDays(1)));
        certificate.setAlertImminentNotificationDate(getDate(notification.plusDays(1)));
        certificateDao.saveOrUpdate(certificate);
        final List<Certificate> imminentExpirationToNotify = certificateDao.findImminentExpirationToNotifyAsAlert(getDate(notification), getDate(localDateTime), getDate(offset));
        assertEquals(0, imminentExpirationToNotify.size());
    }

    @Test
    @Transactional
    public void findImminentExpirationToNotifyWithPassedNotificationDate() {
        final LocalDateTime localDateTime = LocalDateTime.of(0, 1, 1, 0, 0);
        final LocalDateTime offset = localDateTime.plusDays(15);
        final LocalDateTime notification = localDateTime.plusDays(7);
        Certificate certificate = new Certificate();
        String firstCertificateName = "firstCertificateName";
        certificate.setAlias(firstCertificateName);
        certificate.setNotBefore(new Date());
        certificate.setCertificateStatus(CertificateStatus.SOON_REVOKED);
        certificate.setCertificateType(CertificateType.PRIVATE);
        certificate.setNotAfter(getDate(offset.minusDays(1)));
        certificate.setAlertImminentNotificationDate(getDate(notification.minusDays(1)));
        certificateDao.saveOrUpdate(certificate);
        final List<Certificate> imminentExpirationToNotify = certificateDao.findImminentExpirationToNotifyAsAlert(getDate(notification), getDate(localDateTime), getDate(offset));
        assertEquals(1, imminentExpirationToNotify.size());
    }

    @Test
    @Transactional
    public void imminentExpirationNotTriggeredForExpiredCertificates() {
        final LocalDateTime localDateTime = LocalDateTime.of(0, 1, 1, 0, 0);
        final LocalDateTime offset = localDateTime.plusDays(15);
        final LocalDateTime notification = localDateTime.plusDays(7);
        Certificate certificate = new Certificate();
        String firstCertificateName = "firstCertificateName";
        certificate.setAlias(firstCertificateName);
        certificate.setNotBefore(getDate(localDateTime.minusYears(1)));
        certificate.setCertificateStatus(CertificateStatus.SOON_REVOKED);
        certificate.setCertificateType(CertificateType.PRIVATE);
        certificate.setNotAfter(getDate(localDateTime.minusDays(2))); // expired 2 days ago
        certificate.setAlertImminentNotificationDate(getDate(notification.minusDays(1)));

        certificateDao.saveOrUpdate(certificate);
        final List<Certificate> imminentExpirationToNotify = certificateDao.findImminentExpirationToNotifyAsAlert(getDate(notification), getDate(localDateTime), getDate(offset));
        assertEquals(0, imminentExpirationToNotify.size());
    }

    @Test
    @Transactional
    public void findExpiredToNotify() {
        final LocalDateTime localDateTime = LocalDateTime.of(0, 1, 1, 0, 0);
        final LocalDateTime offset = localDateTime.minusDays(15);
        final LocalDateTime notification = localDateTime.minusDays(7);
        Certificate certificate = new Certificate();
        String firstCertificateName = "firstCertificateName";
        certificate.setAlias(firstCertificateName);
        certificate.setNotBefore(new Date());
        certificate.setCertificateStatus(CertificateStatus.REVOKED);
        certificate.setCertificateType(CertificateType.PRIVATE);
        certificate.setNotAfter(getDate(localDateTime.minusDays(1)));
        certificate.setAlertImminentNotificationDate(getDate(notification.minusDays(1)));
        certificateDao.saveOrUpdate(certificate);
        final List<Certificate> imminentExpirationToNotify = certificateDao.findExpiredToNotifyAsAlert(getDate(notification), getDate(offset));
        assertEquals(1, imminentExpirationToNotify.size());

    }

    @Test
    @Transactional
    public void findExpiredButNotYet() {
        final LocalDateTime localDateTime = LocalDateTime.of(0, 1, 1, 0, 0);
        final LocalDateTime offset = localDateTime.minusDays(15);
        final LocalDateTime notification = localDateTime.minusDays(7);
        Certificate certificate = new Certificate();
        String firstCertificateName = "firstCertificateName";
        certificate.setAlias(firstCertificateName);
        certificate.setNotBefore(new Date());
        certificate.setCertificateStatus(CertificateStatus.REVOKED);
        certificate.setCertificateType(CertificateType.PRIVATE);
        certificate.setNotAfter(getDate(localDateTime.plusDays(1)));
        certificate.setAlertExpiredNotificationDate(getDate(notification.plusDays(1)));
        certificateDao.saveOrUpdate(certificate);
        final List<Certificate> imminentExpirationToNotify = certificateDao.findExpiredToNotifyAsAlert(getDate(notification), getDate(offset));
        assertEquals(0, imminentExpirationToNotify.size());

    }

    @Test
    @Transactional
    public void removeUnusedCertificates() {
        Certificate firstCertificate = new Certificate();
        firstCertificate.setAlias("firstCertificateName");
        firstCertificate.setNotBefore(new Date());
        firstCertificate.setNotAfter(new Date());
        firstCertificate.setCertificateType(CertificateType.PUBLIC);
        firstCertificate.setCertificateStatus(CertificateStatus.OK);
        certificateDao.saveOrUpdate(firstCertificate);

        Certificate secondCertificate = new Certificate();
        secondCertificate.setAlias("secondCertificateName");
        secondCertificate.setNotBefore(new Date());
        secondCertificate.setNotAfter(new Date());
        secondCertificate.setCertificateType(CertificateType.PUBLIC);
        secondCertificate.setCertificateStatus(CertificateStatus.OK);
        certificateDao.saveOrUpdate(secondCertificate);

        Certificate storeCertificate = new Certificate();
        storeCertificate.setAlias("firstCertificateName");
        storeCertificate.setNotBefore(new Date());
        storeCertificate.setNotAfter(new Date());
        storeCertificate.setCertificateType(CertificateType.PUBLIC);
        storeCertificate.setCertificateStatus(CertificateStatus.OK);

        certificateDao.removeUnusedCertificates(Collections.singletonList(storeCertificate));

        TypedQuery<Certificate> namedQuery = em.createNamedQuery("Certificate.findAll", Certificate.class);
        List<Certificate> persistedCertificates = namedQuery.getResultList();

        assertEquals(1, persistedCertificates.size());
        assertEquals("firstCertificateName", persistedCertificates.get(0).getAlias());
    }

}
