package eu.domibus.core.crypto.api;

import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.security.TrustStoreEntry;

import java.util.List;

/**
 * "Orchestrator"/facade class that connects the dots between clientauthentication.xml properties and certificate server
 *
 * @author Ion Perpegel
 * @since 5.0
 */
public interface TLSCertificateManager {

    /**
     * Replaces the truststore pointed by the clientauthentication.xml file with the one provided as parameters
     *
     * @param trustFileName
     * @param trustContent
     * @param password
     * @throws CryptoException
     */
    void replaceTrustStore(String trustFileName, byte[] trustContent, String password) throws CryptoException;

    /**
     * Returns the certificate entries found in the tls truststore pointed by the clientauthentication.xml file
     *
     * @return
     */
    List<TrustStoreEntry> getTrustStoreEntries();

    /**
     * Returns the tls truststore content pointed by the clientauthentication.xml file
     *
     * @return
     */
    byte[] getTruststoreContent();

    /**
     * Adds the specified certificate to the tls truststore content pointed by the clientauthentication.xml file
     * @param certificateData
     * @param alias
     * @return
     */
    boolean addCertificate(byte[] certificateData, final String alias);

    /**
     * Removes the specified certificate from the tls truststore content pointed by the clientauthentication.xml file
     * @param alias
     * @return
     */
    boolean removeCertificate(String alias);
}
