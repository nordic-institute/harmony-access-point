package eu.domibus.core.crypto;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.pki.CertificateEntry;
import eu.domibus.core.crypto.spi.CertificateEntrySpi;
import eu.domibus.core.crypto.spi.DomainCryptoServiceSpi;
import eu.domibus.core.crypto.spi.DomainSpi;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.wss4j.common.crypto.CryptoType;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.security.auth.callback.CallbackHandler;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@RunWith(JMockit.class)
public class BaseDomainCryptoServiceImplTest {

    @Tested
    private BaseDomainCryptoServiceImpl domainCryptoService;

    @Injectable
    Domain domain;

    @Injectable
    DomainCryptoServiceSpi iamProvider;

    @Test
    public void init(@Mocked DomainCryptoServiceSpi iamProvider) {

        domainCryptoService.init(iamProvider);

        new Verifications() {{
            iamProvider.setDomain(new DomainSpi(domain.getCode(), domain.getName()));
            iamProvider.init();
        }};
    }

    @Test
    public void reset() {
        domainCryptoService.reset();

        new Verifications() {{
            domainCryptoService.init();
        }};
    }

    @Test
    public void addCertificate(@Mocked X509Certificate cert1, @Mocked X509Certificate cert2) {
        boolean overwrite = true;
        CertificateEntry ce1 = new CertificateEntry("alias1", cert1);
        CertificateEntry ce2 = new CertificateEntry("alias2", cert2);
        List<CertificateEntry> certificates = Arrays.asList(ce1, ce2);

        domainCryptoService.addCertificate(certificates, overwrite);

        new Verifications() {{
            List<CertificateEntrySpi> list;
            iamProvider.addCertificate(list = withCapture(), overwrite);

            Assert.assertEquals(2, list.size());
            Assert.assertEquals(list.get(0).getAlias(), "alias1");
            Assert.assertEquals(list.get(1).getAlias(), "alias2");
        }};
    }

    @Test
    public void getCertificateFromKeyStore(@Mocked String alias, @Mocked X509Certificate cert) throws KeyStoreException {
        new Expectations() {{
            iamProvider.getCertificateFromKeyStore(alias);
            result = cert;
        }};

        X509Certificate result = domainCryptoService.getCertificateFromKeyStore(alias);

        Assert.assertEquals(cert, result);

        new Verifications() {{
            iamProvider.getCertificateFromKeyStore(alias);
        }};
    }

    @Test
    public void getCertificateFromTrustStore(@Mocked String alias, @Mocked X509Certificate cert) throws KeyStoreException {
        new Expectations() {{
            iamProvider.getCertificateFromTrustStore(alias);
            result = cert;
        }};

        X509Certificate result = domainCryptoService.getCertificateFromTrustStore(alias);

        Assert.assertEquals(cert, result);

        new Verifications() {{
            iamProvider.getCertificateFromTrustStore(alias);
        }};
    }

    @Test
    public void getX509Certificates(@Mocked CryptoType cryptoType, @Mocked X509Certificate[] certs) throws KeyStoreException, WSSecurityException {
        new Expectations() {{
            iamProvider.getX509Certificates(cryptoType);
            result = certs;
        }};

        X509Certificate[] result = domainCryptoService.getX509Certificates(cryptoType);

        Assert.assertEquals(certs, result);

        new Verifications() {{
            iamProvider.getX509Certificates(cryptoType);
        }};
    }

    @Test
    public void getX509Identifier(@Mocked String id, @Mocked X509Certificate cert) throws WSSecurityException {
        new Expectations() {{
            iamProvider.getX509Identifier(cert);
            result = id;
        }};

        String result = domainCryptoService.getX509Identifier(cert);

        Assert.assertEquals(id, result);

        new Verifications() {{
            iamProvider.getX509Identifier(cert);
        }};
    }

    @Test
    public void getPrivateKey(@Mocked PrivateKey key, @Mocked X509Certificate certificate, @Mocked CallbackHandler callbackHandler) throws WSSecurityException {
        new Expectations() {{
            iamProvider.getPrivateKey(certificate, callbackHandler);
            result = key;
        }};

        PrivateKey result = domainCryptoService.getPrivateKey(certificate, callbackHandler);

        Assert.assertEquals(key, result);

        new Verifications() {{
            iamProvider.getPrivateKey(certificate, callbackHandler);
        }};
    }

    @Test
    public void verifyTrust(@Mocked PublicKey publicKey) throws WSSecurityException {
        new Expectations() {{
            iamProvider.verifyTrust(publicKey);
        }};

        domainCryptoService.verifyTrust(publicKey);

        new Verifications() {{
            iamProvider.verifyTrust(publicKey);
        }};
    }

    @Test
    public void getDefaultX509Identifier() throws WSSecurityException {
        String id = "id";
        new Expectations() {{
            iamProvider.getDefaultX509Identifier();
            result = id;
        }};

        String result = domainCryptoService.getDefaultX509Identifier();

        Assert.assertEquals(id, result);

        new Verifications() {{
            iamProvider.getDefaultX509Identifier();
        }};
    }

    @Test
    public void getPrivateKeyPassword() {
        String pass = "id";
        String alias = "alias";

        new Expectations() {{
            iamProvider.getPrivateKeyPassword(alias);
            result = pass;
        }};

        String result = domainCryptoService.getPrivateKeyPassword(alias);

        Assert.assertEquals(pass, result);

        new Verifications() {{
            iamProvider.getPrivateKeyPassword(alias);
        }};
    }

    @Test
    public void refreshTrustStore() {
        new Expectations() {{
            iamProvider.refreshTrustStore();
        }};

        domainCryptoService.refreshTrustStore();

        new Verifications() {{
            iamProvider.refreshTrustStore();
        }};
    }

    @Test
    public void replaceTrustStore(@Mocked byte[] store, @Mocked String password) {
        new Expectations() {{
            iamProvider.replaceTrustStore(store, password);
        }};

        domainCryptoService.replaceTrustStore(store, password);

        new Verifications() {{
            iamProvider.replaceTrustStore(store, password);
        }};
    }

    @Test
    public void getKeyStore(@Mocked KeyStore keyStore) {
        new Expectations() {{
            iamProvider.getKeyStore();
            result = keyStore;
        }};

        KeyStore result = domainCryptoService.getKeyStore();

        Assert.assertEquals(keyStore, result);

        new Verifications() {{
            iamProvider.getKeyStore();
        }};
    }

    @Test
    public void getTrustStore(@Mocked KeyStore store) {
        new Expectations() {{
            iamProvider.getTrustStore();
            result = store;
        }};

        KeyStore result = domainCryptoService.getTrustStore();

        Assert.assertEquals(store, result);

        new Verifications() {{
            iamProvider.getTrustStore();
        }};
    }

    @Test
    public void removeCertificate(@Mocked String alias) {
        boolean removed = true;
        new Expectations() {{
            iamProvider.removeCertificate(alias);
            result = removed;
        }};

        boolean result = domainCryptoService.removeCertificate(alias);

        Assert.assertEquals(removed, result);

        new Verifications() {{
            iamProvider.removeCertificate(alias);
        }};
    }

    @Test
    public void removeCertificate(@Mocked List<String> aliases) {
        new Expectations() {{
            iamProvider.removeCertificate(aliases);
        }};

        domainCryptoService.removeCertificate(aliases);

        new Verifications() {{
            iamProvider.removeCertificate(aliases);
        }};
    }
}