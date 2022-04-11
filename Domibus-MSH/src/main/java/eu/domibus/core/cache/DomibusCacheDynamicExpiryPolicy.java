package eu.domibus.core.cache;

import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.spring.SpringContextProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.ehcache.expiry.ExpiryPolicy;
import org.springframework.context.ApplicationContext;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * Class enables dynamic update of the cache TTL for ehcache 3.x provider. The class must be configured in the
 * ehcache configuration. The Default value of the DomibusCacheDynamicExpiryPolicy is 1h.
 * <p>
 * Example:
 * <pre>
 * &lt;cache alias="lookupInfo">
 *   &lt;expiry>
 *     <class>eu.domibus.core.cache.DomibusCacheDynamicExpiryPolicy</class>
 *   &lt;/expiry>
 * &lt;/cache>
 * </pre>
 *
 * @author Joze Rihtarsic
 * @author Cosmin Baciu
 * @since 5.0
 */
public class DomibusCacheDynamicExpiryPolicy implements ExpiryPolicy {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusCacheDynamicExpiryPolicy.class);

    protected volatile Duration expiryDuration = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public Duration getExpiryForCreation(Object key, Object value) {
        if (expiryDuration == null) {
            synchronized (DomibusCacheDynamicExpiryPolicy.class) {
                if (expiryDuration == null) {
                    LOG.debug("Getting the TTL for dynamic discovery lookup cache");

                    final ApplicationContext applicationContext = SpringContextProvider.getApplicationContext();
                    final DomibusPropertyProvider domibusPropertyProvider = applicationContext.getBean(DomibusPropertyProvider.SPRING_BEAN_NAME, DomibusPropertyProvider.class);
                    final Long ttlCache = domibusPropertyProvider.getLongProperty(DomibusPropertyMetadataManagerSPI.DOMIBUS_DYNAMICDISCOVERY_LOOKUP_CACHE_TTL);
                    expiryDuration = Duration.ofSeconds(ttlCache);

                    LOG.debug("Using the TTL for dynamic discovery lookup cache [{}]", expiryDuration);
                }
            }
        }
        return expiryDuration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Duration getExpiryForAccess(Object o, Supplier supplier) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Duration getExpiryForUpdate(Object o, Supplier supplier, Object o2) {
        return null;
    }

    public void resetTTLInSeconds() {
        this.expiryDuration = null;
    }
}