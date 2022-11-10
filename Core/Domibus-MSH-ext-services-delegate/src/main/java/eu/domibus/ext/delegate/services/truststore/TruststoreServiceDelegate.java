package eu.domibus.ext.delegate.services.truststore;

import eu.domibus.api.crypto.TrustStoreContentDTO;
import eu.domibus.api.exceptions.RequestValidationException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.pki.MultiDomainCryptoService;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.api.util.MultiPartFileUtil;
import eu.domibus.ext.delegate.mapper.DomibusExtMapper;
import eu.domibus.ext.domain.TrustStoreDTO;
import eu.domibus.ext.services.TruststoreExtService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author Soumya Chandran
 * @since 5.1
 */
@Service
public class TruststoreServiceDelegate implements TruststoreExtService {

    public static final String ERROR_MESSAGE_EMPTY_TRUSTSTORE_PASSWORD = "Failed to upload the truststoreFile file since its password was empty."; //NOSONAR

    public static final String DOMIBUS_TRUSTSTORE_NAME = "domibus.truststore";

    private final MultiDomainCryptoService multiDomainCertificateProvider;

    private final DomainContextProvider domainProvider;

    private final CertificateService certificateService;

    private final MultiPartFileUtil multiPartFileUtil;

    private final DomibusExtMapper domibusExtMapper;


    public TruststoreServiceDelegate(MultiDomainCryptoService multiDomainCertificateProvider,
                                     DomainContextProvider domainProvider, CertificateService certificateService,
                                     MultiPartFileUtil multiPartFileUtil, DomibusExtMapper domibusExtMapper) {
        this.multiDomainCertificateProvider = multiDomainCertificateProvider;
        this.domainProvider = domainProvider;
        this.certificateService = certificateService;
        this.multiPartFileUtil = multiPartFileUtil;
        this.domibusExtMapper = domibusExtMapper;
    }


    @Override
    public ResponseEntity<ByteArrayResource> downloadTruststoreContent() {
        TrustStoreContentDTO content = multiDomainCertificateProvider.getTruststoreContent(domainProvider.getCurrentDomain());
        ByteArrayResource resource = new ByteArrayResource(content.getContent());

        HttpStatus status = HttpStatus.OK;
        if (resource.getByteArray().length == 0) {
            status = HttpStatus.NO_CONTENT;
        }

        return ResponseEntity.status(status)
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header("content-disposition", "attachment; filename=" + "truststore" + ".jks")
                .body(resource);
    }


    @Override
    public List<TrustStoreDTO> getTrustStoreEntries() {
        List<TrustStoreEntry> trustStoreEntries = certificateService.getTrustStoreEntries(DOMIBUS_TRUSTSTORE_NAME);
        return domibusExtMapper.trustStoreEntriesToTrustStoresDTO(trustStoreEntries);
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

