package eu.domibus.dao;

import eu.domibus.core.multitenancy.DomibusConnectionProvider;
import org.hibernate.cfg.Environment;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.mockito.internal.util.reflection.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public ConnectionProvider connectionProvider() {
        DomibusConnectionProvider domibusConnectionProvider = new DomibusConnectionProvider();
        Whitebox.setInternalState(domibusConnectionProvider, "dataSource", dataSource());
        return domibusConnectionProvider;
    }

    @Bean
    public JpaTransactionManager jpaTransactionManager(){
        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
        jpaTransactionManager.setEntityManagerFactory(domibusJTA().getObject());
        return jpaTransactionManager;
    }

}
