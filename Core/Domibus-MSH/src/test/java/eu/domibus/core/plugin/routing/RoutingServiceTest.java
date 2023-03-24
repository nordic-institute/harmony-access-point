package eu.domibus.core.plugin.routing;

import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.routing.BackendFilter;
import eu.domibus.api.routing.RoutingCriteria;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.core.converter.BackendFilterCoreMapper;
import eu.domibus.core.converter.DomibusCoreMapper;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.core.plugin.BackendConnectorProvider;
import eu.domibus.core.plugin.routing.dao.BackendFilterDao;
import eu.domibus.plugin.AbstractBackendConnector;
import eu.domibus.plugin.BackendConnector;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.util.*;

import static eu.domibus.core.plugin.notification.BackendPlugin.*;
import static eu.domibus.core.plugin.notification.BackendPlugin.Name.*;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;

/**
 * @author Ion Perpegel
 * @author Cosmin Baciu
 * @since 4.1
 */
@SuppressWarnings({"unchecked", "ResultOfMethodCallIgnored", "ConstantConditions", "rawtypes", "JUnitMalformedDeclaration"})
@RunWith(JMockit.class)
public class RoutingServiceTest {

    public static final int MAX_INDEX = 10;

    public static final String MESSAGE_ID = "MessageId";

    @Injectable
    private BackendConnectorProvider backendConnectorProvider;

    @Injectable
    private BackendFilterCoreMapper backendFilterCoreMapper;

    @Injectable
    private BackendFilterDao backendFilterDao;

    @Injectable
    private DomibusCoreMapper coreMapper;

    @Injectable
    private DomibusConfigurationService domibusConfigurationService;

    @Injectable
    private DomainService domainService;

    @Injectable
    private DomainTaskExecutor domainTaskExecutor;

    @Injectable
    private List<CriteriaFactory> routingCriteriaFactories;

    @Injectable
    private DomainContextProvider domainContextProvider;

    @Injectable
    private AuthUtils authUtils;

    @Injectable
    private SignalService signalService;

    @Tested
    private RoutingService routingService;

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

        routingService.ensureUnicity(Arrays.asList(bf1, bf2, bf3));
    }

    @Test
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

        routingService.ensureUnicity(Arrays.asList(bf1, bf2));
    }

    @Test
    public void invalidateBackendFiltersCache() {
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
        routingService.backendFilterDao = backendFilterDao;
        routingService.backendFilterCoreMapper = backendFilterCoreMapper;

        ArrayList<BackendFilterEntity> backendFilterEntityList = new ArrayList<>();
        backendFilterEntityList.add(new BackendFilterEntity());

        ArrayList<BackendFilter> backendFilters = new ArrayList<>();

        new Expectations() {{
            backendFilterDao.findAll();
            result = backendFilterEntityList;

            backendFilterCoreMapper.backendFilterEntityListToBackendFilterList(backendFilterEntityList);
            result = backendFilters;
        }};
        List<BackendFilter> actual = routingService.getBackendFilters();

        assertEquals(backendFilters, actual);

        new FullVerifications() {
        };
    }

    @Test
    public void getBackendFilters_return1(@Injectable BackendFilterEntity backendFilterEntity) {
        routingService.backendFilterDao = backendFilterDao;
        routingService.backendFilterCoreMapper = backendFilterCoreMapper;

        ArrayList<BackendFilterEntity> backendFilterEntityList = new ArrayList<>();
        backendFilterEntityList.add(backendFilterEntity);

        new Expectations() {{
            backendFilterDao.findAll();
            result = backendFilterEntityList;
        }};

        routingService.getBackendFilters();

        new FullVerifications() {{
            backendFilterCoreMapper.backendFilterEntityListToBackendFilterList(backendFilterEntityList);
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
        // these 2 filters are defined by the user in the Message Filter screen
        final List<RoutingCriteria> criteriaList = new ArrayList<>();
        criteriaList.add(fromRoutingCriteria);
        criteriaList.add(actionRoutingCriteria);

        final String fromCriteriaName = "FROM";
        final String actionCriteriaName = "ACTION";

        new Expectations() {{

            filter.isActive();
            result = true;

            filter.getRoutingCriterias();
            result = criteriaList;

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
        // these 2 filters are defined by the user in the Message Filter screen
        final List<RoutingCriteria> criteriaList = new ArrayList<>();
        criteriaList.add(fromRoutingCriteria);
        criteriaList.add(actionRoutingCriteria);

        final String fromCriteriaName = "FROM";
        final String actionCriteriaName = "ACTION";

        new Expectations() {{

            filter.isActive();
            result = true;

            filter.getRoutingCriterias();
            result = criteriaList;

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
        // these 2 filters are defined by the user in the Message Filter screen
        final List<RoutingCriteria> criteriaList = new ArrayList<>();
        criteriaList.add(fromRoutingCriteria);
        criteriaList.add(actionRoutingCriteria);

        final String fromCriteriaName = "FROM";
        final String actionCriteriaName = "ACTION";

        new Expectations() {{

            filter.isActive();
            result = true;

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

        new FullVerifications() {{
            criteriaMap.get(actionCriteriaName);
            times = 0;

            actionRoutingCriteriaConfiguration.matches(userMessage, anyString);
            times = 0;

            fromRoutingCriteria.toString();
            userMessage.toString();
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
        // these 2 filters are defined by the user in the Message Filter screen
        final List<RoutingCriteria> criteriaList = new ArrayList<>();
        criteriaList.add(fromRoutingCriteria);
        criteriaList.add(actionRoutingCriteria);

        final String fromCriteriaName = "FROM";
        final String actionCriteriaName = "ACTION";

        new Expectations() {{
            filter.isActive();
            result = true;

            filter.getRoutingCriterias();
            result = criteriaList;

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

            fromRoutingCriteria.toString();
            actionRoutingCriteria.toString();
            userMessage.toString();
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
        new Expectations() {{

            filter.isActive();
            result = true;

            filter.getRoutingCriterias();
            result = null;
        }};

        final boolean backendFilterMatching = routingService.isBackendFilterMatching(filter, criteriaMap, userMessage);
        Assert.assertTrue(backendFilterMatching);

        new FullVerifications() {{
            criteriaMap.get(anyString);
            times = 0;

            userMessage.toString();
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
        // these 2 filters are defined by the user in the Message Filter screen
        final List<RoutingCriteria> criteriaList = new ArrayList<>();
        criteriaList.add(fromRoutingCriteria);
        criteriaList.add(actionRoutingCriteria);

        final String fromCriteriaName = "FROM";

        new Expectations() {{

            filter.isActive();
            result = true;

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
    public void testCreateBackendFiltersBasedOnExistingUserPriority() {
        List<String> notificationListenerPluginsList = new ArrayList<>();
        notificationListenerPluginsList.add(WS);
        notificationListenerPluginsList.add(JMS);

        List<BackendFilterEntity> backendFilterEntities = routingService.buildBackendFilterEntities(notificationListenerPluginsList, 1);
        assertEquals(2, backendFilterEntities.size());

        new FullVerifications() {{
            routingCriteriaFactories.iterator();
        }};
    }

    @Test
    public void createBackendFilterEntity_empty(@Injectable BackendFilterEntity backendFilterEntity) {
        RoutingService routingService = new RoutingService();

        List<BackendFilterEntity> backendFilters = routingService.buildBackendFilterEntities(null, 1);
        assertEquals(0, backendFilters.size());
    }

    @Test
    public void createBackendFilterEntity(@Injectable BackendFilterEntity backendFilterEntity) {
        List<String> pluginList = new ArrayList<>();
        pluginList.add(FS);
        pluginList.add("TEST2");
        pluginList.add(JMS);
        pluginList.add("TEST1");
        pluginList.add(WS);
        pluginList.add("TEST3");

        int priority = 4;
        List<BackendFilterEntity> backendFilters = routingService.buildBackendFilterEntities(pluginList, priority);

        assertEquals("TEST2", backendFilters.get(0).getBackendName());
        assertEquals(4, backendFilters.get(0).getIndex());
        assertEquals("TEST1", backendFilters.get(1).getBackendName());
        assertEquals(5, backendFilters.get(1).getIndex());
        assertEquals("TEST3", backendFilters.get(2).getBackendName());
        assertEquals(6, backendFilters.get(2).getIndex());
        assertTrue(WS_PLUGIN.getNames().contains(backendFilters.get(3).getBackendName()));
        assertEquals(7, backendFilters.get(3).getIndex());
        assertTrue(JMS_PLUGIN.getNames().contains(backendFilters.get(4).getBackendName()));
        assertEquals(8, backendFilters.get(4).getIndex());
        assertTrue(FS_PLUGIN.getNames().contains(backendFilters.get(5).getBackendName()));
        assertEquals(9, backendFilters.get(5).getIndex());
    }

    @Test
    public void createBackendFilterEntities_defaultPlugin(@Injectable BackendFilterEntity backendFilterEntity) {
        List<String> pluginToAdd = new ArrayList<>();
        pluginToAdd.add(FS);
        pluginToAdd.add(JMS);
        pluginToAdd.add(WS);

        List<BackendFilterEntity> allBackendFilters = routingService.buildBackendFilterEntities(pluginToAdd, 0);

        assertEquals(3, allBackendFilters.size());
        assertEquals(FS, allBackendFilters.get(2).getBackendName());
        assertEquals(2, allBackendFilters.get(2).getIndex());
        assertEquals(JMS, allBackendFilters.get(1).getBackendName());
        assertEquals(1, allBackendFilters.get(1).getIndex());
        assertTrue(WS_PLUGIN.getNames().contains(allBackendFilters.get(0).getBackendName()));
        assertEquals(0, allBackendFilters.get(0).getIndex());
    }

    @Test
    public void createBackendFilters_emptyDbEntities(@Injectable BackendConnectorProvider backendConnectorProvider,
                                                     @Injectable BackendConnector backendConnector,
                                                     @Injectable BackendFilterEntity dbBackendFilterEntity) {
        routingService.backendFilterDao = backendFilterDao;
        routingService.backendConnectorProvider = backendConnectorProvider;

        List<BackendFilterEntity> entitiesInDb = new ArrayList<>();

        List<List<String>> pluginsToAdd = new ArrayList<>();

        List<BackendFilterEntity> backendFilterEntities = new ArrayList<>();

        new Expectations(routingService) {{
            backendFilterDao.findAll();
            result = entitiesInDb;

            backendConnectorProvider.getBackendConnectors();
            result = backendConnector;

            backendConnector.getName();
            result = JMS;

            routingService.getMaxIndex(entitiesInDb);
            result = 0;

            routingService.buildBackendFilterEntities(withCapture(pluginsToAdd), 1);
            result = backendFilterEntities;
        }};

        routingService.createBackendFilters();

        new FullVerifications() {{
            backendFilterDao.create(backendFilterEntities);
            times = 1;
        }};

        assertEquals(1, pluginsToAdd.size());
        assertTrue(pluginsToAdd.get(0).containsAll(JMS_PLUGIN.getNames()));
    }

    @Test
    public void testGetBackendFiltersWithCache(@Injectable BackendFilter backendFilter1,
                                               @Injectable BackendFilter backendFilter2,
                                               @Injectable AbstractBackendConnector backendEnableAware1,
                                               @Injectable BackendConnector<?, ?> backend2,
                                               @Injectable List<BackendFilterEntity> backendFilterEntities
    ) {
        List<BackendFilter> backendFilters = asList(backendFilter1, backendFilter2);
        routingService.domainContextProvider = domainContextProvider;

        new Expectations(routingService) {{
            domainContextProvider.getCurrentDomain();
            result = new Domain("default", "default");

            backendFilterDao.findAll();
            result = backendFilterEntities;

            backendFilterCoreMapper.backendFilterEntityListToBackendFilterList(backendFilterEntities);
            result = backendFilters;

            backendFilter1.getBackendName();
            result = "backendFilter1";

            backendFilter2.getBackendName();
            result = "backendFilter2";

            backendConnectorProvider.getBackendConnector("backendFilter1");
            result = backendEnableAware1;

            backendConnectorProvider.getBackendConnector("backendFilter2");
            result = backend2;

            backendEnableAware1.isEnabled("default");
            result = true;

            backend2.getName();
            result = "backendConnector";

        }};

        routingService.backendFiltersCache = new HashMap<>();
        List<BackendFilter> backendFiltersWithCache = routingService.getBackendFiltersWithCache();
        List<BackendFilter> backendFiltersWithCache1 = routingService.getBackendFiltersWithCache();

        assertNotNull(backendFiltersWithCache);
        assertNotNull(backendFiltersWithCache1);

        new Verifications() {{
            routingService.getBackendFilters();
            times = 1;

            backendFilter1.setActive(true);
            times = 1;

            backendFilter2.setActive(true);
            times = 1;
        }};
    }

    @Test
    public void getMatchingBackendFilter(
            @Injectable Map<String, IRoutingCriteria> criteriaMap,
            @Injectable UserMessage userMessage,
            @Injectable BackendFilter backendFilter1,
            @Injectable BackendFilter backendFilter2) {
        List<BackendFilter> backendFilters = asList(backendFilter1, backendFilter2);

        new Expectations(routingService) {{
            userMessage.getMessageId();
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
        List<BackendFilter> backendFilters = new ArrayList<>();

        new Expectations() {{
            userMessage.getMessageId();
            result = MESSAGE_ID;
        }};

        BackendFilter matchingBackendFilter = routingService.getMatchingBackendFilter(backendFilters, criteriaMap, userMessage);

        assertNull(matchingBackendFilter);

        new FullVerifications() {
        };
    }

    @Test
    public void getMaxIndex_empty() {
        int maxIndex = routingService.getMaxIndex(new ArrayList<>());
        assertEquals(0, maxIndex);
    }

    @Test
    public void getMaxIndex_null() {
        int maxIndex = routingService.getMaxIndex(null);
        assertEquals(0, maxIndex);
    }

    @Test
    public void getMaxIndex_maxIndex(@Injectable BackendFilterEntity b1,
                                     @Injectable BackendFilterEntity b2,
                                     @Injectable BackendFilterEntity b3) {
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

        routingService.ensureAtLeastOneFilterForEachPlugin(Collections.singletonList(bf1));
    }

    @Test
    public void ensureAtLeastOneFilterForEachPluginValid(@Mocked BackendConnector<?, ?> bc1,
                                                         @Mocked BackendConnector<?, ?> bc2,
                                                         @Mocked BackendFilter bf1,
                                                         @Mocked BackendFilter bf2,
                                                         @Mocked BackendFilter bf3) {
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

    @Test
    public void createBackendFiltersWithDbEntities(@Injectable BackendConnector backendConnector,
                                                   @Injectable BackendFilterEntity dbBackendFilterEntity,
                                                   @Injectable BackendFilterEntity dbBackendFilterEntity1) {
        List<BackendFilterEntity> backendFilterEntities = new ArrayList<>();
        backendFilterEntities.add(dbBackendFilterEntity);
        backendFilterEntities.add(dbBackendFilterEntity1);

        new Expectations(routingService) {{
            backendFilterDao.findAll();
            result = backendFilterEntities;

            dbBackendFilterEntity.getBackendName();
            result = JMS;

            dbBackendFilterEntity1.getBackendName();
            result = FS;

            backendConnectorProvider.getBackendConnectors();
            result = backendConnector;

            backendConnector.getName();
            result = JMS;
        }};

        routingService.createBackendFilters();

        new Verifications() {{
            backendFilterDao.findAll();
            times = 1;
            routingService.updateFilterIndices(backendFilterEntities);
            times = 1;
            backendFilterDao.update(backendFilterEntities);
            times = 1;
        }};
    }

    @Test
    public void ceateBackendFilters_updatesOldWsPluginBackendName(@Injectable BackendConnector backendConnector,
                                                                  @Injectable BackendFilterEntity dbBackendFilterEntity) {
        List<BackendFilterEntity> backendFilterEntities = new ArrayList<>();
        backendFilterEntities.add(dbBackendFilterEntity);

        new Expectations(routingService) {{
            backendFilterDao.findAll();
            result = backendFilterEntities;

            dbBackendFilterEntity.getBackendName();
            result = WS_OLD;

            backendConnectorProvider.getBackendConnectors();
            result = backendConnector;

            backendConnector.getName();
            result = WS;
        }};

        routingService.createBackendFilters();

        new VerificationsInOrder() {{
            backendFilterDao.findAll();
            dbBackendFilterEntity.setBackendName(WS);
            backendFilterDao.update(dbBackendFilterEntity);
        }};
    }

    @Test
    public void updateBackendFilters(@Injectable BackendFilter filter1,
                                     @Injectable BackendFilter filter2,
                                     @Injectable List<BackendFilterEntity> allBackendFilterEntities) {
        List<BackendFilter> filters = new ArrayList<>();
        filters.add(filter1);
        filters.add(filter2);

        new Expectations(routingService) {{
            backendFilterDao.findAll();
            result = allBackendFilterEntities;

            backendFilterCoreMapper.backendFilterListToBackendFilterEntityList(filters);
            result = allBackendFilterEntities;

            routingService.validateFilters((List<BackendFilter>) any);
            routingService.invalidateBackendFiltersCache();
            routingService.updateFilterIndices((List<BackendFilterEntity>) any);
        }};

        routingService.updateBackendFilters(filters);

        new FullVerifications() {{
            backendFilterDao.delete(allBackendFilterEntities);
            routingService.updateFilterIndices(allBackendFilterEntities);
            backendFilterDao.update(allBackendFilterEntities);
            routingService.invalidateBackendFiltersCache();
            signalService.signalMessageFiltersUpdated();
            allBackendFilterEntities.toString();
        }};
    }
}
