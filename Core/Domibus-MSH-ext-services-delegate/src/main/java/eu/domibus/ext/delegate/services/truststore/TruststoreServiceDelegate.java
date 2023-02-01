package eu.domibus.ext.delegate.services.truststore;

import eu.domibus.api.exceptions.RequestValidationException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.pki.KeyStoreContentInfo;
import eu.domibus.api.pki.MultiDomainCryptoService;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.ext.delegate.mapper.DomibusExtMapper;
import eu.domibus.ext.domain.KeyStoreContentInfoDTO;
import eu.domibus.ext.domain.TrustStoreDTO;
import eu.domibus.ext.services.TruststoreExtService;
import org.springframework.stereotype.Service;

import java.security.cert.X509Certificate;
import java.util.List;

/**
 * @author Soumya Chandran
 * @since 5.1
 */
@Service
public class TruststoreServiceDelegate implements TruststoreExtService {

    private final MultiDomainCryptoService multiDomainCertificateProvider;

    private final DomainContextProvider domainProvider;

    private final CertificateService certificateService;

    private final DomibusExtMapper domibusExtMapper;


    public TruststoreServiceDelegate(MultiDomainCryptoService multiDomainCertificateProvider,
                                     DomainContextProvider domainProvider, CertificateService certificateService,
                                     DomibusExtMapper domibusExtMapper) {
        this.multiDomainCertificateProvider = multiDomainCertificateProvider;
        this.domainProvider = domainProvider;
        this.certificateService = certificateService;
        this.domibusExtMapper = domibusExtMapper;
    }


    @Override
    public KeyStoreContentInfoDTO downloadTruststoreContent() {
        KeyStoreContentInfo content = multiDomainCertificateProvider.getTrustStoreContent(domainProvider.getCurrentDomain());
        return domibusExtMapper.keyStoreContentInfoToKeyStoreContentInfoDTO(content);
    }

    @Override
    public List<TrustStoreDTO> getTrustStoreEntries() {
        List<TrustStoreEntry> trustStoreEntries = multiDomainCertificateProvider.getTrustStoreEntries(domainProvider.getCurrentDomain());
        return domibusExtMapper.trustStoreEntriesToTrustStoresDTO(trustStoreEntries);
    }

    @Override
    public void uploadTruststoreFile(KeyStoreContentInfoDTO contentInfoDTO) {
        Domain currentDomain = domainProvider.getCurrentDomain();
        KeyStoreContentInfo storeContentInfo = domibusExtMapper.keyStoreContentInfoDTOToKeyStoreContentInfo(contentInfoDTO);
        multiDomainCertificateProvider.replaceTrustStore(currentDomain, storeContentInfo);
    }

    @Override
    public boolean addCertificate(byte[] certificateFile, String alias) throws RequestValidationException {
        Domain currentDomain = domainProvider.getCurrentDomain();
        X509Certificate cert = certificateService.loadCertificate(certificateFile);
        return multiDomainCertificateProvider.addCertificate(currentDomain, cert, alias, true);
    }

    @Override
    public boolean removeCertificate(String alias) throws RequestValidationException {
        Domain currentDomain = domainProvider.getCurrentDomain();
        return multiDomainCertificateProvider.removeCertificate(currentDomain, alias);
    }

    @Override
    public String getStoreFileExtension() {
        return multiDomainCertificateProvider.getTrustStoreFileExtension();
    }
}

