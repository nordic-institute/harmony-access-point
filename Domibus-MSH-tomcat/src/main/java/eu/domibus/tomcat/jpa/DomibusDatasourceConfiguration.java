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

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Configuration
public class DomibusDatasourceConfiguration {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DomibusDatasourceConfiguration.class);

    public static final String DOMIBUS_DATASOURCE_XA_PROPERTY_PASSWORD = "domibus.datasource.xa.property.password";
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

        final String xaDataSourceClassName = domibusPropertyProvider.getProperty("domibus.datasource.xa.xaDataSourceClassName");
        dataSource.setXaDataSourceClassName(xaDataSourceClassName);
        dataSource.setXaProperties(xaProperties());
        final Integer minPoolSize = domibusPropertyProvider.getIntegerProperty("domibus.datasource.xa.minPoolSize");
        dataSource.setMinPoolSize(minPoolSize);
        final Integer maxPoolSize = domibusPropertyProvider.getIntegerProperty("domibus.datasource.xa.maxPoolSize");
        dataSource.setMaxPoolSize(maxPoolSize);
        final Integer maxLifeTime = domibusPropertyProvider.getIntegerProperty("domibus.datasource.xa.maxLifetime");
        dataSource.setMaxLifetime(maxLifeTime);

        final Integer borrowConnectionTimeout = domibusPropertyProvider.getIntegerProperty("domibus.datasource.xa.borrowConnectionTimeout");
        dataSource.setBorrowConnectionTimeout(borrowConnectionTimeout);
        final Integer reapTimeout = domibusPropertyProvider.getIntegerProperty("domibus.datasource.xa.reapTimeout");
        dataSource.setReapTimeout(reapTimeout);
        final Integer maxIdleTime = domibusPropertyProvider.getIntegerProperty("domibus.datasource.xa.maxIdleTime");
        dataSource.setMaxIdleTime(maxIdleTime);
        final Integer maintenanceInterval = domibusPropertyProvider.getIntegerProperty("domibus.datasource.xa.maintenanceInterval");
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

        final String driverClassName = domibusPropertyProvider.getProperty("domibus.datasource.driverClassName");
        dataSource.setDriverClassName(driverClassName);
        final String dataSourceURL = domibusPropertyProvider.getProperty("domibus.datasource.url");
        dataSource.setUrl(dataSourceURL);
        final String user = domibusPropertyProvider.getProperty("domibus.datasource.user");
        dataSource.setUser(user);
        final String password = domibusPropertyProvider.getProperty("domibus.datasource.password", true);
        dataSource.setPassword(password);
        final Integer minPoolSize = domibusPropertyProvider.getIntegerProperty("domibus.datasource.minPoolSize");
        dataSource.setMinPoolSize(minPoolSize);
        final Integer maxPoolSize = domibusPropertyProvider.getIntegerProperty("domibus.datasource.maxPoolSize");
        dataSource.setMaxPoolSize(maxPoolSize);
        final Integer maxLifetime = domibusPropertyProvider.getIntegerProperty("domibus.datasource.maxLifetime");
        dataSource.setMaxLifetime(maxLifetime);

        return dataSource;
    }

}
