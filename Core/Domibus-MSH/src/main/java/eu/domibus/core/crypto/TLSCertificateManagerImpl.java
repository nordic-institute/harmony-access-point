package eu.domibus.core.crypto;

import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.cxf.TLSReaderService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.pki.KeyStoreInfo;
import eu.domibus.api.pki.KeystorePersistenceInfo;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.core.audit.AuditService;
import eu.domibus.api.crypto.TLSCertificateManager;
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

    protected final AuditService auditService;

    public TLSCertificateManagerImpl(TLSReaderService tlsReaderService,
                                     CertificateService certificateService,
                                     DomainContextProvider domainProvider,
                                     SignalService signalService,
                                     DomibusConfigurationService domibusConfigurationService,
                                     DomainService domainService,
                                     AuditService auditService) {
        this.tlsReaderService = tlsReaderService;
        this.certificateService = certificateService;
        this.domainProvider = domainProvider;
        this.signalService = signalService;
        this.domibusConfigurationService = domibusConfigurationService;
        this.domainService = domainService;
        this.auditService = auditService;
    }

    @Override
    public synchronized void replaceTrustStore(String fileName, byte[] fileContent, String filePassword) throws CryptoException {
        Long entityId = certificateService.replaceStore(fileName, fileContent, filePassword, TLS_TRUSTSTORE_NAME);
        resetTLSTruststore();

        auditService.addTLSTruststoreUploadedAudit(entityId != null ? entityId.toString() : "tlstruststore");
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
    public KeyStoreInfo getTruststoreContent() {
        return certificateService.getStoreContent(new KeystorePersistenceInfoImpl());
    }

    @Override
    public synchronized boolean addCertificate(byte[] certificateData, String alias) {
        Long entityId = certificateService.addCertificate(TLS_TRUSTSTORE_NAME, certificateData, alias, true);
        if (entityId != null) {
            LOG.debug("Added certificate [{}] to the tls truststore; resetting it.", alias);
            resetTLSTruststore();
        }

        auditService.addCertificateAddedAudit(entityId != null ? entityId.toString() : "tlstruststore");

        return entityId != null;
    }

    @Override
    public synchronized boolean removeCertificate(String alias) {
        Long entityId = certificateService.removeCertificate(TLS_TRUSTSTORE_NAME, alias);
        if (entityId != null) {
            LOG.debug("Removed certificate [{}] from the tls truststore; resetting it.", alias);
            resetTLSTruststore();
        }

        auditService.addCertificateRemovedAudit(entityId != null ? entityId.toString() : "tlstruststore");

        return entityId != null;
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
        removeTruststore(domain);
    }

    private void removeTruststore(Domain domain) {
        certificateService.removeStore(TLS_TRUSTSTORE_NAME, domain);
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
        public Optional<String> getFilePath() {
            Optional<KeyStoreType> params = getTruststoreParams();
            return params.map(KeyStoreType::getFile);
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
    }

    protected Optional<KeyStoreType> getTruststoreParams() {
        final String domainCode;
        if (domibusConfigurationService.isSingleTenantAware()) {
            domainCode = null;
        } else {
            Domain domain = domainProvider.getCurrentDomain();
            domainCode = domain != null ? domain.getCode() : null;
        }
        Optional<TLSClientParametersType> params = tlsReaderService.getTlsClientParametersType(domainCode);
        return params.map(k -> {
            KeyStoreType result = k.getTrustManagers().getKeyStore();
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

}
