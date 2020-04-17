package eu.domibus.web.rest;

import eu.domibus.api.routing.BackendFilter;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.csv.CsvService;
import eu.domibus.core.csv.MessageFilterCsvServiceImpl;
import eu.domibus.core.plugin.routing.RoutingService;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.ro.MessageFilterRO;
import eu.domibus.web.rest.ro.MessageFilterResultRO;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
@RestController
@RequestMapping(value = "/rest/messagefilters")
public class MessageFilterResource extends BaseResource {

    private static final Logger LOGGER = DomibusLoggerFactory.getLogger(MessageFilterResource.class);

    @Autowired
    RoutingService routingService;

    @Autowired
    DomainCoreConverter coreConverter;

    @Autowired
    private MessageFilterCsvServiceImpl messageFilterCsvServiceImpl;

    @GetMapping
    public MessageFilterResultRO getMessageFilter() {
        final Pair<List<MessageFilterRO>, Boolean> backendFiltersInformation = getBackendFiltersInformation();

        MessageFilterResultRO resultRO = new MessageFilterResultRO();
        resultRO.setMessageFilterEntries(backendFiltersInformation.getKey());
        resultRO.setAreFiltersPersisted(backendFiltersInformation.getValue());
        return resultRO;
    }

    @PutMapping
    public void updateMessageFilters(@RequestBody List<MessageFilterRO> messageFilterROS) {
        List<BackendFilter> backendFilters = coreConverter.convert(messageFilterROS, BackendFilter.class);
        routingService.updateBackendFilters(backendFilters);
    }

    /**
     * This method returns a CSV file with the contents of Message Filter table
     *
     * @return CSV file with the contents of Message Filter table
     */
    @GetMapping(path = "/csv")
    public ResponseEntity<String> getCsv() {
        List<MessageFilterRO> list = getBackendFiltersInformation().getKey();
        getCsvService().validateMaxRows(list.size());
        return exportToCSV(list, MessageFilterRO.class, "message-filter");
    }

    @Override
    public CsvService getCsvService() {
        return messageFilterCsvServiceImpl;
    }

    protected Pair<List<MessageFilterRO>, Boolean> getBackendFiltersInformation() {
        boolean areFiltersPersisted = true;
        List<BackendFilter> backendFilters = routingService.getBackendFiltersUncached();
        List<MessageFilterRO> messageFilterResultROS = coreConverter.convert(backendFilters, MessageFilterRO.class);
        for (MessageFilterRO messageFilter : messageFilterResultROS) {
            if (messageFilter.getEntityId() == 0) {
                messageFilter.setPersisted(false);
                areFiltersPersisted = false;
            } else {
                messageFilter.setPersisted(true);
            }
        }
        return new ImmutablePair<>(messageFilterResultROS, areFiltersPersisted);
    }
}
