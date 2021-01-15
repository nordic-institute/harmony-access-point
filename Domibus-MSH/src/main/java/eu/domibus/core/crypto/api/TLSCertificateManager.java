package eu.domibus.core.crypto.api;

import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.cxf.TLSReaderService;
import eu.domibus.api.security.TrustStoreEntry;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.security.KeyStore;
import java.util.List;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
public interface TLSCertificateManager {

    void replaceTrustStore(String trustFileName, byte[] trustContent, String password) throws CryptoException;

    List<TrustStoreEntry> getTrustStoreEntries();

    byte[] getTruststoreContent() ;

    boolean addCertificate(byte[] certificateData, final String alias);

    boolean removeCertificate(String alias);
}
