package eu.domibus.core.crypto;

import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.cxf.TLSReaderService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.core.crypto.api.TLSCertificateManager;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.configuration.security.KeyStoreType;
import org.apache.cxf.configuration.security.TLSClientParametersType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public TLSCertificateManagerImpl(TLSReaderService tlsReaderService, CertificateService certificateService,
                                     DomainContextProvider domainProvider, SignalService signalService) {

        this.tlsReaderService = tlsReaderService;
        this.certificateService = certificateService;
        this.domainProvider = domainProvider;
        this.signalService = signalService;
    }

    @Override
    public synchronized void replaceTrustStore(String fileName, byte[] fileContent, String filePassword, String backupLocation) throws CryptoException {
        KeyStoreType params = getTruststoreParams();

        certificateService.replaceTrustStore(fileName, fileContent, filePassword, params.getType(), TLS_TRUSTSTORE_NAME, params.getPassword(), backupLocation);

        resetTLSTruststore();
    }

    @Override
    public List<TrustStoreEntry> getTrustStoreEntries() {
        KeyStoreType params = getTruststoreParams();
        return certificateService.getTrustStoreEntries(TLS_TRUSTSTORE_NAME, params.getPassword(), params.getType());
    }

    @Override
    public byte[] getTruststoreContent() {
        return certificateService.getTruststoreContent(TLS_TRUSTSTORE_NAME);
    }

    @Override
    public byte[] getTruststoreContentFromFile() {
        KeyStoreType params = getTruststoreParams();
        return certificateService.getTruststoreContentFromFile(params.getFile());
    }

    @Override
    public synchronized boolean addCertificate(byte[] certificateData, String alias, String backupLocation) {
        KeyStoreType params = getTruststoreParams();
        boolean added = certificateService.addCertificate(params.getPassword(), TLS_TRUSTSTORE_NAME, params.getType(), certificateData, alias, true, backupLocation);
        if (added) {
            LOG.debug("Added certificate [{}] to the tls truststore; reseting it.", alias);
            resetTLSTruststore();
        }
        return added;
    }

    @Override
    public synchronized boolean removeCertificate(String alias, String backupLocation) {
        KeyStoreType params = getTruststoreParams();
        boolean deleted = certificateService.removeCertificate(params.getPassword(), TLS_TRUSTSTORE_NAME, params.getType(), alias, backupLocation);
        if (deleted) {
            LOG.debug("Removed certificate [{}] from the tls truststore; reseting it.", alias);
            resetTLSTruststore();
        }
        return deleted;
    }

    final static String TLS_TRUSTSTORE_NAME = "TLS.truststore";

    @Override
    public void persistTruststoresIfApplicable() {
        certificateService.persistTruststoresIfApplicable(TLS_TRUSTSTORE_NAME, () -> getTruststoreParams().getFile());
    }

    protected KeyStoreType getTruststoreParams() {
        Domain domain = domainProvider.getCurrentDomain();
        String domainCode = domain != null ? domain.getCode() : null;
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
