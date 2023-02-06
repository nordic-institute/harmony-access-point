package eu.domibus.core.crypto.spi;

import eu.domibus.core.crypto.spi.model.KeyStoreContentInfoDTO;
import org.apache.wss4j.common.crypto.CryptoType;
import org.apache.wss4j.common.ext.WSSecurityException;

import javax.security.auth.callback.CallbackHandler;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Cosmin Baciu
 * @see org.apache.wss4j.common.crypto.CryptoBase
 * @since 4.0
 * <p>
 * Same methods as CryptoBase defined in the IAM api. Will be used as a delegate by core DomainCryptoService
 */
public interface DomainCryptoServiceSpi {
    /* START - Methods required to be implemented by the org.apache.wss4j.common.crypto.CryptoBase */
    default X509Certificate[] getX509Certificates(CryptoType cryptoType, String alias) throws WSSecurityException {
        return new X509Certificate[0];
    }

    X509Certificate[] getX509Certificates(CryptoType cryptoType) throws WSSecurityException;

    default String getX509Identifier(X509Certificate cert, String alias) throws WSSecurityException {
        return null;
    }

    String getX509Identifier(X509Certificate cert) throws WSSecurityException;

    default PrivateKey getPrivateKey(X509Certificate certificate, CallbackHandler callbackHandler, String alias) throws WSSecurityException {
        return null;
    }

    PrivateKey getPrivateKey(X509Certificate certificate, CallbackHandler callbackHandler) throws WSSecurityException;

    default PrivateKey getPrivateKey(PublicKey publicKey, CallbackHandler callbackHandler, String alias) throws WSSecurityException {
        return null;
    }

    PrivateKey getPrivateKey(PublicKey publicKey, CallbackHandler callbackHandler) throws WSSecurityException;

    PrivateKey getPrivateKey(String identifier, String password) throws WSSecurityException;

    default void verifyTrust(PublicKey publicKey, String alias) throws WSSecurityException {
    }

    void verifyTrust(PublicKey publicKey) throws WSSecurityException;

    default void verifyTrust(X509Certificate[] certs, boolean enableRevocation, Collection<Pattern> subjectCertConstraints, Collection<Pattern> issuerCertConstraints, String alias) throws WSSecurityException {
    }

    void verifyTrust(X509Certificate[] certs, boolean enableRevocation, Collection<Pattern> subjectCertConstraints, Collection<Pattern> issuerCertConstraints) throws WSSecurityException;

    default String getDefaultX509Identifier(String alias) throws WSSecurityException {
        return null;
    }

    String getDefaultX509Identifier() throws WSSecurityException;
    /* END - Methods required to be implemented by the org.apache.wss4j.common.crypto.CryptoBase */

    String getPrivateKeyPassword(String alias);

    /**
     * @deprecated use {@link #resetTrustStore()} instead
     */
    void refreshTrustStore();

    void replaceTrustStore(byte[] storeContent, String storeFileName, String storePassword);

    default void replaceTrustStore(KeyStoreContentInfoDTO keyStoreContentInfoDTO) {
        replaceTrustStore(keyStoreContentInfoDTO.getContent(), keyStoreContentInfoDTO.getFileName(), keyStoreContentInfoDTO.getPassword());
    }

    /**
     * Loads the KeyStore specified by the location and password
     *
     * @param storeFileLocation
     * @param storePassword
     */
    void replaceTrustStore(String storeFileLocation, String storePassword);

    KeyStore getKeyStore();

    KeyStore getTrustStore();

    X509Certificate getCertificateFromKeyStore(String alias) throws KeyStoreException;

    boolean isCertificateChainValid(String alias) throws DomibusCertificateSpiException;

    boolean addCertificate(X509Certificate certificate, String alias, boolean overwrite);

    void addCertificate(List<CertificateEntrySpi> certificates, boolean overwrite);

    X509Certificate getCertificateFromTrustStore(String alias) throws KeyStoreException;

    boolean removeCertificate(String alias);

    void removeCertificate(List<String> aliases);

    String getIdentifier();

    void setDomain(DomainSpi domain);

    void init();

    void replaceKeyStore(byte[] storeContent, String storeFileName, String storePassword);

    default void replaceKeyStore(KeyStoreContentInfoDTO storeContentInfoSpi) {
        replaceKeyStore(storeContentInfoSpi.getContent(), storeContentInfoSpi.getFileName(), storeContentInfoSpi.getPassword());
    }

    /**
     * Loads the KeyStore specified by the location and password
     *
     * @param storeFileLocation
     * @param storePassword
     */
    void replaceKeyStore(String storeFileLocation, String storePassword);

    /**
     * @deprecated use {@link #resetKeyStore()} instead
     */
    void refreshKeyStore();

    void resetKeyStore();

    void resetTrustStore();

    void resetSecurityProfiles();

    default boolean isTrustStoreChanged() {
        return false;
    }

    default boolean isKeyStoreChanged() {
        return false;
    }
}
