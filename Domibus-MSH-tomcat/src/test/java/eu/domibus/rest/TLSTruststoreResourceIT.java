package eu.domibus.rest;

import eu.domibus.AbstractIT;
import eu.domibus.api.exceptions.RequestValidationException;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.DomibusCertificateException;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.MultiPartFileUtil;
import eu.domibus.core.audit.AuditService;
import eu.domibus.core.converter.PartyCoreMapper;
import eu.domibus.core.crypto.TruststoreDao;
import eu.domibus.core.crypto.TruststoreEntity;
import eu.domibus.core.ebms3.sender.client.TLSReaderServiceImpl;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.web.rest.TLSTruststoreResource;
import eu.domibus.web.rest.error.ErrorHandlerService;
import eu.domibus.web.rest.ro.TrustStoreRO;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static eu.domibus.core.crypto.TLSCertificateManagerImpl.TLS_TRUSTSTORE_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Soumya
 * @since 5.0
 */
@Transactional
public class TLSTruststoreResourceIT extends AbstractIT {

    @Autowired
    private TLSTruststoreResource tlsTruststoreResource;

    @Autowired
    protected PartyCoreMapper partyCoreConverter;

    @Autowired
    protected ErrorHandlerService errorHandlerService;

    @Autowired
    protected MultiPartFileUtil multiPartFileUtil;

    @Autowired
    protected AuditService auditService;

    @Autowired
    protected DomainContextProvider domainProvider;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    PartyCoreMapper coreMapper;

    @Autowired
    TLSReaderServiceImpl tlsReaderService;

    @Autowired
    DomibusConfigurationService domibusConfigurationService;

    @Autowired
    private TruststoreDao truststoreDao;

    @Before
    public void clean() {
        final List<TruststoreEntity> all = truststoreDao.findAll();
        truststoreDao.deleteAll(all);
        em.flush();
    }

    @Test(expected = ConfigurationException.class)
    public void getEntries() {
        tlsTruststoreResource.getTLSTruststoreEntries();
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
            assertEquals(ex.getMessage(), "[DOM_001]:Failed to upload the truststoreFile file since its password was empty.");
        }
    }

    @Test
    public void replaceTrust_WithPass() {
        byte[] content = {1, 0, 1};
        String filename = "file.jks";
        MockMultipartFile truststoreFile = new MockMultipartFile("file", filename, "octetstream", content);
        try {
            tlsTruststoreResource.uploadTLSTruststoreFile(truststoreFile, "test123");
            Assert.fail();
        } catch (ConfigurationException ex) {
            assertTrue(ex.getMessage().contains("Could not retrieve truststore"));
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

    @Test(expected = ConfigurationException.class)
    public void removeTLSCertificate() {
        tlsTruststoreResource.removeTLSCertificate("tlscert");
    }


    @Test
    public void testUploadTLSTruststore() throws IOException {
        uploadTLSTruststore();
        final List<TrustStoreRO> entriesRO = tlsTruststoreResource.getTLSTruststoreEntries();
        assertEquals(entriesRO.size(), 2);
    }

    private void uploadTLSTruststore() throws IOException {
        byte[] trustStoreBytes = IOUtils.toByteArray(this.getClass().getClassLoader().getResourceAsStream("keystores/gateway_truststore.jks"));
        final String originalFilename = "tlstruststore.jks";
        MultipartFile tlsTruststore = new MockMultipartFile(originalFilename, originalFilename, "application/octet-stream", trustStoreBytes);

        createTLSTruststore(trustStoreBytes);

        tlsTruststoreResource.uploadTLSTruststoreFile(tlsTruststore, "test123");
    }

    private void createTLSTruststore(byte[] trustStoreBytes) {
        TruststoreEntity domibusTruststoreEntity = new TruststoreEntity();
        domibusTruststoreEntity.setName(TLS_TRUSTSTORE_NAME);
        domibusTruststoreEntity.setType("JKS");
        domibusTruststoreEntity.setPassword("test123");
        domibusTruststoreEntity.setContent(trustStoreBytes);
        truststoreDao.create(domibusTruststoreEntity);
    }

}