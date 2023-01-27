package eu.domibus.ext.delegate.services.truststore;

import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.crypto.KeyStoreContentDTO;
import eu.domibus.api.crypto.TLSCertificateManager;
import eu.domibus.api.cxf.TLSReaderService;
import eu.domibus.api.exceptions.RequestValidationException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.pki.KeyStoreInfo;
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

import static eu.domibus.api.crypto.TLSCertificateManager.TLS_TRUSTSTORE_NAME;

/**
 * @author Soumya Chandran
 * @since 5.1
 */
@Service
public class TLSTruststoreServiceDelegate implements TLSTruststoreExtService {

    private final TLSCertificateManager tlsCertificateManager;

    private final DomibusExtMapper domibusExtMapper;

    public TLSTruststoreServiceDelegate(TLSCertificateManager tlsCertificateManager, DomibusExtMapper domibusExtMapper) {
        this.tlsCertificateManager = tlsCertificateManager;
        this.domibusExtMapper = domibusExtMapper;
    }

    @Override
    public byte[] downloadTruststoreContent() {
        KeyStoreInfo content;
        try {
            content = tlsCertificateManager.getTruststoreContent();
            return content.getContent();
        } catch (Exception e) {
            throw new TruststoreExtException(e);
        }
    }

    @Override
    public List<TrustStoreDTO> getTrustStoreEntries() {
        try {
            List<TrustStoreEntry> trustStoreEntries = tlsCertificateManager.getTrustStoreEntries();
            return domibusExtMapper.trustStoreEntriesToTrustStoresDTO(trustStoreEntries);
        } catch (Exception ex) {
            throw new TruststoreExtException(ex);
        }
    }

    @Override
    public void uploadTruststoreFile(byte[] truststoreFileContent, String fileName, String password) {
        tlsCertificateManager.replaceTrustStore(fileName, truststoreFileContent, password);
    }

    @Override
    public void addCertificate(byte[] fileContent, String alias) {
        tlsCertificateManager.addCertificate(fileContent, alias);
    }

    @Override
    public void removeCertificate(String alias) {
        tlsCertificateManager.removeCertificate(alias);
    }

}

