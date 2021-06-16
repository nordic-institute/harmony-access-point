package eu.domibus.core.certificate;

import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import java.security.InvalidParameterException;
import java.util.Arrays;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
public class CertificateHelper {
    /**
     * Validates the truststore type with the file extension
     *
     * @param storeType the type of the trust: pkcs12, jks
     * @param storeFileName the name of the truststore file
     */
    public void validateStoreType(String storeType, String storeFileName) {
        String fileType = FilenameUtils.getExtension(storeFileName).toLowerCase();
        switch (storeType.toLowerCase()) {
            case "pkcs12":
                if (Arrays.asList("p12", "pfx").contains(fileType)) {
                    return;
                }
            case "jks":
                if (Arrays.asList("jks").contains(fileType)) {
                    return;
                }
        }
        throw new InvalidParameterException("Store file type (" + fileType + ") should match the configured truststore type (" + storeType + ").");
    }
}
