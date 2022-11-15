package eu.domibus.ext.services;

import eu.domibus.ext.domain.TrustStoreDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * All operations related to TLS truststore files
 *
 * @author Soumya Chandran
 * @since5.1
 */
public interface TLSTruststoreExtService {

    /**
     * Download the TLS truststore file
     *
     * @return byte[]
     */
    byte[] downloadTLSTruststoreContent();

    /**
     * Returns TLS truststore files information
     *
     * @return list of {@code TrustStoreDTO}
     */
    List<TrustStoreDTO> getTLSTrustStoreEntries();

    /**
     * Upload a new version of the TLS truststore file
     *
     * @param file     TLS truststore file wrapping class
     * @param password of the TLS truststore uploaded
     */
    void uploadTLSTruststoreFile(MultipartFile file, String password);


    /**
     * Adds the specified certificate to the TLS truststore pointed by the parameters
     *
     * @param certificateFile the content of the certificate
     * @param alias           the name of the certificate
     */

    void addTLSCertificate(MultipartFile certificateFile, String alias);

    /**
     * Removes the specified certificate from the TLS truststore by the alias name
     *
     * @param alias the certificate name
     */

    void removeTLSCertificate(String alias);
}

