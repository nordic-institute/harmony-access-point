package eu.domibus.core;

import eu.domibus.AbstractIT;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.pki.CertificateEntry;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.core.certificate.Certificate;
import eu.domibus.core.certificate.CertificateDaoImpl;
import eu.domibus.core.certificate.CertificateServiceImpl;
import eu.domibus.core.crypto.*;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Base64;
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
    @Transactional
    public void persistTruststoresIfApplicable() {
        multiDomainCryptoService.persistTruststoresIfApplicable();
        boolean isPersisted = truststoreDao.existsWithName(DOMIBUS_TRUSTSTORE_NAME);
        Assert.assertTrue(isPersisted);
    }

    @Test
    @Transactional
    public void replaceTrustStore() {
        Domain domain = DomainService.DEFAULT_DOMAIN;
        String password = "test123";
        multiDomainCryptoService.persistTruststoresIfApplicable();
        byte[] store = certificateService.getTruststoreContent(DOMIBUS_TRUSTSTORE_NAME);

        multiDomainCryptoService.replaceTrustStore(domain, DOMIBUS_TRUSTSTORE_NAME + ".jks", store, password);
        boolean isPersisted = truststoreDao.existsWithName(DOMIBUS_TRUSTSTORE_NAME);
        Assert.assertTrue(isPersisted);
    }

    @Test
    @Transactional
    public void replaceTrustStore2() throws IOException {
        multiDomainCryptoService.persistTruststoresIfApplicable();

        List<TrustStoreEntry> trustStoreEntries = certificateService.getTrustStoreEntries(DOMIBUS_TRUSTSTORE_NAME);
        Assert.assertTrue(trustStoreEntries.size() == 2);

        Path path = Paths.get(domibusConfigurationService.getConfigLocation(), "keystores", "cefsupportgwtruststore.jks");
        byte[] content = Files.readAllBytes(path);
        String file_name = "cefsupportgwtruststore.jks";
        multiDomainCryptoService.replaceTrustStore(DomainService.DEFAULT_DOMAIN, file_name, content, "test123");

        trustStoreEntries = certificateService.getTrustStoreEntries(DOMIBUS_TRUSTSTORE_NAME);
        Assert.assertTrue(trustStoreEntries.size() == 9);
    }

    @Test
    @Transactional
    public void getTrustStoreEntries() {
        multiDomainCryptoService.persistTruststoresIfApplicable();
        List<TrustStoreEntry> trustStoreEntries = certificateService.getTrustStoreEntries(DOMIBUS_TRUSTSTORE_NAME);
        Assert.assertTrue(trustStoreEntries.size() == 2);
    }

    @Test
    @Transactional
    public void addCertificate() throws IOException {
        multiDomainCryptoService.persistTruststoresIfApplicable();

        List<TrustStoreEntry> trustStoreEntries = certificateService.getTrustStoreEntries(DOMIBUS_TRUSTSTORE_NAME);
        Assert.assertTrue(trustStoreEntries.size() == 2);

        Path path = Paths.get(domibusConfigurationService.getConfigLocation(), "keystores", "green_gw.cer");
        byte[] content = Files.readAllBytes(path);
        String green_gw = "green_gw";
        X509Certificate x509Certificate = certificateService.loadCertificateFromString(Base64.getEncoder().encodeToString(content));
        multiDomainCryptoService.addCertificate(domainContextProvider.getCurrentDomain(), Arrays.asList(new CertificateEntry(green_gw, x509Certificate)), true);

        trustStoreEntries = certificateService.getTrustStoreEntries(DOMIBUS_TRUSTSTORE_NAME);
        Assert.assertTrue(trustStoreEntries.size() == 3);
        Assert.assertTrue(trustStoreEntries.stream().anyMatch(entry -> entry.getName().equals(green_gw)));
    }

    @Test
    @Transactional
    public void getCertificateFromTruststore() throws KeyStoreException {
        multiDomainCryptoService.persistTruststoresIfApplicable();

        List<TrustStoreEntry> trustStoreEntries = certificateService.getTrustStoreEntries(DOMIBUS_TRUSTSTORE_NAME);
        Assert.assertTrue(trustStoreEntries.size() == 2);

        String blue_gw = "blue_gw";
        X509Certificate certificateFromTruststore = multiDomainCryptoService.getCertificateFromTruststore(domainContextProvider.getCurrentDomain(), blue_gw);

        trustStoreEntries = certificateService.getTrustStoreEntries(DOMIBUS_TRUSTSTORE_NAME);
        Assert.assertTrue(trustStoreEntries.size() == 2);
        Assert.assertTrue(certificateFromTruststore.getIssuerDN().getName().contains(blue_gw));
    }

    @Test
    @Transactional
    public void getTrustStore() throws KeyStoreException, IOException {
        multiDomainCryptoService.persistTruststoresIfApplicable();

        KeyStore trustStore = multiDomainCryptoService.getTrustStore(domainContextProvider.getCurrentDomain());
        Assert.assertTrue(trustStore.containsAlias("blue_gw"));

        Path path = Paths.get(domibusConfigurationService.getConfigLocation(), "keystores", "cefsupportgwtruststore.jks");
        byte[] content = Files.readAllBytes(path);
        String file_name = "cefsupportgwtruststore.jks";
        multiDomainCryptoService.replaceTrustStore(DomainService.DEFAULT_DOMAIN, file_name, content, "test123");

        trustStore = multiDomainCryptoService.getTrustStore(domainContextProvider.getCurrentDomain());
        List<TrustStoreEntry> trustStoreEntries = certificateService.getTrustStoreEntries(DOMIBUS_TRUSTSTORE_NAME);
        Assert.assertTrue(trustStore.containsAlias("ceftestparty4gw"));
    }

    @Test
    @Transactional
    public void isCertificateChainValid() {
        multiDomainCryptoService.persistTruststoresIfApplicable();

        String blue_gw = "blue_gw";
        boolean certificateChainValid = multiDomainCryptoService.isCertificateChainValid(domainContextProvider.getCurrentDomain(), blue_gw);

        Assert.assertTrue(certificateChainValid);
    }

    @Test
    @Transactional
    public void getDefaultX509Identifier() throws WSSecurityException {
        multiDomainCryptoService.persistTruststoresIfApplicable();

        String blue_gw = "blue_gw";
        String defaultX509Identifier = multiDomainCryptoService.getDefaultX509Identifier(domainContextProvider.getCurrentDomain());

        Assert.assertTrue(defaultX509Identifier.equals(blue_gw));
    }

    @Test
    @Transactional
    public void removeCertificate() throws IOException {
        multiDomainCryptoService.persistTruststoresIfApplicable();

        List<TrustStoreEntry> trustStoreEntries = certificateService.getTrustStoreEntries(DOMIBUS_TRUSTSTORE_NAME);
        Assert.assertTrue(trustStoreEntries.size() == 2);

        String red_gw = "red_gw";
        multiDomainCryptoService.removeCertificate(domainContextProvider.getCurrentDomain(), red_gw);

        trustStoreEntries = certificateService.getTrustStoreEntries(DOMIBUS_TRUSTSTORE_NAME);
        Assert.assertTrue(trustStoreEntries.size() == 1);
        Assert.assertTrue(!trustStoreEntries.stream().anyMatch(entry -> entry.getName().equals(red_gw)));
    }
}
