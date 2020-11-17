package eu.domibus.core.spring;

import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.core.property.DomibusPropertyProviderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * Beans and configuration needed by Spring Session
 * @author Ion Perpegel
 * @since 5.0
 */
@Configuration
@EnableJdbcHttpSession
public class SessionConfig {

    @Autowired
    DomibusPropertyProviderImpl domibusPropertyProvider;

    @Bean
    public EmbeddedDatabase dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .addScript("org/springframework/session/jdbc/schema-h2.sql")
                .build();
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public CookieSerializer cookieSerializer() {
        Boolean secure = domibusPropertyProvider.getBooleanProperty(DomibusPropertyMetadataManagerSPI.DOMIBUS_UI_SESSION_SECURE);

        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setUseSecureCookie(secure);
        return serializer;
    }

}
