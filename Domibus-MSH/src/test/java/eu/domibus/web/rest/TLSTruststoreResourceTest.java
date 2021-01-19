package eu.domibus.web.rest;

import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.api.util.MultiPartFileUtil;
import eu.domibus.core.audit.AuditService;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.crypto.api.TLSCertificateManager;
import eu.domibus.core.csv.CsvServiceImpl;
import eu.domibus.web.rest.error.ErrorHandlerService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@RunWith(JMockit.class)
public class TLSTruststoreResourceTest {

    @Tested
    TLSTruststoreResource truststoreResource;

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
        truststoreResource.doReplaceTrustStore(fileContent, filename, pass);

        new Verifications() {{
            tlsCertificateManager.replaceTrustStore(filename, fileContent, pass);
        }};
    }

    @Test
    public void getTrustStoreEntries(@Mocked List<TrustStoreEntry> trustStoreEntries) {

        new Expectations() {{
        }};

        List<TrustStoreEntry> res = truststoreResource.doGetTrustStoreEntries();

        Assert.assertEquals(trustStoreEntries, res);
    }

}
