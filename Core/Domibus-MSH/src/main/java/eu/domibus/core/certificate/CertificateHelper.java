package eu.domibus.core.certificate;

import eu.domibus.api.pki.DomibusCertificateException;
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
import java.util.Arrays;
import java.util.List;

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
}
