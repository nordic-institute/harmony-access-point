package eu.domibus.ext.services;

import eu.domibus.ext.domain.TrustStoreDTO;

import java.util.List;

/**
 * All operations related to truststore files
 *
 * @author Soumya Chandran
 * @since 5.1
 */
public interface TruststoreExtService {

    /**
     * Download the truststore file
     *
     * @return byte[]
     */
    byte[] downloadTruststoreContent();

    /**
     * Returns PMode current file information
     *
     * @return list of {@code TrustStoreDTO}
     */
    List<TrustStoreDTO> getTrustStoreEntries();

    /**
     * Upload a new version of the truststore file
     *  @param truststoreFileContent     truststore file content bytes
     * @param originalFilename
     * @param password of the truststore uploaded
     */
    void uploadTruststoreFile(byte[] truststoreFileContent, String originalFilename, String password);


    /**
     * Adds the specified certificate to the truststore pointed by the parameters
     *  @param certificateFile the content of the certificate
     * @param alias           the name of the certificate
     */
    void addCertificate(byte[] certificateFile, String alias);

    /**
     * Removes the specified certificate from the truststore by the alias name
     *
     * @param alias the certificate name
     */
    void removeCertificate(String alias);;

    String getStoreFileExtension();
}

