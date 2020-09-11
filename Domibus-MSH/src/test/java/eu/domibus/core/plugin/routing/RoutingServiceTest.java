package eu.domibus.core.plugin.routing;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.routing.BackendFilter;
import eu.domibus.api.routing.RoutingCriteria;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.core.plugin.BackendConnectorProvider;
import eu.domibus.core.plugin.routing.dao.BackendFilterDao;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.plugin.BackendConnector;
import eu.domibus.plugin.NotificationListener;
import eu.domibus.plugin.notification.AsyncNotificationConfiguration;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.util.*;

import static eu.domibus.core.plugin.notification.BackendPluginEnum.*;
import static java.util.Arrays.asList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * @author Ion Perpegel
 * @author Cosmin Baciu
 * @since 4.1
 */
@RunWith(JMockit.class)
public class RoutingServiceTest {

    public static final int MAX_INDEX = 10;
    public static final String MESSAGE_ID = "MessageId";

//    @Tested
//    RoutingService routingService;

    @Injectable
    protected BackendConnectorProvider backendConnectorProvider;

    @Injectable
    private BackendFilterDao backendFilterDao;

    @Injectable
    private DomainCoreConverter coreConverter;

    @Injectable
    protected DomibusConfigurationService domibusConfigurationService;

    @Injectable
    protected DomainService domainService;

    @Injectable
    protected DomainTaskExecutor domainTaskExecutor;

    @Injectable
    protected List<CriteriaFactory> routingCriteriaFactories;

    @Injectable
    protected DomainContextProvider domainContextProvider;

    @Injectable
    AuthUtils authUtils;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test(expected = ConfigurationException.class)
    public void ensureUnicityThrowsError() {
        BackendFilter bf1 = new BackendFilter();
        bf1.setBackendName("wsPlugin");
        List<RoutingCriteria> rc = Arrays.asList(
                new RoutingCriteria() {{
                    setName("rc1Name");
                    setExpression("rc1Expression");
                }},
                new RoutingCriteria() {{
                    setName("rc2Name");
                    setExpression("rc2Expression");
                }});
        bf1.setRoutingCriterias(rc);

        BackendFilter bf2 = new BackendFilter();
        bf2.setBackendName("wsPlugin");
        List<RoutingCriteria> rcc = Arrays.asList(
                new RoutingCriteria() {{
                    setName("rc1Name");
                    setExpression("rc1Expression");
                }},
                new RoutingCriteria() {{
                    setName("rc2Name");
                    setExpression("rc2Expression");
                }});
        bf2.setRoutingCriterias(rcc);

        BackendFilter bf3 = new BackendFilter();
        bf3.setBackendName("wsPlugin");
        List<RoutingCriteria> rcd = Arrays.asList(
                new RoutingCriteria() {{
                    setName("rc1Name");
                    setExpression("rc1Expression");
                }},
                new RoutingCriteria() {{
                    setName("rc2Name");
                    setExpression("rc2Expression");
                }},
                new RoutingCriteria() {{
                    setName("rc3Name");
                    setExpression("rc3Expression");
                }});
        bf3.setRoutingCriterias(rcd);

        RoutingService routingService = new RoutingService();
        routingService.ensureUnicity(Arrays.asList(bf1, bf2, bf3));
    }

    @Test()
    public void ensureUnicity() {
        BackendFilter bf1 = new BackendFilter();
        bf1.setBackendName("wsPlugin");
        List<RoutingCriteria> rc = Arrays.asList(
                new RoutingCriteria() {{
                    setName("rc1Name");
                    setExpression("rc1Expression");
                }},
                new RoutingCriteria() {{
                    setName("rc2Name");
                    setExpression("rc2Expression");
                }});
        bf1.setRoutingCriterias(rc);

        BackendFilter bf2 = new BackendFilter();
        bf2.setBackendName("wsPlugin");
        List<RoutingCriteria> rcc = Arrays.asList(
                new RoutingCriteria() {{
                    setName("rc1Name");
                    setExpression("rc1Expression");
                }},
                new RoutingCriteria() {{
                    setName("rc2Name");
                    setExpression("rc2ExpressionDifferent");
                }});
        bf2.setRoutingCriterias(rcc);

        RoutingService routingService = new RoutingService();
        routingService.ensureUnicity(Arrays.asList(bf1, bf2));
    }

    @Test
    public void invalidateBackendFiltersCache() {
        RoutingService routingService = new RoutingService();
        routingService.domainContextProvider = domainContextProvider;

        Domain domain1 = new Domain("D1", "DOMAIN1");
        Domain domain2 = new Domain("D2", "DOMAIN2");
        routingService.backendFiltersCache = new HashMap<>();
        routingService.backendFiltersCache.put(domain1, new ArrayList<>());
        routingService.backendFiltersCache.put(domain2, new ArrayList<>());

        assertEquals(2, routingService.backendFiltersCache.size());

        new Expectations() {{
            domainContextProvider.getCurrentDomain();
            result = domain1;
        }};

        routingService.invalidateBackendFiltersCache();

        assertEquals(1, routingService.backendFiltersCache.size());
        assertNull(routingService.backendFiltersCache.get(domain1));
        assertNotNull(routingService.backendFiltersCache.get(domain2));
    }

    @Test
    public void getBackendFilters_backendFilterNotEmptyInDao() {
        RoutingService routingService = new RoutingService();
        routingService.backendFilterDao = backendFilterDao;
        routingService.coreConverter = coreConverter;

        ArrayList<BackendFilterEntity> backendFilterEntityList = new ArrayList<>();
        backendFilterEntityList.add(new BackendFilterEntity());

        ArrayList<BackendFilter> backendFilters = new ArrayList<>();

        new Expectations() {{
            backendFilterDao.findAll();
            result = backendFilterEntityList;

            coreConverter.convert(backendFilterEntityList, BackendFilter.class);
            result = backendFilters;
        }};
        List<BackendFilter> actual = routingService.getBackendFiltersUncached();

        assertEquals(backendFilters, actual);

        new FullVerifications() {
        };
    }

    @Test
    public void getBackendFilters_return1(@Injectable BackendFilterEntity backendFilterEntity) {
        RoutingService routingService = new RoutingService();
        routingService.backendFilterDao = backendFilterDao;
        routingService.coreConverter = coreConverter;

        ArrayList<BackendFilterEntity> backendFilterEntityList = new ArrayList<>();
        backendFilterEntityList.add(backendFilterEntity);

        new Expectations() {{
            backendFilterDao.findAll();
            result = backendFilterEntityList;
        }};

        routingService.getBackendFiltersUncached();

        new FullVerifications() {{
            coreConverter.convert(backendFilterEntityList, BackendFilter.class);
        }};
    }


    @Test
    public void testIsBackendFilterMatchingANDOperationWithFromAndActionMatching(@Injectable final BackendFilter filter,
                                                                                 @Injectable final Map<String, IRoutingCriteria> criteriaMap,
                                                                                 @Injectable final UserMessage userMessage,
                                                                                 @Injectable final IRoutingCriteria fromRoutingCriteriaConfiguration,
                                                                                 @Injectable final IRoutingCriteria actionRoutingCriteriaConfiguration,
                                                                                 @Injectable final RoutingCriteria fromRoutingCriteria, //contains the FROM filter defined by the user
                                                                                 @Injectable final RoutingCriteria actionRoutingCriteria) { //contains the ACTION filter defined by the user

        RoutingService routingService = new RoutingService();

        // these 2 filters are defined by the user in the Message Filter screen
        final List<RoutingCriteria> criteriaList = new ArrayList<>();
        criteriaList.add(fromRoutingCriteria);
        criteriaList.add(actionRoutingCriteria);

        final String fromCriteriaName = "FROM";
        final String actionCriteriaName = "ACTION";

        new Expectations() {{
            filter.getRoutingCriterias();
            result = criteriaList;

            // TODO: Criteria Operator is not used.
            /*filter.getCriteriaOperator();
            result = LogicalOperator.AND;*/

            fromRoutingCriteria.getName();
            result = fromCriteriaName;

            fromRoutingCriteria.getExpression();
            result = "domibus-blue:partyType";

            criteriaMap.get(fromCriteriaName);
            result = fromRoutingCriteriaConfiguration;

            actionRoutingCriteria.getName();
            result = actionCriteriaName;

            actionRoutingCriteria.getExpression();
            result = "myAction";

            criteriaMap.get(actionCriteriaName);
            result = actionRoutingCriteriaConfiguration;

            fromRoutingCriteriaConfiguration.matches(userMessage, fromRoutingCriteria.getExpression());
            result = true;

            actionRoutingCriteriaConfiguration.matches(userMessage, actionRoutingCriteria.getExpression());
            result = true;
        }};

        final boolean backendFilterMatching = routingService.isBackendFilterMatching(filter, criteriaMap, userMessage);
        Assert.assertTrue(backendFilterMatching);
    }

    @Test
    public void testIsBackendFilterMatchingANDOperationWithFromMatchingAndActionNotMatching(@Injectable final BackendFilter filter,
                                                                                            @Injectable final Map<String, IRoutingCriteria> criteriaMap,
                                                                                            @Injectable final UserMessage userMessage,
                                                                                            @Injectable final IRoutingCriteria fromRoutingCriteriaConfiguration,
                                                                                            @Injectable final IRoutingCriteria actionRoutingCriteriaConfiguration,
                                                                                            @Injectable final RoutingCriteria fromRoutingCriteria, //contains the FROM filter defined by the user
                                                                                            @Injectable final RoutingCriteria actionRoutingCriteria) { //contains the ACTION filter defined by the user

        RoutingService routingService = new RoutingService();

        // these 2 filters are defined by the user in the Message Filter screen
        final List<RoutingCriteria> criteriaList = new ArrayList<>();
        criteriaList.add(fromRoutingCriteria);
        criteriaList.add(actionRoutingCriteria);

        final String fromCriteriaName = "FROM";
        final String actionCriteriaName = "ACTION";

        new Expectations() {{
            filter.getRoutingCriterias();
            result = criteriaList;

            //filter.getCriteriaOperator();
            //result = LogicalOperator.AND;

            fromRoutingCriteria.getName();
            result = fromCriteriaName;

            fromRoutingCriteria.getExpression();
            result = "domibus-blue:partyType";

            criteriaMap.get(fromCriteriaName);
            result = fromRoutingCriteriaConfiguration;

            actionRoutingCriteria.getName();
            result = actionCriteriaName;

            actionRoutingCriteria.getExpression();
            result = "myAction";

            criteriaMap.get(actionCriteriaName);
            result = actionRoutingCriteriaConfiguration;

            fromRoutingCriteriaConfiguration.matches(userMessage, fromRoutingCriteria.getExpression());
            result = true;

            actionRoutingCriteriaConfiguration.matches(userMessage, actionRoutingCriteria.getExpression());
            result = false;
        }};

        final boolean backendFilterMatching = routingService.isBackendFilterMatching(filter, criteriaMap, userMessage);
        Assert.assertFalse(backendFilterMatching);
    }


    @Test
    public void testIsBackendFilterMatchingANDOperationWithFromNotMatching(@Injectable final BackendFilter filter,
                                                                           @Injectable final Map<String, IRoutingCriteria> criteriaMap,
                                                                           @Injectable final UserMessage userMessage,
                                                                           @Injectable final IRoutingCriteria fromRoutingCriteriaConfiguration,
                                                                           @Injectable final IRoutingCriteria actionRoutingCriteriaConfiguration,
                                                                           @Injectable final RoutingCriteria fromRoutingCriteria, //contains the FROM filter defined by the user
                                                                           @Injectable final RoutingCriteria actionRoutingCriteria) { //contains the ACTION filter defined by the user

        RoutingService routingService = new RoutingService();

        // these 2 filters are defined by the user in the Message Filter screen
        final List<RoutingCriteria> criteriaList = new ArrayList<>();
        criteriaList.add(fromRoutingCriteria);
        criteriaList.add(actionRoutingCriteria);

        final String fromCriteriaName = "FROM";
        final String actionCriteriaName = "ACTION";

        new Expectations() {{
            filter.getRoutingCriterias();
            result = criteriaList;

            //filter.getCriteriaOperator();
            //result = LogicalOperator.AND;

            fromRoutingCriteria.getName();
            result = fromCriteriaName;

            fromRoutingCriteria.getExpression();
            result = "domibus-blue:partyType";

            criteriaMap.get(fromCriteriaName);
            result = fromRoutingCriteriaConfiguration;

            fromRoutingCriteriaConfiguration.matches(userMessage, fromRoutingCriteria.getExpression());
            result = false;
        }};

        final boolean backendFilterMatching = routingService.isBackendFilterMatching(filter, criteriaMap, userMessage);
        Assert.assertFalse(backendFilterMatching);

        new FullVerifications() {{
            criteriaMap.get(actionCriteriaName);
            times = 0;

            actionRoutingCriteriaConfiguration.matches(userMessage, anyString);
            times = 0;
        }};
    }

    @Test
    public void testIsBackendFilterMatchingWithFromMatchingAndActionNotMatching(@Injectable final BackendFilter filter,
                                                                                @Injectable final Map<String, IRoutingCriteria> criteriaMap,
                                                                                @Injectable final UserMessage userMessage,
                                                                                @Injectable final IRoutingCriteria fromRoutingCriteriaConfiguration,
                                                                                @Injectable final IRoutingCriteria actionRoutingCriteriaConfiguration,
                                                                                @Injectable final RoutingCriteria fromRoutingCriteria, //contains the FROM filter defined by the user
                                                                                @Injectable final RoutingCriteria actionRoutingCriteria) { //contains the ACTION filter defined by the user

        RoutingService routingService = new RoutingService();

        // these 2 filters are defined by the user in the Message Filter screen
        final List<RoutingCriteria> criteriaList = new ArrayList<>();
        criteriaList.add(fromRoutingCriteria);
        criteriaList.add(actionRoutingCriteria);

        final String fromCriteriaName = "FROM";
        final String actionCriteriaName = "ACTION";

        new Expectations() {{
            filter.getRoutingCriterias();
            result = criteriaList;

            //filter.getCriteriaOperator();
            //result = LogicalOperator.OR;

            fromRoutingCriteria.getName();
            result = fromCriteriaName;

            fromRoutingCriteria.getExpression();
            result = "domibus-blue:partyType";

            criteriaMap.get(fromCriteriaName);
            result = fromRoutingCriteriaConfiguration;

            fromRoutingCriteriaConfiguration.matches(userMessage, fromRoutingCriteria.getExpression());
            result = true;

            actionRoutingCriteria.getName();
            result = actionCriteriaName;

            actionRoutingCriteria.getExpression();
            result = "domibus-blue:partyType";

            criteriaMap.get(actionCriteriaName);
            result = actionRoutingCriteriaConfiguration;

            actionRoutingCriteriaConfiguration.matches(userMessage, actionRoutingCriteria.getExpression());
            result = false;
        }};

        final boolean backendFilterMatching = routingService.isBackendFilterMatching(filter, criteriaMap, userMessage);
        Assert.assertFalse(backendFilterMatching);

        new FullVerifications() {{
            actionRoutingCriteriaConfiguration.matches(userMessage, anyString);
            times = 1;
        }};
    }

    @Test
    public void testIsBackendFilterMatchingWithNoRoutingCriteriaDefined(@Injectable final BackendFilter filter,
                                                                        @Injectable final Map<String, IRoutingCriteria> criteriaMap,
                                                                        @Injectable final UserMessage userMessage,
                                                                        @Injectable final IRoutingCriteria fromRoutingCriteriaConfiguration,
                                                                        @Injectable final IRoutingCriteria actionRoutingCriteriaConfiguration,
                                                                        @Injectable final RoutingCriteria fromRoutingCriteria, //contains the FROM filter defined by the user
                                                                        @Injectable final RoutingCriteria actionRoutingCriteria) { //contains the ACTION filter defined by the user

        RoutingService routingService = new RoutingService();

        new Expectations() {{
            filter.getRoutingCriterias();
            result = null;
        }};

        final boolean backendFilterMatching = routingService.isBackendFilterMatching(filter, criteriaMap, userMessage);
        Assert.assertTrue(backendFilterMatching);

        new FullVerifications() {{
            criteriaMap.get(anyString);
            times = 0;
        }};
    }

    @Test
    public void testIsBackendFilterMatchingANDOperationWithFromNotMatchingAndActionMatching(@Injectable final BackendFilter filter,
                                                                                            @Injectable final Map<String, IRoutingCriteria> criteriaMap,
                                                                                            @Injectable final UserMessage userMessage,
                                                                                            @Injectable final IRoutingCriteria fromRoutingCriteriaConfiguration,
                                                                                            @Injectable final IRoutingCriteria actionRoutingCriteriaConfiguration,
                                                                                            @Injectable final RoutingCriteria fromRoutingCriteria, //contains the FROM filter defined by the user
                                                                                            @Injectable final RoutingCriteria actionRoutingCriteria) { //contains the ACTION filter defined by the user
        RoutingService routingService = new RoutingService();

        // these 2 filters are defined by the user in the Message Filter screen
        final List<RoutingCriteria> criteriaList = new ArrayList<>();
        criteriaList.add(fromRoutingCriteria);
        criteriaList.add(actionRoutingCriteria);

        final String fromCriteriaName = "FROM";

        new Expectations() {{
            filter.getRoutingCriterias();
            result = criteriaList;

            fromRoutingCriteria.getName();
            result = fromCriteriaName;

            fromRoutingCriteria.getExpression();
            result = "domibus-blue:partyType";

            criteriaMap.get(fromCriteriaName);
            result = fromRoutingCriteriaConfiguration;

            fromRoutingCriteriaConfiguration.matches(userMessage, fromRoutingCriteria.getExpression());
            result = false;
        }};

        final boolean backendFilterMatching = routingService.isBackendFilterMatching(filter, criteriaMap, userMessage);
        Assert.assertFalse(backendFilterMatching);
    }

    @Test
    public void testGetMatchingBackendFilter(@Injectable final UserMessage userMessage,
                                             @Injectable final List<BackendFilter> backendFilters) {
        RoutingService routingService = new RoutingService();

        new Expectations(routingService) {{
            routingService.getBackendFiltersWithCache();
            result = backendFilters;
            routingService.getMatchingBackendFilter(backendFilters, withAny(new HashMap<>()), userMessage);
        }};

        routingService.getMatchingBackendFilter(userMessage);

        new FullVerifications() {
        };
    }

    @Test
    public void testInitWithOutEmptyBackendFilter(@Injectable CriteriaFactory criteriaFactory,
                                                  @Injectable IRoutingCriteria iRoutingCriteria,
                                                  @Injectable BackendConnector backendConnector,
                                                  @Injectable BackendConnectorProvider backendConnectorProvider) {
        RoutingService routingService = new RoutingService();

        List<CriteriaFactory> routingCriteriaFactories = new ArrayList<>();
        routingCriteriaFactories.add(criteriaFactory);
        routingService.routingCriteriaFactories = routingCriteriaFactories;
        routingService.domibusConfigurationService = domibusConfigurationService;
        routingService.domainService = domainService;
        routingService.domainTaskExecutor = domainTaskExecutor;
        routingService.backendConnectorProvider = backendConnectorProvider;

        List<BackendConnector> backendConnectors = new ArrayList<>();
        backendConnectors.add(backendConnector);

        new Expectations(routingService) {{
            backendConnectorProvider.getBackendConnectors();
            result = backendConnectors;

            domibusConfigurationService.isSingleTenantAware();
            result = true;

            criteriaFactory.getName();
            result = "Name criteriaFactory";

            criteriaFactory.getInstance();
            result = iRoutingCriteria;

            routingService.createMissingBackendFilters();
            times = 1;
        }};

        routingService.init();

        new FullVerifications() {
        };
    }

    @Test
    public void testInitWithBackendFilterInMultitenancyEnv(@Injectable CriteriaFactory routingCriteriaFactory,
                                                           @Injectable Domain domain,
                                                           @Injectable IRoutingCriteria iRoutingCriteria,
                                                           @Injectable BackendConnectorProvider backendConnectorProvider,
                                                           @Injectable BackendConnector backendConnector) {
        RoutingService routingService = new RoutingService();

        List<CriteriaFactory> routingCriteriaFactories = new ArrayList<>();
        routingCriteriaFactories.add(routingCriteriaFactory);
        routingService.routingCriteriaFactories = routingCriteriaFactories;
        List<Domain> domains = new ArrayList<>();
        domains.add(domain);

        routingService.routingCriteriaFactories = routingCriteriaFactories;
        routingService.domibusConfigurationService = domibusConfigurationService;
        routingService.domainService = domainService;
        routingService.domainTaskExecutor = domainTaskExecutor;
        routingService.backendConnectorProvider = backendConnectorProvider;

        List<BackendConnector> backendConnectors = new ArrayList<>();
        backendConnectors.add(backendConnector);

        new Expectations(routingService) {{
            backendConnectorProvider.getBackendConnectors();
            result = backendConnectors;

            domibusConfigurationService.isSingleTenantAware();
            result = false;

            domainService.getDomains();
            result = domains;

            routingCriteriaFactory.getName();
            result = anyString;

            routingCriteriaFactory.getInstance();
            result = iRoutingCriteria;
        }};

        routingService.init();

        new FullVerifications() {{
            domainTaskExecutor.submit((Runnable) any, domain);
            minTimes = 1;
        }};

    }

    @Test
    public void testInit_noNotificationListenerBeanMap(@Injectable BackendConnectorProvider backendConnectorProvider,
                                                       @Injectable CriteriaFactory routingCriteriaFactory,
                                                       @Injectable BackendFilterEntity backendFilterEntity) {

        RoutingService routingService = new RoutingService();
        routingService.backendConnectorProvider = backendConnectorProvider;

        new Expectations(routingService) {{
        }};

        thrown.expect(ConfigurationException.class);
        routingService.init();

        new FullVerifications() {
        };
    }

    @Test
    public void testInitMultiAware(@Injectable CriteriaFactory routingCriteriaFactory,
                                   @Injectable Domain domain,
                                   @Injectable IRoutingCriteria iRoutingCriteria,
                                   @Injectable BackendConnector backendConnector,
                                   @Injectable BackendConnectorProvider backendConnectorProvider) {
        RoutingService routingService = new RoutingService();

        List<CriteriaFactory> routingCriteriaFactories = new ArrayList<>();
        routingCriteriaFactories.add(routingCriteriaFactory);
        routingService.routingCriteriaFactories = routingCriteriaFactories;
        List<Domain> domains = new ArrayList<>();
        domains.add(domain);

        routingService.routingCriteriaFactories = routingCriteriaFactories;
        routingService.domibusConfigurationService = domibusConfigurationService;
        routingService.domainService = domainService;
        routingService.domainTaskExecutor = domainTaskExecutor;
        routingService.backendConnectorProvider = backendConnectorProvider;

        List<BackendConnector> backendConnectors = new ArrayList<>();
        backendConnectors.add(backendConnector);


        new Expectations(routingService) {{
            backendConnectorProvider.getBackendConnectors();
            result = backendConnectors;

            domibusConfigurationService.isSingleTenantAware();
            result = false;

            domainService.getDomains();
            result = Collections.singletonList(domain);

            routingCriteriaFactory.getName();
            result = "routingCriteriaFactory";

            routingCriteriaFactory.getInstance();
            result = null;
        }};

        routingService.init();

        new FullVerifications() {{
            domainTaskExecutor.submit((Runnable) any, domain);
            times = 1;
        }};
    }

    @Test
    public void testInit_NonMultiTenancy(@Injectable CriteriaFactory routingCriteriaFactory,
                                         @Injectable Domain domain,
                                         @Injectable IRoutingCriteria iRoutingCriteria,
                                         @Injectable BackendConnector backendConnector,
                                         @Injectable BackendConnectorProvider backendConnectorProvider) {
        RoutingService routingService = new RoutingService();

        List<CriteriaFactory> routingCriteriaFactories = new ArrayList<>();
        routingCriteriaFactories.add(routingCriteriaFactory);
        routingService.routingCriteriaFactories = routingCriteriaFactories;
        List<Domain> domains = new ArrayList<>();
        domains.add(domain);

        routingService.routingCriteriaFactories = routingCriteriaFactories;
        routingService.domibusConfigurationService = domibusConfigurationService;
        routingService.domainService = domainService;
        routingService.domainTaskExecutor = domainTaskExecutor;
        routingService.backendConnectorProvider = backendConnectorProvider;

        List<BackendConnector> backendConnectors = new ArrayList<>();
        backendConnectors.add(backendConnector);

        new Expectations(routingService) {{
            backendConnectorProvider.getBackendConnectors();
            result = backendConnectors;

            domibusConfigurationService.isSingleTenantAware();
            result = true;

            routingCriteriaFactory.getName();
            result = "routingCriteriaFactory";

            routingCriteriaFactory.getInstance();
            result = null;

            routingService.createMissingBackendFilters();
        }};

        routingService.init();

        new FullVerifications() {
        };
    }

    @Test
    public void testCreateBackendFiltersBasedOnExistingUserPriority(@Injectable BackendFilterEntity backendFilterEntity,
                                                                    @Injectable AsyncNotificationConfiguration notificationListener,
                                                                    @Injectable BackendConnector backendConnector) {
        List<AsyncNotificationConfiguration> notificationListenerServices = new ArrayList<>();
        notificationListenerServices.add(notificationListener);

        List<String> notificationListenerPluginsList = new ArrayList<>();
        notificationListenerPluginsList.add(WS_PLUGIN.getPluginName());
        notificationListenerPluginsList.add(JMS_PLUGIN.getPluginName());


        RoutingService routingService = new RoutingService();


        List<BackendFilterEntity> backendFilterEntities = routingService.createBackendFilterEntities(notificationListenerPluginsList, 1);
        assertEquals(backendFilterEntities.size(), 2);

        new FullVerifications() {
        };
    }

    @Test
    public void createBackendFilterEntity_empty(@Injectable BackendFilterEntity backendFilterEntity) {
        RoutingService routingService = new RoutingService();

        List<BackendFilterEntity> backendFilters = routingService.createBackendFilterEntities(null, 1);
        assertEquals(0, backendFilters.size());
    }

    @Test
    public void createBackendFilterEntity(@Injectable BackendFilterEntity backendFilterEntity) {
        RoutingService routingService = new RoutingService();

        List<String> pluginList = new ArrayList<>();
        int priority = 4;
        pluginList.add(FS_PLUGIN.getPluginName());
        pluginList.add("TEST2");
        pluginList.add(JMS_PLUGIN.getPluginName());
        pluginList.add("TEST1");
        pluginList.add(WS_PLUGIN.getPluginName());
        pluginList.add("TEST3");

        List<BackendFilterEntity> backendFilters = routingService.createBackendFilterEntities(pluginList, priority);

        assertEquals("TEST2", backendFilters.get(0).getBackendName());
        assertEquals(4, backendFilters.get(0).getIndex());
        assertEquals("TEST1", backendFilters.get(1).getBackendName());
        assertEquals(5, backendFilters.get(1).getIndex());
        assertEquals("TEST3", backendFilters.get(2).getBackendName());
        assertEquals(6, backendFilters.get(2).getIndex());
        assertEquals(WS_PLUGIN.getPluginName(), backendFilters.get(3).getBackendName());
        assertEquals(7, backendFilters.get(3).getIndex());
        assertEquals(JMS_PLUGIN.getPluginName(), backendFilters.get(4).getBackendName());
        assertEquals(8, backendFilters.get(4).getIndex());
        assertEquals(FS_PLUGIN.getPluginName(), backendFilters.get(5).getBackendName());
        assertEquals(9, backendFilters.get(5).getIndex());
    }

    @Test
    public void createBackendFilterEntities_defaultPlugin(@Injectable NotificationListener notificationListener1,
                                                          @Injectable NotificationListener notificationListener2,
                                                          @Injectable NotificationListener notificationListener3,
                                                          @Injectable BackendFilterEntity backendFilterEntity) {
        RoutingService routingService = new RoutingService();

        List<String> pluginToAdd = asList(FS_PLUGIN.getPluginName(),
                JMS_PLUGIN.getPluginName(),
                WS_PLUGIN.getPluginName());

        List<BackendFilterEntity> allBackendFilters = routingService.createBackendFilterEntities(pluginToAdd, 0);

        assertThat(allBackendFilters.size(), is(3));
        assertThat(allBackendFilters.get(2).getBackendName(), is(FS_PLUGIN.getPluginName()));
        assertThat(allBackendFilters.get(2).getIndex(), is(2));
        assertThat(allBackendFilters.get(1).getBackendName(), is(JMS_PLUGIN.getPluginName()));
        assertThat(allBackendFilters.get(1).getIndex(), is(1));
        assertThat(allBackendFilters.get(0).getBackendName(), is(WS_PLUGIN.getPluginName()));
        assertThat(allBackendFilters.get(0).getIndex(), is(0));
    }

    @Test
    public void createBackendFilters_emptyDbEntities(@Injectable List<BackendFilterEntity> backendFilterEntities,
                                                     @Injectable BackendConnectorProvider backendConnectorProvider,
                                                     @Injectable BackendConnector backendConnector,
                                                     @Injectable BackendFilterEntity dbBackendFilterEntity) {
        RoutingService routingService = new RoutingService();
        routingService.backendFilterDao = backendFilterDao;
        routingService.backendConnectorProvider = backendConnectorProvider;
        routingService.authUtils = authUtils;

        List<BackendFilterEntity> entitiesInDb = new ArrayList<>();
        entitiesInDb.add(dbBackendFilterEntity);

        List<List<String>> pluginsToAdd = new ArrayList<>();

        new Expectations(routingService) {{
            backendFilterDao.findAll();
            result = entitiesInDb;

            dbBackendFilterEntity.getBackendName();
            result = FS_PLUGIN.getPluginName();

            backendConnectorProvider.getBackendConnectors();
            result = backendConnector;

            backendConnector.getName();
            result = JMS_PLUGIN.getPluginName();

            routingService.getMaxIndex(entitiesInDb);
            result = 1;

            routingService.createBackendFilterEntities(withCapture(pluginsToAdd), 2);
            result = backendFilterEntities;
        }};

        routingService.createMissingBackendFilters();

        new FullVerifications() {{
            authUtils.setAuthenticationToSecurityContext("domibus", "domibus", AuthRole.ROLE_AP_ADMIN);

            backendFilterDao.create(backendFilterEntities);
            times = 1;
        }};

        assertThat(pluginsToAdd.size(), is(1));
        assertThat(pluginsToAdd.get(0), CoreMatchers.hasItems(
                JMS_PLUGIN.getPluginName()));
    }

    @Test
    public void testGetBackendFiltersWithCache(@Injectable List<BackendFilter> backendFilters) {
        RoutingService routingService = new RoutingService();
        routingService.domainContextProvider = domainContextProvider;

        new Expectations(routingService) {{
            domainContextProvider.getCurrentDomain();
            result = new Domain();

            routingService.getBackendFiltersUncached();
            result = backendFilters;
        }};

        routingService.backendFiltersCache = new HashMap<>();
        List<BackendFilter> backendFiltersWithCache = routingService.getBackendFiltersWithCache();
        List<BackendFilter> backendFiltersWithCache1 = routingService.getBackendFiltersWithCache();

        assertNotNull(backendFiltersWithCache);
        assertNotNull(backendFiltersWithCache1);

        new FullVerifications() {{
            routingService.getBackendFiltersUncached();
            times = 1;
        }};
    }

    @Test
    public void getMatchingBackendFilter(
            @Injectable Map<String, IRoutingCriteria> criteriaMap,
            @Injectable UserMessage userMessage,
            @Injectable BackendFilter backendFilter1,
            @Injectable BackendFilter backendFilter2) {

        RoutingService routingService = new RoutingService();

        List<BackendFilter> backendFilters = asList(backendFilter1, backendFilter2);

        new Expectations(routingService) {{
            userMessage.getMessageInfo().getMessageId();
            result = MESSAGE_ID;

            routingService.isBackendFilterMatching(backendFilter1, criteriaMap, userMessage);
            result = false;

            routingService.isBackendFilterMatching(backendFilter2, criteriaMap, userMessage);
            result = true;
        }};

        BackendFilter matchingBackendFilter = routingService.getMatchingBackendFilter(backendFilters, criteriaMap, userMessage);

        assertEquals(backendFilter2, matchingBackendFilter);

        //No fullVerifications because of UnexpectedInvocation BackendFilter#toString()
        new Verifications() {
        };
    }

    @Test
    public void getMatchingBackendFilter_noFilters(
            @Injectable Map<String, IRoutingCriteria> criteriaMap,
            @Injectable UserMessage userMessage) {
        RoutingService routingService = new RoutingService();

        List<BackendFilter> backendFilters = new ArrayList<>();

        new Expectations() {{
            userMessage.getMessageInfo().getMessageId();
            result = MESSAGE_ID;
        }};

        BackendFilter matchingBackendFilter = routingService.getMatchingBackendFilter(backendFilters, criteriaMap, userMessage);

        assertNull(matchingBackendFilter);

        new FullVerifications() {
        };
    }

    @Test
    public void getMaxIndex_empty() {
        RoutingService routingService = new RoutingService();

        int maxIndex = routingService.getMaxIndex(new ArrayList<>());
        assertEquals(0, maxIndex);
    }

    @Test
    public void getMaxIndex_null() {
        RoutingService routingService = new RoutingService();

        int maxIndex = routingService.getMaxIndex(null);
        assertEquals(0, maxIndex);
    }

    @Test
    public void getMaxIndex_maxIndex(@Injectable BackendFilterEntity b1,
                                     @Injectable BackendFilterEntity b2,
                                     @Injectable BackendFilterEntity b3) {
        RoutingService routingService = new RoutingService();

        new Expectations() {{
            b1.getIndex();
            result = MAX_INDEX - 2;

            b2.getIndex();
            result = MAX_INDEX - 1;

            b3.getIndex();
            result = MAX_INDEX;
        }};

        int maxIndex = routingService.getMaxIndex(asList(b1, b2, b3));
        assertEquals(10, maxIndex);

        new FullVerifications() {
        };
    }

    @Test(expected = ConfigurationException.class)
    public void ensureAtLeastOneFilterForEachPluginInvalid(@Mocked BackendConnector<?, ?> bc1,
                                                           @Mocked BackendConnector<?, ?> bc2,
                                                           @Mocked BackendFilter bf1) {
        RoutingService routingService = new RoutingService();
        routingService.backendConnectorProvider = backendConnectorProvider;

        new Expectations() {{
            backendConnectorProvider.getBackendConnectors();
            result = Arrays.asList(bc1, bc2);

            bf1.getBackendName();
            result = "wsPlugin";
            bc1.getName();
            result = "wsPlugin";
            bc2.getName();
            result = "jmsPlugin";
        }};

        routingService.ensureAtLeastOneFilterForEachPlugin(Arrays.asList(bf1));
    }

    @Test
    public void ensureAtLeastOneFilterForEachPluginValid(@Mocked BackendConnector<?, ?> bc1,
                                                         @Mocked BackendConnector<?, ?> bc2,
                                                         @Mocked BackendFilter bf1,
                                                         @Mocked BackendFilter bf2,
                                                         @Mocked BackendFilter bf3) {

        RoutingService routingService = new RoutingService();
        routingService.backendConnectorProvider = backendConnectorProvider;

        new Expectations() {{
            backendConnectorProvider.getBackendConnectors();
            result = Arrays.asList(bc1, bc2);

            bc1.getName();
            result = "wsPlugin";
            bc2.getName();
            result = "jmsPlugin";

            bf1.getBackendName();
            result = "wsPlugin";
            bf2.getBackendName();
            result = "jmsPlugin";
        }};

        try {
            routingService.ensureAtLeastOneFilterForEachPlugin(Arrays.asList(bf1, bf2, bf3));
            new FullVerifications() {
            };
        } catch (ConfigurationException ex) {
            Assert.fail();
        }
    }
}