package eu.domibus.ext.rest;


import eu.domibus.ext.delegate.converter.DomainExtConverter;
import eu.domibus.ext.domain.ErrorDTO;
import eu.domibus.ext.domain.PModeArchiveInfoDTO;
import eu.domibus.ext.domain.ValidationIssueDTO;
import eu.domibus.ext.domain.ValidationResponseDTO;
import eu.domibus.ext.exceptions.PModeExtException;
import eu.domibus.ext.rest.error.ExtExceptionHelper;
import eu.domibus.ext.services.PModeExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
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
public class PModeFileExtResource {

    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PModeFileExtResource.class);

    @Autowired
    ExtExceptionHelper extExceptionHelper;

    @ExceptionHandler(PModeExtException.class)
    protected ResponseEntity<ErrorDTO> handlePModeExtException(PModeExtException e) {
        return extExceptionHelper.handleExtException(e);
    }

    @Autowired
    PModeExtService pModeExtService;

    @Autowired
    DomainExtConverter domainConverter;

    @ApiOperation(value = "Get PMode file", notes = "Retrieve the PMode file of specified id",
            authorizations = @Authorization(value = "basicAuth"), tags = "pmode")
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_XML_VALUE)
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
    @PostMapping(consumes = {"multipart/form-data", "application/x-www-form-urlencoded"})
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
