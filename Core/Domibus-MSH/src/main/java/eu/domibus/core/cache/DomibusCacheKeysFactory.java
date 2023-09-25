package eu.domibus.core.cache;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.hibernate.cache.internal.DefaultCacheKeysFactory;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.EntityPersister;

/**
 * Custom cache key factory for second level cache:
 * it uses the tenantIdentifier from the MDC (not the one from the hibernate session)
 *
 * @author Ion Perpegel
 * @since 5.1.1
 */
public class DomibusCacheKeysFactory extends DefaultCacheKeysFactory implements org.hibernate.cache.spi.CacheKeysFactory {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusCacheKeysFactory.class);

    private static volatile CurrentTenantIdentifierResolver tenantIdentifierResolver;

    @Override
    public Object createCollectionKey(Object id, CollectionPersister persister, SessionFactoryImplementor factory, String tenantIdentifier) {
        final String actualTenantIdentifier = resolveTenantIdentifier();
        return super.createCollectionKey(id, persister, factory, actualTenantIdentifier);
    }

    @Override
    public Object createEntityKey(Object id, EntityPersister persister, SessionFactoryImplementor factory, String tenantIdentifier) {
        final String actualTenantIdentifier = resolveTenantIdentifier();
        return super.createEntityKey(id, persister, factory, actualTenantIdentifier);
    }

    public static void setTenantIdentifierResolver(CurrentTenantIdentifierResolver tenantIdentifierResolver) {
        DomibusCacheKeysFactory.tenantIdentifierResolver = tenantIdentifierResolver;
    }

    private String resolveTenantIdentifier() {
        if (DomibusCacheKeysFactory.tenantIdentifierResolver == null) { // single-tenancy
            LOG.trace("Resolving the tenant identifier skipped in single tenancy");
            return null;
        } else { // multi-tenancy
            String tenantIdentifier = DomibusCacheKeysFactory.tenantIdentifierResolver.resolveCurrentTenantIdentifier();
            LOG.trace("Tenant identifier resolved to [{}]", tenantIdentifier);
            return tenantIdentifier;
        }
    }

}
