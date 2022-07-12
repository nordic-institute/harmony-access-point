package eu.domibus.ext.rest;

import eu.domibus.common.MSHRole;
import eu.domibus.ext.domain.ErrorDTO;
import eu.domibus.ext.domain.PartInfoDTO;
import eu.domibus.ext.domain.PartPropertiesDTO;
import eu.domibus.ext.domain.PropertyDTO;
import eu.domibus.ext.exceptions.DomibusErrorCode;
import eu.domibus.ext.exceptions.DomibusServiceExtException;
import eu.domibus.ext.exceptions.PayloadExtException;
import eu.domibus.ext.rest.error.ExtExceptionHelper;
import eu.domibus.ext.services.PayloadExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.activation.DataHandler;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.QueryParam;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.zip.GZIPInputStream;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@RestController
@RequestMapping(value = "/ext/messages")
@OpenAPIDefinition(tags = {
        @Tag(name = "payloads", description = "Domibus User Message Payloads management API"),
})
public class UserMessagePayloadExtResource {

    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessagePayloadExtResource.class);

    @Autowired
    PayloadExtService payloadExtService;

    @Autowired
    ExtExceptionHelper extExceptionHelper;

    @ExceptionHandler(DomibusServiceExtException.class)
    public ResponseEntity<ErrorDTO> handleUserMessageExtException(DomibusServiceExtException e) {
        LOG.error(e.getMessage(), e);
        return extExceptionHelper.handleExtException(e);
    }

    @Operation(summary = "Validates a payload using the configured validator SPI extension", description = "Validates a payload using the configured validator SPI extension",
            security = @SecurityRequirement(name = "DomibusBasicAuth"))
    @PostMapping(path = "payloads/validation", consumes = {"multipart/form-data"})
    public void validatePayload(@RequestPart("file") MultipartFile payload) {
        try {
            payloadExtService.validatePayload(payload.getInputStream(), payload.getContentType());
        } catch (IOException e) {
            throw new PayloadExtException(DomibusErrorCode.DOM_005, "Could not get the payload inputstream");
        }
    }

    @Operation(summary = "Download the UserMessage payload", description = "Download the UserMessage payload with a specific cid",
            security = @SecurityRequirement(name = "DomibusBasicAuth"))
    @GetMapping(path = "ids/{messageEntityId}/payloads/{cid}", produces = MediaType.APPLICATION_XML_VALUE)
    public void downloadPayloadByEntityId(@PathVariable(value = "messageEntityId") Long messageEntityId, @PathVariable(value = "cid") String cid, HttpServletResponse response) {
        LOG.debug("Downloading the payload with cid [{}] for message with id [{}]", cid, messageEntityId);

        final PartInfoDTO payload = payloadExtService.getPayload(messageEntityId, cid);
        writePayloadToResponse(cid, response, payload);
    }

    @Operation(summary = "Download the UserMessage payload", description = "Download the UserMessage payload with a specific cid",
            security = @SecurityRequirement(name = "DomibusBasicAuth"))
    @GetMapping(path = "{messageId}/payloads/{cid}", produces = MediaType.APPLICATION_XML_VALUE)
    public void downloadPayloadByMessageId(@PathVariable(value = "messageId") String messageId, @PathVariable(value = "cid") String cid,
                                           @QueryParam("mshRole") eu.domibus.common.MSHRole role, HttpServletResponse response) {
        if (role == null) {
            role = MSHRole.RECEIVING;
        }
        LOG.debug("Downloading the payload with cid [{}] for message with id [{}] and role [{}]", cid, messageId, role);

        // what role: how to get the msh role here????
        final PartInfoDTO payload = payloadExtService.getPayload(messageId, role, cid);
        writePayloadToResponse(cid, response, payload);
    }

    protected boolean isPayloadCompressed(final PartInfoDTO partInfo) {
        final PartPropertiesDTO partProperties = partInfo.getPartProperties();
        if (partProperties == null || CollectionUtils.isEmpty(partProperties.getProperty())) {
            return false;
        }

        final Set<PropertyDTO> properties = partProperties.getProperty();
        for (final PropertyDTO property : properties) {
            if (MessageConstants.COMPRESSION_PROPERTY_KEY.equalsIgnoreCase(property.getName()) && MessageConstants.COMPRESSION_PROPERTY_VALUE.equalsIgnoreCase(property.getValue())) {
                return true;
            }
        }
        return false;
    }

    private void writePayloadToResponse(String cid, HttpServletResponse response, PartInfoDTO payload) {
        final InputStream payloadInputStream = getPayloadInputStream(cid, payload);
        try {
            final MediaType payloadMediaType = getPayloadMediaType(payload);
            response.setContentType(payloadMediaType.toString());
            response.setHeader("Content-Disposition", "attachment; filename=" + cid);
            IOUtils.copy(payloadInputStream, response.getOutputStream());
            response.flushBuffer();
            IOUtils.close(payloadInputStream);
        } catch (IOException e) {
            throw new PayloadExtException(DomibusErrorCode.DOM_005, "Could not write payload with cid [" + cid + "]", e);
        }
    }

    protected MediaType getPayloadMediaType(PartInfoDTO payload) {
        MediaType result = MediaType.APPLICATION_OCTET_STREAM;
        final String mime = payload.getMime();
        if (StringUtils.isBlank(mime)) {
            return result;
        }

        try {
            result = MediaType.parseMediaType(mime);
        } catch (IllegalArgumentException e) {
            LOG.warn("Could not parse media type [" + mime + "] for payload with cid [" + payload.getHref() + "]", e);
        }
        return result;
    }

    private InputStream getPayloadInputStream(String cid, PartInfoDTO partInfo) {
        if (partInfo == null) {
            throw new PayloadExtException(DomibusErrorCode.DOM_005, "Could not find payload with cid [" + cid + "]");
        }

        final DataHandler payloadDatahandler = partInfo.getPayloadDatahandler();
        if (payloadDatahandler == null) {
            throw new PayloadExtException(DomibusErrorCode.DOM_005, "Error getting the payload with cid [" + cid + "]");
        }

        final boolean payloadCompressed = isPayloadCompressed(partInfo);

        try {
            InputStream inputStream = payloadDatahandler.getInputStream();
            if (payloadCompressed) {
                inputStream = new GZIPInputStream(inputStream);
            }
            return inputStream;
        } catch (IOException e) {
            throw new PayloadExtException(DomibusErrorCode.DOM_005, "Error getting the payload with cid [" + cid + "]", e);
        }
    }
}
