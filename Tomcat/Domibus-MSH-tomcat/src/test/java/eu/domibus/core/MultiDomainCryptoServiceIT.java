package eu.domibus.core;

import eu.domibus.AbstractIT;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.pki.KeyStoreContentInfo;
import eu.domibus.api.pki.KeystorePersistenceInfo;
import eu.domibus.api.pki.KeystorePersistenceService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.api.util.FileServiceUtil;
import eu.domibus.core.certificate.Certificate;
import eu.domibus.core.certificate.CertificateDaoImpl;
import eu.domibus.core.certificate.CertificateHelper;
import eu.domibus.core.certificate.CertificateServiceImpl;
import eu.domibus.core.crypto.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.KeyStore;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

import static eu.domibus.core.crypto.MultiDomainCryptoServiceImpl.DOMIBUS_TRUSTSTORE_NAME;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Transactional
public class MultiDomainCryptoServiceIT extends AbstractIT {


    @Autowired
    private MultiDomainCryptoServiceImpl multiDomainCryptoService;

    @Autowired
    TruststoreDao truststoreDao;

    @Autowired
    DomainCryptoServiceFactory domainCertificateProviderFactory1;

    @Autowired
    DefaultDomainCryptoServiceSpiImpl defaultDomainCryptoServiceSpi;

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

    private Date getDate(LocalDateTime localDateTime1) {
        return Date.from(localDateTime1.atZone(ZoneOffset.UTC).toInstant());
    }

    @After
    public void clean() {
        final LocalDateTime localDateTime = LocalDateTime.of(0, 1, 1, 0, 0);
        final LocalDateTime offset = localDateTime.minusDays(15);
        final LocalDateTime notification = localDateTime.minusDays(7);
        List<Certificate> certs2 = certificateDao.findExpiredToNotifyAsAlert(getDate(notification), getDate(offset));
        certificateDao.deleteAll(certs2);

        if (truststoreDao.existsWithName(DOMIBUS_TRUSTSTORE_NAME)) {
            TruststoreEntity trust = truststoreDao.findByName(DOMIBUS_TRUSTSTORE_NAME);
            truststoreDao.delete(trust);
        }
    }

    @Test
    @Ignore
    @Transactional
    public void persistTrustStoresIfApplicable() {
        multiDomainCryptoService.saveStoresFromDBToDisk();
        boolean isPersisted = truststoreDao.existsWithName(DOMIBUS_TRUSTSTORE_NAME);
        Assert.assertTrue(isPersisted);
    }

    @Test
    @Transactional
    public void replaceTrustStore() throws IOException {
        String newStoreName = "gateway_truststore2.jks";
        String storePassword = "test123";
        Domain domain = DomainService.DEFAULT_DOMAIN;

        Path path = Paths.get(domibusConfigurationService.getConfigLocation(), "keystores", newStoreName);
        byte[] content = Files.readAllBytes(path);
        KeyStoreContentInfo storeInfo = certificateHelper.createStoreContentInfo(DOMIBUS_TRUSTSTORE_NAME, newStoreName, content, storePassword);

        KeyStore initialStore = multiDomainCryptoService.getTrustStore(domain);
        KeyStoreContentInfo initialStoreContent = multiDomainCryptoService.getTrustStoreContent(domain);
        List<TrustStoreEntry> initialStoreEntries = multiDomainCryptoService.getTrustStoreEntries(domain);

        backupStore(initialStoreContent);

        multiDomainCryptoService.replaceTrustStore(domain, storeInfo);

        KeyStore newStore = multiDomainCryptoService.getTrustStore(domain);
        List<TrustStoreEntry> newStoreEntries = multiDomainCryptoService.getTrustStoreEntries(domain);
        KeyStoreContentInfo newStoreContent = multiDomainCryptoService.getTrustStoreContent(domain);

        Assert.assertFalse(initialStore.equals(newStore));
        Assert.assertNotEquals(initialStoreEntries.size(), newStoreEntries.size());

        restore();
    }

    private void restore() throws IOException {
        KeystorePersistenceInfo trustPersistInfo = keystorePersistenceService.getTrustStorePersistenceInfo();
        String backupFileName = FilenameUtils.getBaseName(trustPersistInfo.getFileLocation()) + "_back." + FilenameUtils.getExtension(trustPersistInfo.getFileLocation());
        Path backupFileLocation = Paths.get(FilenameUtils.getFullPath(trustPersistInfo.getFileLocation()), backupFileName);
        Path initialLocation = Paths.get(trustPersistInfo.getFileLocation());
        byte[] initialContent = fileServiceUtil.getContentFromFile(backupFileLocation.toString());
        Files.write(initialLocation, initialContent, StandardOpenOption.WRITE);
    }

    private void backupStore(KeyStoreContentInfo currentStoreContent) throws IOException {
        KeystorePersistenceInfo trustPersistInfo = keystorePersistenceService.getTrustStorePersistenceInfo();
        String backupFileName = FilenameUtils.getBaseName(trustPersistInfo.getFileLocation()) + "_back." + FilenameUtils.getExtension(trustPersistInfo.getFileLocation());
        Path backupFileLocation = Paths.get(FilenameUtils.getFullPath(trustPersistInfo.getFileLocation()), backupFileName);
        Files.write(backupFileLocation, currentStoreContent.getContent(), StandardOpenOption.CREATE);
    }

//    @Test
//    @Transactional
//    public void replaceTrustStore2() throws IOException {
//        multiDomainCryptoService.saveStoresFromDBToDisk();
//
//        List<TrustStoreEntry> trustStoreEntries = certificateService.getStoreEntries(DOMIBUS_TRUSTSTORE_NAME);
//        Assert.assertEquals(2, trustStoreEntries.size());
//
//        Path path = Paths.get(domibusConfigurationService.getConfigLocation(), "keystores", "cefsupportgwtruststore.jks");
//        byte[] content = Files.readAllBytes(path);
//        String file_name = "cefsupportgwtruststore.jks";
//        multiDomainCryptoService.replaceTrustStore(DomainService.DEFAULT_DOMAIN, file_name, content, "test123");
//
//        trustStoreEntries = certificateService.getStoreEntries(DOMIBUS_TRUSTSTORE_NAME);
//        Assert.assertEquals(9, trustStoreEntries.size());
//    }

//    @Test
//    @Transactional
//    public void getTrustStoreEntries() {
//        multiDomainCryptoService.saveStoresFromDBToDisk();
//        List<TrustStoreEntry> trustStoreEntries = certificateService.getStoreEntries(DOMIBUS_TRUSTSTORE_NAME);
//        Assert.assertEquals(2, trustStoreEntries.size());
//    }

//    @Test
//    @Transactional
//    public void addCertificate() throws IOException {
//        multiDomainCryptoService.saveStoresFromDBToDisk();
//
//        List<TrustStoreEntry> trustStoreEntries = certificateService.getStoreEntries(DOMIBUS_TRUSTSTORE_NAME);
//        Assert.assertEquals(2, trustStoreEntries.size());
//
//        Path path = Paths.get(domibusConfigurationService.getConfigLocation(), "keystores", "green_gw.cer");
//        byte[] content = Files.readAllBytes(path);
//        String green_gw = "green_gw";
//        X509Certificate x509Certificate = certificateService.loadCertificateFromString(Base64.getEncoder().encodeToString(content));
//        multiDomainCryptoService.addCertificate(domainContextProvider.getCurrentDomain(), Arrays.asList(new CertificateEntry(green_gw, x509Certificate)), true);
//
//        trustStoreEntries = certificateService.getStoreEntries(DOMIBUS_TRUSTSTORE_NAME);
//        Assert.assertEquals(3, trustStoreEntries.size());
//        Assert.assertTrue(trustStoreEntries.stream().anyMatch(entry -> entry.getName().equals(green_gw)));
//    }

//    @Test
//    @Transactional
//    public void getCertificateFromTruststore() throws KeyStoreException {
//        multiDomainCryptoService.saveStoresFromDBToDisk();
//
//        List<TrustStoreEntry> trustStoreEntries = certificateService.getStoreEntries(DOMIBUS_TRUSTSTORE_NAME);
//        Assert.assertEquals(2, trustStoreEntries.size());
//
//        String blue_gw = "blue_gw";
//        X509Certificate certificateFromTruststore = multiDomainCryptoService.getCertificateFromTruststore(domainContextProvider.getCurrentDomain(), blue_gw);
//
//        trustStoreEntries = certificateService.getStoreEntries(DOMIBUS_TRUSTSTORE_NAME);
//        Assert.assertEquals(2, trustStoreEntries.size());
//        Assert.assertTrue(certificateFromTruststore.getIssuerDN().getName().contains(blue_gw));
//    }

//    @Test
//    @Transactional
//    public void getTrustStore() throws KeyStoreException, IOException {
//        multiDomainCryptoService.saveStoresFromDBToDisk();
//
//        KeyStore trustStore = multiDomainCryptoService.getTrustStore(domainContextProvider.getCurrentDomain());
//        Assert.assertTrue(trustStore.containsAlias("blue_gw"));
//
//        Path path = Paths.get(domibusConfigurationService.getConfigLocation(), "keystores", "cefsupportgwtruststore.jks");
//        byte[] content = Files.readAllBytes(path);
//        String file_name = "cefsupportgwtruststore.jks";
//        multiDomainCryptoService.replaceTrustStore(DomainService.DEFAULT_DOMAIN, file_name, content, "test123");
//
//        trustStore = multiDomainCryptoService.getTrustStore(domainContextProvider.getCurrentDomain());
//        List<TrustStoreEntry> trustStoreEntries = certificateService.getStoreEntries(DOMIBUS_TRUSTSTORE_NAME);
//        Assert.assertTrue(trustStore.containsAlias("ceftestparty4gw"));
//    }

    @Test
    @Transactional
    public void isCertificateChainValid() {
        multiDomainCryptoService.saveStoresFromDBToDisk();

        String blue_gw = "blue_gw";
        boolean certificateChainValid = multiDomainCryptoService.isCertificateChainValid(domainContextProvider.getCurrentDomain(), blue_gw);

        Assert.assertTrue(certificateChainValid);
    }

    @Test
    @Transactional
    public void getDefaultX509Identifier() throws WSSecurityException {
        multiDomainCryptoService.saveStoresFromDBToDisk();

        String blue_gw = "blue_gw";
        String defaultX509Identifier = multiDomainCryptoService.getDefaultX509Identifier(domainContextProvider.getCurrentDomain());

        Assert.assertEquals(defaultX509Identifier, blue_gw);
    }

//    @Test
//    @Transactional
//    public void removeCertificate()  {
//        multiDomainCryptoService.saveStoresFromDBToDisk();
//
//        List<TrustStoreEntry> trustStoreEntries = certificateService.getStoreEntries(DOMIBUS_TRUSTSTORE_NAME);
//        Assert.assertEquals(2, trustStoreEntries.size());
//
//        String red_gw = "red_gw";
//        multiDomainCryptoService.removeCertificate(domainContextProvider.getCurrentDomain(), red_gw);
//
//        trustStoreEntries = certificateService.getStoreEntries(DOMIBUS_TRUSTSTORE_NAME);
//        Assert.assertEquals(1, trustStoreEntries.size());
//        Assert.assertTrue(trustStoreEntries.stream().noneMatch(entry -> entry.getName().equals(red_gw)));
//    }
}
