package eu.domibus.dao;

import eu.domibus.core.jpa.DomibusConnectionProvider;
import eu.domibus.core.util.DatabaseUtil;
import mockit.Deencapsulation;
import org.hibernate.cfg.Environment;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Configuration
public abstract class AbstractDatabaseConfig {

    @Bean("domibusJDBC-XADataSource")
    abstract DataSource dataSource();

    abstract Map<Object, Object> getProperties();

    @Bean
    public LocalContainerEntityManagerFactoryBean domibusJTA() {
        Properties jpaProperties = new Properties();
        jpaProperties.put("hibernate.hbm2ddl.auto","update");
        jpaProperties.put("hibernate.show_sql","true");
        jpaProperties.put("hibernate.format_sql","true");
        jpaProperties.put("hibernate.id.new_generator_mappings","false");
        jpaProperties.put(Environment.CONNECTION_PROVIDER, connectionProvider());
        jpaProperties.putAll(getProperties());

        LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        localContainerEntityManagerFactoryBean.setPackagesToScan("eu.domibus");
        localContainerEntityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        localContainerEntityManagerFactoryBean.setJpaProperties(jpaProperties);
        localContainerEntityManagerFactoryBean.setDataSource(dataSource());
        return localContainerEntityManagerFactoryBean;
    }

    @Bean
    public ConnectionProvider connectionProvider() {
        DomibusConnectionProvider domibusConnectionProvider = new DomibusConnectionProvider();
        Deencapsulation.setField(domibusConnectionProvider, "dataSource", dataSource());
        Deencapsulation.setField(domibusConnectionProvider, "databaseUtil", databaseUtil());
        return domibusConnectionProvider;
    }

    @Bean
    public DatabaseUtil databaseUtil() {
        DatabaseUtil databaseUtil = new DatabaseUtil();
        Deencapsulation.setField(databaseUtil, "dataSource", dataSource());
        databaseUtil.init();
        return databaseUtil;
    }

    @Bean
    public JpaTransactionManager jpaTransactionManager(){
        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
        jpaTransactionManager.setEntityManagerFactory(domibusJTA().getObject());
        return jpaTransactionManager;
    }

}
