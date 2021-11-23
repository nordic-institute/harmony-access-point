package eu.domibus.ext.rest;

import eu.domibus.ext.domain.ErrorDTO;
import eu.domibus.ext.domain.archive.*;
import eu.domibus.ext.exceptions.DomibusEArchiveExtException;
import eu.domibus.ext.rest.error.ExtExceptionHelper;
import eu.domibus.ext.services.DomibusEArchiveExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
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

    final DomibusEArchiveExtService domibusEArchiveExtService;
    final ExtExceptionHelper extExceptionHelper;

    public DomibusEArchiveExtResource(DomibusEArchiveExtService domibusEArchiveExtService, ExtExceptionHelper extExceptionHelper) {
        this.domibusEArchiveExtService = domibusEArchiveExtService;
        this.extExceptionHelper = extExceptionHelper;
    }

    /**
     * Handling EArchive exceptions
     *
     * @return ErrorDTO object.
     * @param extException Rest Exception response
     */
    @ExceptionHandler(DomibusEArchiveExtException.class)
    public ResponseEntity<ErrorDTO> handleEArchiveExtException(DomibusEArchiveExtException extException) {
        return extExceptionHelper.handleExtException(extException);
    }

    /**
     * List batch export requests that are queued (continuous and one-time)
     * <p>
     * Method returns the list of batches that are queued to be processed asynchronously by
     * Domibus. It can be used for monitoring purposes.
     *
     * @param lastCountRequests return last N enqueued batch export requests - if this parameter is given all others are ignored*
     * @param requestTypes      return batches for given batch types
     * @param startDate         start day-time  of batches enqueued
     * @param endDate           end day-time  of batches enqueued
     * @param pageStart:        the offset from which the message IDs export will start
     * @param pageSize:         maximum number of records in the page
     * @return the list of queued batches
     */
    @Operation(summary = "List batch export requests that are queued",
            description = "Method returns the list of batches that are queued to be processed asynchronously by Domibus.",
            security = @SecurityRequirement(name = "DomibusBasicAuth")
    )
    @GetMapping(path = "batches/queued", produces = {MediaType.APPLICATION_JSON_VALUE})
    public QueuedBatchResultDTO getQueuedBatchRequests(
            @Parameter(description = "Return last N enqueued batch export requests. If this parameter is given all others are ignored.") @RequestParam(value = "lastCountRequests", required = false) Integer lastCountRequests,
            @Parameter(description = "Filter by batch type") @RequestParam(value = "requestType", required = false) List<BatchRequestType> requestTypes,
            @Parameter(description = "Start date-time of batches enqueued") @RequestParam(value = "startDate", required = false) Date startDate,
            @Parameter(description = "End date-time of batches enqueued") @RequestParam(value = "endDate", required = false) Date endDate,
            @Parameter(description = "The offset/page of the result list.") @RequestParam(value = "pageStart", defaultValue = "0") Integer pageStart,
            @Parameter(description = "Maximum number of returned records/page size") @RequestParam(value = "pageSize", defaultValue = "100") Integer pageSize

    ) {

        QueuedBatchFilterDTO filter = new QueuedBatchFilterDTO(lastCountRequests, requestTypes, startDate, endDate);
        QueuedBatchResultDTO resultDTO = new QueuedBatchResultDTO(filter, pageStart, pageSize);
        LOG.info("Return queued batches with filters: [{}] for page: [{}] and page size: [{}].", filter, pageStart, pageSize);
        Long total = domibusEArchiveExtService.getQueuedBatchRequestsCount(filter);

        if (total == null || total < 1L) {
            LOG.trace("No results found found!");
            resultDTO.getPagination().setTotal(0);
            return resultDTO;
        }
        resultDTO.getPagination().setTotal(total.intValue());
        List<QueuedBatchDTO> batches = domibusEArchiveExtService.getQueuedBatchRequests(filter, pageStart, pageSize);
        resultDTO.getQueuedBatches().addAll(batches);
        LOG.trace("Return [{}] results of total: [{}].", batches.size(), total);
        return resultDTO;
    }

    /**
     * Get the message IDs exported in a batch
     * <p>
     * Method returns the message IDs exported in a batch for the given ID. All message IDs are exported if the
     * limit and start parameters are not provided.
     *
     * @param batchId:   batch id of the message ids,
     * @param pageStart: the offset from which the message IDs export will start
     * @param pageSize:  maximum number of records in the page
     * @return List of message ids in the batch
     */

    @Operation(summary = "Get the message IDs exported in a batch",
            description = "Method returns the message IDs exported in a batch for the given ID. All message IDs are exported if the\n" +
                    "limit and start parameters are not provided.",
            security = @SecurityRequirement(name = "DomibusBasicAuth"))
    @GetMapping(path = "/batches/exported/{batchId:.+}/messages", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ExportedBatchMessagesResultDTO getBatchMessageIds(
            @Parameter(description = "Batch id.") @PathVariable(name = "batchId") String batchId,
            @Parameter(description = "The offset/page of the result list.") @RequestParam(value = "pageStart", defaultValue = "0") Integer pageStart,
            @Parameter(description = "Maximum number of returned records/page size.") @RequestParam(value = "pageSize", defaultValue = "100") Integer pageSize
    ) {
        ExportedBatchMessagesResultDTO resultDTO = new ExportedBatchMessagesResultDTO(batchId, pageStart, pageSize);
        LOG.info("Return batch messages with batch id [{}] for page: [{}] and page size: [{}].", batchId, pageStart, pageSize);
        Long total = domibusEArchiveExtService.getBatchMessageCount(batchId);
        if (total == null || total < 1L) {
            LOG.trace("No results found found!");
            resultDTO.getPagination().setTotal(0);
            return resultDTO;
        }
        resultDTO.getPagination().setTotal(total.intValue());
        List<String> messagePage = domibusEArchiveExtService.getBatchMessageIds(batchId, pageStart, pageSize);
        resultDTO.getMessages().addAll(messagePage);
        LOG.trace("Return [{}] results of total: [{}].", messagePage.size(), total);
        return resultDTO;
    }

    /**
     * History of the exported batches
     * <p>
     * This REST endpoint provides a history of exported batches with status success, failed or expired. It
     * allows the archiving client to validate if it has ~~archived all exported batches.
     *
     * @param messageStartDate: start date and hour of the exported messages in the batch yyMMddHH
     * @param messageEndDate:   end date  of the exported messages included in the batch,
     * @param statuses:         Filter by list of batch statues
     * @param includeReExportedBatches:     Batch re-export status (true/false; includes batches for which a re-export has been requested using the REST endpoint)
     * @param pageStart:        the offset/page from which the message IDs export will start. List is sorted by batch request date
     * @param pageSize:         maximum number of records in the page
     * @return list of the exported batches
     */
    @Operation(summary = "History of the exported batches",
            description = "This REST endpoint provides a history of exported batches with status success, failed or expired. It\n" +
                    " allows the archiving client to validate if it has archived all exported batches.",
            security = @SecurityRequirement(name = "DomibusBasicAuth"))
    @GetMapping(path = "/batches/exported", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ExportedBatchResultDTO historyOfTheExportedBatches(
            // message start date-hour in format yyMMddHH
            @Parameter(description = "Start date and hour of the exported messages in the batch. The value is 8 digit number with format yyMMddHH!") @RequestParam("messageStartDate") Long messageStartDate,
            @Parameter(description = "End date and hour of the exported messages in the batch. The value is 8 digit number with format yyMMddHH!") @RequestParam("messageEndDate") Long messageEndDate,
            @Parameter(description = "Filter batches for statuses") @RequestParam(value = "statuses", required = false) List<ExportedBatchStatusType> statuses,
            @Parameter(description = "Include ReExported Batches (true/false; includes batches for which a re-export has been requested using the REST endpoint)!") @RequestParam(value = "reExport", defaultValue = "false") Boolean includeReExportedBatches,
            @Parameter(description = "The offset/page of the result list.") @RequestParam(value = "pageStart", defaultValue = "0") Integer pageStart,
            @Parameter(description = "Maximum number of returned records/page size") @RequestParam(value = "pageSize", defaultValue = "100") Integer pageSize
    ) {

        ExportedBatchFilterDTO filter = new ExportedBatchFilterDTO(messageStartDate, messageEndDate, statuses, includeReExportedBatches);
        ExportedBatchResultDTO resultDTO = new ExportedBatchResultDTO(filter, pageStart, pageSize);
        LOG.info("Return exported batches with filters: [{}] for page: [{}] and page size: [{}].", filter, pageStart, pageSize);

        Long total = domibusEArchiveExtService.getExportedBatchRequestsCount(filter);

        if (total == null || total < 1L) {
            LOG.trace("No results found found!");
            resultDTO.getPagination().setTotal(0);
            return resultDTO;
        }
        resultDTO.getPagination().setTotal(total.intValue());
        List<ExportedBatchDTO> batches = domibusEArchiveExtService.getExportedBatchRequests(filter, pageStart, pageSize);
        resultDTO.getExportedBatches().addAll(batches);
        LOG.trace("Return [{}] results of total: [{}].", batches.size(), total);
        return resultDTO;
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
            security = @SecurityRequirement(name = "DomibusBasicAuth"))
    @PutMapping(path = "/batches/{batchId:.+}/export", produces = {MediaType.APPLICATION_JSON_VALUE})
    public BatchStatusDTO reExportBatch(
            @PathVariable(name = "batchId") String batchId
    ) {
        LOG.info("ReExport batch with ID: [{}].", batchId);
        return domibusEArchiveExtService.reExportBatch(batchId);
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
     * @param batchId:     The batch id that has been extracted, for instance, from the history of batch  requests
     * @param batchStatus: Status of the batch ARCHIVED if successfully archived or  FAILED if archival system fail to archive it
     * @return status of the queued export request
     */
    @Operation(summary = "Sets final status to batch as archived or failed.",
            description = "This REST endpoint will be used by the archiving client to confirm that a batch was archived " +
                    "successfully or that it failed to archive it. The request contains the batch identifier which allows " +
                    "Domibus to identify all messages in the batch to mark them as archived or failed and eligible for purging. " +
                    "Note that, for performance reasons, Domibus will asynchronously mark the batch messages as " +
                    "archived.",
            security = @SecurityRequirement(name = "DomibusBasicAuth"))
    @PutMapping(path = "/batches/exported/{batchId:.+}/close", produces = {MediaType.APPLICATION_JSON_VALUE})
    public BatchStatusDTO setBatchClientStatus(
            @Parameter(description = "The batch Id.") @PathVariable(name = "batchId") String batchId,
            @Parameter(description = "Set the batch archive status.") @RequestParam("status") BatchArchiveStatusType batchStatus,
            @Parameter(description = "Set the batch message/error - reason.") @RequestParam(value = "message", required = false) String message) {
        LOG.info("Set client's final status [{}] for batch with ID: [{}] and message [{}].", batchStatus, batchId, message);
        return domibusEArchiveExtService.setBatchClientStatus(batchId, batchStatus, message);
    }

    /**
     * Get messages which were not archived within a specific period
     * <p>
     * This REST endpoint can be used to check if all AS4 messages received or sent within a specific period
     * were archived.
     * The response will contain the list of the message IDs which were not archived during the specified
     * period.
     *
     * @param messageStartDate: start date of the period to be checked
     * @param messageEndDate:   end date of the period to be checked,
     * @param pageStart:        the offset/page from which the message IDs export will start. List is sorted by batch request date
     * @param pageSize:         maximum number of records in the page
     * @return message list
     */
    @Operation(summary = " Messages which were not archived within a specific period",
            description = "This REST endpoint can be used to check if all AS4 messages received or sent within a specific period " +
                    "were archived.",
            security = @SecurityRequirement(name = "DomibusBasicAuth"))
    @GetMapping(path = "/messages/not-archived", produces = {MediaType.APPLICATION_JSON_VALUE})
    public NotArchivedMessagesResultDTO notArchivedMessages(@Parameter(description = "Message start date of the period to be checked.") @RequestParam("messageStartDate") Date messageStartDate,
                                                            @Parameter(description = "Message end date of the period to be checked.") @RequestParam("messageEndDate") Date messageEndDate,
                                                            @Parameter(description = "The offset/page of the result list.") @RequestParam(value = "pageStart", defaultValue = "0") Integer pageStart,
                                                            @Parameter(description = "Maximum number of returned records/page size.") @RequestParam(value = "pageSize", defaultValue = "100") Integer pageSize
    ) {
        NotArchivedMessagesFilterDTO filter = new NotArchivedMessagesFilterDTO(messageStartDate, messageEndDate);
        LOG.info("Return not archived messagesIds with filters: [{}] for page: [{}] and page size: [{}].", filter, pageStart, pageSize);
        NotArchivedMessagesResultDTO messagesDTO = new NotArchivedMessagesResultDTO(filter, pageSize, pageSize);


        Long total = domibusEArchiveExtService.getNotArchivedMessageCount(filter);
        if (total == null || total < 1L) {
            LOG.trace("No results found found!");
            messagesDTO.getPagination().setTotal(0);
            return messagesDTO;
        }
        messagesDTO.getPagination().setTotal(total.intValue());
        List<String> messageIds = domibusEArchiveExtService.getNotArchivedMessages(filter, pageStart, pageSize);
        LOG.trace("Return [{}] results of total: [{}].", messageIds.size(), total);
        messagesDTO.getMessages().addAll(messageIds);
        return messagesDTO;
    }

    /**
     * Request to update the start date of the next continuous archive job.
     */
    @Operation(summary = "Reset the Continuous archiving with a date",
            description = "This REST endpoint force the continuous archiving process to start at a given date provided by the user." +
                    " All messages older than this date will be consider for archiving if they are not already archived," +
                    " not deleted and in a final state.",
            security = @SecurityRequirement(name = "DomibusBasicAuth"))
    @PutMapping(path = "/continuous-mechanism/start-date")
    public void resetContinuousArchivingStartDate(
            @Parameter(description = "Start date and hour. The value is 8 digit number with format yyMMddHH!") @RequestParam("messageStartDate") Long messageStartDate
    ) {
        LOG.info("Reset continuous archive start date [{}]", messageStartDate);
        domibusEArchiveExtService.updateStartDateContinuousArchive(messageStartDate);
    }

    /**
     * Request the start date of the next continuous archive job.
     */
    @Operation(summary = "Get the Continuous archiving start date",
            description = "This REST endpoint get the date of the next batch for continuous archiving.",
            security = @SecurityRequirement(name = "DomibusBasicAuth"))
    @GetMapping(path = "/continuous-mechanism/start-date", produces = {MediaType.APPLICATION_JSON_VALUE})
    public Long getContinuousArchivingStartDate() {
        Long startDateContinuousArchive = domibusEArchiveExtService.getStartDateContinuousArchive();
        LOG.info("Get continuous archive start date: [{}]", startDateContinuousArchive);
        return startDateContinuousArchive;
    }

    /**
     * Request to update the start date of the next archive job.
     */
    @Operation(summary = "Reset the Sanity archiving with a date",
            description = "This REST endpoint force the sanity archiving process to start at a given date provided by the user." +
                    " All messages older than this date will be consider for archiving if they are not already archived," +
                    " not deleted and in a final state.",
            security = @SecurityRequirement(name = "DomibusBasicAuth"))
    @PutMapping(path = "/sanity-mechanism/start-date")
    public void resetSanityArchivingStartDate(
            @Parameter(description = "Start date and hour. The value is 8 digit number with format yyMMddHH!") @RequestParam("messageStartDate") Long messageStartDate
    ) {
        LOG.info("Reset sanity archive start date [{}]", messageStartDate);
        domibusEArchiveExtService.updateStartDateSanityArchive(messageStartDate);
    }

    /**
     * Request the start date of the next sanity archive job.
     */
    @Operation(summary = "Get the Sanity archiving start date",
            description = "This REST endpoint get the date of the next batch for sanity archiving.",
            security = @SecurityRequirement(name = "DomibusBasicAuth"))
    @GetMapping(path = "/sanity-mechanism/start-date", produces = {MediaType.APPLICATION_JSON_VALUE})
    public Long getSanityArchivingStartDate() {
        Long startDateSanityArchive = domibusEArchiveExtService.getStartDateSanityArchive();
        LOG.info("Get sanity archive start date: [{}]", startDateSanityArchive);
        return startDateSanityArchive;
    }
}

