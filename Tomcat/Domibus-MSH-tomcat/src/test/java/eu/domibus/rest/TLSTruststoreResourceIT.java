package eu.domibus.rest;

import eu.domibus.AbstractIT;
import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.exceptions.RequestValidationException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.pki.DomibusCertificateException;
import eu.domibus.api.pki.KeyStoreContentInfo;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.util.MultiPartFileUtil;
import eu.domibus.core.certificate.CertificateDaoImpl;
import eu.domibus.core.certificate.CertificateHelper;
import eu.domibus.core.crypto.TLSCertificateManagerImpl;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.TLSTruststoreResource;
import eu.domibus.web.rest.ro.TrustStoreRO;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static eu.domibus.core.crypto.MultiDomainCryptoServiceImpl.DOMIBUS_TRUSTSTORE_NAME;
import static eu.domibus.core.crypto.TLSCertificateManagerImpl.TLS_TRUSTSTORE_NAME;

/**
 * @author Soumya
 * @author Ion Perpegel
 * @since 5.0
 */
public class TLSTruststoreResourceIT extends AbstractIT {
    public static final String KEYSTORES = "keystores";

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TLSTruststoreResourceIT.class);

    @Autowired
    private TLSTruststoreResource tlsTruststoreResource;

    @Autowired
    protected MultiPartFileUtil multiPartFileUtil;

    @Autowired
    DomibusConfigurationService domibusConfigurationService;

    @Autowired
    CertificateHelper certificateHelper;

    @Autowired
    TLSCertificateManagerImpl tlsCertificateManager;

    @Before
    public void before() {
        resetInitalTruststore();
    }

    @Test
    public void testTruststoreEntries_ok() throws IOException {
        List<TrustStoreRO> trustStoreROS = tlsTruststoreResource.getTLSTruststoreEntries();

        for (TrustStoreRO trustStoreRO : trustStoreROS) {
            Assert.assertNotNull("Certificate name should be populated in TrustStoreRO:", trustStoreRO.getName());
            Assert.assertNotNull("Certificate subject should be populated in TrustStoreRO:", trustStoreRO.getSubject());
            Assert.assertNotNull("Certificate issuer should be populated in TrustStoreRO:", trustStoreRO.getIssuer());
            Assert.assertNotNull("Certificate validity from should be populated in TrustStoreRO:", trustStoreRO.getValidFrom());
            Assert.assertNotNull("Certificate validity until should be populated in TrustStoreRO:", trustStoreRO.getValidUntil());
            Assert.assertNotNull("Certificate fingerprints should be populated in TrustStoreRO:", trustStoreRO.getFingerprints());
            Assert.assertNotNull("Certificate imminent expiry alert days should be populated in TrustStoreRO:", trustStoreRO.getCertificateExpiryAlertDays());
            Assert.assertEquals("Certificate imminent expiry alert days should be populated in TrustStoreRO:", 60, trustStoreRO.getCertificateExpiryAlertDays());
        }
    }

    @Test
    public void replaceTrust_EmptyPass() {
        byte[] content = {1, 0, 1};
        String filename = "file";
        MockMultipartFile truststoreFile = new MockMultipartFile("file", filename, "octetstream", content);
        try {
            tlsTruststoreResource.uploadTLSTruststoreFile(truststoreFile, "");
            Assert.fail();
        } catch (RequestValidationException ex) {
            Assert.assertEquals("[DOM_001]:Failed to upload the truststoreFile file since its password was empty.", ex.getMessage());
        }
    }

    @Test
    public void replaceTrust_NotValid() {
        byte[] content = {1, 0, 1};
        String filename = "file.jks";
        MockMultipartFile truststoreFile = new MockMultipartFile("file", filename, "octetstream", content);
        try {
            tlsTruststoreResource.uploadTLSTruststoreFile(truststoreFile, "test123");
            Assert.fail();
        } catch (CryptoException ex) {
            Assert.assertTrue(ex.getMessage().contains("[DOM_001]:Error while replacing the store [TLS.truststore] with content of the file named [file.jks]."));
        }
    }

    @Test
    public void replaceExisting() throws IOException {
        List<TrustStoreRO> entries = tlsTruststoreResource.getTLSTruststoreEntries();

        try (InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("keystores/gateway_truststore2.jks")) {
            MultipartFile multiPartFile = new MockMultipartFile("gateway_truststore2.jks", "gateway_truststore2.jks",
                    "octetstream", IOUtils.toByteArray(resourceAsStream));
            tlsTruststoreResource.uploadTLSTruststoreFile(multiPartFile, "test123");

            List<TrustStoreRO> newEntries = tlsTruststoreResource.getTLSTruststoreEntries();

            Assert.assertNotEquals(entries.size(), newEntries.size());
        }
    }

    @Test
    public void setAnew() throws IOException {
        try (InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("keystores/gateway_truststore.jks")) {
            MultipartFile multiPartFile = new MockMultipartFile("gateway_truststore.jks", "gateway_truststore.jks",
                    "octetstream", IOUtils.toByteArray(resourceAsStream));
            try {
                tlsTruststoreResource.uploadTLSTruststoreFile(multiPartFile, "test123");
                Assert.fail();
            } catch (CryptoException ex) {
                Assert.assertTrue(ex.getMessage().contains("[DOM_001]:Current store [TLS.truststore] was not replaced with the content of the file [gateway_truststore.jks] because they are identical."));
            }
        }
    }


    @Test()
    public void downloadTrust() {
        ResponseEntity<ByteArrayResource> res = tlsTruststoreResource.downloadTLSTrustStore();
        Assert.assertEquals(HttpStatus.OK, res.getStatusCode());
    }

    @Test(expected = DomibusCertificateException.class)
    public void addTLSCertificate() {
        byte[] content = {1, 0, 1};
        String filename = "file";
        MockMultipartFile truststoreFile = new MockMultipartFile("file", filename, "octetstream", content);
        tlsTruststoreResource.addTLSCertificate(truststoreFile, "tlscert");
    }

    @Test(expected = DomibusCertificateException.class)
    public void removeTLSCertificate() {
        tlsTruststoreResource.removeTLSCertificate("tlscert");
    }

    private void resetInitalTruststore() {
        try {
            String storePassword = "test123";
            Domain domain = DomainService.DEFAULT_DOMAIN;
            Path path = Paths.get(domibusConfigurationService.getConfigLocation(), KEYSTORES, "gateway_truststore_original.jks");
            byte[] content = Files.readAllBytes(path);
            KeyStoreContentInfo storeInfo = certificateHelper.createStoreContentInfo(DOMIBUS_TRUSTSTORE_NAME, "gateway_truststore.jks", content, storePassword);
            tlsCertificateManager.replaceTrustStore(storeInfo);
        } catch (Exception e) {
            LOG.info("Error restoring initial keystore", e);
        }
    }
}
