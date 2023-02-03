package eu.domibus.rest;

import eu.domibus.AbstractIT;
import eu.domibus.api.exceptions.RequestValidationException;
import eu.domibus.api.pki.DomibusCertificateException;
import eu.domibus.api.util.MultiPartFileUtil;
import eu.domibus.core.exception.ConfigurationException;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static eu.domibus.core.crypto.TLSCertificateManagerImpl.TLS_TRUSTSTORE_NAME;

/**
 * @author Soumya
 * @author Ion Perpegel
 * @since 5.0
 */
public class TLSTruststoreResourceIT extends AbstractIT {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TLSTruststoreResourceIT.class);

    @Autowired
    private TLSTruststoreResource tlsTruststoreResource;

    @Autowired
    protected MultiPartFileUtil multiPartFileUtil;

    @Before
    public void before() {
        removeStore(TLS_TRUSTSTORE_NAME);
    }

    @After
    public void after() {
        removeStore(TLS_TRUSTSTORE_NAME);
    }

    @Test
    public void testTruststoreEntries_ok() throws IOException {
        createTrustStore();

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
        } catch (DomibusCertificateException ex) {
            Assert.assertTrue(ex.getMessage().contains("Could not load store"));
        }
    }

    @Test
    public void replaceExisting() throws IOException {
        createTrustStore();

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
        try {
            tlsTruststoreResource.getTLSTruststoreEntries();
            Assert.fail();
        } catch (Exception ex) {
            LOG.trace("expected error on tlsTruststoreResource.getTLSTruststoreEntries()", ex);
        }

        try (InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("keystores/gateway_truststore.jks")) {
            MultipartFile multiPartFile = new MockMultipartFile("gateway_truststore.jks", "gateway_truststore.jks",
                    "octetstream", IOUtils.toByteArray(resourceAsStream));
            tlsTruststoreResource.uploadTLSTruststoreFile(multiPartFile, "test123");

            List<TrustStoreRO> newEntries = tlsTruststoreResource.getTLSTruststoreEntries();

            Assert.assertEquals( 2, newEntries.size());
        }
    }


    @Test(expected = ConfigurationException.class)
    public void downloadTrust() {
        tlsTruststoreResource.downloadTLSTrustStore();
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

    private void createTrustStore() throws IOException {
        createStore(TLS_TRUSTSTORE_NAME, "keystores/gateway_truststore.jks");
    }
}
