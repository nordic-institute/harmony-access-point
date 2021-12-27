package eu.domibus.core;

import eu.domibus.AbstractIT;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.pki.CertificateEntry;
import eu.domibus.api.pki.CertificateInitValueType;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.core.crypto.DefaultDomainCryptoServiceSpiImpl;
import eu.domibus.core.crypto.DomainCryptoServiceFactory;
import eu.domibus.core.crypto.MultiDomainCryptoServiceImpl;
import eu.domibus.core.crypto.TruststoreDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.wss4j.common.ext.WSSecurityException;
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
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import static eu.domibus.core.crypto.MultiDomainCryptoServiceImpl.DOMIBUS_TRUSTSTORE_NAME;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
public class MultiDomainCryptoServiceIT extends AbstractIT {

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(MultiDomainCryptoServiceIT.class);

    private final String DOMIBUS_TRUSTSTORE_FILE_NAME = "domibus.truststore.jks";

    @Autowired
    private MultiDomainCryptoServiceImpl multiDomainCryptoService;

    @Autowired
    TruststoreDao truststoreDao;

    @Autowired
    DomainCryptoServiceFactory domainCertificateProviderFactory1;

    @Autowired
    DefaultDomainCryptoServiceSpiImpl defaultDomainCryptoServiceSpi;

    @Autowired
    CertificateService certificateService;

    @Autowired
    DomibusConfigurationService domibusConfigurationService;

    @Test
    @Transactional
    public void persistTruststoresIfApplicable() {
        boolean isPersisted = truststoreDao.existsWithName(DOMIBUS_TRUSTSTORE_FILE_NAME);
        Assert.assertFalse(isPersisted);
        multiDomainCryptoService.persistTruststoresIfApplicable();
        isPersisted = truststoreDao.existsWithName(DOMIBUS_TRUSTSTORE_NAME);
        Assert.assertTrue(isPersisted);

//        truststoreDao.delete(truststoreDao.findByName(DOMIBUS_TRUSTSTORE_FILE_NAME));
    }

    @Test
    @Transactional
    public void replaceTrustStore() {
        Domain domain = DomainService.DEFAULT_DOMAIN;
        String password = "test123";
        multiDomainCryptoService.persistTruststoresIfApplicable();
        byte[] store = certificateService.getTruststoreContent(DOMIBUS_TRUSTSTORE_NAME);

        multiDomainCryptoService.replaceTrustStore(domain, DOMIBUS_TRUSTSTORE_FILE_NAME, store, password, Arrays.asList(CertificateInitValueType.TRUSTSTORE));
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
        multiDomainCryptoService.replaceTrustStore(DomainService.DEFAULT_DOMAIN, file_name, content, "test123", Arrays.asList(CertificateInitValueType.TRUSTSTORE));

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
    public void getTrustStore() throws KeyStoreException, CertificateParsingException, IOException {
        multiDomainCryptoService.persistTruststoresIfApplicable();

        KeyStore trustStore = multiDomainCryptoService.getTrustStore(domainContextProvider.getCurrentDomain());
        Assert.assertTrue(trustStore.containsAlias("blue_gw"));

        Path path = Paths.get(domibusConfigurationService.getConfigLocation(), "keystores", "cefsupportgwtruststore.jks");
        byte[] content = Files.readAllBytes(path);
        String file_name = "cefsupportgwtruststore.jks";
        multiDomainCryptoService.replaceTrustStore(DomainService.DEFAULT_DOMAIN, file_name, content, "test123", Arrays.asList(CertificateInitValueType.TRUSTSTORE));

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
