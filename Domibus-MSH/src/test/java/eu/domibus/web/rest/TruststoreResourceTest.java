package eu.domibus.web.rest;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.pki.MultiDomainCryptoService;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.api.util.MultiPartFileUtil;
import eu.domibus.core.audit.AuditService;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.crypto.MultiDomainCryptoServiceImpl;
import eu.domibus.core.csv.CsvServiceImpl;
import eu.domibus.web.rest.error.ErrorHandlerService;
import eu.domibus.web.rest.ro.TrustStoreRO;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
@RunWith(JMockit.class)
public class TruststoreResourceTest {

    @Tested
    TruststoreResource truststoreResource;

    @Injectable
    MultiDomainCryptoService multiDomainCertificateProvider;

    @Injectable
    protected DomainContextProvider domainProvider;

    @Injectable
    CertificateService certificateService;

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
    public void replaceTruststore(@Mocked Domain domain, @Mocked KeyStore trustStore, @Mocked KeyStore keyStore) {
        final byte[] fileContent = new byte[]{1, 0, 1};
        String filename = "filename";

        new Expectations() {{
            domainProvider.getCurrentDomain();
            result = domain;
            multiDomainCertificateProvider.getTrustStore(domain);
            result = trustStore;
            multiDomainCertificateProvider.getKeyStore(domain);
            result = keyStore;
        }};

        // When
        String pass = "pass";
        truststoreResource.doReplaceTrustStore(fileContent, filename, pass);

        new Verifications() {{
            multiDomainCertificateProvider.replaceTrustStore(domainProvider.getCurrentDomain(), filename, fileContent, pass);
            certificateService.saveCertificateAndLogRevocation(trustStore, keyStore);
        }};
    }

    @Test
    public void getTrustStoreEntries(@Mocked Domain domain, @Mocked KeyStore store, @Mocked List<TrustStoreEntry> trustStoreEntries) {

        new Expectations() {{
            domainProvider.getCurrentDomain();
            result = domain;
            multiDomainCertificateProvider.getTrustStore(domain);
            result = store;
            certificateService.getTrustStoreEntries(store);
            result = trustStoreEntries;
        }};

        List<TrustStoreEntry> res = truststoreResource.doGetTrustStoreEntries();

        Assert.assertEquals(trustStoreEntries, res);
    }

}
