package eu.domibus.web.rest;

import eu.domibus.api.csv.CsvException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.routing.BackendFilter;
import eu.domibus.api.routing.RoutingCriteria;
import eu.domibus.core.converter.BackendFilterCoreMapper;
import eu.domibus.core.csv.CsvServiceImpl;
import eu.domibus.core.csv.MessageFilterCSV;
import eu.domibus.core.plugin.routing.RoutingService;
import eu.domibus.web.rest.ro.MessageFilterRO;
import eu.domibus.web.rest.ro.MessageFilterResultRO;
import mockit.Expectations;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
@RunWith(JMockit.class)
public class MessageFilterResourceTest {

    private static final String CSV_TITLE = "Backend Name, From, To, Action, Service, Is Persisted";
    @Tested
    MessageFilterResource messageFilterResource;

    @Injectable
    RoutingService routingService;

    @Injectable
    DomainContextProvider domainContextProvider;

    @Injectable
    BackendFilterCoreMapper backendFilterCoreMapper;

    @Injectable
    private CsvServiceImpl csvServiceImpl;

    @Test
    public void updateMessageFilters() {
        List<BackendFilter> backendFilters = singletonList(new BackendFilter());
        List<MessageFilterRO> messageFilterROS = singletonList(new MessageFilterRO());

        new Expectations(){{
            backendFilterCoreMapper.messageFilterROListToBackendFilterList(messageFilterROS);
            this.result = backendFilters;
            times = 1;
        }};

        messageFilterResource.updateMessageFilters(messageFilterROS);

        new FullVerifications(){{
            routingService.updateBackendFilters(backendFilters);
            times = 1;
        }};
    }

    @Test
    public void testGetMessageFilterPersisted() {
        MessageFilterResultRO messageFilterResultRO = getMessageFilterResultRO(null);

        // Then
        Assert.assertNotNull(messageFilterResultRO);
        Assert.assertFalse(messageFilterResultRO.isAreFiltersPersisted());
        Assert.assertEquals(getMessageFilterROS(null), messageFilterResultRO.getMessageFilterEntries());
    }

    @Test
    public void testGetMessageFilterNotPersisted() {
        MessageFilterResultRO messageFilterResultRO = getMessageFilterResultRO("1");

        // Then
        Assert.assertNotNull(messageFilterResultRO);
        Assert.assertTrue(messageFilterResultRO.isAreFiltersPersisted());
        Assert.assertEquals(getMessageFilterROS("1"), messageFilterResultRO.getMessageFilterEntries());
    }

    @Test
    public void testGetMessageFilterCsv() throws CsvException {
        // Given
        final String backendName = "Backend Filter 1";
        final String fromExpression = "from:expression";
        List<MessageFilterRO> messageFilterResultROS = new ArrayList<>();

        List<RoutingCriteria> routingCriterias = new ArrayList<>();
        RoutingCriteria routingCriteria = new RoutingCriteria();
        routingCriteria.setEntityId("1");
        routingCriteria.setName("From");
        routingCriteria.setExpression(fromExpression);
        routingCriterias.add(routingCriteria);

        MessageFilterRO messageFilterRO = new MessageFilterRO();
        messageFilterRO.setIndex(1);

        messageFilterRO.setBackendName(backendName);
        messageFilterRO.setEntityId("1");
        messageFilterRO.setRoutingCriterias(routingCriterias);
        messageFilterRO.setPersisted(true);

        messageFilterResultROS.add(messageFilterRO);

        new Expectations(messageFilterResource){{
            domainContextProvider.getCurrentDomain();
            result = new Domain("default", "default");
            messageFilterResource.getBackendFiltersInformation();
            result = new ImmutablePair<>(messageFilterResultROS, true);
            messageFilterResource.fromMessageFilterRO(messageFilterRO);
            result = new MessageFilterCSV();
            csvServiceImpl.exportToCSV((List<?>) any, MessageFilterCSV.class,new HashMap<>(), new ArrayList<>());
            result = CSV_TITLE + backendName + "," + fromExpression + ", , , ," + true + System.lineSeparator();

            csvServiceImpl.getCsvFilename("message-filter", "default");
            result = "TEST";
        }};

        // When
        final ResponseEntity<String> csv = messageFilterResource.getCsv();

        // Then
        Assert.assertEquals(HttpStatus.OK, csv.getStatusCode());
        Assert.assertEquals(CSV_TITLE +
                        backendName + "," + fromExpression + ", , , ," + true + System.lineSeparator(),
                csv.getBody());
        new FullVerifications(){{
            csvServiceImpl.validateMaxRows(1);
        }};
    }

    private MessageFilterResultRO getMessageFilterResultRO(String messageFilterEntityId) {
        // Given
        final ArrayList<BackendFilter> backendFilters = new ArrayList<>();
        BackendFilter backendFilter = new BackendFilter();
        backendFilter.setEntityId("1");
        backendFilter.setBackendName("backendName1");
        backendFilter.setIndex(0);
        backendFilter.setActive(true);
        backendFilters.add(backendFilter);

        final List<MessageFilterRO> messageFilterROS = getMessageFilterROS(messageFilterEntityId);

        new Expectations() {{

            routingService.getBackendFiltersWithCache();
            result = backendFilters;

            backendFilterCoreMapper.backendFilterListToMessageFilterROList(backendFilters);
            result = messageFilterROS;
        }};

        // When
        return messageFilterResource.getMessageFilter();
    }

    private List<MessageFilterRO> getMessageFilterROS(String messageFilterEntityId) {
        final List<MessageFilterRO> messageFilterROS = new ArrayList<>();
        MessageFilterRO messageFilterRO = getMessageFilterRO(messageFilterEntityId);
        messageFilterROS.add(messageFilterRO);
        return messageFilterROS;
    }

    private MessageFilterRO getMessageFilterRO(String messageFilterEntityId) {
        MessageFilterRO messageFilterRO = new MessageFilterRO();
        messageFilterRO.setEntityId(messageFilterEntityId);
        messageFilterRO.setPersisted(StringUtils.isNotEmpty(messageFilterEntityId));
        return messageFilterRO;
    }

    @Test
    public void fromMessageFilterRO() {
        MessageFilterRO messageFilterRO1 = new MessageFilterRO();
        messageFilterRO1.setEntityId("1");
        messageFilterRO1.setPersisted(true);
        messageFilterRO1.setBackendName("Plugin");

        RoutingCriteria from = getRoutingCriteria("from", "from:expression");
        messageFilterRO1.setRoutingCriterias(singletonList(from));

        MessageFilterCSV result = messageFilterResource.fromMessageFilterRO(messageFilterRO1);

        assertThat(result.getPlugin(), is("Plugin"));
        assertThat(result.isPersisted(), is(true));
        assertThat(result.getFrom(), is(from));
        assertThat(result.getTo(), nullValue());
        assertThat(result.getAction(), nullValue());
        assertThat(result.getService(), nullValue());
    }

    private RoutingCriteria getRoutingCriteria(String name, String expression) {
        RoutingCriteria routingCriteria = new RoutingCriteria();
        routingCriteria.setName(name);
        routingCriteria.setExpression(expression);
        return routingCriteria;
    }

    @Test
    public void getValue() {
        RoutingCriteria from = getRoutingCriteria("from", "from:expression");
        List<RoutingCriteria> routingCriteria = Arrays.asList(from,
                getRoutingCriteria("to", "to:expression"),
                getRoutingCriteria("action", "action:expression"));

        RoutingCriteria result = messageFilterResource.getValue(routingCriteria, "from");

        assertThat(result, is(from));
    }

    @Test
    public void getValue_null() {
        List<RoutingCriteria> routingCriteria = Arrays.asList(
                getRoutingCriteria("to", "to:expression"),
                getRoutingCriteria("action", "action:expression"));

        RoutingCriteria result = messageFilterResource.getValue(routingCriteria, "from");

        assertThat(result, nullValue());
    }
}
