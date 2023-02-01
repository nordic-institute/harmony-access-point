package eu.domibus.ext.services;

import eu.domibus.ext.domain.KeyStoreContentInfoDTO;
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
    KeyStoreContentInfoDTO downloadTruststoreContent();

    /**
     * Returns PMode current file information
     *
     * @return list of {@code TrustStoreDTO}
     */
    List<TrustStoreDTO> getTrustStoreEntries();

    /**
     * Upload a new version of the truststore file
     *  @param contentInfo     truststore file content bytes
     */
    void uploadTruststoreFile(KeyStoreContentInfoDTO contentInfo);

    /**
     * Adds the specified certificate to the truststore pointed by the parameters
     *  @param certificateFile the content of the certificate
     * @param alias           the name of the certificate
     */
    boolean addCertificate(byte[] certificateFile, String alias);

    /**
     * Removes the specified certificate from the truststore by the alias name
     *
     * @param alias the certificate name
     */
    boolean removeCertificate(String alias);

    String getStoreFileExtension();
}

