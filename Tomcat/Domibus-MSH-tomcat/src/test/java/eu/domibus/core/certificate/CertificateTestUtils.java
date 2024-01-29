package eu.domibus.core.certificate;

import eu.domibus.api.crypto.SameResourceCryptoException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.pki.DomibusCertificateException;
import eu.domibus.api.pki.KeyStoreContentInfo;
import eu.domibus.api.pki.MultiDomainCryptoService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import static eu.domibus.core.crypto.MultiDomainCryptoServiceImpl.DOMIBUS_TRUSTSTORE_NAME;

/**
 * Utility methods class used in tests that need to work with certificates
 *
 * @author Ion Perpegel
 * @since 4.1
 */
@Service
public class CertificateTestUtils {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CertificateTestUtils.class);

    @Autowired
    CertificateHelper certificateHelper;

    @Autowired
    MultiDomainCryptoService multiDomainCryptoService;

    /**
     * Loads a certificate from a JKS file
     *
     * @param filePath path to the file representing the keystore
     * @param password the password to open the keystore
     * @param alias    the name of the certificate
     */
    public static X509Certificate loadCertificateFromJKSFile(String filePath, String alias, String password) {
        try (InputStream fileInputStream = CertificateTestUtils.class.getClassLoader().getResourceAsStream(filePath)) {

            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(fileInputStream, password.toCharArray());

            Certificate cert = keyStore.getCertificate(alias);

            return (X509Certificate) cert;
        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
            throw new DomibusCertificateException("Could not load certificate from file " + filePath + ", alias " + alias, e);
        }
    }


    /**
     * Loads a keystore from a JKS file
     *
     * @param filePath path to the file representing the keystore
     * @param password the password to open the keystore
     */
    public static KeyStore loadKeyStoreFromJKSFile(String filePath, String password) {
        try (FileInputStream fileInputStream = new FileInputStream(filePath)) {

            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(fileInputStream, password.toCharArray());

            return keyStore;
        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
            throw new DomibusCertificateException("Could not load keystore from file " + filePath, e);
        }
    }

    public void resetTruststore(String truststoreClasspath, String password) throws IOException {
        Domain domain = DomainService.DEFAULT_DOMAIN;
        final InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(truststoreClasspath);
        final byte[] truststoreBytes = IOUtils.toByteArray(resourceAsStream);
        KeyStoreContentInfo storeInfo = certificateHelper.createStoreContentInfo(DOMIBUS_TRUSTSTORE_NAME, "gateway_truststore.jks", truststoreBytes, password);
        try {
            multiDomainCryptoService.replaceTrustStore(domain, storeInfo);
        } catch (SameResourceCryptoException e) {
            LOG.debug(e.getMessage(), e);
        }
    }
}
