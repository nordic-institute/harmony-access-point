package eu.domibus.core.crypto;

import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.cxf.TLSReaderService;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.core.crypto.api.TLSCertificateManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.security.KeyStore;
import java.util.List;

public class TLSCertificateManagerImpl implements TLSCertificateManager {

    @Autowired
    TLSReaderService tlsReaderService;

    @Autowired
    CertificateService certificateService;

    @Override
    public void replaceTrustStore(byte[] store, String password) throws CryptoException {

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
