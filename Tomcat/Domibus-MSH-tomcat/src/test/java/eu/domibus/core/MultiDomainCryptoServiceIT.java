package eu.domibus.core;

import eu.domibus.AbstractIT;
import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.pki.*;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.api.util.FileServiceUtil;
import eu.domibus.core.certificate.Certificate;
import eu.domibus.core.certificate.CertificateDaoImpl;
import eu.domibus.core.certificate.CertificateHelper;
import eu.domibus.core.certificate.CertificateServiceImpl;
import eu.domibus.core.crypto.MultiDomainCryptoServiceImpl;
import eu.domibus.core.crypto.TruststoreDao;
import eu.domibus.core.crypto.spi.CryptoSpiException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.junit.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;
import static eu.domibus.core.crypto.MultiDomainCryptoServiceImpl.DOMIBUS_TRUSTSTORE_NAME;

/**
 * @author Ion Perpegel
 * @since 5.0
 */

public class MultiDomainCryptoServiceIT extends AbstractIT {
    public static final String KEYSTORES = "keystores";

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MultiDomainCryptoServiceIT.class);

    @Autowired
    private MultiDomainCryptoServiceImpl multiDomainCryptoService;

    @Autowired
    TruststoreDao truststoreDao;

    @Autowired
    CertificateServiceImpl certificateService;

    @Autowired
    DomibusConfigurationService domibusConfigurationService;

    @Autowired
    CertificateDaoImpl certificateDao;

    @Autowired
    CertificateHelper certificateHelper;

    @Autowired
    KeystorePersistenceService keystorePersistenceService;

    @Autowired
    FileServiceUtil fileServiceUtil;

    @Autowired
    DomibusPropertyProvider domibusPropertyProvider;

    @Before
    public void before() {
        clean();
    }

    @After
    public void after() {
        clean();
    }

    public void clean() {
        final LocalDateTime localDateTime = LocalDateTime.of(0, 1, 1, 0, 0);
        final LocalDateTime offset = localDateTime.minusDays(15);
        final LocalDateTime notification = localDateTime.minusDays(7);
        List<Certificate> certs2 = certificateDao.findExpiredToNotifyAsAlert(getDate(notification), getDate(offset));
        certificateDao.deleteAll(certs2);

        resetInitialTruststore();
    }

    @Test
    @Ignore
    public void saveStoresFromDBToDisk() {
        Domain domain = DomainService.DEFAULT_DOMAIN;
        createStore(DOMIBUS_TRUSTSTORE_NAME, "keystores/gateway_truststore2.jks");

        boolean exists = truststoreDao.existsWithName(DOMIBUS_TRUSTSTORE_NAME);
        Assert.assertTrue(exists);

        multiDomainCryptoService.saveStoresFromDBToDisk();

        exists = truststoreDao.existsWithName(DOMIBUS_TRUSTSTORE_NAME);
        Assert.assertFalse(exists);

        List<TrustStoreEntry> storeEntries = multiDomainCryptoService.getTrustStoreEntries(domain);
        Assert.assertEquals(2, storeEntries.size());
        Assert.assertTrue(storeEntries.stream().noneMatch(entry -> entry.getName().equals("cefsupportgw")));

        multiDomainCryptoService.resetTrustStore(domain);

        storeEntries = multiDomainCryptoService.getTrustStoreEntries(domain);
        Assert.assertEquals(9, storeEntries.size());
        Assert.assertTrue(storeEntries.stream().anyMatch(entry -> entry.getName().equals("cefsupportgw")));
    }

    @Test
    public void replaceTrustStore() throws IOException {
        String newStoreName = "gateway_truststore2.jks";
        String storePassword = "test123";
        Domain domain = DomainService.DEFAULT_DOMAIN;

        Path path = Paths.get(domibusConfigurationService.getConfigLocation(), KEYSTORES, newStoreName);
        byte[] content = Files.readAllBytes(path);
        KeyStoreContentInfo storeInfo = certificateHelper.createStoreContentInfo(DOMIBUS_TRUSTSTORE_NAME, newStoreName, content, storePassword);

        KeyStore initialStore = multiDomainCryptoService.getTrustStore(domain);
        KeyStoreContentInfo initialStoreContent = multiDomainCryptoService.getTrustStoreContent(domain);
        List<TrustStoreEntry> initialStoreEntries = multiDomainCryptoService.getTrustStoreEntries(domain);

        multiDomainCryptoService.replaceTrustStore(domain, storeInfo);

        KeyStore newStore = multiDomainCryptoService.getTrustStore(domain);
        List<TrustStoreEntry> newStoreEntries = multiDomainCryptoService.getTrustStoreEntries(domain);
        KeyStoreContentInfo newStoreContent = multiDomainCryptoService.getTrustStoreContent(domain);

        Assert.assertNotEquals(initialStore, newStore);
        Assert.assertNotEquals(initialStoreContent.getContent(), newStoreContent.getContent());
        Assert.assertNotEquals(initialStoreEntries.size(), newStoreEntries.size());
    }

    @Test
    public void replaceTrustStoreWithDifferentTypeAndPassword() throws IOException {
        Domain domain = DomainService.DEFAULT_DOMAIN;
        String initialLocation = domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_TRUSTSTORE_LOCATION);
        String initialType = domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_TRUSTSTORE_TYPE);
        String initialPassword = domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_TRUSTSTORE_PASSWORD);

        String newStoreName = "gateway_truststore_diffPass.p12";
        String newStorePassword = "test1234";

        Path path = Paths.get(domibusConfigurationService.getConfigLocation(), KEYSTORES, newStoreName);
        byte[] content = Files.readAllBytes(path);
        KeyStoreContentInfo storeInfo = certificateHelper.createStoreContentInfo(DOMIBUS_TRUSTSTORE_NAME, newStoreName, content, newStorePassword);

        KeyStore initialStore = multiDomainCryptoService.getTrustStore(domain);
        KeyStoreContentInfo initialStoreContent = multiDomainCryptoService.getTrustStoreContent(domain);
        List<TrustStoreEntry> initialStoreEntries = multiDomainCryptoService.getTrustStoreEntries(domain);

        multiDomainCryptoService.replaceTrustStore(domain, storeInfo);

        String newLocation = domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_TRUSTSTORE_LOCATION);
        String newType = domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_TRUSTSTORE_TYPE);
        String newPassword = domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_TRUSTSTORE_PASSWORD);

        // initial properties didn't change
        Assert.assertEquals(initialLocation, newLocation);
        Assert.assertEquals(initialType, newType);
        Assert.assertEquals(initialPassword, newPassword);

        // can still open the store
        KeyStore newStore = multiDomainCryptoService.getTrustStore(domain);
        List<TrustStoreEntry> newStoreEntries = multiDomainCryptoService.getTrustStoreEntries(domain);
        KeyStoreContentInfo newStoreContent = multiDomainCryptoService.getTrustStoreContent(domain);

        Assert.assertNotEquals(initialStore, newStore);
        Assert.assertNotEquals(initialStoreContent.getContent(), newStoreContent.getContent());
        Assert.assertNotEquals(initialStoreEntries.size(), newStoreEntries.size());
    }

    @Test
    public void replaceTrustStoreWithDifferentTypeAndPasswordWhenCurrentStoreIsBroken() throws IOException {
        Domain domain = DomainService.DEFAULT_DOMAIN;
        String initialLocation = domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_TRUSTSTORE_LOCATION);
        String initialType = domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_TRUSTSTORE_TYPE);
        String initialPassword = domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_TRUSTSTORE_PASSWORD);
        //change the password domibus property to incur error when trying to get it
        domibusPropertyProvider.setProperty(domain, DOMIBUS_SECURITY_TRUSTSTORE_PASSWORD,"test111");
        String modifiedInitialPassword = domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_TRUSTSTORE_PASSWORD);

        String newStoreName = "gateway_truststore_diffPass.p12";
        String newStorePassword = "test1234";

        Path path = Paths.get(domibusConfigurationService.getConfigLocation(), KEYSTORES, newStoreName);
        byte[] content = Files.readAllBytes(path);
        KeyStoreContentInfo storeInfo = certificateHelper.createStoreContentInfo(DOMIBUS_TRUSTSTORE_NAME, newStoreName, content, newStorePassword);

        // error trying to check that the store is changed on disk and reload since the password is not correct
        Assert.assertThrows(CryptoException.class, () -> multiDomainCryptoService.isTrustStoreChangedOnDisk(domain));
        Assert.assertThrows(CryptoSpiException.class, () -> multiDomainCryptoService.resetTrustStore(domain));

        // still can replace it
        multiDomainCryptoService.replaceTrustStore(domain, storeInfo);

        String newLocation = domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_TRUSTSTORE_LOCATION);
        String newType = domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_TRUSTSTORE_TYPE);
        String newPassword = domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_TRUSTSTORE_PASSWORD);

        // initial properties didn't change
        Assert.assertEquals(initialLocation, newLocation);
        Assert.assertEquals(initialType, newType);
        Assert.assertEquals(modifiedInitialPassword, newPassword);

        // can still open the store
        List<TrustStoreEntry> newStoreEntries = multiDomainCryptoService.getTrustStoreEntries(domain);

        Assert.assertEquals(1, newStoreEntries.size());
    }

    @Test
    public void getTrustStoreEntries() {
        List<TrustStoreEntry> trustStoreEntries = multiDomainCryptoService.getTrustStoreEntries(DomainService.DEFAULT_DOMAIN);

        KeystorePersistenceInfo trustPersistInfo = keystorePersistenceService.getTrustStorePersistenceInfo();
        List<TrustStoreEntry> trustStoreEntries2 = certificateService.getStoreEntries(trustPersistInfo);

        Assert.assertEquals(trustStoreEntries2.size(), trustStoreEntries.size());
        Assert.assertEquals(trustStoreEntries2.get(0), trustStoreEntries.get(0));

        Assert.assertEquals(2, trustStoreEntries.size());
    }

    @Test
    public void addCertificate() throws IOException {
        Domain domain = DomainService.DEFAULT_DOMAIN;

        List<TrustStoreEntry> initialStoreEntries = multiDomainCryptoService.getTrustStoreEntries(domain);
        Assert.assertEquals(2, initialStoreEntries.size());

        Path path = Paths.get(domibusConfigurationService.getConfigLocation(), KEYSTORES, "green_gw.cer");
        byte[] content = Files.readAllBytes(path);
        String green_gw = "green_gw";
        X509Certificate x509Certificate = certificateService.loadCertificate(Base64.getEncoder().encodeToString(content));
        multiDomainCryptoService.addCertificate(domainContextProvider.getCurrentDomain(), Arrays.asList(new CertificateEntry(green_gw, x509Certificate)), true);

        List<TrustStoreEntry> trustStoreEntries = multiDomainCryptoService.getTrustStoreEntries(domain);
        Assert.assertEquals(3, trustStoreEntries.size());
        Assert.assertTrue(trustStoreEntries.stream().anyMatch(entry -> entry.getName().equals(green_gw)));
    }

    @Test
    public void addSameCertificate() throws IOException {
        Domain domain = DomainService.DEFAULT_DOMAIN;

        List<TrustStoreEntry> initialStoreEntries = multiDomainCryptoService.getTrustStoreEntries(domain);
        Assert.assertEquals(2, initialStoreEntries.size());

        Path path = Paths.get(domibusConfigurationService.getConfigLocation(), KEYSTORES, "green_gw.cer");
        byte[] content = Files.readAllBytes(path);
        String green_gw = "green_gw";
        X509Certificate x509Certificate = certificateService.loadCertificate(Base64.getEncoder().encodeToString(content));

        multiDomainCryptoService.addCertificate(domain, Arrays.asList(new CertificateEntry(green_gw, x509Certificate)), true);

        List<TrustStoreEntry> trustStoreEntries = multiDomainCryptoService.getTrustStoreEntries(domain);
        Assert.assertEquals(3, trustStoreEntries.size());
        Assert.assertTrue(trustStoreEntries.stream().anyMatch(entry -> entry.getName().equals(green_gw)));

        boolean added = multiDomainCryptoService.addCertificate(domain, x509Certificate, green_gw, true);
        Assert.assertFalse(added);
        trustStoreEntries = multiDomainCryptoService.getTrustStoreEntries(domain);
        Assert.assertEquals(3, trustStoreEntries.size());
    }

    @Test
    public void getCertificateFromTruststore() throws KeyStoreException {
        Domain domain = DomainService.DEFAULT_DOMAIN;

        List<TrustStoreEntry> initialStoreEntries = multiDomainCryptoService.getTrustStoreEntries(domain);

        Assert.assertEquals(2, initialStoreEntries.size());

        String blue_gw = "blue_gw";
        X509Certificate certificateFromTruststore = multiDomainCryptoService.getCertificateFromTruststore(domain, blue_gw);

        Assert.assertTrue(certificateFromTruststore.getIssuerDN().getName().contains(blue_gw));
    }

    @Test
    public void getTrustStoreReplaceTrustStore() throws KeyStoreException, IOException {
        Domain domain = DomainService.DEFAULT_DOMAIN;
        String file_name = "cefsupportgwtruststore.jks";

        KeyStore trustStore = multiDomainCryptoService.getTrustStore(domain);
        Assert.assertTrue(trustStore.containsAlias("blue_gw"));

        Path path = Paths.get(domibusConfigurationService.getConfigLocation(), KEYSTORES, file_name);
        byte[] content = Files.readAllBytes(path);

        String password = "test123";
        KeyStoreContentInfo storeInfo = certificateHelper.createStoreContentInfo(DOMIBUS_TRUSTSTORE_NAME, file_name, content, password);

        multiDomainCryptoService.replaceTrustStore(DomainService.DEFAULT_DOMAIN, storeInfo);

        trustStore = multiDomainCryptoService.getTrustStore(domain);
        Assert.assertTrue(trustStore.containsAlias("ceftestparty4gw"));
    }

    @Test
    public void isCertificateChainValid() {
        String blue_gw = "blue_gw";
        boolean certificateChainValid = multiDomainCryptoService.isCertificateChainValid(domainContextProvider.getCurrentDomain(), blue_gw);

        Assert.assertTrue(certificateChainValid);
    }

    @Test
    public void getDefaultX509Identifier() throws WSSecurityException {
        String blue_gw = "blue_gw";
        String defaultX509Identifier = multiDomainCryptoService.getDefaultX509Identifier(domainContextProvider.getCurrentDomain());

        Assert.assertEquals(defaultX509Identifier, blue_gw);
    }

    @Test
    public void removeCertificate() {
        Domain domain = DomainService.DEFAULT_DOMAIN;

        List<TrustStoreEntry> trustStoreEntries = multiDomainCryptoService.getTrustStoreEntries(domain);
        Assert.assertEquals(2, trustStoreEntries.size());

        String red_gw = "red_gw";
        boolean removed = multiDomainCryptoService.removeCertificate(domainContextProvider.getCurrentDomain(), red_gw);

        Assert.assertTrue(removed);
        trustStoreEntries = multiDomainCryptoService.getTrustStoreEntries(domain);
        Assert.assertEquals(1, trustStoreEntries.size());
        Assert.assertTrue(trustStoreEntries.stream().noneMatch(entry -> entry.getName().equals(red_gw)));
    }

    @Test
    public void removeSameCertificate() {
        Domain domain = DomainService.DEFAULT_DOMAIN;

        List<TrustStoreEntry> trustStoreEntries = multiDomainCryptoService.getTrustStoreEntries(domain);
        Assert.assertEquals(2, trustStoreEntries.size());

        String red_gw = "red_gw";
        boolean removed = multiDomainCryptoService.removeCertificate(domainContextProvider.getCurrentDomain(), red_gw);

        Assert.assertTrue(removed);
        trustStoreEntries = multiDomainCryptoService.getTrustStoreEntries(domain);
        Assert.assertEquals(1, trustStoreEntries.size());
        Assert.assertTrue(trustStoreEntries.stream().noneMatch(entry -> entry.getName().equals(red_gw)));

        removed = multiDomainCryptoService.removeCertificate(domainContextProvider.getCurrentDomain(), red_gw);

        Assert.assertFalse(removed);
        trustStoreEntries = multiDomainCryptoService.getTrustStoreEntries(domain);
        Assert.assertEquals(1, trustStoreEntries.size());
        Assert.assertTrue(trustStoreEntries.stream().noneMatch(entry -> entry.getName().equals(red_gw)));
    }

    @Test
    public void isChangedOnDisk() throws KeyStoreException, IOException {
        Domain domain = DomainService.DEFAULT_DOMAIN;

        KeyStore trustStore = multiDomainCryptoService.getTrustStore(domain);
        Assert.assertTrue(trustStore.containsAlias("blue_gw"));

        List<TrustStoreEntry> initialStoreEntries = multiDomainCryptoService.getTrustStoreEntries(domain);
        Assert.assertEquals(2, initialStoreEntries.size());

        Path path = Paths.get(domibusConfigurationService.getConfigLocation(), KEYSTORES, "cefsupportgwtruststore.jks");
        byte[] content = Files.readAllBytes(path);
        Path currentPath = Paths.get(domibusConfigurationService.getConfigLocation(), KEYSTORES, "gateway_truststore.jks");
        Files.write(currentPath, content, StandardOpenOption.WRITE);

        boolean isChangedOnDisk = multiDomainCryptoService.isTrustStoreChangedOnDisk(domain);
        Assert.assertTrue(isChangedOnDisk);

        trustStore = multiDomainCryptoService.getTrustStore(domain);
        Assert.assertFalse(trustStore.containsAlias("ceftestparty4gw"));

        multiDomainCryptoService.resetTrustStore(domain);
        trustStore = multiDomainCryptoService.getTrustStore(domain);
        Assert.assertTrue(trustStore.containsAlias("ceftestparty4gw"));
    }

//    private void backupTrustStore() throws IOException {
//        Domain domain = DomainService.DEFAULT_DOMAIN;
//        KeyStoreContentInfo initialStoreContent = multiDomainCryptoService.getTrustStoreContent(domain);
//        backupStore(initialStoreContent);
//    }
//
//    private void backupStore(KeyStoreContentInfo currentStoreContent) throws IOException {
//        KeystorePersistenceInfo trustPersistInfo = keystorePersistenceService.getTrustStorePersistenceInfo();
//        String backupFileName = FilenameUtils.getBaseName(trustPersistInfo.getFileLocation()) + "_back." + FilenameUtils.getExtension(trustPersistInfo.getFileLocation());
//        Path backupFileLocation = Paths.get(FilenameUtils.getFullPath(trustPersistInfo.getFileLocation()), backupFileName);
//        Files.write(backupFileLocation, currentStoreContent.getContent(), StandardOpenOption.CREATE);
//    }
//
//    private void restore() throws IOException {
//        KeystorePersistenceInfo trustPersistInfo = keystorePersistenceService.getTrustStorePersistenceInfo();
//        String backupFileName = FilenameUtils.getBaseName(trustPersistInfo.getFileLocation()) + "_back." + FilenameUtils.getExtension(trustPersistInfo.getFileLocation());
//        Path backupFileLocation = Paths.get(FilenameUtils.getFullPath(trustPersistInfo.getFileLocation()), backupFileName);
//        Path initialLocation = Paths.get(trustPersistInfo.getFileLocation());
//        byte[] initialContent = fileServiceUtil.getContentFromFile(backupFileLocation.toString());
//        Files.write(initialLocation, initialContent, StandardOpenOption.WRITE);
//    }

    private void resetInitialTruststore() {
        try {
            Domain domain = DomainService.DEFAULT_DOMAIN;
            multiDomainCryptoService.resetTrustStore(domain);
            String storePassword = "test123";
            domibusPropertyProvider.setProperty(domain, DOMIBUS_SECURITY_TRUSTSTORE_PASSWORD, storePassword);
            Path path = Paths.get(domibusConfigurationService.getConfigLocation(), KEYSTORES, "gateway_truststore_original.jks");
            byte[] content = Files.readAllBytes(path);
            KeyStoreContentInfo storeInfo = certificateHelper.createStoreContentInfo(DOMIBUS_TRUSTSTORE_NAME, "gateway_truststore.jks", content, storePassword);
            multiDomainCryptoService.replaceTrustStore(domain, storeInfo);
        } catch (Exception e) {
            LOG.info("Error restoring initial keystore", e);
        }
    }

    private Date getDate(LocalDateTime localDateTime1) {
        return Date.from(localDateTime1.atZone(ZoneOffset.UTC).toInstant());
    }
}
