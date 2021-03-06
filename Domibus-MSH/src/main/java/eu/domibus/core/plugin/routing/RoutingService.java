package eu.domibus.core.plugin.routing;

import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.routing.BackendFilter;
import eu.domibus.api.routing.RoutingCriteria;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.core.plugin.BackendConnectorProvider;
import eu.domibus.core.plugin.notification.BackendPluginEnum;
import eu.domibus.core.plugin.routing.dao.BackendFilterDao;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.BackendConnector;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Arrays.stream;
import static java.util.Comparator.comparing;

/**
 * @author Christian Walczac
 * @author Cosmin Baciu
 */
@Service
public class RoutingService {
    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(RoutingService.class);

    @Autowired
    protected BackendFilterDao backendFilterDao;

    @Autowired
    protected BackendConnectorProvider backendConnectorProvider;

    @Autowired
    protected DomainCoreConverter coreConverter;

    @Autowired
    protected List<CriteriaFactory> routingCriteriaFactories;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    protected SignalService signalService;

    protected Map<String, IRoutingCriteria> criteriaMap;
    protected final Object backendFiltersCacheLock = new Object();
    protected volatile Map<Domain, List<BackendFilter>> backendFiltersCache = new HashMap<>();

    @PostConstruct
    public void init() {
        criteriaMap = new HashMap<>();
        for (final CriteriaFactory routingCriteriaFactory : routingCriteriaFactories) {
            criteriaMap.put(routingCriteriaFactory.getName(), routingCriteriaFactory.getInstance());
        }
    }

    public void invalidateBackendFiltersCache() {
        Domain currentDomain = domainContextProvider.getCurrentDomain();
        LOG.debug("Invalidating the backend filter cache for domain [{}]", currentDomain);
        backendFiltersCache.remove(currentDomain);
    }

    protected List<BackendFilter> getBackendFiltersWithCache() {
        final Domain currentDomain = domainContextProvider.getCurrentDomain();
        LOG.trace("Get backend filters with cache for domain [{}]", currentDomain);
        List<BackendFilter> backendFilters = backendFiltersCache.get(currentDomain);

        if (backendFilters == null) {
            synchronized (backendFiltersCacheLock) {
                // retrieve again from map, otherwise it is null even for the second thread(because the variable has method scope)
                backendFilters = backendFiltersCache.get(currentDomain);
                if (backendFilters == null) {
                    LOG.debug("Initializing backendFilterCache for domain [{}]", currentDomain);
                    backendFilters = getBackendFiltersUncached();
                    backendFiltersCache.put(currentDomain, backendFilters);
                }
            }
        }
        return backendFilters;
    }

    /**
     * Create backend filters for the installed plugins that do not have one already created
     */
    @Transactional
    public void createBackendFilters() {
        LOG.debug("Checking backend filters");

        List<BackendFilterEntity> backendFilterEntitiesInDB = backendFilterDao.findAll();
        LOG.debug("Found backend filters in database [{}]", backendFilterEntitiesInDB);

        List<String> pluginToAdd = backendConnectorProvider.getBackendConnectors()
                .stream()
                .map(BackendConnector::getName)
                .collect(Collectors.toList());
        LOG.debug("Found configured plugins [{}]", pluginToAdd);

        LOG.debug("Checking if any existing database plugins are already removed from the plugin location");

        List<BackendFilterEntity> dbFiltersNotInBackendConnectors = backendFilterEntitiesInDB.stream().filter(
                backendFilterEntity -> pluginToAdd.stream().noneMatch(plugin -> StringUtils.equalsIgnoreCase(plugin, backendFilterEntity.getBackendName()))).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(dbFiltersNotInBackendConnectors)) {
            LOG.debug("Deleting backend filters from database as its already removed from the plugin location [{}]", dbFiltersNotInBackendConnectors);
            backendFilterDao.delete(dbFiltersNotInBackendConnectors);
            LOG.debug("Finished deleting backend filters");

            backendFilterEntitiesInDB.removeAll(dbFiltersNotInBackendConnectors);
            if (!CollectionUtils.isEmpty(backendFilterEntitiesInDB)) {
                LOG.info("Updating backend filter indices for [{}]", backendFilterEntitiesInDB);

                updateFilterIndices(backendFilterEntitiesInDB);
                backendFilterDao.update(backendFilterEntitiesInDB);

                LOG.info("Finished updating backend filter indices");
            }
        }
        pluginToAdd.removeAll(backendFilterEntitiesInDB.stream().map(BackendFilterEntity::getBackendName).collect(Collectors.toList()));

        if (CollectionUtils.isNotEmpty(pluginToAdd)) {
            List<BackendFilterEntity> backendFilterEntities = buildBackendFilterEntities(pluginToAdd, getMaxIndex(backendFilterEntitiesInDB) + 1);
            LOG.debug("Creating backend filters [{}]", backendFilterEntities);

            backendFilterDao.create(backendFilterEntities);
            LOG.debug("Finished creating backend filters");
        }

        LOG.debug("Finished checking backend filters");
    }

    protected int getMaxIndex(List<BackendFilterEntity> backendFilterEntitiesInDB) {
        if (CollectionUtils.isEmpty(backendFilterEntitiesInDB)) {
            return 0;
        }
        return backendFilterEntitiesInDB
                .stream()
                .map(BackendFilterEntity::getIndex)
                .max(Integer::compareTo)
                .orElse(0);
    }

    /**
     * Assigning priorities to the default plugin, which doesn't have any priority set by User
     *
     * @return backendFilters
     */
    protected List<BackendFilterEntity> buildBackendFilterEntities(List<String> pluginList, int priority) {
        if (CollectionUtils.isEmpty(pluginList)) {
            return new ArrayList<>();
        }

        List<String> defaultPluginOrderList = stream(BackendPluginEnum.values())
                .sorted(comparing(BackendPluginEnum::getPriority))
                .map(BackendPluginEnum::getPluginName)
                .collect(Collectors.toList());
        // If plugin not part of the list of default plugin, it will be put in highest priority by default
        pluginList.sort(comparing(defaultPluginOrderList::indexOf));
        LOG.debug("Assigning lower priorities (over [{}]) to the backend plugins which don't have any existing priority", priority);

        List<BackendFilterEntity> backendFilters = new ArrayList<>();
        for (String pluginName : pluginList) {
            LOG.debug("Assigning priority [{}] to the backend plugin [{}].", priority, pluginName);
            BackendFilterEntity filterEntity = new BackendFilterEntity();
            filterEntity.setBackendName(pluginName);
            filterEntity.setIndex(priority++);
            backendFilters.add(filterEntity);
        }
        return backendFilters;
    }

    public List<BackendFilter> getBackendFiltersUncached() {
        List<BackendFilterEntity> backendFilterEntities = backendFilterDao.findAll();
        return coreConverter.convert(backendFilterEntities, BackendFilter.class);
    }

    public BackendFilter getMatchingBackendFilter(final UserMessage userMessage) {
        List<BackendFilter> backendFilters = getBackendFiltersWithCache();
        return getMatchingBackendFilter(backendFilters, criteriaMap, userMessage);

    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_AP_ADMIN')")
    public void updateBackendFilters(final List<BackendFilter> filters) {
        validateFilters(filters);

        List<BackendFilterEntity> allBackendFilterEntities = backendFilterDao.findAll();
        backendFilterDao.delete(allBackendFilterEntities);

        List<BackendFilterEntity> backendFilterEntities = coreConverter.convert(filters, BackendFilterEntity.class);
        updateFilterIndices(backendFilterEntities);
        backendFilterDao.update(backendFilterEntities);

        invalidateBackendFiltersCache();
        signalService.signalMessageFiltersUpdated();
    }

    protected BackendFilter getMatchingBackendFilter(final List<BackendFilter> backendFilters, final Map<String, IRoutingCriteria> criteriaMap, final UserMessage userMessage) {
        LOG.debug("Getting the backend filter for message [" + userMessage.getMessageInfo().getMessageId() + "]");
        for (final BackendFilter filter : backendFilters) {
            final boolean backendFilterMatching = isBackendFilterMatching(filter, criteriaMap, userMessage);
            if (backendFilterMatching) {
                LOG.debug("Filter [{}] matched for message [{}]", filter, userMessage.getMessageInfo().getMessageId());
                return filter;
            }
        }
        LOG.trace("No filter matched for message [{}]", userMessage.getMessageInfo().getMessageId());
        return null;
    }

    protected boolean isBackendFilterMatching(BackendFilter filter, Map<String, IRoutingCriteria> criteriaMap, final UserMessage userMessage) {
        if (filter.getRoutingCriterias() != null) {
            for (final RoutingCriteria routingCriteriaEntity : filter.getRoutingCriterias()) {
                final IRoutingCriteria criteria = criteriaMap.get(StringUtils.upperCase(routingCriteriaEntity.getName()));
                boolean matches = criteria.matches(userMessage, routingCriteriaEntity.getExpression());
                //if at least one criteria does not match it means the filter is not matching
                if (!matches) {
                    return false;
                }
            }
        }
        return true;
    }

    protected void updateFilterIndices(List<BackendFilterEntity> filters) {
        IntStream.range(0, filters.size()).forEach(index -> filters.get(index).setIndex(index + 1));
    }

    protected void validateFilters(List<BackendFilter> filters) {
        LOG.trace("Validating backend filters");

        ensureAtLeastOneFilterForEachPlugin(filters);

        ensureUnicity(filters);
    }

    protected void ensureUnicity(List<BackendFilter> filters) {
        filters.forEach(filter1 -> {
            if (filters.stream().anyMatch(filter2 -> filter2 != filter1
                    && filter2.getBackendName().equals(filter1.getBackendName())
                    && areEqual(filter2.getRoutingCriterias(), filter1.getRoutingCriterias()))) {
                throw new ConfigurationException("Two message filters cannot have the same criteria." + filter1.getBackendName());
            }
        });
    }

    protected void ensureAtLeastOneFilterForEachPlugin(List<BackendFilter> filters) {
        List<String> pluginNames = backendConnectorProvider.getBackendConnectors().stream()
                .map(BackendConnector::getName)
                .collect(Collectors.toList());
        List<String> missingFilterNames = pluginNames.stream().filter(pluginName -> filters.stream().noneMatch(filter -> filter.getBackendName().equals(pluginName))).collect(Collectors.toList());
        if (missingFilterNames.size() > 0) {
            throw new ConfigurationException("Each installed plugin must have at least one filter and the following do not: "
                    + StringUtils.join(missingFilterNames, ","));
        }
    }

    protected boolean areEqual(List<RoutingCriteria> c1, List<RoutingCriteria> c2) {
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

}


