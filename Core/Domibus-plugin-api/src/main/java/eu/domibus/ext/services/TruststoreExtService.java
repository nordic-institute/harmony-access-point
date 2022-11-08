package eu.domibus.ext.services;

import eu.domibus.ext.domain.PModeArchiveInfoDTO;
import org.springframework.web.multipart.MultipartFile;

/**
 * All operations related to truststore files     *
 * <ul>
 * <li>get current truststore file information</li>
 * <li>download the truststore file</li>
 * <ul/>
 *
 * @author Soumya Chandran
 * @since5.1
 */
public interface TruststoreExtService {

    /**
     * Get PMode file as {@code byte[]}
     *
     * @param id id of the truststore to download/get
     * @return array of bytes
     */
    byte[] getTrustStoreFile(long id);

    /**
     * Returns PMode current file information
     *
     * @return an instance of {@code PModeArchiveInfoDTO}
     */
    PModeArchiveInfoDTO getTrustStoreEntries();

    /**
     * Upload a new version of the truststore file         *
     *
     * @param file     truststore file wrapping class
     * @param password of the truststore uploaded
     * @return String as error
     */
    String uploadTruststoreFile(MultipartFile file, String password);
}

