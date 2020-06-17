package eu.domibus;


import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.property.encryption.PasswordEncryptionService;
import eu.domibus.api.util.HttpUtil;
import eu.domibus.core.cache.DomibusCacheService;
import eu.domibus.core.crypto.DomainCryptoServiceFactoryImpl;
import eu.domibus.core.crypto.MultiDomainCryptoServiceImpl;
import eu.domibus.core.crypto.api.DomainCryptoServiceFactory;
import eu.domibus.core.crypto.api.MultiDomainCryptoService;
import eu.domibus.core.multitenancy.DomainContextProviderImpl;
import eu.domibus.core.multitenancy.DomainServiceImpl;
import eu.domibus.core.multitenancy.dao.DomainDao;
import eu.domibus.core.multitenancy.dao.DomainDaoImpl;
import eu.domibus.core.property.DefaultDomibusConfigurationService;
import eu.domibus.core.property.DomibusPropertyProviderImpl;
import eu.domibus.core.property.encryption.PasswordEncryptionContextFactory;
import eu.domibus.core.proxy.DomibusProxyService;
import eu.domibus.core.proxy.DomibusProxyServiceImpl;
import eu.domibus.core.proxy.ProxyUtil;
import eu.domibus.core.util.DomibusX509TrustManager;
import eu.domibus.core.util.HttpUtilImpl;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * Spring test {@code Configuration} class to be re-used in Spring IT classes
 * Use  {@code @Import({SpringTestConfiguration.class})
 *
 * @author Catalin Enache
 * @since 4.1.2
 */
@Configuration
public class SpringTestConfiguration {

    @Bean
    HttpUtil httpUtil() {
        return Mockito.mock(HttpUtilImpl.class);
    }

    @Bean
    ProxyUtil proxyUtil() {
        return Mockito.mock(ProxyUtil.class);
    }

    @Bean
    DomibusProxyService domibusProxyService() {
        return Mockito.mock(DomibusProxyServiceImpl.class);
    }

    @Bean
    DomibusX509TrustManager domibusX509TrustManager() {
        return Mockito.mock(DomibusX509TrustManager.class);
    }

    @Bean
    MultiDomainCryptoService multiDomainCryptoService() {
        return Mockito.mock(MultiDomainCryptoServiceImpl.class);
    }

    @Bean
    DomainCryptoServiceFactory domainCertificateProviderFactory() {
        return Mockito.mock(DomainCryptoServiceFactoryImpl.class);
    }

    @Bean
    DomibusCacheService domibusCacheService() {
        return Mockito.mock(DomibusCacheService.class);
    }

    @Bean
    DomibusPropertyProvider domibusPropertyProvider() {
        return new DomibusPropertyProviderImpl();
    }

    @Bean
    public DomainService domainService() {
        return Mockito.mock(DomainServiceImpl.class);
    }

    @Bean
    public DomainDao domainDao() {
        return Mockito.mock(DomainDaoImpl.class);
    }

    @Bean
    public DomibusConfigurationService domibusConfigurationService() {
        return Mockito.mock(DefaultDomibusConfigurationService.class);
    }

    @Bean(name = "domibusDefaultProperties")
    public Properties domibusDefaultProperties() {
        return Mockito.mock(Properties.class);
    }

    @Bean(name = "domibusProperties")
    public Properties domibusProperties() {
        return Mockito.mock(Properties.class);
    }

    @Bean
    public DomainContextProvider domainContextProvider() {
        return Mockito.mock(DomainContextProviderImpl.class);
    }

    @Bean
    public PasswordEncryptionService passwordEncryptionService() {
        return Mockito.mock(PasswordEncryptionService.class);
    }

    @Bean
    public PasswordEncryptionContextFactory passwordEncryptionContextFactory() {
        return Mockito.mock(PasswordEncryptionContextFactory.class);
    }
}
