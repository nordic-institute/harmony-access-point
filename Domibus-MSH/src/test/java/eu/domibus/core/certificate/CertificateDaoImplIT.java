package eu.domibus.core.certificate;

import eu.domibus.api.util.DateUtil;
import eu.domibus.core.dao.InMemoryDatabaseMshConfig;
import eu.domibus.core.util.DateUtilImpl;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {InMemoryDatabaseMshConfig.class,
        CertificateDaoImplIT.CertificateDaoConfig.class})
@ActiveProfiles("IN_MEMORY_DATABASE")
public class CertificateDaoImplIT {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CertificateDaoImplIT.class);

    //needed because CertificateDaoImpl implements an interface, so spring tries to convert it to interface based
    //proxy. But one of the method tested is not declared in the interface.
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    @Configuration
    static public class CertificateDaoConfig {

        @Bean
        public CertificateDaoImpl certificateDao() {
            return new CertificateDaoImpl();
        }

        @Bean
        public DateUtil dateUtil() {
            return new DateUtilImpl();
        }
    }

    @PersistenceContext
    private javax.persistence.EntityManager em;

    @Autowired
    private CertificateDaoImpl certificateDao;

    @Autowired
    private DateUtil dateUtil;

    @Before
    public void setup() {
        LOG.putMDC(DomibusLogger.MDC_USER, "test_user");
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
        em.persist(certificate);
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

        em.persist(secondCertificate);

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

        em.persist(firstCertificate);
        em.persist(secondCertificate);

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
        em.persist(certificate);
        List<Certificate> unNotifiedRevoked = certificateDao.getUnNotifiedSoonRevoked();
        assertEquals(1, unNotifiedRevoked.size());

        certificate.setLastNotification(dateUtil.getStartOfDay());
        em.persist(certificate);
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
        em.persist(certificate);
        List<Certificate> unNotifiedRevoked = certificateDao.getUnNotifiedRevoked();
        assertEquals(1, unNotifiedRevoked.size());

        certificate.setLastNotification(dateUtil.getStartOfDay());
        em.persist(certificate);
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
        em.persist(certificate);
        final List<Certificate> imminentExpirationToNotify = certificateDao.findImminentExpirationToNotifyAsAlert(getDate(notification), getDate(localDateTime), getDate(offset));
        assertEquals(1, imminentExpirationToNotify.size());
    }

    private Date getDate(LocalDateTime localDateTime1) {
        return Date.from(localDateTime1.atZone(ZoneId.systemDefault()).toInstant());
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
        em.persist(certificate);
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
        em.persist(certificate);
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
        em.persist(certificate);
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
        em.persist(certificate);
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
        em.persist(certificate);
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
        em.persist(firstCertificate);

        Certificate secondCertificate = new Certificate();
        secondCertificate.setAlias("secondCertificateName");
        secondCertificate.setNotBefore(new Date());
        secondCertificate.setNotAfter(new Date());
        secondCertificate.setCertificateType(CertificateType.PUBLIC);
        secondCertificate.setCertificateStatus(CertificateStatus.OK);
        em.persist(secondCertificate);

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