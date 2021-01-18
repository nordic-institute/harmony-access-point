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

@Service
public class TLSCertificateManagerImpl implements TLSCertificateManager {

    @Autowired
    TLSReaderService tlsReaderService;

    @Autowired
    CertificateService certificateService;

    @Autowired
    DomainContextProvider domainProvider;

    @Autowired
    protected SignalService signalService;

    @Override
    public void replaceTrustStore(String fileName, byte[] fileContent, String filePassword) throws CryptoException {
        KeyStoreType trustParams = getTruststoreParams();
        String trustStoreLocation = trustParams.getFile();
        String trustStorePassword = trustParams.getPassword();
        String trustStoreType = trustParams.getType();

        certificateService.replaceTrustStore(fileName, fileContent, filePassword, trustStoreType, trustStoreLocation, trustStorePassword);

        Domain currentDomain = domainProvider.getCurrentDomain();
        tlsReaderService.reset(currentDomain.getCode()); // needed really?
        signalService.signalTLSTrustStoreUpdate(currentDomain);
    }

    @Override
    public List<TrustStoreEntry> getTrustStoreEntries() {
        KeyStoreType trustParams = getTruststoreParams();
        String trustStoreLocation = trustParams.getFile();
        String trustStorePassword = trustParams.getPassword();

        return certificateService.getTrustStoreEntries(trustStoreLocation, trustStorePassword);
    }

    @Override
    public byte[] getTruststoreContent() {
        KeyStoreType trustParams = getTruststoreParams();
        String trustStoreLocation = trustParams.getFile();
        return certificateService.getTruststoreContent(trustStoreLocation);
    }

    @Override
    public boolean addCertificate(byte[] certificateData, String alias) {
        KeyStoreType trustParams = getTruststoreParams();
        String trustStoreLocation = trustParams.getFile();
        String trustStorePassword = trustParams.getPassword();

        return certificateService.addCertificate(trustStorePassword, trustStoreLocation, certificateData, alias, true);
    }

    @Override
    public boolean removeCertificate(String alias) {
        KeyStoreType trustParams = getTruststoreParams();
        String trustStoreLocation = trustParams.getFile();
        String trustStorePassword = trustParams.getPassword();

        return certificateService.removeCertificate(trustStorePassword, trustStoreLocation, alias, true);
    }

    protected KeyStoreType getTruststoreParams() {
        Domain domain = domainProvider.getCurrentDomain();
        TLSClientParametersType params = tlsReaderService.getTlsClientParametersType(domain.getCode());
        return params.getTrustManagers().getKeyStore();
    }
}
