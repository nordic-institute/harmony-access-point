package eu.domibus.ext.rest;


import eu.domibus.ext.domain.PModeArchiveInfoDTO;
import eu.domibus.ext.services.PModeExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author Catalin Enache
 * @since 4.1.1
 */
@RestController
@RequestMapping(value = "/ext/pmode")
public class PModeDownloadResource {

    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PModeDownloadResource.class);

    @Autowired
    PModeExtService pModeExtService;

    @ApiOperation(value = "Get PMode file", notes = "Retrieve the PMode file of specified id",
            authorizations = @Authorization(value = "basicAuth"), tags = "pmode")
    @RequestMapping(path = "/{id}", method = RequestMethod.GET, produces = "application/xml")
    public ResponseEntity<? extends Resource> downloadPMode(@PathVariable(value = "id") int id) {
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
                .contentType(MediaType.parseMediaType("application/octet-stream"))
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

}
