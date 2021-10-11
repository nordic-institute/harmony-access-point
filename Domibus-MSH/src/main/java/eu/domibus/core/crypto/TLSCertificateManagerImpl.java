package eu.domibus.core.crypto;

import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.cxf.TLSReaderService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.core.crypto.api.TLSCertificateManager;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.configuration.security.KeyStoreType;
import org.apache.cxf.configuration.security.TLSClientParametersType;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
public class TLSCertificateManagerImpl implements TLSCertificateManager {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TLSCertificateManagerImpl.class);

    final static String TLS_TRUSTSTORE_NAME = "TLS.truststore";

    private final TLSReaderService tlsReaderService;

    private final CertificateService certificateService;

    private final DomainContextProvider domainProvider;

    private final SignalService signalService;

    private final DomibusConfigurationService domibusConfigurationService;

    public TLSCertificateManagerImpl(TLSReaderService tlsReaderService,
                                     CertificateService certificateService,
                                     DomainContextProvider domainProvider,
                                     SignalService signalService, DomibusConfigurationService domibusConfigurationService) {
        this.tlsReaderService = tlsReaderService;
        this.certificateService = certificateService;
        this.domainProvider = domainProvider;
        this.signalService = signalService;
        this.domibusConfigurationService = domibusConfigurationService;
    }

    @Override
    public synchronized void replaceTrustStore(String fileName, byte[] fileContent, String filePassword) throws CryptoException {
        certificateService.replaceTrustStore(fileName, fileContent, filePassword, TLS_TRUSTSTORE_NAME);
        resetTLSTruststore();
    }

    @Override
    public List<TrustStoreEntry> getTrustStoreEntries() {
        return certificateService.getTrustStoreEntries(TLS_TRUSTSTORE_NAME);
    }

    @Override
    public byte[] getTruststoreContent() {
        return certificateService.getTruststoreContent(TLS_TRUSTSTORE_NAME);
    }

    @Override
    public synchronized boolean addCertificate(byte[] certificateData, String alias) {
        boolean added = certificateService.addCertificate(TLS_TRUSTSTORE_NAME, certificateData, alias, true);
        if (added) {
            LOG.debug("Added certificate [{}] to the tls truststore; reseting it.", alias);
            resetTLSTruststore();
        }
        return added;
    }

    @Override
    public synchronized boolean removeCertificate(String alias) {
        boolean deleted = certificateService.removeCertificate(TLS_TRUSTSTORE_NAME, alias);
        if (deleted) {
            LOG.debug("Removed certificate [{}] from the tls truststore; reseting it.", alias);
            resetTLSTruststore();
        }
        return deleted;
    }

    @Override
    public void persistTruststoresIfApplicable() {
        certificateService.persistTruststoresIfApplicable(TLS_TRUSTSTORE_NAME,
                () -> getTruststoreParams().getFile(),
                () -> getTruststoreParams().getType(),
                () -> getTruststoreParams().getPassword()
        );
    }

    protected KeyStoreType getTruststoreParams() {
        String domainCode = null;
        if(domibusConfigurationService.isMultiTenantAware()) {
            Domain domain = domainProvider.getCurrentDomain();
            domainCode = domain != null ? domain.getCode() : null;
        }
        TLSClientParametersType params = tlsReaderService.getTlsClientParametersType(domainCode);
        KeyStoreType result = params.getTrustManagers().getKeyStore();
        LOG.debug("TLS parameters for domain [{}] are [{}]", domainCode, result);
        return result;
    }

    protected void resetTLSTruststore() {
        Domain domain = domainProvider.getCurrentDomain();
        String domainCode = domain != null ? domain.getCode() : null;
        tlsReaderService.reset(domainCode);
        signalService.signalTLSTrustStoreUpdate(domain);
    }

}
