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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author Soumya Chandran
 * @since 5.1
 */
@Service
public class TruststoreServiceDelegate implements TruststoreExtService {

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
    public byte[] downloadTruststoreContent() {
        TrustStoreContentDTO content = multiDomainCertificateProvider.getTruststoreContent(domainProvider.getCurrentDomain());
        return content.getContent();
    }

    @Override
    public List<TrustStoreDTO> getTrustStoreEntries() {
        List<TrustStoreEntry> trustStoreEntries = certificateService.getTrustStoreEntries(DOMIBUS_TRUSTSTORE_NAME);
        return domibusExtMapper.trustStoreEntriesToTrustStoresDTO(trustStoreEntries);
    }

    @Override
    public void uploadTruststoreFile(MultipartFile truststoreFile, String password) {
        byte[] truststoreFileContent = multiPartFileUtil.validateAndGetFileContent(truststoreFile);
        Domain currentDomain = domainProvider.getCurrentDomain();
        multiDomainCertificateProvider.replaceTrustStore(currentDomain, truststoreFile.getOriginalFilename(), truststoreFileContent, password);
    }

    @Override
    public void addCertificate(MultipartFile certificateFile, String alias) throws RequestValidationException {
        if (StringUtils.isBlank(alias)) {
            throw new RequestValidationException("Please provide an alias for the certificate.");
        }

        byte[] fileContent = multiPartFileUtil.validateAndGetFileContent(certificateFile);

        certificateService.addCertificate(DOMIBUS_TRUSTSTORE_NAME, fileContent, alias, true);
    }

    @Override
    public void removeCertificate(String alias) throws RequestValidationException {
        certificateService.removeCertificate(DOMIBUS_TRUSTSTORE_NAME, alias);
    }
}

