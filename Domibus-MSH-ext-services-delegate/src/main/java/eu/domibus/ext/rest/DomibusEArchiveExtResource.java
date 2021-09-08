package eu.domibus.ext.rest;

import eu.domibus.ext.domain.ErrorDTO;
import eu.domibus.ext.domain.archive.*;
import eu.domibus.ext.exceptions.DomibusEArchiveExtException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * eArchive Domibus services.
 * Domibus expose the REST API eArchive services to be used by an archiving client for managing exported archive batches.
 *
 * @author Joze Rihtarsic
 * @since 5.0
 */
@RestController
@RequestMapping(value = "/ext/archive")
public class DomibusEArchiveExtResource {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusEArchiveExtResource.class);


    /**
     * Handling EArchive exceptions
     * @param e
     * @return ErrorDTO object.
     */
    @ExceptionHandler(DomibusEArchiveExtException.class)
    public ResponseEntity<ErrorDTO> handleEArchiveExtException(DomibusEArchiveExtException e) {
        // TODO implement error handling
        return null;
    }

    /**
     * List batch export requests that are queued (continuous and one-time)
     *
     * Method returns the list of batches that are queued to be processed asynchronously by
     * Domibus. It can be used for monitoring purposes.
     *
     * @param filterRequestDTO: object contains filtering parameters as: last N enqueued, maximum N results, only one-time or continuous requests, batches enqueued in
     * a specific day or time period.
     * @return MonitoringInfoDTO with the status of monitoring services specific to the filter
     */
    @ApiOperation(value = "List batch export requests that are queued",
            notes = " Method returns the list of batches that are queued to be processed asynchronously by Domibus.",
            authorizations = @Authorization(value = "basicAuth"), tags = "archive")
    @GetMapping(path = "batches/queued")
    public List<QueuedBatchDTO> getQueuedBatchRequests(QueuedBatchFilterRequestDTO filterRequestDTO ) {
        // TODO implement search method
        return null;
    }

    /**
     * Get the message IDs exported in a batch
     *
     * Method returns the message IDs exported in a batch for the given ID. All message IDs are exported if the
     * limit and start parameters are not provided.
     *
     * @param filterRequestDTO: the message filter with parameters:  batch id, limit: the total number of message IDs exported and  start: the offset from which the message IDs export will start
     * @return List of message ids for the batch
     */

    @ApiOperation(value = "Get the message IDs exported in a batch",
            notes = "Method returns the message IDs exported in a batch for the given ID. All message IDs are exported if the\n" +
                    "limit and start parameters are not provided.",
            authorizations = @Authorization(value = "basicAuth"), tags = "archive")
    @GetMapping(path = "batches/exported/{batchId:.+}/messages")
    public ExportedBatchMessagesDTO getBatchMessageIds(@PathVariable(name = "batchId") String batchId,
                                                       ExportedBatchMessagesFilterRequestDTO filterRequestDTO) {

        // TODO implement search method
        return null;
    }


    /**
     * History of the exported batches
     *
     * This REST endpoint provides a history of exported batches with status success, failed or expired. It
     * allows the archiving client to validate if it has archived all exported batches.
     *
     *
     * @param filterRequestDTO: filters for start date and end date of the exported messages included in the batch, batch status,
     * batch re-export status (true/false; includes batches for which a re-export has been requested using the REST endpoint)
     *
     * @return MonitoringInfoDTO with the status of monitoring services specific to the filter
     */
    @ApiOperation(value = "History of the exported batches",
            notes = "This REST endpoint provides a history of exported batches with status success, failed or expired. It\n" +
                    " allows the archiving client to validate if it has archived all exported batches.",
            authorizations = @Authorization(value = "basicAuth"), tags = "archive")
    @GetMapping(path = "batches/exported")
    public List<ExportedBatchDTO> historyOfTheExportedBatches(
            ExportedBatchFilterRequestDTO filterRequestDTO

    ) {
        // TODO implement search method
        return null;
    }

    /**
     *  Request to export a batch based on batch id
     *
     * This REST endpoint will export a new batch with a new batch id containing the same messages that
     * were already exported in a batch identified by the batch id provided as a parameter. The batch id
     * identifying the previously exported batch will not be automatically deleted or modified in the
     * database or on the disk storage. The retention mechanism can potentially delete it later
     *
     * This endpoint can be used in cases where the export or archival of a batch has failed or it expired as
     * well as for other unexpected situations.
     * The request contains a batch id that has been extracted, for instance, from the history of batch
     *
     * @param batchId: The batch id that has been extracted, for instance, from the history of batch  requests
     *
     */
    @ApiOperation(value = "Export a batch based on batch id",
            notes = "This REST endpoint will export a new batch with a new batch id containing the same messages that" +
                    " were already exported in a batch identified by the batch id provided as a parameter.",
            authorizations = @Authorization(value = "basicAuth"), tags = "archive")
    @PutMapping(path = "batches/exported/{batchId:.+}/export")
    public BatchStatusDTO exportBatch(
            @PathVariable(name = "batchId") String batchId
    ) {
        // TODO implement method
        return null;
    }

    /**
     *  Notification from the archiving client that it has successfully archived or failed to
     * archive a specific batch
     *
     * This REST endpoint will be used by the archiving client to confirm that a batch was archived
     * successfully or that it failed to archive it. The request contains the batch identifier which allows
     * Domibus to identify all messages in the batch to mark them as archived and eligible for purging.
     * Note that, for performance reasons, Domibus will asynchronously mark the batch messages as
     * archived.
     * Therefore, this REST endpoint only confirms to the client that it has acknowledged the notification
     * and it does not mean that the batch messages are already marked as archived.
     *
     * @param batchId: The batch id that has been extracted, for instance, from the history of batch  requests
     *
     */
    @ApiOperation(value = "Sets batch as successfully archived",
            notes = "This REST endpoint will be used by the archiving client to confirm that a batch was archived " +
                    "successfully or that it failed to archive it. The request contains the batch identifier which allows " +
                    "Domibus to identify all messages in the batch to mark them as archived and eligible for purging. " +
                    "Note that, for performance reasons, Domibus will asynchronously mark the batch messages as " +
                    "archived.",
            authorizations = @Authorization(value = "basicAuth"), tags = "archive")
    @PutMapping(path = "batches/exported/{batchId:.+}/confirm-archived")
    public BatchStatusDTO setBatchAsArchived(
            @PathVariable(name = "batchId") String batchId
    ) {
        // TODO implement method
        return null;
    }

    /**
     *  Get messages which were not archived within a specific period
     *
     * This REST endpoint can be used to check if all AS4 messages received or sent within a specific period
     * were archived.
     * The response will contain the list of the message IDs which were not archived during the specified
     * period.
     *
     * @param filterRequestDTO:  filter object contains start date and end date of the period to be checked, limit: the total number of message IDs exported
     * start: the offset for the message IDs listed
     *
     */
    @ApiOperation(value = " Messages which were not archived within a specific period",
            notes = "This REST endpoint can be used to check if all AS4 messages received or sent within a specific period " +
                    "were archived.",
            authorizations = @Authorization(value = "basicAuth"), tags = "archive")
    @GetMapping(path = "messages/not-archived")
    public MessagesDTO notArchivedMessages(MessagesFilterRequestDTO filterRequestDTO
    ) {
        // TODO implement method
        return null;
    }

    /**
     *  Get the current start date of the continuous export
     *
     * This REST endpoint will expose the continuous export mechanism current start date
     */
    @ApiOperation(value = "Get the current start date of the continuous export",
            notes = "This REST endpoint will expose the continuous export mechanism current start date.",
            authorizations = @Authorization(value = "basicAuth"), tags = "archive")
    @GetMapping(path = "batches/queued/current-start-date")
    public CurrentBatchStartDateDTO getCurrentExportDate() {
        // TODO implement method
        return null;
    }
}

