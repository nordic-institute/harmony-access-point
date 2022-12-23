package eu.domibus.core.certificate;

import eu.domibus.api.pki.DomibusCertificateException;
import eu.domibus.archive.client.configuration.EArchiveConfiguration;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
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
        String fileType = FilenameUtils.getExtension(storeFileName).toLowerCase();
        if (Arrays.asList(P_12, PFX).contains(fileType)) {
            return PKCS_12;
        }
        return JKS;
    }

}
