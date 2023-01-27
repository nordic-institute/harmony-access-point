package eu.domibus.ext.services;

import eu.domibus.ext.domain.TrustStoreDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * All operations related to TLS truststore files
 *
 * @author Soumya Chandran
 * @since 5.1
 */
public interface TLSTruststoreExtService {

    /**
     * Download the TLS truststore file
     *
     * @return byte[]
     */
    byte[] downloadTruststoreContent();

    /**
     * Returns TLS truststore files information
     *
     * @return list of {@code TrustStoreDTO}
     */
    List<TrustStoreDTO> getTrustStoreEntries();

    /**
     * Upload a new version of the TLS truststore file
     *
     * @param truststoreFileContent     TLS truststore file wrapping class
     * @param password of the TLS truststore uploaded
     */
    void uploadTruststoreFile(byte[] truststoreFileContent, String originalFilename, String password);

    /**
     * Adds the specified certificate to the TLS truststore pointed by the parameters
     *
     * @param fileContent the content of the certificate
     * @param alias           the name of the certificate
     */
    void addCertificate(byte[] fileContent, String alias);

    /**
     * Removes the specified certificate from the TLS truststore by the alias name
     *
     * @param alias the certificate name
     */
    void removeCertificate(String alias);
}

