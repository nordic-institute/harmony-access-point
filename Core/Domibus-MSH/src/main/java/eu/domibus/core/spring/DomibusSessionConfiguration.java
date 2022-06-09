package eu.domibus.core.spring;

import eu.domibus.core.property.DomibusPropertyProviderImpl;
import eu.domibus.core.security.configuration.SecurityInternalAuthProviderCondition;
import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.session.jdbc.config.annotation.SpringSessionDataSource;
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
@Conditional(SecurityInternalAuthProviderCondition.class)
public class DomibusSessionConfiguration {

    private static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusSessionConfiguration.class);

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
        setJvmRoute(serializer);

        return serializer;
    }

    private void setSameSite(DefaultCookieSerializer serializer) {
        String propertyValue = domibusPropertyProvider.getProperty(DOMIBUS_UI_SESSION_SAME_SITE);
        serializer.setSameSite(propertyValue);
    }

    private void setName(DefaultCookieSerializer serializer) {
        serializer.setCookieName(SESSION_COOKIE_NAME);
        LOG.info("Session cookie name set to [{}].", SESSION_COOKIE_NAME);
    }

    private void setTimeout(DefaultCookieSerializer serializer) {
        int timeout = domibusPropertyProvider.getIntegerProperty(DOMIBUS_UI_SESSION_TIMEOUT);
        if (timeout <= 0) {
            LOG.info("Session timeout should be positive, not [{}].", timeout);
            return;
        }

        serializer.setCookieMaxAge(timeout * 60);
        LOG.info("Session timeout set to [{}].", timeout);
    }

    private void setSecure(DefaultCookieSerializer serializer) {
        Boolean secure = domibusPropertyProvider.getBooleanProperty(DOMIBUS_UI_SESSION_SECURE);
        serializer.setUseSecureCookie(secure);
        LOG.info("Session secure set to [{}].", secure);
    }

    private void setJvmRoute(DefaultCookieSerializer serializer) {
        String jvmRoute = domibusPropertyProvider.getProperty(DOMIBUS_UI_SESSION_JVMROUTE);
        if(StringUtils.isNotBlank(jvmRoute)) {
            LOG.info("Session JVM route property set to [{}]: parsing its actual value...", jvmRoute);
            String jvmRouteValue = parseJvmRoutePropertyValue(jvmRoute);

            LOG.info("Parsed JVM actual route value [{}]", jvmRouteValue);
            serializer.setJvmRoute(jvmRouteValue);

            LOG.info("Disable Base64 encoding of the session cookie to prevent encoding the jvmRoute");
            serializer.setUseBase64Encoding(false);
        }
    }

    private String parseJvmRoutePropertyValue(String jvmRoute) {
        String environment = System.getenv(jvmRoute);
        if (StringUtils.isNotBlank(environment)) {
            LOG.debug("Found an environment variable having the name of [{}]", environment);
            return environment;
        }

        String propertyValue = System.getProperty(jvmRoute);
        if (StringUtils.isNotBlank(propertyValue)) {
            LOG.debug("Found a system property having the name of [{}]", propertyValue);
            return propertyValue;
        }

        LOG.debug("No system environment variables nor system properties found matching the name of [{}]: treating itself as the actual value", jvmRoute);
        return jvmRoute;
    }
}
