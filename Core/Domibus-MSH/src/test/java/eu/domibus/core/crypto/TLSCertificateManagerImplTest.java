package eu.domibus.core.crypto;

import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.crypto.NoKeyStoreContentInformationException;
import eu.domibus.api.cxf.TLSReaderService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.pki.KeyStoreContentInfo;
import eu.domibus.api.pki.KeystorePersistenceInfo;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.core.certificate.CertificateHelper;
import eu.domibus.core.exception.ConfigurationException;
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
    CertificateHelper certificateHelper;

    @Test
    public void replaceTrustStore(@Injectable KeystorePersistenceInfo persistenceInfo,
                                  @Injectable KeyStoreContentInfo contentInfo) {
        new Expectations() {{
            certificateService.replaceStore(contentInfo, (KeystorePersistenceInfo) any);
            result = true;
        }};

        tlsCertificateManager.replaceTrustStore(contentInfo);

        new Verifications() {{
            tlsCertificateManager.resetTLSTruststore();
        }};
    }

    @Test
    public void getTrustStoreEntries(@Injectable List<TrustStoreEntry> entries, @Injectable KeystorePersistenceInfo persistenceInfo) {
        new Expectations(tlsCertificateManager) {{
            tlsCertificateManager.getPersistenceInfo();
            result = persistenceInfo;
            certificateService.getStoreEntries(persistenceInfo);
            result = entries;
        }};

        List<TrustStoreEntry> result = tlsCertificateManager.getTrustStoreEntries();

        Assert.assertEquals(entries, result);
        new Verifications() {{
            certificateService.getStoreEntries(persistenceInfo);
        }};
    }

    @Test(expected = ConfigurationException.class)
    public void getTrustStoreEntries_throwsExceptionForMissingTrustStore(@Injectable KeystorePersistenceInfo persistenceInfo) {
        new Expectations(tlsCertificateManager) {{
            tlsCertificateManager.getPersistenceInfo();
            result = persistenceInfo;

            certificateService.getStoreEntries(persistenceInfo);
            result = new NoKeyStoreContentInformationException("");
        }};

        tlsCertificateManager.getTrustStoreEntries();
    }

    @Test
    public void addCertificate(@Injectable KeyStoreType trustStore, @Injectable byte[] certificateData, @Injectable KeystorePersistenceInfo persistenceInfo) {
        String alias = "mockalias";

        new Expectations(tlsCertificateManager) {{
            tlsCertificateManager.getPersistenceInfo();
            result = persistenceInfo;
            certificateService.addCertificate(persistenceInfo, certificateData, alias, true);
            result = true;
        }};

        boolean result = tlsCertificateManager.addCertificate(certificateData, alias);

        Assert.assertTrue(result);
        new Verifications() {{
            certificateService.addCertificate(persistenceInfo, certificateData, alias, true);
            tlsCertificateManager.resetTLSTruststore();
        }};
    }

    @Test
    public void removeCertificate(@Injectable KeyStoreType trustStore, @Injectable KeystorePersistenceInfo persistenceInfo) {
        String alias = "mockalias";

        new Expectations(tlsCertificateManager) {{
            tlsCertificateManager.getPersistenceInfo();
            result = persistenceInfo;
            certificateService.removeCertificate(persistenceInfo, alias);
            result = true;
        }};

        boolean result = tlsCertificateManager.removeCertificate(alias);

        Assert.assertTrue(result);
        new Verifications() {{
            certificateService.removeCertificate(persistenceInfo, alias);
            tlsCertificateManager.resetTLSTruststore();
        }};
    }

    @Test
    public void getTruststoreParams(@Injectable TLSClientParametersType params, @Injectable KeyStoreType trustStore, @Injectable Domain domain) {
        new Expectations(tlsCertificateManager) {{
            domibusConfigurationService.isSingleTenantAware();
            result = false;
            domainProvider.getCurrentDomain();
            result = domain;
            tlsReaderService.getTlsTrustStoreConfiguration(domain.getCode());
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
