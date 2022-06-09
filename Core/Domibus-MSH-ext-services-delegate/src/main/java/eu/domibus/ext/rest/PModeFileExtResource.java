package eu.domibus.ext.rest;


import eu.domibus.ext.domain.ErrorDTO;
import eu.domibus.ext.domain.PModeArchiveInfoDTO;
import eu.domibus.ext.domain.ValidationIssueDTO;
import eu.domibus.ext.domain.ValidationResponseDTO;
import eu.domibus.ext.exceptions.PModeExtException;
import eu.domibus.ext.rest.error.ExtExceptionHelper;
import eu.domibus.ext.services.PModeExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author Catalin Enache
 * @since 4.1.1
 */
@RestController
@RequestMapping(value = "/ext/pmode")
@Tag(name = "pmode", description = "Domibus PMode management API")
public class PModeFileExtResource {

    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PModeFileExtResource.class);

    final PModeExtService pModeExtService;

    final ExtExceptionHelper extExceptionHelper;

    public PModeFileExtResource(PModeExtService pModeExtService, ExtExceptionHelper extExceptionHelper) {
        this.pModeExtService = pModeExtService;
        this.extExceptionHelper = extExceptionHelper;
    }

    @ExceptionHandler(PModeExtException.class)
    protected ResponseEntity<ErrorDTO> handlePModeExtException(PModeExtException e) {
        return extExceptionHelper.handleExtException(e);
    }

    @Operation(summary="Get PMode file", description="Retrieve the PMode file of specified id",
            security = @SecurityRequirement(name ="DomibusBasicAuth"))
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<ByteArrayResource> downloadPMode(@PathVariable(value = "id") long id) {
        LOG.debug("downloadPMode -> start");
        final byte[] rawConfiguration;
        try {
            rawConfiguration = pModeExtService.getPModeFile(id);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
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

    @Operation(summary="Get current PMode info", description="Retrieve the current PMode file information",
            security = @SecurityRequirement(name ="DomibusBasicAuth"))
    @GetMapping(path = "current")
    public PModeArchiveInfoDTO getCurrentPMode() {
        LOG.debug("getCurrentPMode -> start");
        return pModeExtService.getCurrentPmode();
    }

    @Operation(summary="Upload a PMode file", description="Upload the PMode file",
            security = @SecurityRequirement(name ="DomibusBasicAuth"))
    @PostMapping(consumes = {"multipart/form-data"})
    public ValidationResponseDTO uploadPMode(
            @RequestPart("file") MultipartFile pmode,
            @RequestParam("description") @Valid @NotEmpty String pModeDescription) {

        List<ValidationIssueDTO> pmodeUpdateMessage = pModeExtService.updatePModeFile(pmode, pModeDescription);

        String message = "PMode file has been successfully uploaded";
        if (!CollectionUtils.isEmpty(pmodeUpdateMessage)) {
            message += " but some issues were detected:";
        }
        return new ValidationResponseDTO(message, pmodeUpdateMessage);
    }

}
