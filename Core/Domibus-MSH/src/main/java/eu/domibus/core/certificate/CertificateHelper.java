package eu.domibus.core.certificate;

import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.pki.DomibusCertificateException;
import eu.domibus.api.pki.KeyStoreContentInfo;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
// todo improve???
public class CertificateHelper {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CertificateHelper.class);

    public static final String PKCS_12 = "pkcs12";

    public static final String P_12 = "p12";
    public static final String PFX = "pfx";
    public static final String JKS = "jks";

    /**
     * Validates the truststore type with the file extension
     *
     * @param storeType     the type of the trust: pkcs12, jks
     * @param storeFileName the name of the truststore file
     */
    public void validateStoreType(String storeType, String storeFileName) {
        String fileType = FilenameUtils.getExtension(storeFileName).toLowerCase();
        switch (storeType.toLowerCase()) {
            case PKCS_12:
                if (Arrays.asList(P_12, PFX).contains(fileType)) {
                    return;
                }
            case JKS:
                if (Arrays.asList(JKS).contains(fileType)) {
                    return;
                }
        }
        throw new DomibusCertificateException("Store file type (" + fileType + ") should match the configured truststore type (" + storeType + ").");
    }

    public void validateStoreFileName(String storeFileName) {
        String fileType = FilenameUtils.getExtension(storeFileName);
        List<String> validTypes = Arrays.asList(P_12, PFX, JKS);
        if (validTypes.contains(fileType)) {
            LOG.debug("Valid file type [{}]", fileType);
            return;
        }
        throw new DomibusCertificateException("Keystore file type [" + fileType + "] is not a valid type. Valid types are " + validTypes);
    }

    public String getStoreType(String storeFileName) {
        String fileExtension = FilenameUtils.getExtension(storeFileName).toLowerCase();
        if (Arrays.asList(P_12, PFX).contains(fileExtension)) {
            return PKCS_12;
        } else if (Arrays.asList(JKS).contains(fileExtension)) {
            return JKS;
        } else {
            throw new DomibusCertificateException("Invalid store file name:" + storeFileName);
        }
    }

    public String getStoreFileExtension(String storeType) {
        if (StringUtils.equals(storeType, PKCS_12)) {
            return P_12;
        } else if (StringUtils.equals(storeType, JKS)) {
            return JKS;
        } else {
            throw new DomibusCertificateException("Invalid store type:" + storeType);
        }
    }

    public byte[] getContentFromFile(String location) {
        File file = new File(location);
        Path path = Paths.get(file.getAbsolutePath());
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new DomibusCertificateException("Could not read store from [" + location + "]");
        }
    }

    public boolean containsAndIdentical(KeyStore keystore, String alias, X509Certificate certificate) {
        try {
            if (!keystore.containsAlias(alias)) {
                LOG.debug("The store [{}] does not contain alias [{}]", keystore, alias);
                return false;
            }
            X509Certificate cert = (X509Certificate) keystore.getCertificate(alias);
            if (Arrays.equals(cert.getIssuerUniqueID(), certificate.getIssuerUniqueID())
                    && Objects.equals(cert.getSerialNumber(), certificate.getSerialNumber())) {
                LOG.debug("The store [{}] contains a certificate with alias [{}] and it is the same as [{}]", keystore, alias, certificate);
                return true;
            }
            LOG.debug("The store [{}] contains a certificate with alias [{}] but it is different than [{}]", keystore, alias, certificate);
            return false;
        } catch (KeyStoreException e) {
            throw new CryptoException("Error while trying to get the alias from the store. This should never happen", e);
        }
    }

    public KeyStoreContentInfo createStoreContentInfo(String storeName, byte[] storeContent, String storeType, String storePassword) {
        KeyStoreContentInfo storeInfo = new KeyStoreContentInfo();
        storeInfo.setName(storeName);
        storeInfo.setContent(storeContent);
        storeInfo.setType(storeType);
        storeInfo.setPassword(storePassword);

        return storeInfo;
    }

    public KeyStoreContentInfo createStoreContentInfo(String storeName, String storeFileName, byte[] storeContent, String storePassword) {
        if(StringUtils.isNotEmpty(storeFileName)) {
            validateStoreFileName(storeFileName);
        }

        KeyStoreContentInfo storeInfo = new KeyStoreContentInfo();
        storeInfo.setName(storeName);
        storeInfo.setFileName(storeFileName);
        storeInfo.setContent(storeContent);
        storeInfo.setPassword(storePassword);
        storeInfo.setType(getStoreType(storeFileName));

        return storeInfo;
    }
}
