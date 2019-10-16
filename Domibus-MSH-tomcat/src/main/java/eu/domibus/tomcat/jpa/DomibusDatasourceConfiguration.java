package eu.domibus.tomcat.jpa;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.atomikos.jdbc.nonxa.AtomikosNonXADataSourceBean;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.spring.PrefixedProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.util.Properties;

import static eu.domibus.api.property.DomibusPropertyMetadataManager.*;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Configuration
public class DomibusDatasourceConfiguration {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DomibusDatasourceConfiguration.class);

    public static final String DOMIBUS_DATASOURCE_XA_PROPERTY = "domibus.datasource.xa.property.";

    @Autowired
    @Qualifier("domibusProperties")
    protected Properties domibusProperties;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Bean(name = "domibusJDBC-XADataSource", initMethod = "init", destroyMethod = "close")
    @DependsOn("userTransactionService")
    public AtomikosDataSourceBean domibusXADatasource() {
        AtomikosDataSourceBean dataSource = new AtomikosDataSourceBean();
        dataSource.setUniqueResourceName("domibusJDBC-XA");

        final String xaDataSourceClassName = domibusPropertyProvider.getProperty(DOMIBUS_DATASOURCE_XA_XA_DATA_SOURCE_CLASS_NAME);
        dataSource.setXaDataSourceClassName(xaDataSourceClassName);
        dataSource.setXaProperties(xaProperties());
        final Integer minPoolSize = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DATASOURCE_XA_MIN_POOL_SIZE);
        dataSource.setMinPoolSize(minPoolSize);
        final Integer maxPoolSize = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DATASOURCE_XA_MAX_POOL_SIZE);
        dataSource.setMaxPoolSize(maxPoolSize);
        final Integer maxLifeTime = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DATASOURCE_XA_MAX_LIFETIME);
        dataSource.setMaxLifetime(maxLifeTime);

        final Integer borrowConnectionTimeout = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DATASOURCE_XA_BORROW_CONNECTION_TIMEOUT);
        dataSource.setBorrowConnectionTimeout(borrowConnectionTimeout);
        final Integer reapTimeout = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DATASOURCE_XA_REAP_TIMEOUT);
        dataSource.setReapTimeout(reapTimeout);
        final Integer maxIdleTime = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DATASOURCE_XA_MAX_IDLE_TIME);
        dataSource.setMaxIdleTime(maxIdleTime);
        final Integer maintenanceInterval = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DATASOURCE_XA_MAINTENANCE_INTERVAL);
        dataSource.setMaintenanceInterval(maintenanceInterval);

        return dataSource;
    }

    @Bean
    public PrefixedProperties xaProperties() {
        final PrefixedProperties prefixedProperties = new PrefixedProperties(domibusProperties, DOMIBUS_DATASOURCE_XA_PROPERTY);

        LOGGER.debug("Decrypting the property [{}]", DOMIBUS_DATASOURCE_XA_PROPERTY_PASSWORD);
        prefixedProperties.setProperty("password", domibusPropertyProvider.getProperty(DOMIBUS_DATASOURCE_XA_PROPERTY_PASSWORD, true));
        return prefixedProperties;
    }


    @Bean(name = "domibusJDBC-nonXADataSource", initMethod = "init", destroyMethod = "close")
    public AtomikosNonXADataSourceBean domibusNonXADatasource() {
        AtomikosNonXADataSourceBean dataSource = new AtomikosNonXADataSourceBean();
        dataSource.setUniqueResourceName("domibusNonXADataSource");

        final String driverClassName = domibusPropertyProvider.getProperty(DOMIBUS_DATASOURCE_DRIVER_CLASS_NAME);
        dataSource.setDriverClassName(driverClassName);
        final String dataSourceURL = domibusPropertyProvider.getProperty(DOMIBUS_DATASOURCE_URL);
        dataSource.setUrl(dataSourceURL);
        final String user = domibusPropertyProvider.getProperty(DOMIBUS_DATASOURCE_USER);
        dataSource.setUser(user);
        final String password = domibusPropertyProvider.getProperty(DOMIBUS_DATASOURCE_PASSWORD, true);
        dataSource.setPassword(password);
        final Integer minPoolSize = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DATASOURCE_MIN_POOL_SIZE);
        dataSource.setMinPoolSize(minPoolSize);
        final Integer maxPoolSize = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DATASOURCE_MAX_POOL_SIZE);
        dataSource.setMaxPoolSize(maxPoolSize);
        final Integer maxLifetime = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DATASOURCE_MAX_LIFETIME);
        dataSource.setMaxLifetime(maxLifetime);

        return dataSource;
    }

}
