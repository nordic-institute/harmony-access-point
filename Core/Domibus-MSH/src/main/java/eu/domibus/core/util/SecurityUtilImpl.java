package eu.domibus.core.util;

import eu.domibus.api.pki.DomibusCertificateException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;
import java.util.Enumeration;

/**
 * Provides functionality for security certificates configuration
 *
 * @author Lucian FURCA
 * @since 5.1
 */
@Service
public class SecurityUtilImpl {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SecurityUtilImpl.class);

    public boolean areKeystoresIdentical(KeyStore store1, KeyStore store2) {
        if (store1 == null && store2 == null) {
            LOG.debug("Identical keystores: both are null");
            return true;
        }
        if (store1 == null || store2 == null) {
            LOG.debug("Different keystores: [{}] vs [{}]", store1, store2);
            return false;
        }
        try {
            if (store1.size() != store2.size()) {
                LOG.debug("Different keystores: [{}] vs [{}] entries", store1.size(), store2.size());
                return false;
            }
            final Enumeration<String> aliases = store1.aliases();
            while (aliases.hasMoreElements()) {
                final String alias = aliases.nextElement();
                if (!store2.containsAlias(alias)) {
                    LOG.debug("Different keystores: [{}] alias is not found in both", alias);
                    return false;
                }
                if (!store1.getCertificate(alias).equals(store2.getCertificate(alias))) {
                    LOG.debug("Different keystores: [{}] certificate is different", alias);
                    return false;
                }
            }
            return true;
        } catch (KeyStoreException e) {
            throw new DomibusCertificateException("Invalid keystore", e);
        }
    }

    public String encryptValue(String publicKeyPem, String value) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        PEMParser pemParser = new PEMParser(new StringReader(publicKeyPem));
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
        PublicKey publicKey = converter.getPublicKey((SubjectPublicKeyInfo) pemParser.readObject());

        // Initialize the Cipher with the public key for encryption
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        // Encrypt the value
        byte[] encryptedBytes = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));

        // Convert the encrypted bytes to Base64 to get a string
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }
}