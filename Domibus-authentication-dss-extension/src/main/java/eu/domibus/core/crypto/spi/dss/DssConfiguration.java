package eu.domibus.core.crypto.spi.dss;

import com.google.common.collect.Lists;
import eu.domibus.core.crypto.spi.DomainCryptoServiceSpi;
import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.DomibusConfigurationExtService;
import eu.domibus.ext.services.DomibusPropertyExtService;
import eu.europa.esig.dss.client.http.DataLoader;
import eu.europa.esig.dss.client.http.proxy.ProxyConfig;
import eu.europa.esig.dss.client.http.proxy.ProxyProperties;
import eu.europa.esig.dss.tsl.OtherTrustedList;
import eu.europa.esig.dss.tsl.TrustedListsCertificateSource;
import eu.europa.esig.dss.tsl.service.DomibusTSLValidationJob;
import eu.europa.esig.dss.tsl.service.TSLRepository;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.x509.KeyStoreCertificateSource;
import org.apache.wss4j.dom.engine.WSSConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;

import java.io.File;
import java.io.IOException;
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

    private static final Logger LOG = LoggerFactory.getLogger(DssConfiguration.class);

    private static final String NONE = "NONE";

    private static final String DOMIBUS_AUTHENTICATION_DSS_ENABLE_CUSTOM_TRUSTED_LIST_FOR_MULTITENANT = "domibus.authentication.dss.enable.custom.trusted.list.for.multitenant";

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

    @Bean
    public TrustedListsCertificateSource trustedListSource() {
        return new TrustedListsCertificateSource();
    }

    @Bean
    public TSLRepository tslRepository(TrustedListsCertificateSource trustedListSource) {
        LOG.debug("Dss trusted list cache path:[{}]", dssCachePath);
        TSLRepository tslRepository = new TSLRepository();
        tslRepository.setTrustedListsCertificateSource(trustedListSource);
        tslRepository.setCacheDirectoryPath(dssCachePath);
        return tslRepository;
    }

    @Bean
    public CertificateVerifier certificateVerifier(DomibusDataLoader dataLoader, TrustedListsCertificateSource trustedListSource) {
        CommonCertificateVerifier certificateVerifier = new CommonCertificateVerifier();
        certificateVerifier.setTrustedCertSource(trustedListSource);
        certificateVerifier.setDataLoader(dataLoader);

        certificateVerifier.setExceptionOnMissingRevocationData(enableExceptionOnMissingRevocationData);
        certificateVerifier.setCheckRevocationForUntrustedChains(checkRevocationForUntrustedChain);

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
        return dataLoader;
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
                return Lists.newArrayList();
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
    public DomibusTSLValidationJob tslValidationJob(DataLoader dataLoader, TSLRepository tslRepository, KeyStoreCertificateSource ojContentKeyStore, List<OtherTrustedList> otherTrustedLists) {
        LOG.info("Configuring DSS lotl with url:[{}],schema uri:[{}],country code:[{}],oj url:[{}]", currentLotlUrl, lotlSchemeUri, lotlCountryCode, currentOjUrl);
        DomibusTSLValidationJob validationJob = new DomibusTSLValidationJob();
        validationJob.setDataLoader(dataLoader);
        validationJob.setRepository(tslRepository);
        validationJob.setLotlUrl(currentLotlUrl);
        validationJob.setLotlRootSchemeInfoUri(lotlSchemeUri);
        validationJob.setLotlCode(lotlCountryCode);
        validationJob.setOjUrl(currentOjUrl);
        validationJob.setOjContentKeyStore(ojContentKeyStore);
        validationJob.setCheckLOTLSignature(true);
        validationJob.setCheckTSLSignatures(true);
        validationJob.setOtherTrustedLists(otherTrustedLists);
        validationJob.initRepository();
        validationJob.refresh();
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
                                                        final CertificateVerifier certificateVerifier,
                                                        final TSLRepository tslRepository,
                                                        final ValidationReport validationReport,
                                                        final ValidationConstraintPropertyMapper constraintMapper) {
        //needed to initialize WSS4J property bundles to have correct message in the WSSException.
        WSSConfig.init();
        return new DomibusDssCryptoSpi(
                defaultDomainCryptoService,
                certificateVerifier,
                tslRepository,
                validationReport,
                constraintMapper);
    }
}
