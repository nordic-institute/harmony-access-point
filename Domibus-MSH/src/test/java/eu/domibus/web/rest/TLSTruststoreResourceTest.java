package eu.domibus.web.rest;

import eu.domibus.api.exceptions.RequestValidationException;
import eu.domibus.api.util.MultiPartFileUtil;
import eu.domibus.core.audit.AuditService;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.crypto.api.TLSCertificateManager;
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
    DomainCoreConverter domainConverter;

    @Injectable
    CsvServiceImpl csvServiceImpl;

    @Injectable
    ErrorHandlerService errorHandlerService;

    @Injectable
    MultiPartFileUtil multiPartFileUtil;

    @Injectable
    private AuditService auditService;

    @Test
    public void replaceTruststore() {
        final byte[] fileContent = new byte[]{1, 0, 1};
        String filename = "filename";

        new Expectations() {{
        }};

        // When
        String pass = "pass";
        tlsTruststoreResource.doReplaceTrustStore(fileContent, filename, pass);

        new Verifications() {{
            tlsCertificateManager.replaceTrustStore(filename, fileContent, pass, anyString);
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
            tlsCertificateManager.addCertificate(content, alias, anyString);
        }};

        String outcome = tlsTruststoreResource.addTLSCertificate(multiPartFile, alias);

        Assert.assertTrue(outcome.contains("Certificate [" + alias + "] has been successfully added to the TLS truststore."));

        new Verifications() {{
            tlsCertificateManager.addCertificate(content, alias, anyString);
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
