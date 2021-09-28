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
    public synchronized void replaceTrustStore(String fileName, byte[] fileContent, String filePassword, String trustStoreBackupLocation) throws CryptoException {
        KeyStoreType trustStore = getTruststoreParams();

        certificateService.replaceTrustStore(fileName, fileContent, filePassword,
                trustStore.getType(), trustStore.getFile(), trustStore.getPassword(), trustStoreBackupLocation);

        resetTLSTruststore();
    }

    @Override
    public List<TrustStoreEntry> getTrustStoreEntries() {
        KeyStoreType trustStore = getTruststoreParams();

        return certificateService.getTrustStoreEntries(trustStore.getFile(), trustStore.getPassword(), trustStore.getType());
    }

    @Override
    // todo schimbat sa ia din BD
    public byte[] getTruststoreContent() {
        KeyStoreType trustStore = getTruststoreParams();
        return certificateService.getTruststoreContentFromFile(trustStore.getFile());
    }

    @Override
    public byte[] getTruststoreContentFromFile() {
        KeyStoreType trustStore = getTruststoreParams();
        return certificateService.getTruststoreContentFromFile(trustStore.getFile());
    }

    @Override
    public synchronized boolean addCertificate(byte[] certificateData, String alias, String trustStoreBackupLocation) {
        KeyStoreType trustStore = getTruststoreParams();
        boolean added = certificateService.addCertificate(trustStore.getPassword(), trustStore.getFile(), trustStore.getType(), certificateData, alias, true, trustStoreBackupLocation);
        if (added) {
            LOG.debug("Added certificate [{}] to the tls truststore; reseting it.", alias);
            resetTLSTruststore();
        }
        return added;
    }

    @Override
    public synchronized boolean removeCertificate(String alias, String trustStoreBackupLocation) {
        KeyStoreType trustStore = getTruststoreParams();
        boolean deleted = certificateService.removeCertificate(trustStore.getPassword(), trustStore.getFile(), trustStore.getType(), alias, trustStoreBackupLocation);
        if (deleted) {
            LOG.debug("Removed certificate [{}] from the tls truststore; reseting it.", alias);
            resetTLSTruststore();
        }
        return deleted;
    }

    @Autowired
    protected DomainService domainService;

    @Autowired
    protected DomainTaskExecutor domainTaskExecutor;

    @Autowired
    TruststoreDao truststoreDao;

    @Override
    public void persistTruststoresIfNecessarry() {
        LOG.debug("Creating encryption key for all domains if not yet exists");

        final List<Domain> domains = domainService.getDomains();
        for (Domain domain : domains) {
            persistTruststoreIfNecessarry(domain);
        }

        LOG.debug("Finished creating encryption key for all domains if not yet exists");
    }

    private void persistTruststoreIfNecessarry(Domain domain) {
        domainTaskExecutor.submit(() -> persistCurrentDomainTruststoreIfNecessarry(), domain);
    }

    final static String trustType = "TLS";

    private void persistCurrentDomainTruststoreIfNecessarry() {
        if (truststoreDao.existsWithName(trustType)) {
            return;
        }
        // todo check if this method also loads the cert, in which case, find another approach
        byte[] content = getTruststoreContentFromFile();
        Truststore entity = new Truststore();
        entity.setType(trustType);
        entity.setContent(content);
        truststoreDao.create(entity);
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
