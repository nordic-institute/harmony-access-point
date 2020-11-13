package eu.domibus.tomcat.jpa;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.atomikos.jdbc.nonxa.AtomikosNonXADataSourceBean;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.jpa.DomibusJPAConfiguration;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.core.property.PrefixedProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Configuration
public class TomcatDatasourceConfiguration {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(TomcatDatasourceConfiguration.class);

    protected static final String DOMIBUS_DATASOURCE_XA_PROPERTY = "domibus.datasource.xa.property.";

    protected static final String DOMIBUS_DATASOURCE_XA_PROPERTY_VALUE_PREFIX_ORACLE_URL = "jdbc:oracle:";

    protected static final String DOMIBUS_DATASOURCE_XA_PROPERTY_KEY_URL = "url";

    protected static final String DOMIBUS_DATASOURCE_XA_PROPERTY_KEY_ORACLE_URL = "URL";

    @Bean(name = DomibusJPAConfiguration.DOMIBUS_JDBC_XA_DATA_SOURCE, initMethod = "init", destroyMethod = "close")
    @DependsOn("userTransactionService")
    public AtomikosDataSourceBean domibusXADatasource(DomibusPropertyProvider domibusPropertyProvider,
                                                      @Qualifier("xaProperties") PrefixedProperties xaProperties) {
        AtomikosDataSourceBean dataSource = new AtomikosDataSourceBean();
        dataSource.setUniqueResourceName("domibusJDBC-XA");

        final String xaDataSourceClassName = domibusPropertyProvider.getProperty(DOMIBUS_DATASOURCE_XA_XA_DATA_SOURCE_CLASS_NAME);
        dataSource.setXaDataSourceClassName(xaDataSourceClassName);
        dataSource.setXaProperties(xaProperties);
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

    @Bean("xaProperties")
    public PrefixedProperties xaProperties(DomibusPropertyProvider domibusPropertyProvider) {
        final PrefixedProperties prefixedProperties = new PrefixedProperties(domibusPropertyProvider, DOMIBUS_DATASOURCE_XA_PROPERTY);

        /*
         * The URL property name for the OracleXADataSource is 'URL', not 'url', with the setter like #setURL(String).
         * Because of this issue we need to change the case of the URL property from the domibus.properties to uppercase.
         * This is only needed when configuring Tomcat with Oracle, and only for the XA URL property (the non-XA URL
         * property is set through Atomikos' API - using #setUrl(String) below - , while the XA one may be set using reflection
         * - within #setXaProperties(Properties) above -).
         */
        prefixedProperties.entrySet()
                .stream()
                .filter(property -> StringUtils.equalsIgnoreCase(property.getKey().toString(), DOMIBUS_DATASOURCE_XA_PROPERTY_KEY_URL)
                        && StringUtils.startsWithIgnoreCase(property.getValue().toString(), DOMIBUS_DATASOURCE_XA_PROPERTY_VALUE_PREFIX_ORACLE_URL))
                .peek(property -> LOGGER.info("Switching the URL property key to uppercase as required by an OracleXEDataSource: [{}]->[{}]", property.getKey(), property.getValue()))
                .findAny()
                .ifPresent(property -> {
                    prefixedProperties.remove(DOMIBUS_DATASOURCE_XA_PROPERTY_KEY_URL);
                    prefixedProperties.setProperty(DOMIBUS_DATASOURCE_XA_PROPERTY_KEY_ORACLE_URL, property.getValue().toString());
                });

        prefixedProperties.setProperty("password", domibusPropertyProvider.getProperty(DOMIBUS_DATASOURCE_XA_PROPERTY_PASSWORD));
        return prefixedProperties;
    }


    @Bean(name = DomibusJPAConfiguration.DOMIBUS_JDBC_NON_XA_DATA_SOURCE, initMethod = "init", destroyMethod = "close")
    public AtomikosNonXADataSourceBean domibusNonXADatasource(DomibusPropertyProvider domibusPropertyProvider) {
        AtomikosNonXADataSourceBean dataSource = new AtomikosNonXADataSourceBean();
        dataSource.setUniqueResourceName("domibusNonXADataSource");

        final String driverClassName = domibusPropertyProvider.getProperty(DOMIBUS_DATASOURCE_DRIVER_CLASS_NAME);
        dataSource.setDriverClassName(driverClassName);
        final String dataSourceURL = domibusPropertyProvider.getProperty(DOMIBUS_DATASOURCE_URL);
        dataSource.setUrl(dataSourceURL);
        final String user = domibusPropertyProvider.getProperty(DOMIBUS_DATASOURCE_USER);
        dataSource.setUser(user);
        final String password = domibusPropertyProvider.getProperty(DOMIBUS_DATASOURCE_PASSWORD); //NOSONAR
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
