package eu.domibus.core.cache;

import org.ehcache.expiry.ExpiryPolicy;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * Class enables dynamic update of the cache TTL for ehcache 3.x provider. The class must be configured in the
 * ehcache configuration. The Default value of the DomibusCacheDynamicExpiryPolicy is 1h.
 *
 * Example:
 * <pre>
 * &lt;cache alias="lookupInfo">
 *   &lt;expiry>
 *     <class>eu.domibus.core.cache.DomibusCacheDynamicExpiryPolicy</class>
 *   &lt;/expiry>
 * &lt;/cache>
 *</pre>
 *
 * @author Joze Rihtarsic
 * @since 5.0
 */
public class DomibusCacheDynamicExpiryPolicy implements ExpiryPolicy {

    // default duration for 1H
    Duration expiryDuration = Duration.ofSeconds(3600);

    /**
     * {@inheritDoc}
     */
    @Override
    public Duration getExpiryForCreation(Object key, Object value) {
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

    @Override
    public String toString() {
        return "Dynamic discovery expiry configuration ["+expiryDuration+"]";
    }

    public void setTTLInSeconds(long ttlInSeconds) {
        this.expiryDuration = Duration.ofSeconds(ttlInSeconds);
    }
}