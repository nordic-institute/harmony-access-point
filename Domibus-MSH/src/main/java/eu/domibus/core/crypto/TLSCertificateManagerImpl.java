package eu.domibus.core.crypto;

import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.cxf.TLSReaderService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.core.crypto.api.TLSCertificateManager;
import org.apache.cxf.configuration.security.KeyStoreType;
import org.apache.cxf.configuration.security.TLSClientParametersType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
public class TLSCertificateManagerImpl implements TLSCertificateManager {

    private final TLSReaderService tlsReaderService;

    private final CertificateService certificateService;

    private final DomainContextProvider domainProvider;

    private final SignalService signalService;

    public TLSCertificateManagerImpl(TLSReaderService tlsReaderService, CertificateService certificateService,
                                     DomainContextProvider domainProvider, SignalService signalService) {

        this.tlsReaderService = tlsReaderService;
        this.certificateService = certificateService;
        this.domainProvider = domainProvider;
        this.signalService = signalService;
    }

    @Override
    public void replaceTrustStore(String fileName, byte[] fileContent, String filePassword) throws CryptoException {
        KeyStoreType trustStore = getTruststoreParams();

        certificateService.replaceTrustStore(fileName, fileContent, filePassword,
                trustStore.getType(), trustStore.getFile(), trustStore.getPassword());

        resetTLSTruststore();
    }

    @Override
    public List<TrustStoreEntry> getTrustStoreEntries() {
        KeyStoreType trustStore = getTruststoreParams();

        return certificateService.getTrustStoreEntries(trustStore.getFile(), trustStore.getPassword());
    }

    @Override
    public byte[] getTruststoreContent() {
        KeyStoreType trustStore = getTruststoreParams();
        return certificateService.getTruststoreContent(trustStore.getFile());
    }

    @Override
    public boolean addCertificate(byte[] certificateData, String alias) {
        KeyStoreType trustStore = getTruststoreParams();
        boolean result = certificateService.addCertificate(trustStore.getPassword(), trustStore.getFile(), certificateData, alias, true);

        resetTLSTruststore();
        return result;
    }

    @Override
    public boolean removeCertificate(String alias) {
        KeyStoreType trustStore = getTruststoreParams();
        boolean result = certificateService.removeCertificate(trustStore.getPassword(), trustStore.getFile(), alias, true);

        resetTLSTruststore();
        return result;
    }

    protected KeyStoreType getTruststoreParams() {
        Domain domain = domainProvider.getCurrentDomain();
        String domainCode = domain != null ? domain.getCode() : null;
        TLSClientParametersType params = tlsReaderService.getTlsClientParametersType(domainCode);
        return params.getTrustManagers().getKeyStore();
    }

    protected void resetTLSTruststore() {
        Domain domain = domainProvider.getCurrentDomain();
        String domainCode = domain != null ? domain.getCode() : null;
        tlsReaderService.reset(domainCode);
        signalService.signalTLSTrustStoreUpdate(domain);
    }
}
