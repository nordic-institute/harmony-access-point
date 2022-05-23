package eu.domibus.core.crypto;

import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.cxf.TLSReaderService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.core.audit.AuditService;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.apache.cxf.configuration.security.KeyStoreType;
import org.apache.cxf.configuration.security.TLSClientParametersType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Optional;

import static eu.domibus.core.crypto.TLSCertificateManagerImpl.TLS_TRUSTSTORE_NAME;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@RunWith(JMockit.class)
public class TLSCertificateManagerImplTest {

    @Tested
    TLSCertificateManagerImpl tlsCertificateManager;

    @Injectable
    private TLSReaderService tlsReaderService;

    @Injectable
    private CertificateService certificateService;

    @Injectable
    private DomainContextProvider domainProvider;

    @Injectable
    private SignalService signalService;

    @Injectable
    private DomibusConfigurationService domibusConfigurationService;

    @Injectable
    DomainService domainService;

    @Injectable
    private AuditService auditService;

    @Test
    public void replaceTrustStore(@Injectable KeyStoreType trustStore, @Injectable String fileName, @Injectable byte[] fileContent, @Injectable String filePassword, @Injectable String backupLocation) {
        tlsCertificateManager.replaceTrustStore(fileName, fileContent, filePassword);

        new Verifications() {{
            certificateService.replaceStore(fileName, fileContent, filePassword, TLS_TRUSTSTORE_NAME);
            tlsCertificateManager.resetTLSTruststore();
            auditService.addTLSTruststoreUploadedAudit();
        }};
    }

    @Test
    public void getTrustStoreEntries(@Injectable KeyStoreType trustStore, @Injectable List<TrustStoreEntry> entries) {
        new Expectations(tlsCertificateManager) {{
            certificateService.getTrustStoreEntries(TLS_TRUSTSTORE_NAME);
            result = entries;
        }};

        List<TrustStoreEntry> result = tlsCertificateManager.getTrustStoreEntries();

        Assert.assertEquals(entries, result);
        new Verifications() {{
            certificateService.getTrustStoreEntries(TLS_TRUSTSTORE_NAME);
        }};
    }

    @Test
    public void addCertificate(@Injectable KeyStoreType trustStore, @Injectable byte[] certificateData) {
        String alias = "mockalias";

        new Expectations(tlsCertificateManager) {{
            certificateService.addCertificate(TLS_TRUSTSTORE_NAME, certificateData, alias, true);
            result = 1L;
        }};

        boolean result = tlsCertificateManager.addCertificate(certificateData, alias);

        Assert.assertTrue(result);
        new Verifications() {{
            certificateService.addCertificate(TLS_TRUSTSTORE_NAME, certificateData, alias, true);
            tlsCertificateManager.resetTLSTruststore();
            auditService.addCertificateAddedAudit("1");
        }};
    }

    @Test
    public void removeCertificate(@Injectable KeyStoreType trustStore) {
        String alias = "mockalias";

        new Expectations(tlsCertificateManager) {{
            certificateService.removeCertificate(TLS_TRUSTSTORE_NAME, alias);
            result = 1L;
        }};

        boolean result = tlsCertificateManager.removeCertificate(alias);

        Assert.assertTrue(result);
        new Verifications() {{
            certificateService.removeCertificate(TLS_TRUSTSTORE_NAME, alias);
            tlsCertificateManager.resetTLSTruststore();
            auditService.addCertificateRemovedAudit("1");
        }};
    }

    @Test
    public void getTruststoreParams(@Injectable TLSClientParametersType params, @Injectable KeyStoreType trustStore, @Injectable Domain domain) {
        new Expectations(tlsCertificateManager) {{
            domibusConfigurationService.isSingleTenantAware();
            result = false;
            domainProvider.getCurrentDomain();
            result = domain;
            tlsReaderService.getTlsClientParametersType(domain.getCode());
            result = Optional.of(params);
            params.getTrustManagers().getKeyStore();
            result = trustStore;
        }};

        Optional<KeyStoreType> result = tlsCertificateManager.getTruststoreParams();

        Assert.assertEquals(trustStore, result.get());
    }

    @Test
    public void resetTLSTruststore(@Injectable Domain domain) {
        new Expectations(tlsCertificateManager) {{
            domainProvider.getCurrentDomain();
            result = domain;
        }};

        tlsCertificateManager.resetTLSTruststore();

        new Verifications() {{
            tlsReaderService.reset(domain.getCode());
            signalService.signalTLSTrustStoreUpdate(domain);
        }};
    }
}
