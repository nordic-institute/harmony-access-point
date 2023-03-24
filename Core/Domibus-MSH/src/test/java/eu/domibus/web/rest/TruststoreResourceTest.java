package eu.domibus.web.rest;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.*;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.api.util.MultiPartFileUtil;
import eu.domibus.core.audit.AuditService;
import eu.domibus.core.certificate.CertificateHelper;
import eu.domibus.core.converter.PartyCoreMapper;
import eu.domibus.core.csv.CsvServiceImpl;
import eu.domibus.web.rest.error.ErrorHandlerService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.KeyStore;
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
    PartyCoreMapper partyCoreConverter;

    @Injectable
    CsvServiceImpl csvServiceImpl;

    @Injectable
    ErrorHandlerService errorHandlerService;

    @Injectable
    MultiPartFileUtil multiPartFileUtil;

    @Injectable
    private AuditService auditService;

    @Injectable
    DomainContextProvider domainContextProvider;

    @Injectable
    DomibusConfigurationService domibusConfigurationService;

    @Injectable
    KeystorePersistenceService keystorePersistenceService;

    @Injectable
    CertificateHelper certificateHelper;

    @Test
    public void replaceTruststore(@Mocked Domain domain, @Injectable KeyStoreContentInfo storeInfo) {
        final byte[] fileContent = new byte[]{1, 0, 1};
        String filename = "filename";
        String INIT_VALUE_TRUSTSTORE = "truststore";

        new Expectations() {{
            domainProvider.getCurrentDomain();
            result = domain;
        }};

        // When
        String pass = "pass";
        truststoreResource.doUploadStore(storeInfo);

        new Verifications() {{
            multiDomainCertificateProvider.replaceTrustStore(domainProvider.getCurrentDomain(), storeInfo);
        }};
    }

    @Test
    public void getTrustStoreEntries(@Mocked List<TrustStoreEntry> trustStoreEntries) {

        new Expectations() {{
            multiDomainCertificateProvider.getTrustStoreEntries(domainProvider.getCurrentDomain());
            result = trustStoreEntries;
        }};

        List<TrustStoreEntry> res = truststoreResource.doGetStoreEntries();

        Assert.assertEquals(trustStoreEntries, res);
    }

}
