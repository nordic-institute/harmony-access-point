package eu.domibus.ext.rest;

import eu.domibus.ext.domain.PartyDTO;
import eu.domibus.ext.domain.PartyFilterRequestDTO;
import eu.domibus.ext.domain.ProcessDTO;
import eu.domibus.ext.domain.TrustStoreDTO;
import eu.domibus.ext.exceptions.PartyExtServiceException;
import eu.domibus.ext.services.PartyExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
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

    @ApiOperation(value = "Get Parties", notes = "Get Parties using certain criteria like name, endpoint, partyId, process name. " +
            "Use pageStart and pageSize for pagination purposes",
            authorizations = @Authorization(value = "basicAuth"), tags = "party")
    @GetMapping(value = {"/list"})
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
    @PostMapping(value = "/save")
    public ResponseEntity<Object> createParty(@RequestBody PartyDTO request) {
        try {
            partyExtService.createParty(request);
            //return in the header the location of the new item
            URI uri = ServletUriComponentsBuilder.
                    fromCurrentContextPath().
                    path("/ext/party/list").
                    query("name={name}").
                    buildAndExpand(request.getName()).toUri();
            return ResponseEntity.created(uri).build();
        } catch (PartyExtServiceException e) {
            final String message = ExceptionUtils.getRootCauseMessage(e);
            return ResponseEntity.badRequest().body(message);
        }
    }

    @ApiOperation(value = "Delete a Party",
            notes = "Delete a Party based on party name",
            authorizations = @Authorization(value = "basicAuth"), tags = "party")
    @DeleteMapping
    public ResponseEntity<String> deleteParty(
            @RequestParam(value = "partyName") @Valid @NotNull String partyName) {
        try {
            partyExtService.deleteParty(partyName);
            return ResponseEntity.ok("Party having partyName=[" + partyName + "] has been successfully deleted");
        } catch (PartyExtServiceException e ) {
            final String message = ExceptionUtils.getRootCauseMessage(e);
            return ResponseEntity.badRequest().body(message);
        }
    }

    @ApiOperation(value = "Get Certificate for a Party",
            notes = "Get Certificate for a Party based on party name",
            authorizations = @Authorization(value = "basicAuth"), tags = "party")
    @GetMapping(value = "/{partyName}/certificate")
    public ResponseEntity<Object> getCertificateForParty(@PathVariable(name = "partyName") String partyName) {
        try {
            TrustStoreDTO cert = partyExtService.getPartyCertificateFromTruststore(partyName);
            if (cert == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(cert);
        } catch (PartyExtServiceException e) {
            LOG.error("Failed to get certificate from truststore", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @ApiOperation(value = "List all Processes",
            notes = "List all Processes",
            authorizations = @Authorization(value = "basicAuth"), tags = "party")
    @GetMapping(value = {"/processes"})
    public List<ProcessDTO> listProcesses() {

        return partyExtService.getAllProcesses();
    }


    @PutMapping(value = {"/update"})
    public ResponseEntity updateParties(@RequestBody PartyDTO partyDTO) {
        LOG.debug("Updating party [{}]", partyDTO);
        try {
            partyExtService.updateParty(partyDTO);
            return ResponseEntity.noContent().build();
        } catch (PartyExtServiceException e) {
            final String message = ExceptionUtils.getRootCauseMessage(e);
            return ResponseEntity.badRequest().body(message);
        }
    }

}
