package eu.domibus.ext.rest;

import eu.domibus.ext.domain.*;
import eu.domibus.ext.exceptions.PartyExtServiceException;
import eu.domibus.ext.rest.error.ExtExceptionHelper;
import eu.domibus.ext.services.PartyExtService;
import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "party", description = "Domibus Party management API")
public class PartyExtResource {

    private static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(PartyExtResource.class);

    @Autowired
    PartyExtService partyExtService;

    @Autowired
    ExtExceptionHelper extExceptionHelper;

    @ExceptionHandler(PartyExtServiceException.class)
    public ResponseEntity<ErrorDTO> handlePartyExtServiceException(PartyExtServiceException e) {
        return extExceptionHelper.handleExtException(e);
    }

    @Operation(summary="Get Parties", description="Get Parties using certain criteria like name, endpoint, partyId, process name. " +
            "Use pageStart and pageSize for pagination purposes",
            security = @SecurityRequirement(name ="DomibusBasicAuth"))
    @GetMapping
    public List<PartyDTO> listParties(PartyFilterRequestDTO request) {
        LOG.debug("Searching parties with parameters:" +
                        " name [{}], endPoint [{}], partyId [{}], processName [{}], pageStart [{}], pageSize [{}]",
                request.getName(), request.getEndPoint(), request.getPartyId(), request.getProcess(), request.getPageStart(), request.getPageSize());

        return partyExtService.getParties(request.getName(),
                request.getEndPoint(), request.getPartyId(),
                request.getProcess(), request.getPageStart(), request.getPageSize());
    }

    @Operation(summary="Creates a Party",
             description="Creates a Party using name, party id, endpoint and identifiers which are mandatory fields",
            security = @SecurityRequirement(name ="DomibusBasicAuth"))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String createParty(@RequestBody PartyDTO request) {
        partyExtService.createParty(request);
        return "Party having partyName=[" + request.getName() + "] created successfully!";
    }

    @Operation(summary="Delete a Party",
             description="Delete a Party based on party name",
            security = @SecurityRequirement(name ="DomibusBasicAuth"))
    @DeleteMapping
    public String deleteParty(@RequestParam(value = "partyName") @Valid @NotNull String partyName) {
        partyExtService.deleteParty(partyName);
        return "Party having partyName=[" + partyName + "] has been successfully deleted";

    }

    @Operation(summary="Get Certificate for a Party",
             description="Get Certificate for a Party based on party name",
            security = @SecurityRequirement(name ="DomibusBasicAuth"))
    @GetMapping(value = "/{partyName}/certificate")
    public ResponseEntity<Object> getCertificateForParty(@PathVariable(name = "partyName") String partyName) {
        TrustStoreDTO cert = partyExtService.getPartyCertificateFromTruststore(partyName);
        if (cert == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(cert);
    }

    @Operation(summary="List all Processes",
             description="List all Processes",
            security = @SecurityRequirement(name ="DomibusBasicAuth"))
    @GetMapping(value = {"/processes"})
    public List<ProcessDTO> listProcesses() {
        return partyExtService.getAllProcesses();
    }

    @Operation(summary="Update a Party",
             description="Update a Party based on party name",
            security = @SecurityRequirement(name ="DomibusBasicAuth"))
    @PutMapping
    public String updateParty(@RequestBody PartyDTO partyDTO) {
        LOG.debug("Updating party [{}]", partyDTO);
        partyExtService.updateParty(partyDTO);
        return "Party having partyName=[" + partyDTO.getName() + "] has been successfully updated";
    }

}
