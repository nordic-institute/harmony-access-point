package eu.domibus.web.rest;

import eu.domibus.api.crypto.TLSCertificateManager;
import eu.domibus.api.exceptions.RequestValidationException;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.KeyStoreContentInfo;
import eu.domibus.api.pki.KeystorePersistenceService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.MultiPartFileUtil;
import eu.domibus.core.audit.AuditService;
import eu.domibus.core.certificate.CertificateHelper;
import eu.domibus.core.converter.PartyCoreMapper;
import eu.domibus.core.csv.CsvServiceImpl;
import eu.domibus.web.rest.error.ErrorHandlerService;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@RunWith(JMockit.class)
public class TLSTruststoreResourceTest {

    @Tested
    TLSTruststoreResource tlsTruststoreResource;

    @Injectable
    TLSCertificateManager tlsCertificateManager;

    @Injectable
    PartyCoreMapper coreMapper;

    @Injectable
    CsvServiceImpl csvServiceImpl;

    @Injectable
    ErrorHandlerService errorHandlerService;

    @Injectable
    MultiPartFileUtil multiPartFileUtil;

    @Injectable
    private AuditService auditService;

    @Injectable
    DomainContextProvider domainProvider;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    DomainContextProvider domainContextProvider;

    @Injectable
    DomibusConfigurationService domibusConfigurationService;

    @Injectable
    CertificateHelper certificateHelper;

    @Injectable
    KeystorePersistenceService keystorePersistenceService;

    @Test
    public void replaceTruststore(@Injectable KeyStoreContentInfo storeInfo) {

        // When
        tlsTruststoreResource.doUploadStore(storeInfo);

        new Verifications() {{
            tlsCertificateManager.replaceTrustStore(storeInfo);
        }};
    }

    @Test
    public void addTLSCertificateOK() {
        byte[] content = {1, 0, 1};
        String filename = "filename", alias = "blue_gw";
        MultipartFile multiPartFile = new MockMultipartFile("name", filename, "octetstream", content);
        new Expectations() {{
            multiPartFileUtil.validateAndGetFileContent(multiPartFile);
            result = content;
            tlsCertificateManager.addCertificate(content, alias);
            result = true;
        }};

        String outcome = tlsTruststoreResource.addTLSCertificate(multiPartFile, alias);

        Assert.assertTrue(outcome.contains("Certificate [" + alias + "] has been successfully added to the [" + tlsTruststoreResource.getStoreName() + "]."));

        new Verifications() {{
            tlsCertificateManager.addCertificate(content, alias);
        }};
    }

    @Test
    public void addTLSCertificaEmpty() {
        MultipartFile multiPartFile = new MockMultipartFile("cert", new byte[]{});

        try {
            tlsTruststoreResource.addTLSCertificate(multiPartFile, "");
            Assert.fail();
        } catch (RequestValidationException ex) {
            Assert.assertTrue(ex.getMessage().contains("Please provide an alias for the certificate."));
        }
    }
}
