package eu.domibus.core.crypto;

import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.cxf.TLSReaderService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.security.TrustStoreEntry;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.cxf.configuration.security.KeyStoreType;
import org.apache.cxf.configuration.security.TLSClientParametersType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

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

    @Test
    public void replaceTrustStore(@Mocked KeyStoreType trustStore, @Mocked String fileName, @Mocked byte[] fileContent, @Mocked String filePassword) {
        new Expectations(tlsCertificateManager) {{
            tlsCertificateManager.getTruststoreParams();
            result = trustStore;
        }};

        tlsCertificateManager.replaceTrustStore(fileName, fileContent, filePassword);

        new Verifications() {{
            certificateService.replaceTrustStore(fileName, fileContent, filePassword,
                    trustStore.getType(), trustStore.getFile(), trustStore.getPassword());
            tlsCertificateManager.resetTLSTruststore();
        }};
    }

    @Test
    public void getTrustStoreEntries(@Mocked KeyStoreType trustStore, @Mocked List<TrustStoreEntry> entries) {
        new Expectations(tlsCertificateManager) {{
            tlsCertificateManager.getTruststoreParams();
            result = trustStore;
            certificateService.getTrustStoreEntries(trustStore.getFile(), trustStore.getPassword());
            result = entries;
        }};

        List<TrustStoreEntry> result = tlsCertificateManager.getTrustStoreEntries();

        Assert.assertEquals(entries, result);
        new Verifications() {{
            certificateService.getTrustStoreEntries(trustStore.getFile(), trustStore.getPassword());
        }};
    }

    @Test
    public void getTruststoreContent(@Mocked KeyStoreType trustStore, @Mocked byte[] content) {
        new Expectations(tlsCertificateManager) {{
            tlsCertificateManager.getTruststoreParams();
            result = trustStore;
            certificateService.getTruststoreContent(trustStore.getFile());
            result = content;
        }};

        byte[] result = tlsCertificateManager.getTruststoreContent();

        Assert.assertEquals(content, result);
        new Verifications() {{
            certificateService.getTruststoreContent(trustStore.getFile());
        }};
    }

    @Test
    public void addCertificate(@Mocked KeyStoreType trustStore, @Mocked byte[] certificateData, @Mocked String alias) {
        new Expectations(tlsCertificateManager) {{
            tlsCertificateManager.getTruststoreParams();
            result = trustStore;
            certificateService.addCertificate(trustStore.getPassword(), trustStore.getFile(), certificateData, alias, true);
            result = true;
        }};

        boolean result = tlsCertificateManager.addCertificate(certificateData, alias);

        Assert.assertTrue(result);
        new Verifications() {{
            certificateService.addCertificate(trustStore.getPassword(), trustStore.getFile(), certificateData, alias, true);
            tlsCertificateManager.resetTLSTruststore();
        }};
    }

    @Test
    public void removeCertificate(@Mocked KeyStoreType trustStore, @Mocked String alias) {
        new Expectations(tlsCertificateManager) {{
            tlsCertificateManager.getTruststoreParams();
            result = trustStore;
            certificateService.removeCertificate(trustStore.getPassword(), trustStore.getFile(), alias, true);
            result = true;
        }};

        boolean result = tlsCertificateManager.removeCertificate(alias);

        Assert.assertTrue(result);
        new Verifications() {{
            certificateService.removeCertificate(trustStore.getPassword(), trustStore.getFile(), alias, true);
            tlsCertificateManager.resetTLSTruststore();
        }};
    }

    @Test
    public void getTruststoreParams(@Mocked TLSClientParametersType params, @Mocked KeyStoreType trustStore, @Mocked Domain domain) {
        new Expectations(tlsCertificateManager) {{
            domainProvider.getCurrentDomain();
            result = domain;
            tlsReaderService.getTlsClientParametersType(domain.getCode());
            result = params;
            params.getTrustManagers().getKeyStore();
            result = trustStore;
        }};

        KeyStoreType result = tlsCertificateManager.getTruststoreParams();

        Assert.assertEquals(trustStore, result);
    }

    @Test
    public void resetTLSTruststore(@Mocked Domain domain) {
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