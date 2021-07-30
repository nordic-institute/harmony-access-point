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
import eu.domibus.core.ebms3.sender.client.TLSReaderServiceImpl;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.web.rest.TLSTruststoreResource;
import eu.domibus.web.rest.error.ErrorHandlerService;
import eu.domibus.web.rest.ro.TrustStoreRO;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Soumya
 * @since 5.0
 */
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

    @Test(expected = DomibusCertificateException.class)
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

            Assert.assertEquals(ex.getMessage(), "[DOM_001]:Failed to upload the truststoreFile file since its password was empty.");
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
            Assert.assertEquals(ex.getMessage(), "Exception loading truststore.");
        }
    }

    @Test(expected = DomibusCertificateException.class)
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


    @Test
    public void getEntries_ok() throws IOException {
        TrustStoreRO trustStore1 = new TrustStoreRO();
        trustStore1.setName("blue_gw");
        TrustStoreRO trustStore2 = new TrustStoreRO();
        trustStore2.setName("red_gw");
        List<TrustStoreRO> entriesRO = new ArrayList<>();
        entriesRO.add(trustStore1);
        entriesRO.add(trustStore2);
        //Overriding the config location in AbstractIT
        System.setProperty("domibus.config.location", new File("src/test/resources").getAbsolutePath());

        String fileName = "default_clientauthentication.xml";

        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
        final File destAuthenticationFile = new File(getDomibusConfigLocation(), fileName);
        FileUtils.copyInputStreamToFile(inputStream, destAuthenticationFile);
        entriesRO = tlsTruststoreResource.getTLSTruststoreEntries();

        Assert.assertEquals(entriesRO.size(), 2);
        Assert.assertEquals(entriesRO.get(0).getName(), "blue_gw");
        FileUtils.forceDelete(new File(getDomibusConfigLocation(), fileName));
    }

}