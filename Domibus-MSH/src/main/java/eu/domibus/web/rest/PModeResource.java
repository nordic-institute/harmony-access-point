package eu.domibus.web.rest;

import eu.domibus.api.pmode.PModeArchiveInfo;
import eu.domibus.api.pmode.PModeException;
import eu.domibus.api.pmode.PModeService;
import eu.domibus.api.pmode.ValidationIssue;
import eu.domibus.api.util.MultiPartFileUtil;
import eu.domibus.api.validators.CustomWhiteListed;
import eu.domibus.common.model.configuration.ConfigurationRaw;
import eu.domibus.core.audit.AuditService;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.pmode.validation.PModeValidationHelper;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.ro.PModeResponseRO;
import eu.domibus.web.rest.ro.ValidationResponseRO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author Mircea Musat
 * @author Tiago Miguel
 * @since 3.3
 */
@RestController
@RequestMapping(value = "/rest/pmode")
@Validated
public class PModeResource extends BaseResource {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PModeResource.class);

    @Autowired
    PModeService pModeService;

    @Autowired
    private PModeProvider pModeProvider;

    @Autowired
    private DomainCoreConverter domainConverter;

    @Autowired
    private AuditService auditService;

    @Autowired
    PModeValidationHelper pModeValidationHelper;

    @Autowired
    MultiPartFileUtil multiPartFileUtil;

    @GetMapping(path = "{id}", produces = "application/xml")
    public ResponseEntity<? extends Resource> downloadPmode(
            @PathVariable(value = "id") int id,
            @DefaultValue("false") @QueryParam("noAudit") boolean noAudit, @DefaultValue("false") @QueryParam("archiveAudit") boolean archiveAudit) {

        final byte[] rawConfiguration = pModeProvider.getPModeFile(id);
        ByteArrayResource resource = new ByteArrayResource(new byte[0]);
        if (rawConfiguration != null) {
            resource = new ByteArrayResource(rawConfiguration);
        }

        HttpStatus status = HttpStatus.OK;
        if (resource.getByteArray().length == 0) {
            status = HttpStatus.NO_CONTENT;
        } else if (!noAudit) {
            if (archiveAudit) {
                auditService.addPModeArchiveDownloadedAudit(Integer.toString(id));
            }
            auditService.addPModeDownloadedAudit(Integer.toString(id));
        }

        return ResponseEntity.status(status)
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header("content-disposition", "attachment; filename=Pmodes.xml")
                .body(resource);
    }

    @GetMapping(path = "current")
    public PModeResponseRO getCurrentPMode() {
        final PModeArchiveInfo currentPmode = pModeProvider.getCurrentPmode();
        if (currentPmode != null) {
            final PModeResponseRO convert = domainConverter.convert(currentPmode, PModeResponseRO.class);
            convert.setCurrent(true);
            return convert;
        }
        return null;

    }

    @PostMapping
    public ValidationResponseRO uploadPMode(
            @RequestPart("file") @Valid MultipartFile pModeFile,
            //we permit more chars for description
            @RequestParam("description") @CustomWhiteListed(permitted = ".,;:/*\"&=+%\r\n") String pModeDescription) throws PModeException {

        byte[] pModeContent = multiPartFileUtil.validateAndGetFileContent(pModeFile, Arrays.asList(MimeTypeUtils.APPLICATION_XML, MimeTypeUtils.TEXT_XML));

        List<ValidationIssue> pModeUpdateIssues = pModeService.updatePModeFile(pModeContent, pModeDescription);
        return pModeValidationHelper.getValidationResponse(pModeUpdateIssues, "PMode file has been successfully uploaded.");
    }

    @DeleteMapping
    public ResponseEntity<String> deletePModes(@RequestParam("ids") List<String> pModeIds) {
        if (pModeIds.isEmpty()) {
            LOG.error("Failed to delete PModes since the list of ids was empty.");
            return ResponseEntity.badRequest().body("Failed to delete PModes since the list of ids was empty.");
        }
        try {
            for (String pModeId : pModeIds) {
                pModeProvider.removePMode(Integer.parseInt(pModeId));
            }
        } catch (Exception ex) {
            LOG.error("Impossible to delete PModes", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Impossible to delete PModes due to \n" + ex.getMessage());
        }
        LOG.debug("PModes {} were deleted", pModeIds);
        return ResponseEntity.ok("PModes were deleted\n");
    }

    @PutMapping(value = {"/restore/{id}"})
    public ValidationResponseRO restorePmode(@PathVariable(value = "id") Integer id) {
        ConfigurationRaw existingRawConfiguration = pModeProvider.getRawConfiguration(id);
        ConfigurationRaw newRawConfiguration = new ConfigurationRaw();
        newRawConfiguration.setEntityId(0);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ssO");
        ZonedDateTime confDate = ZonedDateTime.ofInstant(existingRawConfiguration.getConfigurationDate().toInstant(), ZoneId.systemDefault());
        newRawConfiguration.setDescription("Restored version of " + confDate.format(formatter));

        newRawConfiguration.setConfigurationDate(new Date());
        newRawConfiguration.setXml(existingRawConfiguration.getXml());

        List<ValidationIssue> pModeUpdateIssues = pModeService.updatePModeFile(newRawConfiguration.getXml(), newRawConfiguration.getDescription());
        return pModeValidationHelper.getValidationResponse(pModeUpdateIssues, "PMode file has been successfully restored.");
    }

    @GetMapping(value = {"/list"})
    public List<PModeResponseRO> pmodeList() {
        return domainConverter.convert(pModeProvider.getRawConfigurationList(), PModeResponseRO.class);
    }

    /**
     * This method returns a CSV file with the contents of PMode Archive table
     *
     * @return CSV file with the contents of PMode Archive table
     */
    @GetMapping(path = "/csv")
    public ResponseEntity<String> getCsv() {

        // get list of archived pmodes
        List<PModeResponseRO> pModeList = pmodeList();
        getCsvService().validateMaxRows(pModeList.size());

        // set first PMode as current
        if (!pModeList.isEmpty()) {
            pModeList.get(0).setCurrent(true);
        }

        return exportToCSV(pModeList,
                PModeResponseRO.class,
                Arrays.asList("id"),
                "pmodearchive");
    }
}
