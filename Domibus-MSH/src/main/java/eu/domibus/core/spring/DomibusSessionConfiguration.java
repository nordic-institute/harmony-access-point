package eu.domibus.core.spring;

import eu.domibus.core.property.DomibusPropertyProviderImpl;
import eu.domibus.core.security.configuration.SecurityInternalAuthProviderCondition;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.session.jdbc.config.annotation.SpringSessionDataSource;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * Beans and configuration needed by Spring Session
 *
 * @author Ion Perpegel
 * @since 5.0
 */
@Configuration
@EnableJdbcHttpSession
@Conditional(SecurityInternalAuthProviderCondition.class)
public class DomibusSessionConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusSessionConfiguration.class);

    public static final String SESSION_COOKIE_NAME = "JSESSIONID";

    @Autowired
    protected DomibusPropertyProviderImpl domibusPropertyProvider;

    @Bean
    @SpringSessionDataSource
    public EmbeddedDatabase dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .addScript("org/springframework/session/jdbc/schema-h2.sql")
                .build();
    }

    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();

        setName(serializer);
        setSecure(serializer);
        setTimeout(serializer);
        setSameSite(serializer);

        return serializer;
    }

    private void setSameSite(DefaultCookieSerializer serializer) {
        String propertyValue = domibusPropertyProvider.getProperty(DOMIBUS_UI_SESSION_SAME_SITE);
        serializer.setSameSite(propertyValue);
    }

    private void setName(DefaultCookieSerializer serializer) {
        serializer.setCookieName(SESSION_COOKIE_NAME);
        LOG.debug("Session cookie name set to [{}].", SESSION_COOKIE_NAME);
    }

    private void setTimeout(DefaultCookieSerializer serializer) {
        int timeout = domibusPropertyProvider.getIntegerProperty(DOMIBUS_UI_SESSION_TIMEOUT);
        if (timeout <= 0) {
            LOG.info("Session timeout should be positive, not [{}].", timeout);
            return;
        }

        serializer.setCookieMaxAge(timeout * 60);
        LOG.debug("Session timeout set to [{}].", timeout);
    }

    private void setSecure(DefaultCookieSerializer serializer) {
        Boolean secure = domibusPropertyProvider.getBooleanProperty(DOMIBUS_UI_SESSION_SECURE);
        serializer.setUseSecureCookie(secure);
        LOG.debug("Session secure set to [{}].", secure);
    }
}
