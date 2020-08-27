package eu.domibus.core.crypto.spi.dss;

import eu.domibus.core.crypto.spi.DomainCryptoServiceSpi;
import eu.domibus.ext.services.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.europa.esig.dss.service.crl.OnlineCRLSource;
import eu.europa.esig.dss.service.http.proxy.ProxyConfig;
import eu.europa.esig.dss.service.http.proxy.ProxyProperties;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cglib.core.internal.Function;
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
import java.nio.file.DirectoryStream;
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
import java.util.Iterator;
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

    private static final String NONE = "NONE";

    private static final String DOMIBUS_AUTHENTICATION_DSS_ENABLE_CUSTOM_TRUSTED_LIST_FOR_MULTITENANT = "domibus.authentication.dss.enable.custom.trusted.list.for.multitenant";

    private final static String CACERT_PATH = "/lib/security/cacerts";

    @Value("${domibus.authentication.dss.official.journal.content.keystore.type}")
    private String keystoreType;

    @Value("${domibus.authentication.dss.official.journal.content.keystore.path}")
    private String keystorePath;

    @Value("${domibus.authentication.dss.official.journal.content.keystore.password}")
    private String keystorePassword;

    @Value("${domibus.authentication.dss.current.official.journal.url}")
    private String currentOjUrl;

    @Value("${domibus.authentication.dss.current.lotl.url}")
    private String currentLotlUrl;

    @Value("${domibus.authentication.dss.lotl.country.code}")
    private String lotlCountryCode;

    @Value("${domibus.authentication.dss.lotl.root.scheme.info.uri}")
    private String lotlSchemeUri;

    @Value("${domibus.authentication.dss.cache.path}")
    private String dssCachePath;

    @Value("${domibus.authentication.dss.proxy.https.host:NONE}")
    private String proxyHttpsHost;

    @Value("${domibus.authentication.dss.proxy.https.port:0}")
    private String proxyHttpsPort;

    @Value("${domibus.authentication.dss.proxy.https.user:NONE}")
    private String proxyHttpsUser;

    @Value("${domibus.authentication.dss.proxy.https.password:NONE}")
    private String proxyHttpsPassword;

    @Value("${domibus.authentication.dss.proxy.https.excludedHosts:NONE}")
    private String proxyHttpsExcludedHosts;

    @Value("${domibus.authentication.dss.proxy.http.host:NONE}")
    private String proxyHttpHost;

    @Value("${domibus.authentication.dss.proxy.http.port:0}")
    private String proxyHttpPort;

    @Value("${domibus.authentication.dss.proxy.http.user:NONE}")
    private String proxyHttpUser;

    @Value("${domibus.authentication.dss.proxy.http.password:NONE}")
    private String proxyHttpPassword;

    @Value("${domibus.authentication.dss.proxy.http.excludedHosts:NONE}")
    private String proxyHttpExcludedHosts;

    @Value("${domibus.authentication.dss.refresh.cron}")
    private String dssRefreshCronExpression;

    @Value("${" + DOMIBUS_AUTHENTICATION_DSS_ENABLE_CUSTOM_TRUSTED_LIST_FOR_MULTITENANT + "}")
    private boolean enableDssCustomTrustedListForMultiTenant;

    @Value("${domibus.authentication.dss.exception.on.missing.revocation.data}")
    private boolean enableExceptionOnMissingRevocationData;

    @Value("${domibus.authentication.dss.check.revocation.for.untrusted.chains}")
    private boolean checkRevocationForUntrustedChain;

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

    @Value("${domibus.dss.perform.crl.check}")
    private boolean checkCrlInDss;

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
                                              ServerInfoExtService serverInfoExtService,
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
    public Function<Void
            , CertificateVerifier> certificateVerifierFactory() {
        return aVoid -> certificateVerifier();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public CertificateVerifier certificateVerifier() {
        OnlineCRLSource crlSource = null;
        DomibusDataLoader dataLoader = dataLoader();
        if (checkCrlInDss) {
            crlSource = new OnlineCRLSource(dataLoader);
        }
        CommonCertificateVerifier certificateVerifier = new CommonCertificateVerifier(trustedListSource(), crlSource, null, dataLoader);
        certificateVerifier.setExceptionOnMissingRevocationData(enableExceptionOnMissingRevocationData);
        certificateVerifier.setCheckRevocationForUntrustedChains(checkRevocationForUntrustedChain);
        LOG.debug("Instanciating new certificate verifier:[{}], enableExceptionOnMissingRevocationData:[{}], checkRevocationForUntrustedChain:[{}]", certificateVerifier, enableExceptionOnMissingRevocationData, checkRevocationForUntrustedChain);
        return certificateVerifier;
    }

    @Bean
    public KeyStoreCertificateSource ojContentKeyStore() throws IOException {
        LOG.debug("Initializing DSS trust list trustStore with type:[{}], path:[{}]", keystoreType, keystorePath);
        return new KeyStoreCertificateSource(new File(keystorePath), keystoreType, keystorePassword);
    }

    @Bean
    public DomibusDataLoader dataLoader() {
        DomibusDataLoader dataLoader = new DomibusDataLoader();
        ProxyConfig proxyConfig = new ProxyConfig();
        if (!NONE.equals(proxyHttpsHost)) {
            LOG.debug("Configuring Dss https proxy:");
            try {
                int httpsPort = Integer.parseInt(proxyHttpsPort);
                final ProxyProperties httpsProperties = getProxyProperties(proxyHttpsHost, httpsPort, proxyHttpsUser, proxyHttpsPassword, proxyHttpsExcludedHosts);
                proxyConfig.setHttpsProperties(httpsProperties);
            } catch (NumberFormatException n) {
                LOG.warn("Error parsing https port config:[{}], skipping https configuration", proxyHttpsHost, n);
            }
        }
        if (!NONE.equals(proxyHttpHost)) {
            LOG.debug("Configuring Dss http proxy:");
            try {
                int httpPort = Integer.parseInt(proxyHttpPort);
                final ProxyProperties httpProperties = getProxyProperties(proxyHttpHost, httpPort, proxyHttpUser, proxyHttpPassword, proxyHttpExcludedHosts);
                proxyConfig.setHttpProperties(httpProperties);
            } catch (NumberFormatException n) {
                LOG.warn("Error parsing http port config:[{}], skipping http configuration", proxyHttpPort, n);
            }
        }
        dataLoader.setProxyConfig(proxyConfig);
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


    //TODO remove proxy properties and use the one from domibus.
    private ProxyProperties getProxyProperties(final String host,
                                               final int port,
                                               final String user,
                                               final String password,
                                               final String excludedHosts) {

        LOG.debug("Using proxy properties host:[{}],port:[{}],user:[{}],excludedHosts:[{}]", host, port, user, excludedHosts);
        final ProxyProperties httpsProperties = new ProxyProperties();
        httpsProperties.setHost(host);
        httpsProperties.setPort(port);
        httpsProperties.setUser(user);
        httpsProperties.setPassword(password);
        httpsProperties.setExcludedHosts(excludedHosts);
        return httpsProperties;
    }

    @Bean
    List<OtherTrustedList> otherTrustedLists(DomibusPropertyExtService domibusPropertyExtService,
                                             DomainContextExtService domainContextExtService,
                                             DomibusConfigurationExtService domibusConfigurationExtService,
                                             Environment environment) {
        final boolean multiTenant = domibusConfigurationExtService.isMultiTenantAware();
        final List<OtherTrustedList> otherTrustedLists = new CustomTrustedListPropertyMapper(domibusPropertyExtService, domainContextExtService, environment).map();
        if (multiTenant && !otherTrustedLists.isEmpty()) {
            if (enableDssCustomTrustedListForMultiTenant) {
                LOG.warn("Configured custom trusted lists are shared by all tenants.");
            } else {
                LOG.info("In multi-tenant configuration custom DSS trusted list are shared. Therefore they are deactivated by default. Please adapt property:[{}] to change that behavior", DOMIBUS_AUTHENTICATION_DSS_ENABLE_CUSTOM_TRUSTED_LIST_FOR_MULTITENANT);
                return Collections.emptyList();
            }
        }
        for (OtherTrustedList otherTrustedList : otherTrustedLists) {
            LOG.info("Custom trusted list configured with url:[{}], code:[{}]", otherTrustedList.getUrl(), otherTrustedList.getCountryCode());
        }
        if (otherTrustedLists.isEmpty()) {
            LOG.info("No custom trusted list configured.");
        }
        return otherTrustedLists;
    }

    @Bean
    public DomibusTSLValidationJob tslValidationJob(
            DataLoader dataLoader,
            DomibusTSLRepository tslRepository,
            KeyStoreCertificateSource ojContentKeyStore,
            List<OtherTrustedList> otherTrustedLists) {
        LOG.info("Configuring DSS lotl with url:[{}],schema uri:[{}],country code:[{}],oj url:[{}]", currentLotlUrl, lotlSchemeUri, lotlCountryCode, currentOjUrl);
        DomibusTSLValidationJob validationJob = new DomibusTSLValidationJob(certificateVerifierFactory());
        validationJob.setDataLoader(dataLoader);
        validationJob.setRepository(tslRepository);
        validationJob.setLotlUrl(currentLotlUrl);
        validationJob.setLotlCode(lotlCountryCode);
        validationJob.setOjUrl(currentOjUrl);
        validationJob.setOjContentKeyStore(ojContentKeyStore);
        validationJob.setCheckLOTLSignature(true);
        validationJob.setCheckTSLSignatures(true);
        validationJob.setOtherTrustedLists(otherTrustedLists);
        String serverCacheDirectoryPath = tslRepository.getCacheDirectoryPath();
        Path cachePath = Paths.get(serverCacheDirectoryPath);
        if (!cachePath.toFile().exists()) {
            LOG.error("Dss cache directory[{}] should be created by the system, please check permissions", serverCacheDirectoryPath);
            return validationJob;
        }
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(cachePath)) {
            Iterator files = ds.iterator();
            if (!files.hasNext()) {
                LOG.debug("Cache directory is empty, refreshing trusted lists needed");
                validationJob.refresh();
            } else {
                LOG.debug("Cache directory is not empty, loading trusted lists from disk");
                validationJob.initRepository();
            }
        } catch (IOException e) {
            LOG.error("Error while checking if cache directory:[{}] is empty", serverCacheDirectoryPath, e);
        }
        return validationJob;
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
        obj.setCronExpression(dssRefreshCronExpression);
        LOG.debug("dssRefreshTrigger configured with cronExpression [{}]", dssRefreshCronExpression);
        obj.setStartDelay(20000);
        return obj;
    }

    @Bean
    public ValidationConstraintPropertyMapper contraints(DomibusPropertyExtService domibusPropertyExtService,
                                                         DomainContextExtService domainContextExtService, Environment environment) {
        return new ValidationConstraintPropertyMapper(domibusPropertyExtService, domainContextExtService, environment);
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
                                                        final PkiExtService pkiExtService,
                                                        final DssCache dssCache) {
        //needed to initialize WSS4J property bundles to have correct message in the WSSException.
        WSSConfig.init();
        return new DomibusDssCryptoSpi(
                defaultDomainCryptoService,
                certificateVerifierFactory(),
                tslRepository,
                validationReport,
                constraintMapper,
                pkiExtService,
                dssCache);
    }

    @Bean
    public DssCache dssCache(CacheManager cacheManager) {
        org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            throw new IllegalArgumentException(String.format("Cache named:[%s] not found, please configure it.", cacheName));
        }
        return new DssCache((Cache) cache.getNativeCache());
    }

}
