package eu.domibus.core.dao;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@EnableTransactionManagement
@Profile("IN_MEMORY_DATABASE")
public class InMemoryDataBaseConfig extends AbstractDatabaseConfig {

    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .build();
    }

    @Override
    public Map<Object, Object> getProperties() {
        Map<Object, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect","org.hibernate.dialect.H2Dialect");
        return properties;
    }

}
