package eu.domibus.ext.services;

import eu.domibus.ext.domain.TrustStoreDTO;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
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
     * @return ResponseEntity<ByteArrayResource>
     */
    ResponseEntity<ByteArrayResource> downloadTLSTruststoreContent();

    /**
     * Returns TLS truststore files information
     *
     * @return list of {@code TrustStoreDTO}
     */
    List<TrustStoreDTO> getTLSTrustStoreEntries();

    /**
     * Upload a new version of the truststore file
     *
     * @param file     TLS truststore file wrapping class
     * @param password of the TLS truststore uploaded
     * @return String as error
     */
    String uploadTLSTruststoreFile(MultipartFile file, String password);
}

