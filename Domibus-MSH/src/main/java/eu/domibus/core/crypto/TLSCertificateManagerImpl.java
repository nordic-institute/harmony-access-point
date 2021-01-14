package eu.domibus.core.crypto;

import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.cxf.TLSReaderService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.core.crypto.api.TLSCertificateManager;
import org.apache.cxf.configuration.security.TLSClientParametersType;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.security.KeyStore;
import java.util.List;

public class TLSCertificateManagerImpl implements TLSCertificateManager {

    @Autowired
    TLSReaderService tlsReaderService;

    @Autowired
    CertificateService certificateService;

    @Autowired
    DomainContextProvider domainProvider;

    @Override
    public void replaceTrustStore(String fileName, byte[] content, String password) throws CryptoException {
        Domain domain = domainProvider.getCurrentDomain();
        TLSClientParametersType params = tlsReaderService.getTlsClientParametersType(domain.getCode());

//        certificateService.replaceTrustStore(fileName, content, password, type, location);
    }

    @Override
    public List<TrustStoreEntry> getTrustStoreEntries(KeyStore trustStore) {
        return null;
    }

    @Override
    public byte[] getTruststoreContent() throws IOException {
        return new byte[0];
    }

    @Override
    public boolean addCertificate(byte[] certificateData, String alias) {
        return false;
    }

    @Override
    public boolean removeCertificate(String alias) {
        return false;
    }
}
