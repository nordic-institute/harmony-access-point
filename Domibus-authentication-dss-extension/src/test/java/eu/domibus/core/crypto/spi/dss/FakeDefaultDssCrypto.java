package eu.domibus.core.crypto.spi.dss;

import eu.domibus.core.crypto.spi.*;
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

public class FakeDefaultDssCrypto implements DomainCryptoServiceSpi{

        @Override
        public X509Certificate[] getX509Certificates(CryptoType cryptoType) throws WSSecurityException {
        return new X509Certificate[0];
    }

        @Override
        public String getX509Identifier(X509Certificate cert) throws WSSecurityException {
        return null;
    }

        @Override
        public PrivateKey getPrivateKey(X509Certificate certificate, CallbackHandler callbackHandler) throws WSSecurityException {
        return null;
    }

        @Override
        public PrivateKey getPrivateKey(PublicKey publicKey, CallbackHandler callbackHandler) throws WSSecurityException {
        return null;
    }

        @Override
        public PrivateKey getPrivateKey(String identifier, String password) throws WSSecurityException {
        return null;
    }

        @Override
        public void verifyTrust(PublicKey publicKey) throws WSSecurityException {

    }

        @Override
        public void verifyTrust(X509Certificate[] certs, boolean enableRevocation, Collection<Pattern > subjectCertConstraints, Collection<Pattern> issuerCertConstraints) throws WSSecurityException {

    }

        @Override
        public String getDefaultX509Identifier() throws WSSecurityException {
        return null;
    }

        @Override
        public String getPrivateKeyPassword(String alias) {
        return null;
    }

        @Override
        public void refreshTrustStore() {

    }

        @Override
        public void replaceTrustStore(byte[] store, String password) throws CryptoSpiException {

    }

        @Override
        public KeyStore getKeyStore() {
        return null;
    }

        @Override
        public KeyStore getTrustStore() {
        return null;
    }

        @Override
        public X509Certificate getCertificateFromKeyStore(String alias) throws KeyStoreException {
        return null;
    }

        @Override
        public boolean isCertificateChainValid(String alias) throws DomibusCertificateSpiException {
        return false;
    }

        @Override
        public boolean addCertificate(X509Certificate certificate, String alias, boolean overwrite) {
        return false;
    }

        @Override
        public void addCertificate(List< CertificateEntrySpi > certificates, boolean overwrite) {

    }

        @Override
        public X509Certificate getCertificateFromTrustStore(String alias) throws KeyStoreException {
        return null;
    }

        @Override
        public boolean removeCertificate(String alias) {
        return false;
    }

        @Override
        public void removeCertificate(List<String> aliases) {

    }

        @Override
        public String getIdentifier() {
        return null;
    }

        @Override
        public void setDomain(DomainSpi domain) {

    }

        @Override
        public void init() {

    }
}
