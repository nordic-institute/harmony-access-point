package eu.domibus.web.rest;

import eu.domibus.api.jms.JMSDestination;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JmsMessage;


import eu.domibus.core.csv.CsvServiceImpl;
import eu.domibus.ext.rest.ErrorRO;
import eu.domibus.jms.spi.InternalJMSException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.error.ErrorHandlerService;
import eu.domibus.web.rest.ro.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/rest/jms")
@Validated
public class JmsResource extends BaseResource {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(JmsResource.class);

    private static final String APPLICATION_JSON = "application/json";

    @Autowired
    protected JMSManager jmsManager;

    @Autowired
    private ErrorHandlerService errorHandlerService;

    @ExceptionHandler({InternalJMSException.class})
    public ResponseEntity<ErrorRO> handleInternalJMSException(InternalJMSException ex) {
        return errorHandlerService.createResponse(ex, HttpStatus.BAD_REQUEST);
    }

    @GetMapping(value = {"/destinations"})
    public DestinationsResponseRO destinations() {
        SortedMap<String, JMSDestination> destinations = jmsManager.getDestinations();

        final DestinationsResponseRO response = new DestinationsResponseRO();
        response.setJmsDestinations(destinations);
        return response;
    }

    @GetMapping(value = {"/messages"})
    public MessagesResponseRO messages(@Valid JmsFilterRequestRO request) {
        List<JmsMessage> messages = jmsManager.browseMessages(request.getSource(), request.getJmsType(), request.getFromDate(), request.getToDate(), request.getSelector());
        final MessagesResponseRO response = new MessagesResponseRO();
        response.setMessages(messages);
        return response;
    }

    @PostMapping(value = {"/messages/action"})
    public MessagesActionResponseRO action(@RequestBody @Valid MessagesActionRequestRO request) {

        final MessagesActionResponseRO response = new MessagesActionResponseRO();

        List<String> messageIds = request.getSelectedMessages();
        String[] ids = messageIds.toArray(new String[0]);

        if (request.getAction() == MessagesActionRequestRO.Action.MOVE) {
            Map<String, JMSDestination> destinations = jmsManager.getDestinations();
            String destName = request.getDestination();
            if (!destinations.values().stream().anyMatch(dest -> StringUtils.equals(destName, dest.getName()))) {
                throw new IllegalArgumentException("Cannot find destination with the name [" + destName + "].");
            }
            jmsManager.moveMessages(request.getSource(), request.getDestination(), ids);
        } else if (request.getAction() == MessagesActionRequestRO.Action.REMOVE) {
            jmsManager.deleteMessages(request.getSource(), ids);
        }

        response.setOutcome("Success");
        return response;
    }

    /**
     * This method returns a CSV file with the contents of JMS Messages table
     *
     * @return CSV file with the contents of JMS Messages table
     */
    @GetMapping(path = "/csv")
    public ResponseEntity<String> getCsv(@Valid JmsFilterRequestRO request) {

        // get list of messages
        final List<JmsMessage> jmsMessageList = jmsManager.browseMessages(
                request.getSource(),
                request.getJmsType(),
                request.getFromDate(),
                request.getToDate(),
                request.getSelector())
                .stream().sorted(Comparator.comparing(JmsMessage::getTimestamp).reversed())
                .collect(Collectors.toList());

        customizeJMSProperties(jmsMessageList);

        return exportToCSV(jmsMessageList, JmsMessage.class,
                new HashMap<String, String>() {{
                    put("id".toUpperCase(), "ID");
                    put("type".toUpperCase(), "JMS Type");
                    put("Timestamp".toUpperCase(), "Time");
                    put("CustomProperties".toUpperCase(), "Custom prop");
                    put("Properties".toUpperCase(), "JMS prop");
                }},
//                CsvCustomColumns.JMS_RESOURCE.getCustomColumns(),
                Arrays.asList("PROPERTY_ORIGINAL_QUEUE", "jmsCorrelationId"),
//                CsvExcludedItems.JMS_RESOURCE.getExcludedItems(),
                "jmsmonitoring");

    }

    private void customizeJMSProperties(List<JmsMessage> jmsMessageList) {
        for (JmsMessage message : jmsMessageList) {
            message.setCustomProperties(message.getCustomProperties());
            message.setProperties(message.getJMSProperties());
        }
    }

}

