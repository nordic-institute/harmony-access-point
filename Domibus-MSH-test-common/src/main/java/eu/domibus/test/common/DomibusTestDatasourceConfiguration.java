package eu.domibus.test.common;

import com.zaxxer.hikari.HikariDataSource;
import eu.domibus.api.datasource.DataSourceConstants;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.h2.jdbcx.JdbcDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.TimeZone;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Configuration
public class DomibusTestDatasourceConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusTestDatasourceConfiguration.class);

    @Primary
    @Bean(name = DataSourceConstants.DOMIBUS_JDBC_DATA_SOURCE, destroyMethod = "close")
    public DataSource domibusDatasource() {
        HikariDataSource dataSource = createDataSource();

        dataSource.setIdleTimeout(60000);
        dataSource.setConnectionTimeout(30000);

        return dataSource;
    }

    @Primary
    @Bean(name = DataSourceConstants.DOMIBUS_JDBC_QUARTZ_DATA_SOURCE, destroyMethod = "close")
    @DependsOn(DataSourceConstants.DOMIBUS_JDBC_DATA_SOURCE)
    public DataSource quartzDatasource() {
        return createDataSource();
    }


    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(@Qualifier(DataSourceConstants.DOMIBUS_JDBC_DATA_SOURCE) DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();

        entityManagerFactoryBean.setDataSource(dataSource);
        entityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
//        entityManagerFactoryBean.setPackagesToScan(PROPERTY_PACKAGES_TO_SCAN, CONVERT_PACKAGES_TO_SCAN);

//        Properties jpaProperties = new Properties();
//        jpaProperties.put(PROPERTY_NAME_HIBERNATE_DIALECT, environment.getRequiredProperty(PROPERTY_NAME_HIBERNATE_DIALECT));
//        jpaProperties.put(PROPERTY_NAME_HIBERNATE_FORMAT_SQL, environment.getRequiredProperty(PROPERTY_NAME_HIBERNATE_FORMAT_SQL));
//        jpaProperties.put(PROPERTY_NAME_HIBERNATE_HBM2DDL_AUTO, environment.getRequiredProperty(PROPERTY_NAME_HIBERNATE_HBM2DDL_AUTO));
//        jpaProperties.put(PROPERTY_NAME_HIBERNATE_NAMING_STRATEGY, environment.getRequiredProperty(PROPERTY_NAME_HIBERNATE_NAMING_STRATEGY));
//        jpaProperties.put(PROPERTY_NAME_HIBERNATE_SHOW_SQL, environment.getRequiredProperty(PROPERTY_NAME_HIBERNATE_SHOW_SQL));
        //jpaProperties.put("jadira.usertype.autoRegisterUserTypes", "true");

//        entityManagerFactoryBean.setJpaProperties(jpaProperties);

        entityManagerFactoryBean.setMappingResources("META-INF/orm.xml");
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
                entityManagerFactoryBean.setMappingResources(mappingResources);
            }
        } catch (IOException e) {
            LOG.error("Ressources classpath*:config/*-mysql-orm.xml", e);
        }


        return entityManagerFactoryBean;
    }

    private HikariDataSource createDataSource() {
        JdbcDataSource h2DataSource = createH2Datasource();

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setJdbcUrl(h2DataSource.getUrl());
        dataSource.setUsername(h2DataSource.getUser());
        dataSource.setPassword(h2DataSource.getPassword());

        final int maxPoolSize = 20;
        dataSource.setMaximumPoolSize(maxPoolSize);
        final int maxLifetimeInSecs = 10;
        dataSource.setMaxLifetime(maxLifetimeInSecs * 1000L);
        return dataSource;
    }

    private JdbcDataSource createH2Datasource() {
        JdbcDataSource result = new JdbcDataSource();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        result.setUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;INIT=runscript from 'classpath:config/database/create_schema.sql'\\;runscript from 'classpath:config/database/domibus-h2.sql'\\;runscript from 'classpath:config/database/domibus-h2-data.sql'\\;runscript from 'classpath:config/database/schema-h2.sql'");
        result.setUser("sa");
        result.setPassword("");
        return result;
    }
}
