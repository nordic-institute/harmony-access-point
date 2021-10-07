package eu.domibus.ext.rest;

import eu.domibus.ext.domain.ErrorDTO;
import eu.domibus.ext.domain.archive.*;
import eu.domibus.ext.exceptions.DomibusEArchiveExtException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
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
@SecurityScheme(type = SecuritySchemeType.HTTP, name = "DomibusBasicAuth", scheme = "basic")
@Tag(name = "archive", description = "Domibus eArchive services API")
public class DomibusEArchiveExtResource {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusEArchiveExtResource.class);


    /**
     * Handling EArchive exceptions
     *
     * @param e: Domibus Archive Exception
     * @return ErrorDTO object.
     */
    @ExceptionHandler(DomibusEArchiveExtException.class)
    public ResponseEntity<ErrorDTO> handleEArchiveExtException(DomibusEArchiveExtException e) {
        // TODO implement error handling
        return null;
    }

    /**
     * List batch export requests that are queued (continuous and one-time)
     * <p>
     * Method returns the list of batches that are queued to be processed asynchronously by
     * Domibus. It can be used for monitoring purposes.
     *
     * @param lastCountRequests return last N enqueued bath export requests
     * @param maxCountResults   return max number of enqueued bath export requests
     * @param requestType       return bath types  (values  ALL, CONTINUOUS, ONE_TIME)
     * @param startDate         start day-time  of batches enqueued
     * @param endDate           end day-time  of batches enqueued
     * @return the list of queued batches
     */
    @Operation(summary = "List batch export requests that are queued",
            description = "Method returns the list of batches that are queued to be processed asynchronously by Domibus.",
            security = @SecurityRequirement(name ="DomibusBasicAuth")
    )
    @GetMapping(path = "batches/queued")
    public List<QueuedBatchDTO> getQueuedBatchRequests(@RequestParam("lastCountRequests") Integer lastCountRequests,
                                                       @RequestParam("maxCountResults") Integer maxCountResults,
                                                       @RequestParam("requestType") BatchRequestType requestType,
                                                       @RequestParam("startDate") Date startDate,
                                                       @RequestParam("endDate") Date endDate) {
        // TODO implement search method
        return null;
    }

    /**
     * Get the message IDs exported in a batch
     * <p>
     * Method returns the message IDs exported in a batch for the given ID. All message IDs are exported if the
     * limit and start parameters are not provided.
     *
     * @param batchId: batch id of the message ids,
     * @param pageStart: the offset from which the message IDs export will start
     * @param pageSize: maximum number of records in the page
     * @return List of message ids in the batch
     */

    @Operation(summary = "Get the message IDs exported in a batch",
            description = "Method returns the message IDs exported in a batch for the given ID. All message IDs are exported if the\n" +
                    "limit and start parameters are not provided.",
            security = @SecurityRequirement(name ="DomibusBasicAuth"))
    @GetMapping(path = "batches/exported/{batchId:.+}/messages")
    public ExportedBatchMessagesDTO getBatchMessageIds(@PathVariable(name = "batchId") String batchId,
                                                       @RequestParam("pageStart") Integer pageStart,
                                                       @RequestParam("pageSize") Integer pageSize
    ) {

        // TODO implement search method
        return null;
    }


    /**
     * History of the exported batches
     * <p>
     * This REST endpoint provides a history of exported batches with status success, failed or expired. It
     * allows the archiving client to validate if it has archived all exported batches.
     *
     * @param startDate: start date of the exported messages in the batch
     * @param endDate:   end date  of the exported messages included in the batch,
     * @param status:   batch status,
     * @param reExport:   batch re-export status (true/false; includes batches for which a re-export has been requested using the REST endpoint)
     * @param pageStart: the offset/page from which the message IDs export will start. List is sorted by batch request date
     * @param pageSize: maximum number of records in the page
     * @return list of the exported batches
     */
    @Operation(summary = "History of the exported batches",
            description = "This REST endpoint provides a history of exported batches with status success, failed or expired. It\n" +
                    " allows the archiving client to validate if it has archived all exported batches.",
            security = @SecurityRequirement(name ="DomibusBasicAuth"))
    @GetMapping(path = "batches/exported")
    public List<ExportedBatchDTO> historyOfTheExportedBatches(
            @RequestParam("startDate") Date startDate,
            @RequestParam("endDate") Date endDate,
            @RequestParam("status") String status,
            @RequestParam("reExport") Boolean reExport,
            @RequestParam("pageStart") Integer pageStart,
            @RequestParam("pageSize") Integer pageSize
    ) {
        // TODO implement search method
        return null;
    }

    /**
     * Request to export a batch based on batch id
     * <p>
     * This REST endpoint will export a new batch with a new batch id containing the same messages that
     * were already exported in a batch identified by the batch id provided as a parameter. The batch id
     * identifying the previously exported batch will not be automatically deleted or modified in the
     * database or on the disk storage. The retention mechanism can potentially delete it later
     * <p>
     * This endpoint can be used in cases where the export or archival of a batch has failed or it expired as
     * well as for other unexpected situations.
     * The request contains a batch id that has been extracted, for instance, from the history of batch
     *
     * @param batchId: The batch id that has been extracted, for instance, from the history of batch  requests
     * @return status of the queued export request
     */
    @Operation(summary = "Export a batch based on batch id",
            description = "This REST endpoint will export a new batch with a new batch id containing the same messages that" +
                    " were already exported in a batch identified by the batch id provided as a parameter.",
            security = @SecurityRequirement(name ="DomibusBasicAuth"))
    @PutMapping(path = "batches/exported/{batchId:.+}/export")
    public BatchStatusDTO exportBatch(
            @PathVariable(name = "batchId") String batchId
    ) {
        // TODO implement method
        return null;
    }

    /**
     * Notification from the archiving client that it has successfully archived or failed to
     * archive a specific batch
     * <p>
     * This REST endpoint will be used by the archiving client to confirm that a batch was archived
     * successfully or that it failed to archive it. The request contains the batch identifier which allows
     * Domibus to identify all messages in the batch to mark them as archived and eligible for purging.
     * Note that, for performance reasons, Domibus will asynchronously mark the batch messages as
     * archived.
     * Therefore, this REST endpoint only confirms to the client that it has acknowledged the notification
     * and it does not mean that the batch messages are already marked as archived.
     *
     * @param batchId: The batch id that has been extracted, for instance, from the history of batch  requests
     * @param batchFinalStatus: Status of the batch ARCHIVED if successfully archived or  FAILED if archival system fail to archive it
     * @return status of the queued export request
     */
    @Operation(summary = "Sets batch as successfully archived",
            description = "This REST endpoint will be used by the archiving client to confirm that a batch was archived " +
                    "successfully or that it failed to archive it. The request contains the batch identifier which allows " +
                    "Domibus to identify all messages in the batch to mark them as archived or failed and eligible for purging. " +
                    "Note that, for performance reasons, Domibus will asynchronously mark the batch messages as " +
                    "archived.",
            security = @SecurityRequirement(name ="DomibusBasicAuth"))
    @PutMapping(path = "batches/exported/{batchId:.+}/close")
    public BatchStatusDTO setBatchAsArchived(
            @PathVariable(name = "batchId") String batchId,
            @RequestParam("batchFinalStatus") BatchStatusType batchFinalStatus

    ) {
        // TODO implement method
        return null;
    }

    /**
     * Get messages which were not archived within a specific period
     * <p>
     * This REST endpoint can be used to check if all AS4 messages received or sent within a specific period
     * were archived.
     * The response will contain the list of the message IDs which were not archived during the specified
     * period.
     *
     * @param startDate: start date of the period to be checked
     * @param startDate: end date of the period to be checked,
     * @param pageStart: the offset/page from which the message IDs export will start. List is sorted by batch request date
     * @param pageSize: maximum number of records in the page
     * @return message list
     */
    @Operation(summary = " Messages which were not archived within a specific period",
            description = "This REST endpoint can be used to check if all AS4 messages received or sent within a specific period " +
                    "were archived.",
            security = @SecurityRequirement(name ="DomibusBasicAuth"))
    @GetMapping(path = "messages/not-archived")
    public MessagesDTO notArchivedMessages(@RequestParam("startDate") Date startDate,
                                           @RequestParam("endDate") Date endDate,
                                           @RequestParam("pageStart") Integer pageStart,
                                           @RequestParam("pageSize") Integer pageSize
    ) {
        // TODO implement method
        return null;
    }

    /**
     * Get the current start date of the continuous export
     * <p>
     * This REST endpoint will expose the continuous export mechanism current start date
     *
     * @return current "continuous export" batch start date
     */
    @Operation(summary = "Get the current start date of the continuous export",
            description = "This REST endpoint will expose the continuous export mechanism current start date.",
            security = @SecurityRequirement(name ="DomibusBasicAuth"))
    @GetMapping(path = "/continous-export/current-start-date")
    public CurrentBatchStartDateDTO getCurrentExportDate() {
        // TODO implement method
        return null;
    }
}

