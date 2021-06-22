package eu.domibus.core.jpa;

import eu.domibus.api.datasource.DataSourceConstants;
import eu.domibus.api.property.DataBaseEngine;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.JPAConstants;
import eu.domibus.core.cache.DomibusCacheConfiguration;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.core.property.PrefixedProperties;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.MultiTenancyStrategy;
import org.hibernate.cfg.Environment;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.Optional;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_ENTITY_MANAGER_FACTORY_PACKAGES_TO_SCAN;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Configuration
public class DomibusJPAConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusJPAConfiguration.class);

    public static final String JPA_PROPERTIES = "jpaProperties";
    public static final String JPA_PROPERTY_TIMEZONE_UTC = "UTC";

    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        return new HibernateJpaVendorAdapter();
    }

    @Bean
    @DependsOn({DataSourceConstants.DOMIBUS_JDBC_DATA_SOURCE, DomibusCacheConfiguration.CACHE_MANAGER})
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(@Qualifier(DataSourceConstants.DOMIBUS_JDBC_DATA_SOURCE) DataSource dataSource,
                                                                       DomibusPropertyProvider domibusPropertyProvider,
                                                                       @Qualifier(JPA_PROPERTIES) PrefixedProperties jpaProperties,
                                                                       DomibusConfigurationService domibusConfigurationService,
                                                                       Optional<ConnectionProvider> singleTenantConnectionProviderImpl,
                                                                       Optional<MultiTenantConnectionProvider> multiTenantConnectionProviderImpl,
                                                                       Optional<CurrentTenantIdentifierResolver> tenantIdentifierResolver) {
        LocalContainerEntityManagerFactoryBean result = new LocalContainerEntityManagerFactoryBean();
        result.setMappingResources();

        result.setPersistenceUnitName(JPAConstants.PERSISTENCE_UNIT_NAME);
        final String packagesToScanString = domibusPropertyProvider.getProperty(DOMIBUS_ENTITY_MANAGER_FACTORY_PACKAGES_TO_SCAN);
        if (StringUtils.isNotEmpty(packagesToScanString)) {
            final String[] packagesToScan = StringUtils.split(packagesToScanString, ",");
            result.setPackagesToScan(packagesToScan);
        }
        result.setDataSource(dataSource);
        result.setJpaVendorAdapter(jpaVendorAdapter());

        if (singleTenantConnectionProviderImpl.isPresent()) {
            LOG.info("Configuring jpaProperties for single-tenancy");
            jpaProperties.put(Environment.CONNECTION_PROVIDER, singleTenantConnectionProviderImpl.get());
        } else if (multiTenantConnectionProviderImpl.isPresent()) {
            LOG.info("Configuring jpaProperties for multi-tenancy");
            jpaProperties.put(Environment.MULTI_TENANT, MultiTenancyStrategy.SCHEMA);
            jpaProperties.put(Environment.MULTI_TENANT_CONNECTION_PROVIDER, multiTenantConnectionProviderImpl.get());
            if (tenantIdentifierResolver.isPresent()) {
                jpaProperties.put(Environment.MULTI_TENANT_IDENTIFIER_RESOLVER, tenantIdentifierResolver.get());
            }
        }
        initMysqlOrm(domibusConfigurationService, result);
        result.setJpaProperties(jpaProperties);
        return result;
    }

    private void initMysqlOrm(DomibusConfigurationService domibusConfigurationService, LocalContainerEntityManagerFactoryBean result) {
        if (DataBaseEngine.MYSQL == domibusConfigurationService.getDataBaseEngine()) {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            try {
                Resource[] pluginDefaultResourceList = resolver.getResources("classpath*:config/*-mysql-orm.xml");
                if (pluginDefaultResourceList != null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("resolver.getResources -> classpath*:config/*-mysql-orm.xml found [{}] resources. [{}]", pluginDefaultResourceList.length, pluginDefaultResourceList);
                    }
                    String[] mappingResources = new String[pluginDefaultResourceList.length];
                    for (int i = 0; i < pluginDefaultResourceList.length; i++) {
                        String relativePath = StringUtils.substringAfter(pluginDefaultResourceList[i].getURL().getPath(), "!/");
                        mappingResources[i] = relativePath;
                        LOG.debug("setMappingResources [{}]", relativePath);

                    }
                    result.setMappingResources(mappingResources);
                }
            } catch (IOException e) {
                LOG.error("Ressources classpath*:config/*-mysql-orm.xml");
            }
        }
    }

    @Bean(JPA_PROPERTIES)
    public PrefixedProperties jpaProperties(DomibusPropertyProvider domibusPropertyProvider) {
        PrefixedProperties result = new PrefixedProperties(domibusPropertyProvider, "domibus.entityManagerFactory.jpaProperty.");
        result.setProperty(Environment.JDBC_TIME_ZONE, JPA_PROPERTY_TIMEZONE_UTC);
        return result;
    }

    @Bean("transactionManager")
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}