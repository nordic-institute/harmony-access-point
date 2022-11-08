package eu.domibus.ext.delegate.services.truststore;

import eu.domibus.api.exceptions.RequestValidationException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.pki.MultiDomainCryptoService;
import eu.domibus.api.util.MultiPartFileUtil;
import eu.domibus.ext.domain.PModeArchiveInfoDTO;
import eu.domibus.ext.services.TruststoreExtService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Soumya Chandran
 * @since 5.1
 */
@Service
public class TruststoreServiceDelegate implements TruststoreExtService {

    public static final String ERROR_MESSAGE_EMPTY_TRUSTSTORE_PASSWORD = "Failed to upload the truststoreFile file since its password was empty."; //NOSONAR

    private final MultiDomainCryptoService multiDomainCertificateProvider;

    private final DomainContextProvider domainProvider;

    private final CertificateService certificateService;

    private final MultiPartFileUtil multiPartFileUtil;


    public TruststoreServiceDelegate(MultiDomainCryptoService multiDomainCertificateProvider,
                                     DomainContextProvider domainProvider, CertificateService certificateService,
                                     MultiPartFileUtil multiPartFileUtil) {
        this.multiDomainCertificateProvider = multiDomainCertificateProvider;
        this.domainProvider = domainProvider;
        this.certificateService = certificateService;
        this.multiPartFileUtil = multiPartFileUtil;
    }


    @Override
    public byte[] getTrustStoreFile(long id) {
        return null;
    }

    /**
     * Returns truststore file information
     *
     * @return an instance of {@code PModeArchiveInfoDTO}
     */
    @Override
    public PModeArchiveInfoDTO getTrustStoreEntries() {
        return null;
    }


    @Override
    public String uploadTruststoreFile(MultipartFile truststoreFile, String password) {
        byte[] truststoreFileContent = multiPartFileUtil.validateAndGetFileContent(truststoreFile);

        if (StringUtils.isBlank(password)) {
            throw new RequestValidationException(ERROR_MESSAGE_EMPTY_TRUSTSTORE_PASSWORD);
        }

        Domain currentDomain = domainProvider.getCurrentDomain();
        multiDomainCertificateProvider.replaceTrustStore(currentDomain, truststoreFile.getOriginalFilename(), truststoreFileContent, password);
        return "Truststore file has been successfully replaced.";
    }
}

