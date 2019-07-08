package eu.domibus.ext.rest;


import eu.domibus.api.pmode.PModeException;
import eu.domibus.ext.domain.PModeArchiveInfoDTO;
import eu.domibus.ext.services.PModeExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

/**
 * @author Catalin Enache
 * @since 4.1.1
 */
@RestController
@RequestMapping(value = "/ext/pmode")
public class PModeFileResource {

    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PModeFileResource.class);

    @Autowired
    PModeExtService pModeExtService;

    @ApiOperation(value = "Get PMode file", notes = "Retrieve the PMode file of specified id",
            authorizations = @Authorization(value = "basicAuth"), tags = "pmode")
    @RequestMapping(path = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<ByteArrayResource> downloadPMode(@PathVariable(value = "id") int id) {
        LOG.debug("downloadPMode -> start");
        final byte[] rawConfiguration = pModeExtService.getPModeFile(id);
        ByteArrayResource resource = new ByteArrayResource(new byte[0]);
        if (rawConfiguration != null) {
            resource = new ByteArrayResource(rawConfiguration);
        }

        HttpStatus status = HttpStatus.OK;
        if (resource.getByteArray().length == 0) {
            status = HttpStatus.NO_CONTENT;
        }
        return ResponseEntity.status(status)
                .contentType(MediaType.parseMediaType(MediaType.APPLICATION_XML_VALUE))
                .header("content-disposition", "attachment; filename=Pmodes.xml")
                .body(resource);
    }

    @ApiOperation(value = "Get current PMode info", notes = "Retrieve the current PMode file information",
            authorizations = @Authorization(value = "basicAuth"), tags = "pmode")
    @GetMapping(path = "current")
    public PModeArchiveInfoDTO getCurrentPMode() {
        LOG.debug("getCurrentPMode -> start");
        return pModeExtService.getCurrentPmode();
    }

    @ApiOperation(value = "Upload a PMode file", notes = "Upload the PMode file",
            authorizations = @Authorization(value = "basicAuth"), tags = "pmode")
    @PostMapping
    public ResponseEntity<String> uploadPMode(
            @RequestPart("file") MultipartFile pmode,
            @RequestParam("description") @Valid String pModeDescription) {
        if (pmode.isEmpty()) {
            return ResponseEntity.badRequest().body("Failed to upload the PMode file since it was empty.");
        }
        try {
            byte[] bytes = pmode.getBytes();

            List<String> pmodeUpdateMessage = pModeExtService.updatePModeFile(bytes, pModeDescription);
            String message = "PMode file has been successfully uploaded";
            if (pmodeUpdateMessage != null && !pmodeUpdateMessage.isEmpty()) {
                message += " but some issues were detected: \n" + StringUtils.join(pmodeUpdateMessage, "\n");
            }
            return ResponseEntity.ok(message);
        } catch (PModeException e) {
            LOG.error("Error uploading the PMode", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            LOG.error("Error uploading the PMode", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload the PMode file due to: " + ExceptionUtils.getRootCauseMessage(e));
        }
    }

}
