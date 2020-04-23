package eu.domibus.ext.rest;

import eu.domibus.api.pmode.PModeException;
import eu.domibus.ext.domain.*;
import eu.domibus.ext.exceptions.PartyExtServiceException;
import eu.domibus.ext.services.PartyExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * External API for Participants (Parties)
 *
 * @author Catalin Enache
 * @since 4.2
 */
@RestController
@RequestMapping(value = "/ext/party")
public class PartyExtResource {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PartyExtResource.class);

    @Autowired
    PartyExtService partyExtService;

    @ExceptionHandler(PartyExtServiceException.class)
    public ResponseEntity<ErrorDTO> handlePartyExtServiceException(PartyExtServiceException e) {
        String message = e.getMessage();
        if (e.getCause() instanceof PModeException) {
            message = ExceptionUtils.getRootCauseMessage(e);
        }
        ErrorDTO errorRO = new ErrorDTO(message);
        return ResponseEntity.badRequest().body(errorRO);
    }

    @ApiOperation(value = "Get Parties", notes = "Get Parties using certain criteria like name, endpoint, partyId, process name. " +
            "Use pageStart and pageSize for pagination purposes",
            authorizations = @Authorization(value = "basicAuth"), tags = "party")
    @GetMapping
    public List<PartyDTO> listParties(PartyFilterRequestDTO request) {
        LOG.debug("Searching parties with parameters:" +
                        " name [{}], endPoint [{}], partyId [{}], processName [{}], pageStart [{}], pageSize [{}]",
                request.getName(), request.getEndPoint(), request.getPartyId(), request.getProcess(), request.getPageStart(), request.getPageSize());

        return partyExtService.getParties(request.getName(),
                request.getEndPoint(), request.getPartyId(),
                request.getProcess(), request.getPageStart(), request.getPageSize());
    }

    @ApiOperation(value = "Creates a Party",
            notes = "Creates a Party using name, party id, endpoint and identifiers which are mandatory fields",
            authorizations = @Authorization(value = "basicAuth"), tags = "party")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String createParty(@RequestBody PartyDTO request) {
        partyExtService.createParty(request);
        return "Party having partyName=[" + request.getName() + "] created successfully!";
    }

    @ApiOperation(value = "Delete a Party",
            notes = "Delete a Party based on party name",
            authorizations = @Authorization(value = "basicAuth"), tags = "party")
    @DeleteMapping
    public String deleteParty(@RequestParam(value = "partyName") @Valid @NotNull String partyName) {
        partyExtService.deleteParty(partyName);
        return "Party having partyName=[" + partyName + "] has been successfully deleted";

    }

    @ApiOperation(value = "Get Certificate for a Party",
            notes = "Get Certificate for a Party based on party name",
            authorizations = @Authorization(value = "basicAuth"), tags = "party")
    @GetMapping(value = "/{partyName}/certificate")
    public ResponseEntity<Object> getCertificateForParty(@PathVariable(name = "partyName") String partyName) {
        TrustStoreDTO cert = partyExtService.getPartyCertificateFromTruststore(partyName);
        if (cert == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(cert);
    }

    @ApiOperation(value = "List all Processes",
            notes = "List all Processes",
            authorizations = @Authorization(value = "basicAuth"), tags = "party")
    @GetMapping(value = {"/processes"})
    public List<ProcessDTO> listProcesses() {
        return partyExtService.getAllProcesses();
    }

    @ApiOperation(value = "Update a Party",
            notes = "Update a Party based on party name",
            authorizations = @Authorization(value = "basicAuth"), tags = "party")
    @PutMapping
    public String updateParty(@RequestBody PartyDTO partyDTO) {
        LOG.debug("Updating party [{}]", partyDTO);
        partyExtService.updateParty(partyDTO);
        return "Party having partyName=[" + partyDTO.getName() + "] has been successfully updated";
    }

}
