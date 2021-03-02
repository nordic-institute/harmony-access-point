package eu.domibus.core.crypto.spi.dss;

import eu.domibus.core.crypto.spi.DomainCryptoServiceSpi;
import eu.domibus.core.crypto.spi.dss.listeners.CertificateVerifierListener;
import eu.domibus.core.crypto.spi.dss.listeners.NetworkConfigurationListener;
import eu.domibus.core.crypto.spi.dss.listeners.TriggerChangeListener;
import eu.domibus.ext.services.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.europa.esig.dss.alert.ExceptionOnStatusAlert;
import eu.europa.esig.dss.service.crl.OnlineCRLSource;
import eu.europa.esig.dss.service.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.service.http.commons.FileCacheDataLoader;
import eu.europa.esig.dss.service.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.spi.client.http.DSSFileLoader;
import eu.europa.esig.dss.spi.client.http.IgnoreDataLoader;
import eu.europa.esig.dss.spi.tsl.TrustedListsCertificateSource;
import eu.europa.esig.dss.spi.x509.CertificateSource;
import eu.europa.esig.dss.spi.x509.KeyStoreCertificateSource;

import eu.europa.esig.dss.tsl.alerts.LOTLAlert;
import eu.europa.esig.dss.tsl.alerts.TLAlert;
import eu.europa.esig.dss.tsl.alerts.detections.LOTLLocationChangeDetection;
import eu.europa.esig.dss.tsl.alerts.detections.OJUrlChangeDetection;
import eu.europa.esig.dss.tsl.alerts.detections.TLExpirationDetection;
import eu.europa.esig.dss.tsl.alerts.detections.TLSignatureErrorDetection;
import eu.europa.esig.dss.tsl.alerts.handlers.log.LogLOTLLocationChangeAlertHandler;
import eu.europa.esig.dss.tsl.alerts.handlers.log.LogOJUrlChangeAlertHandler;
import eu.europa.esig.dss.tsl.alerts.handlers.log.LogTLExpirationAlertHandler;
import eu.europa.esig.dss.tsl.alerts.handlers.log.LogTLSignatureErrorAlertHandler;
import eu.europa.esig.dss.tsl.cache.CacheCleaner;
import eu.europa.esig.dss.tsl.function.OfficialJournalSchemeInformationURI;
import eu.europa.esig.dss.tsl.job.TLValidationJob;
import eu.europa.esig.dss.tsl.source.LOTLSource;
import eu.europa.esig.dss.tsl.source.TLSource;
import eu.europa.esig.dss.tsl.sync.AcceptAllStrategy;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import org.apache.commons.lang3.StringUtils;
import org.apache.wss4j.dom.engine.WSSConfig;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import static java.util.Arrays.*;


/**
 * @author Thomas Dussart
 * @since 4.1
 * <p>
 * Load dss beans.
 */
@Configuration
@PropertySource(value = "classpath:authentication-dss-extension-default.properties")
@PropertySource(ignoreResourceNotFound = true, value = "file:${domibus.config.location}/extensions/config/authentication-dss-extension.properties")
public class DssConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DssConfiguration.class);

    private static final String DOMIBUS_AUTHENTICATION_DSS_ENABLE_CUSTOM_TRUSTED_LIST_FOR_MULTITENANT = "domibus.authentication.dss.enable.custom.trusted.list.for.multitenant";

    private final static String CACERT_PATH = "/lib/security/cacerts";

    private TrustedListsCertificateSource tslCertificateSource;

    @Value("${domibus.authentication.dss.official.journal.content.keystore.type}")
    private String keystoreType;

    @Value("${domibus.authentication.dss.official.journal.content.keystore.path}")
    private String keystorePath;

    @Value("${domibus.authentication.dss.official.journal.content.keystore.password}")
    private String keystorePassword;

    @Value("${domibus.authentication.dss.lotl.root.scheme.info.uri}")
    private String lotlSchemeUri;

    @Value("${domibus.authentication.dss.cache.path}")
    private String dssCachePath;

    @Value("${" + DOMIBUS_AUTHENTICATION_DSS_ENABLE_CUSTOM_TRUSTED_LIST_FOR_MULTITENANT + "}")
    private boolean enableDssCustomTrustedListForMultiTenant;

    @Value("${domibus.authentication.dss.cache.name}")
    private String cacheName;

    @Value("${domibus.dss.ssl.trust.store.path}")
    private String dssTlsTrustStorePath;

    @Value("${domibus.dss.ssl.trust.store.type}")
    private String dssTlsTrustStoreType;

    @Value("${domibus.dss.ssl.trust.store.password}")
    private String dssTlsTrustStorePassword;

    @Value("${domibus.dss.ssl.cacert.path}")
    private String cacertPath;

    @Value("${domibus.dss.ssl.cacert.type}")
    private String cacertType;

    @Value("${domibus.dss.ssl.cacert.password}")
    private String cacertPassword;

    @Autowired
    private DomibusPropertyExtService domibusPropertyExtService;

    @Autowired
    private DomibusConfigurationExtService domibusConfigurationExtService;

    @Autowired
    private ObjectProvider<CustomTrustedLists> otherTrustedListObjectProvider;

    @Autowired
    protected ObjectProvider<CertificateVerifier> certificateVerifierObjectProvider;

    @Autowired
    private ServerInfoExtService serverInfoExtService;

    @Bean
    public TrustedListsCertificateSource trustedListSource() {
        return new TrustedListsCertificateSource();
    }

    @Bean
    public IgnorePivotFilenameFilter ignorePivotFilenameFilter() {
        return new IgnorePivotFilenameFilter();
    }


    private String getCacheDirectoryName(String dssCachePath, String nodeName) {
        return dssCachePath + File.separator + nodeName + File.separator;
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public CertificateVerifier certificateVerifier() {
        OnlineCRLSource crlSource = null;
        DomibusDataLoader dataLoader = dataLoader(proxyHelper(dssExtensionPropertyManager()));
        boolean crlCheck = Boolean.parseBoolean(dssExtensionPropertyManager().getKnownPropertyValue(DssExtensionPropertyManager.DSS_PERFORM_CRL_CHECK));
        boolean enableExceptionOnMissingRevocationData = Boolean.parseBoolean(dssExtensionPropertyManager().getKnownPropertyValue(DssExtensionPropertyManager.AUTHENTICATION_DSS_EXCEPTION_ON_MISSING_REVOCATION_DATA));
        boolean checkRevocationForUntrustedChain = Boolean.parseBoolean(dssExtensionPropertyManager().getKnownPropertyValue(DssExtensionPropertyManager.AUTHENTICATION_DSS_CHECK_REVOCATION_FOR_UNTRUSTED_CHAINS));
        LOG.debug("New Certificate verifier instance with crl chek:[{}], exception on missing revocation:[{}], check revocation for untrusted chains:[{}]",
                crlCheck,
                enableExceptionOnMissingRevocationData,
                checkRevocationForUntrustedChain);
        if (crlCheck) {
            crlSource = new OnlineCRLSource(dataLoader);
        }

        OnlineOCSPSource onlineOCSPSource = new OnlineOCSPSource();
        onlineOCSPSource.setDataLoader(dataLoader);
        CommonCertificateVerifier certificateVerifier = new CommonCertificateVerifier();
        certificateVerifier.setCrlSource(crlSource);
        certificateVerifier.setOcspSource(onlineOCSPSource);
        certificateVerifier.setDataLoader(dataLoader);
        certificateVerifier.setTrustedCertSources(trustedListSource());

        // Default configs
        certificateVerifier.setAlertOnMissingRevocationData(new ExceptionOnStatusAlert());
        certificateVerifier.setCheckRevocationForUntrustedChains(checkRevocationForUntrustedChain);
        LOG.debug("Instanciating new certificate verifier:[{}], enableExceptionOnMissingRevocationData:[{}], checkRevocationForUntrustedChain:[{}]", certificateVerifier, enableExceptionOnMissingRevocationData, checkRevocationForUntrustedChain);
        return certificateVerifier;
    }

    @Bean
    public CertificateVerifierService certificateVerifierService(DssCache dssCache) {
        return new CertificateVerifierService(dssCache, certificateVerifierObjectProvider);
    }

    @Bean
    public KeyStoreCertificateSource ojContentKeyStore() throws IOException {
        LOG.debug("Initializing DSS trust list trustStore with type:[{}], path:[{}]", keystoreType, keystorePath);
        return new KeyStoreCertificateSource(new File(keystorePath), keystoreType, keystorePassword);
    }

    @Bean
    public DomibusDataLoader dataLoader(ProxyHelper proxyHelper) {
        CommonsDataLoader commonsDataLoader = new CommonsDataLoader();
        commonsDataLoader.setProxyConfig(proxyHelper.getProxyConfig());
        DomibusDataLoader dataLoader = new DomibusDataLoader();
        dataLoader.setProxyConfig(proxyHelper.getProxyConfig());
        commonsDataLoader.setSslTruststore(mergeCustomTlsTrustStoreWithCacert());
        commonsDataLoader.setSslKeystore();
        dataLoader.setSslTrustStore(mergeCustomTlsTrustStoreWithCacert());
        return dataLoader;
    }

    protected KeyStore mergeCustomTlsTrustStoreWithCacert() {

        KeyStore customTlsTrustStore;
        try {
            customTlsTrustStore = KeyStore.getInstance(dssTlsTrustStoreType);
        } catch (KeyStoreException e) {
            LOG.error("Could not instantiate empty keystore DSS keystore of type:[{}]", dssTlsTrustStoreType);
            return null;
        }

        try (FileInputStream fileInputStream = new FileInputStream(dssTlsTrustStorePath)) {
            customTlsTrustStore.load(fileInputStream, dssTlsTrustStorePassword.toCharArray());
        } catch (IOException | NoSuchAlgorithmException | CertificateException e) {
            LOG.info("DSS TLS truststore file:[{}] could not be loaded", dssTlsTrustStorePath);
            LOG.debug("Error while loading DSS TLS truststore file:[{}]", dssTlsTrustStorePath, e);
            customTlsTrustStore = null;
        }
        try {
            KeyStore cacertTrustStore = loadCacertTrustStore();
            if (cacertTrustStore == null) {
                LOG.warn("Cacert truststore skipped for DSS TLS");
                return customTlsTrustStore;
            }
            if (customTlsTrustStore == null) {
                LOG.debug("Custom DSS TLS is based on cacert only.");
                return cacertTrustStore;
            }
            Enumeration enumeration = cacertTrustStore.aliases();
            while (enumeration.hasMoreElements()) {
                // Determine the current alias
                String alias = (String) enumeration.nextElement();
                LOG.debug("Retrieving certificate with alias:[{}] and add it to custom tls trustore.", alias);
                Certificate cert = cacertTrustStore.getCertificate(alias);
                customTlsTrustStore.setCertificateEntry(alias, cert);
            }
            return customTlsTrustStore;
        } catch (KeyStoreException e) {
            LOG.error("Exception occured while merging cacert and dss truststore", e);
            return customTlsTrustStore;
        }
    }

    protected KeyStore loadCacertTrustStore() {
        //from custom defined location.
        if (!StringUtils.isEmpty(cacertPath)) {
            LOG.debug("Loading cacert of type:[{}] from custom location:[{}]", cacertType, cacertPath);
            return loadKeystore(cacertPath, cacertType, cacertPassword);
        }

        String javaHome = getJavaHome();
        if (StringUtils.isEmpty(javaHome)) {
            LOG.warn("Java home environmnent variable not defined, skipping cacert for DSS TLS");
            return null;
        }
        //from default location.
        String filename = javaHome + CACERT_PATH.replace('/', File.separatorChar);
        LOG.debug("Loading cacert of type:[{}] from default location:[{}]", cacertType, cacertPath);
        return loadKeystore(filename, cacertType, cacertPassword);
    }

    protected String getJavaHome() {
        return System.getProperty("java.home");
    }

    protected KeyStore loadKeystore(String filename, String type, String password) {
        KeyStore cacertTrustStore;
        try (FileInputStream is = new FileInputStream(filename)) {
            cacertTrustStore = KeyStore.getInstance(type);
            cacertTrustStore.load(is, password.toCharArray());
            return cacertTrustStore;
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
            LOG.info("Cacert cannot be loaded from  path:[{}]", filename);
            LOG.debug("Error loading cacert file:[{}]", filename, e);
            return null;
        }
    }

    @Bean
    public DssRefreshCommand dssRefreshCommand(TLValidationJob job, DssExtensionPropertyManager dssExtensionPropertyManager,File cacheDirectory) {
        return new DssRefreshCommand(job, dssExtensionPropertyManager,cacheDirectory);
    }

   @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public CustomTrustedLists otherTrustedLists() {
        final List<TLSource> otherTrustedLists = new CustomTrustedListPropertyMapper(domibusPropertyExtService).map();
        CustomTrustedLists customTrustedLists = checkMultiTenancy(otherTrustedLists);
        if (customTrustedLists != null) return customTrustedLists;
        for (TLSource otherTrustedList : otherTrustedLists) {
            LOG.info("Custom trusted list configured with url:[{}]", otherTrustedList.getUrl());
        }
        if (otherTrustedLists.isEmpty()) {
            LOG.info("No custom trusted list configured.");
        }
        return new CustomTrustedLists(otherTrustedLists);
    }


    private CustomTrustedLists checkMultiTenancy(List<TLSource> otherTrustedLists) {
        final boolean multiTenant = domibusConfigurationExtService.isMultiTenantAware();
        if (multiTenant && !otherTrustedLists.isEmpty()) {
            if (enableDssCustomTrustedListForMultiTenant) {
                LOG.warn("Configured custom trusted lists are shared by all tenants.");
            } else {
                LOG.info("In multi-tenant configuration custom DSS trusted list are shared. Therefore they are deactivated by default. Please adapt property:[{}] to change that behavior", DOMIBUS_AUTHENTICATION_DSS_ENABLE_CUSTOM_TRUSTED_LIST_FOR_MULTITENANT);
                return new CustomTrustedLists(Collections.emptyList());
            }
        }
        return null;
    }

    @Bean
    public JobDetailFactoryBean dssRefreshJob() {
        JobDetailFactoryBean obj = new JobDetailFactoryBean();
        obj.setJobClass(DssRefreshWorker.class);
        obj.setDurability(true);
        return obj;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public CronTriggerFactoryBean dssRefreshTrigger() {
        CronTriggerFactoryBean obj = new CronTriggerFactoryBean();
        obj.setJobDetail(dssRefreshJob().getObject());
        String dssRefreshCronExpression = dssExtensionPropertyManager().getKnownPropertyValue(DssExtensionPropertyManager.AUTHENTICATION_DSS_REFRESH_CRON);
        obj.setCronExpression(dssRefreshCronExpression);
        LOG.debug("dssRefreshTrigger configured with cronExpression [{}]", dssRefreshCronExpression);
        obj.setStartDelay(20000);
        return obj;
    }

    @Bean
    public ValidationConstraintPropertyMapper contraints(@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") DomibusPropertyExtService domibusPropertyExtService,
                                                         @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") DomainContextExtService domainContextExtService, Environment environment) {
        return new ValidationConstraintPropertyMapper(domibusPropertyExtService);
    }

    @Bean
    public ValidationReport validationReport() {
        return new ValidationReport();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public DomibusDssCryptoSpi domibusDssCryptoProvider(final DomainCryptoServiceSpi defaultDomainCryptoService,
                                                        final ValidationReport validationReport,
                                                        final ValidationConstraintPropertyMapper constraintMapper,
                                                        final CertificateVerifierService certificateVerifierService,
                                                        @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") final PkiExtService pkiExtService,
                                                        final DssCache dssCache) {
        //needed to initialize WSS4J property bundles to have correct message in the WSSException.
        WSSConfig.init();
        return new DomibusDssCryptoSpi(
                defaultDomainCryptoService,
                validationReport,
                constraintMapper,
                pkiExtService,
                dssCache,
                certificateVerifierService);
    }

    @Bean
    public DssCache dssCache(@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") CacheManager cacheManager) {
        org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            throw new IllegalArgumentException(String.format("Cache named:[%s] not found, please configure it.", cacheName));
        }
        return new DssCache(cache);
    }

    @Bean
    public DssExtensionPropertyManager dssExtensionPropertyManager() {
        return new DssExtensionPropertyManager();
    }

    @Bean
    public ProxyHelper proxyHelper(final DssExtensionPropertyManager dssExtensionPropertyManager) {
        return new ProxyHelper(dssExtensionPropertyManager);
    }

    @Bean
    public NetworkConfigurationListener networkConfigurationListener(final DomibusDataLoader dataLoader, final ProxyHelper proxyHelper) {
        return new NetworkConfigurationListener(dataLoader, proxyHelper);
    }

    @Bean
    public CertificateVerifierListener certificateVerifierListener(final CertificateVerifierService certificateVerifierService) {
        return new CertificateVerifierListener(certificateVerifierService);
    }

    @Bean
    public TriggerChangeListener triggerChangeListener(@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") DomibusSchedulerExtService domibusSchedulerExtService) {
        return new TriggerChangeListener(domibusSchedulerExtService);
    }

    @Bean
    public TLValidationJob job(LOTLSource europeanLOTL,DomibusDataLoader dataLoader) {
        TLValidationJob job = new TLValidationJob();
        job.setOnlineDataLoader(onlineLoader(dataLoader));
        job.setOfflineDataLoader(offlineLoader());
        job.setTrustedListCertificateSource(new TrustedListsCertificateSource());
        job.setSynchronizationStrategy(new AcceptAllStrategy());
        job.setCacheCleaner(cacheCleaner());

        job.setListOfTrustedListSources(europeanLOTL);
        job.setTrustedListSources(otherTrustedLists().getOtherTrustedLists().toArray(new TLSource[0]));
        job.setLOTLAlerts(asList(ojUrlAlert(europeanLOTL), lotlLocationAlert(europeanLOTL)));
        job.setTLAlerts(asList(tlSigningAlert(), tlExpirationDetection()));
        return job;
    }

    public TLAlert tlSigningAlert() {
        TLSignatureErrorDetection signingDetection = new TLSignatureErrorDetection();
        LogTLSignatureErrorAlertHandler handler = new LogTLSignatureErrorAlertHandler();
        return new TLAlert(signingDetection, handler);
    }

    public TLAlert tlExpirationDetection() {
        TLExpirationDetection expirationDetection = new TLExpirationDetection();
        LogTLExpirationAlertHandler handler = new LogTLExpirationAlertHandler();
        return new TLAlert(expirationDetection, handler);
    }

    public LOTLAlert ojUrlAlert(LOTLSource source) {
        OJUrlChangeDetection ojUrlDetection = new OJUrlChangeDetection(source);
        LogOJUrlChangeAlertHandler handler = new LogOJUrlChangeAlertHandler();
        return new LOTLAlert(ojUrlDetection, handler);
    }

    public LOTLAlert lotlLocationAlert(LOTLSource source) {
        LOTLLocationChangeDetection lotlLocationDetection = new LOTLLocationChangeDetection(source);
        LogLOTLLocationChangeAlertHandler handler = new LogLOTLLocationChangeAlertHandler();
        return new LOTLAlert(lotlLocationDetection, handler);
    }

    private DSSFileLoader onlineLoader(DomibusDataLoader dataLoader) {
        FileCacheDataLoader onlineFileLoader = new FileCacheDataLoader();
        onlineFileLoader.setCacheExpirationTime(0);
        onlineFileLoader.setDataLoader(dataLoader);
        onlineFileLoader.setFileCacheDirectory(cacheDirectory());
        return onlineFileLoader;
    }

    private DSSFileLoader offlineLoader() {
        FileCacheDataLoader offlineFileLoader = new FileCacheDataLoader();
        offlineFileLoader.setCacheExpirationTime(Long.MAX_VALUE);
        offlineFileLoader.setDataLoader(new IgnoreDataLoader()); // do not download from Internet
        offlineFileLoader.setFileCacheDirectory(cacheDirectory());
        return offlineFileLoader;
    }

    @Bean
    public CacheCleaner cacheCleaner() {
        CacheCleaner cacheCleaner = new CacheCleaner();
        cacheCleaner.setCleanMemory(true);
        cacheCleaner.setCleanFileSystem(true);
        cacheCleaner.setDSSFileLoader(offlineLoader());
        return cacheCleaner;
    }

    @Bean
    public LOTLSource europeanLOTL(CertificateSource officialJournalContentKeyStore, DssExtensionPropertyManager dssExtensionPropertyManager) {
        String currentLotlUrl = dssExtensionPropertyManager.getKnownPropertyValue(DssExtensionPropertyManager.AUTHENTICATION_DSS_CURRENT_LOTL_URL);
        String currentOjUrl = dssExtensionPropertyManager.getKnownPropertyValue(DssExtensionPropertyManager.AUTHENTICATION_DSS_CURRENT_OFFICIAL_JOURNAL_URL);
        LOTLSource lotlSource = new LOTLSource();
        lotlSource.setUrl(currentLotlUrl);
        lotlSource.setCertificateSource(officialJournalContentKeyStore);
        lotlSource.setSigningCertificatesAnnouncementPredicate(new OfficialJournalSchemeInformationURI(currentOjUrl));
        lotlSource.setPivotSupport(true);
        return lotlSource;
    }

    @Bean
    public CertificateSource officialJournalContentKeyStore() throws IOException {
        LOG.debug("Initializing DSS trust list trustStore with type:[{}], path:[{}]", keystoreType, keystorePath);
        return new KeyStoreCertificateSource(new File(keystorePath), keystoreType, keystorePassword);
    }

    private FileCacheDataLoader getDSSFileLoader() {
        FileCacheDataLoader fileLoader = new FileCacheDataLoader();
        fileLoader.setCacheExpirationTime(0);
        fileLoader.setFileCacheDirectory(cacheDirectory());
        return fileLoader;
    }


    public TrustedListsCertificateSource getCertificateSources() {
        return tslCertificateSource;
    }

    @Bean
    public File cacheDirectory() {
        String nodeName = serverInfoExtService.getNodeName();
        return new File(dssCachePath + File.separator + nodeName + File.separator);
    }




}
