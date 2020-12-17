package eu.domibus.ext.rest;

import eu.domibus.ext.domain.ErrorDTO;
import eu.domibus.ext.domain.MessageAcknowledgementDTO;
import eu.domibus.ext.domain.MessageAcknowledgementRequestDTO;
import eu.domibus.ext.exceptions.MessageAcknowledgeExtException;
import eu.domibus.ext.rest.error.ExtExceptionHelper;
import eu.domibus.ext.services.MessageAcknowledgeExtService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author migueti, Cosmin Baciu
 * @since 3.3
 */
@RestController
@RequestMapping(value = "/ext/messages/acknowledgments")
public class MessageAcknowledgementExtResource {

    @Autowired
    MessageAcknowledgeExtService messageAcknowledgeService;

    @Autowired
    ExtExceptionHelper extExceptionHelper;

    @ExceptionHandler(MessageAcknowledgeExtException.class)
    public ResponseEntity<ErrorDTO> handleMessageAcknowledgeExtException(MessageAcknowledgeExtException e) {
        return extExceptionHelper.handleExtException(e);
    }

    /**
     * Acknowledges that a message has been delivered to the backend
     *
     * @param acknowledgementRequestDTO the details of the message delivered acknowledgement to be created
     * @return The newly created message acknowledgement
     */
    @ApiOperation(value = "Create a message delivered acknowledgement", notes = "Acknowledges that a message has been delivered to the backend",
            authorizations = @Authorization(value = "basicAuth"), tags = "acknowledgement")
    @PostMapping(path = "/delivered")
    public MessageAcknowledgementDTO acknowledgeMessageDelivered(@RequestBody MessageAcknowledgementRequestDTO acknowledgementRequestDTO) {
        return messageAcknowledgeService.acknowledgeMessageDelivered(acknowledgementRequestDTO.getMessageId(), acknowledgementRequestDTO.getAcknowledgeDate(), acknowledgementRequestDTO.getProperties());
    }

    /**
     * Acknowledges that a message has been processed by the backend
     *
     * @param acknowledgementRequestDTO the details of the message delivered acknowledgement to be created
     * @return The newly created message acknowledgement
     */
    @ApiOperation(value = "Create a message processed acknowledgement", notes = "Acknowledges that a message has been processed by the backend",
            authorizations = @Authorization(value = "basicAuth"), tags = "acknowledgement")
    @PostMapping(path = "/processed")
    public MessageAcknowledgementDTO acknowledgeMessageProcessed(@RequestBody MessageAcknowledgementRequestDTO acknowledgementRequestDTO) {
        return messageAcknowledgeService.acknowledgeMessageProcessed(acknowledgementRequestDTO.getMessageId(), acknowledgementRequestDTO.getAcknowledgeDate(), acknowledgementRequestDTO.getProperties());
    }

    /**
     * Gets all acknowledgments associated to a message id
     *
     * @param messageId The message id for which message acknowledgments are retrieved
     * @return All acknowledgments registered for a specific message
     */
    @ApiOperation(value = "Get acknowledgements", notes = "Gets all acknowledgments associated to a message id",
            authorizations = @Authorization(value = "basicAuth"), tags = "acknowledgement")
    @GetMapping(path = "/{messageId:.+}")
    @ResponseBody
    public List<MessageAcknowledgementDTO> getAcknowledgedMessages(@PathVariable(value = "messageId") String messageId) {
        return messageAcknowledgeService.getAcknowledgedMessages(messageId);
    }


}
