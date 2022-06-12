package eu.domibus.archive.client.webhook;

import eu.domibus.archive.client.webhook.model.BatchNotification;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * The class is webhook eArchive domibus REST API example based on "standard" javax RS API annotations. The purpose of the class is to
 * generate OpenAPI document. The class can be used also by the java developers to implement Webhook REST API endpoints.
 * <p>
 * In order to integrate with Domibus, an archiving client MUST implement the REST API
 * services defined in this class.
 * <p>
 * Domibus will notify the archiving client each time it will export a batch.
 * The REST endpoint URLs that Domibus will use to notify the archiving client would be statically
 * configured in the Domibus properties.
 *
 * @author Joze Rihtarsic
 * @since 5.0
 */
@OpenAPIDefinition(
        info = @Info(
                title = "DomibusArchiveWebhook eArchive client's servlet API",
                version = "v1.0",
                description = "In order to fully integrate with the Domibus, an archiving client MUST implement the REST API message notification methods. " +
                        "Domibus will notify the archiving client each time it will export or delete a batch. The REST endpoint URLs that Domibus will use to notify the archiving client " +
                        "must be statically configured in the Domibus properties.",
                license = @License(name = "EUPL 1.2", url = "https://www.eupl.eu/"),
                contact = @Contact(url = "https://ec.europa.eu/digital-building-blocks/wikis/display/CEFDIGITAL/Domibus", name = "Domibus")
        ),
        tags = {
                @Tag(name = "archive-webhook", description = "The REST endpoint URLs that Domibus will use to notify the archiving client each time it will export a batch.")
        },
        externalDocs = @ExternalDocumentation(description = "Domibus page", url = "https://ec.europa.eu/digital-building-blocks/wikis/display/CEFDIGITAL/Domibus")
)
@SecurityScheme(
        name = "basicAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "basic"
)
@Path("/domibus-archive-client/webhook/")
interface DomibusEArchiveWebhookResource {

    /**
     * Receive notification when a batch has been exported in the shared folder
     * <p>
     * Domibus notifies the archiving client when a batch has been exported in the shared folder.
     * The notification is performed for a successful and for a failed export.
     *
     * @param batchId           the targeted batch id
     * @param batchNotification Notification message
     */
    @Operation(summary = "Receive notification when a batch has been exported in the shared folder",
            description = "Domibus notifies the archiving client when a batch has been exported in the shared folder. " +
                    "The notification is performed for a successful and for a failed export.",
            responses = {
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "404", description = "Batch ID not found")
            },
            tags = {"archive-webhook"}
    )
    @PUT
    @Path("/{batch_id:.+}/export-notification")
    @Consumes("application/json")
    void putExportNotification(@PathParam("batch_id") final String batchId,
                               @Parameter(description = "Notification message on export event for the batch", required = true) final BatchNotification batchNotification);

    /**
     * Receive notification when an expired batch has been deleted.
     * <p>
     * Domibus notifies the archiving client when it deletes an expired batch.
     *
     * @param batchId           the targeted batch id
     * @param batchNotification Notification message
     */
    @Operation(summary = "Receive notification when an expired batch has been deleted",
            description = "Domibus notifies the archiving client when it deletes an expired batch.",
            responses = {
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "404", description = "Batch ID not found")
            },
            tags = {"archive-webhook"}
    )
    @PUT
    @Path("/{batch_id:.+}/stale-notification")
    @Consumes("application/json")
    void putStaleNotification(@PathParam("batch_id") final String batchId,
                              @Parameter(description = "Notification message on delete for expired batch", required = true) final BatchNotification batchNotification);

}

