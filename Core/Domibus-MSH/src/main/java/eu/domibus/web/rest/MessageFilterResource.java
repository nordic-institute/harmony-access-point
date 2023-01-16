package eu.domibus.web.rest;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.routing.BackendFilter;
import eu.domibus.api.routing.RoutingCriteria;
import eu.domibus.core.converter.BackendFilterCoreMapper;
import eu.domibus.core.csv.MessageFilterCSV;
import eu.domibus.core.plugin.routing.RoutingService;
import eu.domibus.core.util.MessageUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.ro.MessageFilterRO;
import eu.domibus.web.rest.ro.MessageFilterResultRO;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
@RestController
@RequestMapping(value = "/rest/messagefilters")
public class MessageFilterResource extends BaseResource {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageFilterResource.class);

    private final RoutingService routingService;

    private final BackendFilterCoreMapper backendFilterCoreMapper;
    private DomainContextProvider domainContextProvider;

    public MessageFilterResource(RoutingService routingService,
                                 BackendFilterCoreMapper backendFilterCoreMapper,
                                 DomainContextProvider domainContextProvider) {
        this.routingService = routingService;
        this.backendFilterCoreMapper = backendFilterCoreMapper;
        this.domainContextProvider = domainContextProvider;
    }

    @GetMapping
    public MessageFilterResultRO getMessageFilter() {
        LOG.debug("GET messageFilter");
        final Pair<List<MessageFilterRO>, Boolean> backendFiltersInformation = getBackendFiltersInformation();

        MessageFilterResultRO resultRO = new MessageFilterResultRO();
        resultRO.setMessageFilterEntries(backendFiltersInformation.getKey());
        resultRO.setAreFiltersPersisted(backendFiltersInformation.getValue());
        return resultRO;
    }

    @PutMapping
    public void updateMessageFilters(@RequestBody List<MessageFilterRO> messageFilterROS) {
        LOG.debug("PUT messageFilters [{}]", messageFilterROS);
        List<BackendFilter> backendFilters = backendFilterCoreMapper.messageFilterROListToBackendFilterList(messageFilterROS);
        routingService.updateBackendFilters(backendFilters);
    }

    /**
     * This method returns a CSV file with the contents of Message Filter table
     *
     * @return CSV file with the contents of Message Filter table
     */
    @GetMapping(path = "/csv")
    public ResponseEntity<String> getCsv() {
        LOG.debug("GET csv");
        List<MessageFilterRO> list = getBackendFiltersInformation().getKey();
        getCsvService().validateMaxRows(list.size());
        Domain currentDomain = domainContextProvider.getCurrentDomain();
        return exportToCSV(list.stream().map(this::fromMessageFilterRO).collect(Collectors.toList()), MessageFilterCSV.class, "message-filter", currentDomain.getName());
    }

    protected Pair<List<MessageFilterRO>, Boolean> getBackendFiltersInformation() {
        boolean areFiltersPersisted = true;
        List<BackendFilter> backendFilters = routingService.getBackendFiltersWithCache();
        List<MessageFilterRO> messageFilterResultROS = backendFilterCoreMapper.backendFilterListToMessageFilterROList(backendFilters);
        for (MessageFilterRO messageFilter : messageFilterResultROS) {
            if (StringUtils.isEmpty(messageFilter.getEntityId())) {
                messageFilter.setPersisted(false);
                areFiltersPersisted = false;
            } else {
                messageFilter.setPersisted(true);
            }
        }
        return new ImmutablePair<>(messageFilterResultROS, areFiltersPersisted);
    }
    protected MessageFilterCSV fromMessageFilterRO(MessageFilterRO messageFilterRO) {
        MessageFilterCSV messageFilterCSV = new MessageFilterCSV();
        messageFilterCSV.setPlugin(messageFilterRO.getBackendName());
        messageFilterCSV.setPersisted(messageFilterRO.isPersisted());
        messageFilterCSV.setActive(messageFilterRO.isActive());

        List<RoutingCriteria> routingCriteria = messageFilterRO.getRoutingCriterias();
        messageFilterCSV.setFrom(getValue(routingCriteria, MessageUtil.FROM));
        messageFilterCSV.setTo(getValue(routingCriteria, MessageUtil.TO));
        messageFilterCSV.setAction(getValue(routingCriteria, MessageUtil.ACTION));
        messageFilterCSV.setService(getValue(routingCriteria, MessageUtil.SERVICE));
        return messageFilterCSV;
    }

    protected RoutingCriteria getValue(List<RoutingCriteria> routingCriteria, String key) {
        return routingCriteria
                .stream()
                .filter(routingCriteriaElem -> StringUtils.equalsAnyIgnoreCase(routingCriteriaElem.getName(), key))
                .findAny()
                .orElse(null);
    }
}
