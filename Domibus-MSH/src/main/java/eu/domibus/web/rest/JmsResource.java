package eu.domibus.web.rest;

import com.google.common.collect.ImmutableMap;
import eu.domibus.api.exceptions.RequestValidationException;
import eu.domibus.api.jms.JMSDestination;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.jms.spi.InternalJMSException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.error.ErrorHandlerService;
import eu.domibus.web.rest.ro.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/rest/jms")
@Validated
public class JmsResource extends BaseResource {

    private JMSManager jmsManager;

    private ErrorHandlerService errorHandlerService;

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(JmsResource.class);

    public JmsResource(JMSManager jmsManager, ErrorHandlerService errorHandlerService) {
        this.jmsManager = jmsManager;
        this.errorHandlerService = errorHandlerService;
    }

    @ExceptionHandler({InternalJMSException.class})
    public ResponseEntity<ErrorRO> handleInternalJMSException(InternalJMSException ex) {
        return errorHandlerService.createResponse(ex, HttpStatus.BAD_REQUEST);
    }

    @GetMapping(value = {"/destinations"})
    public DestinationsResponseRO destinations() {
        LOG.info("Getting all destinations available on the JMS server");
        SortedMap<String, JMSDestination> destinations = jmsManager.getDestinations();

        final DestinationsResponseRO response = new DestinationsResponseRO();
        response.setJmsDestinations(destinations);
        return response;
    }

    @GetMapping(value = {"/messages"})
    public MessagesResponseRO messages(@Valid JmsFilterRequestRO request) {
        LOG.info("Getting JMS messages from the source: {}", request.getSource());
        List<JmsMessage> messages = jmsManager.browseMessages(request.getSource(), request.getJmsType(), request.getFromDate(), request.getToDate(), request.getSelector());
        customizeProperties(messages);
        final MessagesResponseRO response = new MessagesResponseRO();
        response.setMessages(messages);
        return response;
    }

    @PostMapping(value = {"/messages/action"})
    public MessagesActionResponseRO action(@RequestBody @Valid MessagesActionRequestRO request) {

        final MessagesActionResponseRO response = new MessagesActionResponseRO();

        List<String> messageIds = request.getSelectedMessages();
        String[] ids = (messageIds != null ? messageIds.toArray(new String[0]) : new String[0]);

        if (request.getAction() == MessagesActionRequestRO.Action.MOVE) {
            LOG.info("Starting to move JMS messages from the source: {} to destination: {}", request.getSource(), request.getDestination());
            jmsManager.moveMessages(request.getSource(), request.getDestination(), ids);
            LOG.info("Moved JMS messages from the source queue successfully.");
        } else if (request.getAction() == MessagesActionRequestRO.Action.REMOVE) {
            LOG.info("Starting to delete JMS messages from the source: {}", request.getSource());
            jmsManager.deleteMessages(request.getSource(), ids);
            LOG.info("Deleted JMS messages from the source queue successfully.");
        } else if (request.getAction() == MessagesActionRequestRO.Action.REMOVE_ALL) {
            LOG.info("Starting to delete all JMS messages from the source: {}", request.getSource());
            jmsManager.deleteAllMessages(request.getSource());
            LOG.info("Deleted all JMS messages from the source queue successfully.");
        } else {
            throw new RequestValidationException("Invalid action specified. Valid actions are 'move', 'remove' and 'remove all'");
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
        final List<JmsMessage> jmsMessageList = jmsManager
                .browseMessages(request.getSource(), request.getJmsType(), request.getFromDate(), request.getToDate(), request.getSelector())
                .stream().sorted(Comparator.comparing(JmsMessage::getTimestamp).reversed())
                .collect(Collectors.toList());

        getCsvService().validateMaxRows(jmsMessageList.size());

        customizePropertiesForCSV(jmsMessageList);

        return exportToCSV(jmsMessageList, JmsMessage.class,
                ImmutableMap.of(
                        "id".toUpperCase(), "ID",
                        "type".toUpperCase(), "JMS Type",
                        "Timestamp".toUpperCase(), "Time",
                        "CustomProperties".toUpperCase(), "Custom prop",
                        "Properties".toUpperCase(), "JMS prop"
                ),
                Arrays.asList("PROPERTY_ORIGINAL_QUEUE", "jmsCorrelationId", "priority", "content"),
                "jmsmonitoring");

    }

    private void customizePropertiesForCSV(List<JmsMessage> jmsMessageList) {
        for (JmsMessage message : jmsMessageList) {
            message.setCustomProperties(message.getCustomProperties());
            message.setProperties(message.getJMSProperties());
            message.setContent(null);
        }
    }

    private void customizeProperties(List<JmsMessage> jmsMessageList) {
        for (JmsMessage message : jmsMessageList) {
            message.setContent(null);
        }
    }

}

