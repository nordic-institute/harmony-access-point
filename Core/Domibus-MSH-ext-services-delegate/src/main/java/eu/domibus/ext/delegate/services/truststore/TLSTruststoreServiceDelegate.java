package eu.domibus.ext.delegate.services.truststore;

import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.crypto.TrustStoreContentDTO;
import eu.domibus.api.cxf.TLSReaderService;
import eu.domibus.api.exceptions.RequestValidationException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.api.util.MultiPartFileUtil;
import eu.domibus.ext.delegate.mapper.DomibusExtMapper;
import eu.domibus.ext.domain.TrustStoreDTO;
import eu.domibus.ext.exceptions.TruststoreExtException;
import eu.domibus.ext.services.TLSTruststoreExtService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author Soumya Chandran
 * @since 5.1
 */
@Service
public class TLSTruststoreServiceDelegate implements TLSTruststoreExtService {

    public final static String TLS_TRUSTSTORE_NAME = "TLS.truststore";

    private final TLSReaderService tlsReaderService;

    private final SignalService signalService;

    private final DomainContextProvider domainProvider;

    private final CertificateService certificateService;

    private final MultiPartFileUtil multiPartFileUtil;

    private final DomibusConfigurationService domibusConfigurationService;

    private final DomibusExtMapper domibusExtMapper;

    public TLSTruststoreServiceDelegate(TLSReaderService tlsReaderService, SignalService signalService, DomainContextProvider domainProvider, CertificateService certificateService,
                                        MultiPartFileUtil multiPartFileUtil, DomibusConfigurationService domibusConfigurationService, DomibusExtMapper domibusExtMapper) {
        this.tlsReaderService = tlsReaderService;
        this.signalService = signalService;
        this.domainProvider = domainProvider;
        this.certificateService = certificateService;
        this.multiPartFileUtil = multiPartFileUtil;
        this.domibusConfigurationService = domibusConfigurationService;
        this.domibusExtMapper = domibusExtMapper;
    }

    @Override
    public byte[] downloadTLSTruststoreContent() {
        TrustStoreContentDTO content;
        try {
            content = certificateService.getStoreContent(TLS_TRUSTSTORE_NAME);
            return content.getContent();
        } catch (Exception e) {
            throw new TruststoreExtException("Could not find truststore entity with name: " + TLS_TRUSTSTORE_NAME);
        }

    }

    @Override
    public List<TrustStoreDTO> getTLSTrustStoreEntries() {
        String errorMessage = "Could not find or read the client authentication file.";
        try {
            if (domibusConfigurationService.isMultiTenantAware()) {
                final String domainName = domainProvider.getCurrentDomain().getName();
                errorMessage = "Could not find or read the client authentication file for domain [" + domainName + "]";
            }
            List<TrustStoreEntry> trustStoreEntries = certificateService.getStoreEntries(TLS_TRUSTSTORE_NAME);
            return domibusExtMapper.trustStoreEntriesToTrustStoresDTO(trustStoreEntries);
        } catch (Exception ex) {
            throw new TruststoreExtException(errorMessage);
        }
    }

    @Override
    public void uploadTLSTruststoreFile(MultipartFile truststoreFile, String password) {
        byte[] truststoreFileContent = multiPartFileUtil.validateAndGetFileContent(truststoreFile);
        certificateService.replaceStore(truststoreFile.getOriginalFilename(), truststoreFileContent, password, TLS_TRUSTSTORE_NAME);
        resetTLSTruststore();
    }

    protected void resetTLSTruststore() {
        Domain domain = domainProvider.getCurrentDomain();
        String domainCode = domain != null ? domain.getCode() : null;
        tlsReaderService.reset(domainCode);
        signalService.signalTLSTrustStoreUpdate(domain);
    }

    public void addTLSCertificate(MultipartFile certificateFile, String alias) throws RequestValidationException {
        if (StringUtils.isBlank(alias)) {
            throw new RequestValidationException("Please provide an alias for the certificate.");
        }

        byte[] fileContent = multiPartFileUtil.validateAndGetFileContent(certificateFile);

        certificateService.addCertificate(TLS_TRUSTSTORE_NAME, fileContent, alias, true);
    }

    public void removeTLSCertificate(String alias) throws RequestValidationException {
        certificateService.removeCertificate(TLS_TRUSTSTORE_NAME, alias);
    }
}

