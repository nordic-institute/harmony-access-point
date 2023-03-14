package eu.domibus.core.crypto;

import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.crypto.NoKeyStoreContentInformationException;
import eu.domibus.api.crypto.SameResourceCryptoException;
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
import eu.domibus.core.certificate.CertificateHelper;
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

    KeystorePersistenceInfo persistenceInfo = new KeystorePersistenceInfoImpl();

    private final TLSReaderService tlsReaderService;

    private final CertificateService certificateService;

    private final DomainContextProvider domainProvider;

    private final SignalService signalService;

    private final DomibusConfigurationService domibusConfigurationService;

    protected final DomainService domainService;

    protected final CertificateHelper certificateHelper;

    public TLSCertificateManagerImpl(TLSReaderService tlsReaderService,
                                     CertificateService certificateService,
                                     DomainContextProvider domainProvider,
                                     SignalService signalService,
                                     DomibusConfigurationService domibusConfigurationService,
                                     DomainService domainService, CertificateHelper certificateHelper) {
        this.tlsReaderService = tlsReaderService;
        this.certificateService = certificateService;
        this.domainProvider = domainProvider;
        this.signalService = signalService;
        this.domibusConfigurationService = domibusConfigurationService;
        this.domainService = domainService;
        this.certificateHelper = certificateHelper;
    }

    @Override
    public synchronized void replaceTrustStore(KeyStoreContentInfo contentInfo) {
        String storeName = contentInfo.getName();
        String storeFileName = contentInfo.getFileName();
        boolean replaced;
        try {
            replaced = certificateService.replaceStore(contentInfo, getPersistenceInfo());
        } catch (CryptoException ex) {
            throw new CryptoException(String.format("Error while replacing the store [%s] with content of the file named [%s].", storeName, storeFileName), ex);
        }
        if (!replaced) {
            throw new SameResourceCryptoException(storeName, storeFileName,
                    String.format("Current store [%s] was not replaced with the content of the file [%s] because they are identical.",
                            storeName, storeFileName));
        }
        resetTLSTruststore();
    }

    @Override
    public List<TrustStoreEntry> getTrustStoreEntries() {
        String errorMessage = "Could not find or read the client authentication file.";
        try {
            if (domibusConfigurationService.isMultiTenantAware()) {
                final String domainName = domainProvider.getCurrentDomain().getName();
                errorMessage = "Could not find or read the client authentication file for domain [" + domainName + "]";
            }
            return certificateService.getStoreEntries(getPersistenceInfo());
        } catch (ConfigurationException | NoKeyStoreContentInformationException ex) {
            throw new ConfigurationException(errorMessage, ex);
        }
    }

    @Override
    public KeyStoreContentInfo getTruststoreContent() {
        return certificateService.getStoreContent(getPersistenceInfo());
    }

    @Override
    public synchronized boolean addCertificate(byte[] certificateData, String alias) {
        boolean added = certificateService.addCertificate(getPersistenceInfo(), certificateData, alias, true);
        if (added) {
            LOG.debug("Added certificate [{}] to the tls truststore; resetting it.", alias);
            resetTLSTruststore();
        }

        return added;
    }

    @Override
    public synchronized boolean removeCertificate(String alias) {
        boolean removed = certificateService.removeCertificate(getPersistenceInfo(), alias);
        if (removed) {
            LOG.debug("Removed certificate [{}] from the tls truststore; resetting it.", alias);
            resetTLSTruststore();
        }

        return removed;
    }

    @Override
    public void saveStoresFromDBToDisk() {
        final List<Domain> domains = domainService.getDomains();
        persistStores(domains);
    }

    @Override
    public KeystorePersistenceInfo getPersistenceInfo() {
        return persistenceInfo;
    }

    @Override
    public String getStoreFileExtension() {
        return certificateHelper.getStoreFileExtension(getPersistenceInfo().getType());
    }

    @Override
    public void onDomainAdded(final Domain domain) {
        persistStores(Arrays.asList(domain));
    }

    @Override
    public void onDomainRemoved(Domain domain) {
    }

    private void persistStores(List<Domain> domains) {
        certificateService.saveStoresFromDBToDisk(getPersistenceInfo(), domains);
    }

    void setTlsTrustStoreTypeAndFileLocation(String type, String fileLocation) {
        final String domainCode = getDomainCode();
        tlsReaderService.updateTlsTrustStoreConfiguration(domainCode, type, fileLocation);
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
        public void updateTypeAndFileLocation(String type, String fileLocation) {
            setTlsTrustStoreTypeAndFileLocation(type, fileLocation);
        }
    }
}
