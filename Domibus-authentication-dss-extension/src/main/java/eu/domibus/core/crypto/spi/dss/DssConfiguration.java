package eu.domibus.core.crypto.spi.dss;

import eu.domibus.core.crypto.spi.DomainCryptoServiceSpi;
import eu.domibus.core.crypto.spi.dss.listeners.CertificateVerifierListener;
import eu.domibus.core.crypto.spi.dss.listeners.NetworkConfigurationListener;
import eu.domibus.core.crypto.spi.dss.listeners.TriggerChangeListener;
import eu.domibus.ext.services.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.europa.esig.dss.service.crl.OnlineCRLSource;
import eu.europa.esig.dss.spi.client.http.DataLoader;
import eu.europa.esig.dss.spi.tsl.TrustedListsCertificateSource;
import eu.europa.esig.dss.spi.x509.KeyStoreCertificateSource;
import eu.europa.esig.dss.tsl.OtherTrustedList;
import eu.europa.esig.dss.tsl.service.DomibusTSLRepository;
import eu.europa.esig.dss.tsl.service.DomibusTSLValidationJob;
import eu.europa.esig.dss.tsl.service.TSLRepository;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import net.sf.ehcache.Cache;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;


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

    @Bean
    public TrustedListsCertificateSource trustedListSource() {
        return new TrustedListsCertificateSource();
    }

    @Bean
    public IgnorePivotFilenameFilter ignorePivotFilenameFilter() {
        return new IgnorePivotFilenameFilter();
    }

    @Bean
    public DomibusTSLRepository tslRepository(TrustedListsCertificateSource trustedListSource,
                                              @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") ServerInfoExtService serverInfoExtService,
                                              IgnorePivotFilenameFilter ignorePivotFilenameFilter) {
        LOG.debug("Dss trusted list cache path:[{}]", dssCachePath);
        String nodeName = serverInfoExtService.getNodeName();
        String serverCacheDirectoryName = getCacheDirectoryName(dssCachePath, nodeName);
        Path dssPerNodePath = Paths.get(serverCacheDirectoryName);
        if (!dssPerNodePath.toFile().exists()) {
            try {
                LOG.debug("Cache directory does not exists, creating path:[{}]", dssCachePath);
                Files.createDirectories(dssPerNodePath);
            } catch (IOException e) {
                LOG.error("Error create dss cache path:[{}], impossible to configure DSS correctly", dssPerNodePath.toAbsolutePath(), e);
            }
        }
        DomibusTSLRepository tslRepository = new DomibusTSLRepository(ignorePivotFilenameFilter);
        tslRepository.setTrustedListsCertificateSource(trustedListSource);
        LOG.debug("Dss configure with cache path:[{}] for server:[{}]", serverCacheDirectoryName, nodeName);
        tslRepository.setCacheDirectoryPath(serverCacheDirectoryName);
        return tslRepository;
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
        CommonCertificateVerifier certificateVerifier = new CommonCertificateVerifier(trustedListSource(), crlSource, null, dataLoader);
        certificateVerifier.setExceptionOnMissingRevocationData(enableExceptionOnMissingRevocationData);
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
        DomibusDataLoader dataLoader = new DomibusDataLoader(dssExtensionPropertyManager());
        dataLoader.setProxyConfig(proxyHelper.getProxyConfig());
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
    public DomibusTSLValidationJob tslValidationJob(
            DataLoader dataLoader,
            DomibusTSLRepository tslRepository,
            KeyStoreCertificateSource ojContentKeyStore,
            DssExtensionPropertyManager dssExtensionPropertyManager,
            CertificateVerifierService certificateVerifierService) {
        String currentLotlUrl = dssExtensionPropertyManager.getKnownPropertyValue(DssExtensionPropertyManager.AUTHENTICATION_DSS_CURRENT_LOTL_URL);
        String currentOjUrl = dssExtensionPropertyManager.getKnownPropertyValue(DssExtensionPropertyManager.AUTHENTICATION_DSS_CURRENT_OFFICIAL_JOURNAL_URL);
        String lotlCountryCode = dssExtensionPropertyManager.getKnownPropertyValue(DssExtensionPropertyManager.AUTHENTICATION_DSS_LOTL_COUNTRY_CODE);
        LOG.info("Configuring DSS lotl with url:[{}],schema uri:[{}],country code:[{}],oj url:[{}]", currentLotlUrl, lotlSchemeUri, lotlCountryCode, currentOjUrl);
        DomibusTSLValidationJob validationJob = new DomibusTSLValidationJob(certificateVerifierService, otherTrustedListObjectProvider);
        validationJob.setDataLoader(dataLoader);
        validationJob.setRepository(tslRepository);
        validationJob.setLotlUrl(currentLotlUrl);
        validationJob.setLotlCode(lotlCountryCode);
        validationJob.setOjUrl(currentOjUrl);
        validationJob.setOjContentKeyStore(ojContentKeyStore);
        validationJob.setCheckLOTLSignature(true);
        validationJob.setCheckTSLSignatures(true);
        return validationJob;
    }

    @Bean
    public DssRefreshCommand dssRefreshCommand(DomibusTSLValidationJob domibusTSLValidationJob, DssExtensionPropertyManager dssExtensionPropertyManager) {
        return new DssRefreshCommand(domibusTSLValidationJob, dssExtensionPropertyManager);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public CustomTrustedLists otherTrustedLists() {
        final List<OtherTrustedList> otherTrustedLists = new CustomTrustedListPropertyMapper(domibusPropertyExtService).map();
        CustomTrustedLists customTrustedLists = checkMultiTenancy(otherTrustedLists);
        if (customTrustedLists != null) return customTrustedLists;
        for (OtherTrustedList otherTrustedList : otherTrustedLists) {
            LOG.info("Custom trusted list configured with url:[{}], code:[{}]", otherTrustedList.getUrl(), otherTrustedList.getCountryCode());
        }
        if (otherTrustedLists.isEmpty()) {
            LOG.info("No custom trusted list configured.");
        }
        return new CustomTrustedLists(otherTrustedLists);
    }


    private CustomTrustedLists checkMultiTenancy(List<OtherTrustedList> otherTrustedLists) {
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
                                                        final TSLRepository tslRepository,
                                                        final ValidationReport validationReport,
                                                        final ValidationConstraintPropertyMapper constraintMapper,
                                                        final CertificateVerifierService certificateVerifierService,
                                                        @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") final PkiExtService pkiExtService,
                                                        final DssCache dssCache) {
        //needed to initialize WSS4J property bundles to have correct message in the WSSException.
        WSSConfig.init();
        return new DomibusDssCryptoSpi(
                defaultDomainCryptoService,
                tslRepository,
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
        return new DssCache((Cache) cache.getNativeCache());
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


}
