package eu.domibus.core.plugin.routing;

import eu.domibus.api.routing.BackendFilter;
import eu.domibus.api.routing.RoutingCriteria;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.core.plugin.routing.dao.BackendFilterDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.NotificationListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Walczac
 */
@Service
public class RoutingService {
    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(RoutingService.class);

    @Autowired
    private BackendFilterDao backendFilterDao;

    @Autowired
    private List<NotificationListener> notificationListeners;

    @Autowired
    private DomainCoreConverter coreConverter;

    @Autowired
    protected BackendNotificationService backendNotificationService;

    /**
     * Returns the configured backend filters present in the classpath
     *
     * @return The configured backend filters
     */
    @Cacheable(value = "backendFilterCache")
    public List<BackendFilter> getBackendFilters() {
        return getBackendFiltersUncached();
    }

    public List<BackendFilter> getBackendFiltersUncached() {
        final List<BackendFilterEntity> filters = new ArrayList<>(backendFilterDao.findAll());
        final List<NotificationListener> backendsTemp = new ArrayList<>(notificationListeners);

        for (BackendFilterEntity filter : filters) {
            for (final NotificationListener backend : backendsTemp) {
                if (filter.getBackendName().equalsIgnoreCase(backend.getBackendName())) {
                    backendsTemp.remove(backend);
                    break;
                }
            }
        }

        for (final NotificationListener backend : backendsTemp) {
            final BackendFilterEntity filter = new BackendFilterEntity();
            filter.setBackendName(backend.getBackendName());
            filters.add(filter);
        }
        return coreConverter.convert(filters, BackendFilter.class);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @CacheEvict(value = "backendFilterCache", allEntries = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_AP_ADMIN')")
    public void updateBackendFilters(final List<BackendFilter> filters) {

        validateFilters(filters);

        List<BackendFilterEntity> backendFilterEntities = coreConverter.convert(filters, BackendFilterEntity.class);
        List<BackendFilterEntity> allBackendFilterEntities = backendFilterDao.findAll();
        List<BackendFilterEntity> backendFilterEntityListToDelete = backendFiltersToDelete(allBackendFilterEntities, backendFilterEntities);
        backendFilterDao.deleteAll(backendFilterEntityListToDelete);
        backendFilterDao.update(backendFilterEntities);

        backendNotificationService.invalidateBackendFiltersCache();
    }

    protected void validateFilters(List<BackendFilter> filters) {
        LOG.trace("Validating backend filters");

        filters.forEach(filter -> {
            if (filters.stream().anyMatch(f -> f != filter
                    && f.getBackendName().equals(filter.getBackendName())
                    && areEqual(f.getRoutingCriterias(), filter.getRoutingCriterias()))) {
                LOG.debug("Two message filters have the same type and criteria: [{}]", filter.getBackendName());
                throw new ConfigurationException("Two message filters cannot have the same criteria.");
            }
        });
    }

    private boolean areEqual(List<RoutingCriteria> c1, List<RoutingCriteria> c2) {
        LOG.trace("Comparing 2 filter criteria");

        if (c1.size() != c2.size()) {
            LOG.trace("Filter criteria have different size, hence false for comparing [{}] and [{}]", c1, c2);
            return false;
        }
        for (RoutingCriteria cr1 : c1) {
            if (!c2.stream().anyMatch(cr2 -> cr2.getName().equals(cr1.getName()) && cr2.getExpression().equals(cr1.getExpression()))) {
                LOG.trace("Filter criteria have different property name or value, hence false for comparing [{}] and [{}]", c1, c2);
                return false;
            }
        }
        LOG.trace("Filter criteria have the same properties and values, hence true for comparing [{}] and [{}]", c1, c2);
        return true;
    }

    private List<BackendFilterEntity> backendFiltersToDelete(final List<BackendFilterEntity> masterData, final List<BackendFilterEntity> newData) {
        List<BackendFilterEntity> result = new ArrayList<>(masterData);
        result.removeAll(newData);
        return result;
    }
}
