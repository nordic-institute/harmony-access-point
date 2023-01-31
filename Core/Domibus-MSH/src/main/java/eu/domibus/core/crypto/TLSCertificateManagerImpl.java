package eu.domibus.core.crypto;

import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.crypto.TLSCertificateManager;
import eu.domibus.api.cxf.TLSReaderService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.pki.KeyStoreContentInfo;
import eu.domibus.api.pki.KeystorePersistenceInfo;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.configuration.security.KeyStoreType;
import org.apache.cxf.configuration.security.TLSClientParametersType;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
public class TLSCertificateManagerImpl implements TLSCertificateManager {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TLSCertificateManagerImpl.class);

    private final TLSReaderService tlsReaderService;

    private final CertificateService certificateService;

    private final DomainContextProvider domainProvider;

    private final SignalService signalService;

    private final DomibusConfigurationService domibusConfigurationService;

    protected final DomainService domainService;

//    protected final AuditService auditService;

    public TLSCertificateManagerImpl(TLSReaderService tlsReaderService,
                                     CertificateService certificateService,
                                     DomainContextProvider domainProvider,
                                     SignalService signalService,
                                     DomibusConfigurationService domibusConfigurationService,
                                     DomainService domainService
//                                     AuditService auditService
    ) {
        this.tlsReaderService = tlsReaderService;
        this.certificateService = certificateService;
        this.domainProvider = domainProvider;
        this.signalService = signalService;
        this.domibusConfigurationService = domibusConfigurationService;
        this.domainService = domainService;
//        this.auditService = auditService;
    }

    @Override
    public synchronized void replaceTrustStore(String fileName, byte[] fileContent, String filePassword) throws CryptoException {
        certificateService.replaceStore(fileName, fileContent, filePassword, new KeystorePersistenceInfoImpl());
        resetTLSTruststore();

//        auditService.addTLSTruststoreUploadedAudit(TLS_TRUSTSTORE_NAME);
    }

    @Override
    public void replaceTrustStore(KeyStoreContentInfo storeInfo) {

    }

    @Override
    public List<TrustStoreEntry> getTrustStoreEntries() {
        String errorMessage = "Could not find or read the client authentication file.";
        try {
            if (domibusConfigurationService.isMultiTenantAware()) {
                final String domainName = domainProvider.getCurrentDomain().getName();
                errorMessage = "Could not find or read the client authentication file for domain [" + domainName + "]";
            }
            return certificateService.getStoreEntries(new KeystorePersistenceInfoImpl());
        } catch (ConfigurationException ex) {
            throw new ConfigurationException(errorMessage, ex);
        }
    }

    @Override
    public KeyStoreContentInfo getTruststoreContent() {
        return certificateService.getStoreContent(new KeystorePersistenceInfoImpl());
    }

    @Override
    public synchronized boolean addCertificate(byte[] certificateData, String alias) {
        KeystorePersistenceInfo persistenceInfo = new KeystorePersistenceInfoImpl();
        boolean added = certificateService.addCertificate(persistenceInfo, certificateData, alias, true);
        if (added) {
            LOG.debug("Added certificate [{}] to the tls truststore; resetting it.", alias);
            resetTLSTruststore();
        }

//        auditService.addCertificateAddedAudit(TLS_TRUSTSTORE_NAME);

        return added;
    }

    @Override
    public synchronized boolean removeCertificate(String alias) {
        KeystorePersistenceInfo persistenceInfo = new KeystorePersistenceInfoImpl();
        boolean removed = certificateService.removeCertificate(persistenceInfo, alias);
        if (removed) {
            LOG.debug("Removed certificate [{}] from the tls truststore; resetting it.", alias);
            resetTLSTruststore();
        }

//        auditService.addCertificateRemovedAudit(TLS_TRUSTSTORE_NAME);

        return removed;
    }

    @Override
    public void saveStoresFromDBToDisk() {
        final List<Domain> domains = domainService.getDomains();
        persistStores(domains);
    }

    @Override
    public void onDomainAdded(final Domain domain) {
        persistStores(Arrays.asList(domain));
    }

    @Override
    public void onDomainRemoved(Domain domain) {
    }

    private void persistStores(List<Domain> domains) {
        certificateService.saveStoresFromDBToDisk(new KeystorePersistenceInfoImpl(), domains);
    }

    class KeystorePersistenceInfoImpl implements KeystorePersistenceInfo {

        @Override
        public String getName() {
            return TLS_TRUSTSTORE_NAME;
        }

        @Override
        public String getFileLocation() {
            Optional<KeyStoreType> params = getTruststoreParams();
            return params.map(KeyStoreType::getFile).orElse(null);
        }

        @Override
        public boolean isOptional() {
            return true;
        }

        @Override
        public String getType() {
            Optional<KeyStoreType> params = getTruststoreParams();
            return params.map(KeyStoreType::getType).orElse(null);
        }

        @Override
        public String getPassword() {
            Optional<KeyStoreType> params = getTruststoreParams();
            return params.map(KeyStoreType::getPassword).orElse(null);
        }

        @Override
        public void updateTypeAndFileLocation(String type,String fileLocation) {
            setTlsTrustStoreTypeAndFileLocation(type, fileLocation);
        }
    }

    void setTlsTrustStoreTypeAndFileLocation(String type, String fileLocation) {
        final String domainCode = getDomainCode();
        tlsReaderService.updateTlsTrustStoreConfiguration(domainCode, type,fileLocation);
    }

    protected Optional<KeyStoreType> getTruststoreParams() {
        final String domainCode = getDomainCode();
        Optional<TLSClientParametersType> tlsParams = tlsReaderService.getTlsTrustStoreConfiguration(domainCode);
        return tlsParams.map(params -> {
            KeyStoreType result = params.getTrustManagers().getKeyStore();
            LOG.debug("TLS parameters for domain [{}] are [{}]", domainCode, result);
            return Optional.of(result);
        }).orElseGet(() -> {
            LOG.info("TLS parameters for domain [{}] could not be read.", domainCode);
            return Optional.empty();
        });
    }

    protected void resetTLSTruststore() {
        Domain domain = domainProvider.getCurrentDomain();
        String domainCode = domain != null ? domain.getCode() : null;
        tlsReaderService.reset(domainCode);
        signalService.signalTLSTrustStoreUpdate(domain);
    }

    private String getDomainCode() {
        final String domainCode;
        if (domibusConfigurationService.isSingleTenantAware()) {
            domainCode = null;
        } else {
            Domain domain = domainProvider.getCurrentDomain();
            domainCode = domain != null ? domain.getCode() : null;
        }
        return domainCode;
    }
}
