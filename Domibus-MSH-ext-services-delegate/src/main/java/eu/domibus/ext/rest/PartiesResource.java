package eu.domibus.ext.rest;

import eu.domibus.ext.domain.PartyDTO;
import eu.domibus.ext.domain.PartyRequestDTO;
import eu.domibus.ext.services.PartyExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * @author Catalin Enache
 * @since 4.2
 */
@RestController
@RequestMapping(value = "/ext/parties")
public class PartiesResource {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PartiesResource.class);

    @Autowired
    protected PartyExtService partyExtService;

    @ApiOperation(value = "Get Parties", notes = "Get Parties using certain criteria like name, endpoint, partyId, process name. " +
            "Use pageStart and pageSize for pagination purposes",
            authorizations = @Authorization(value = "basicAuth"), tags = "parties")
    @GetMapping(value = {"/list"})
    public List<PartyDTO> listParties(@Valid PartyRequestDTO request){
        if (request.getPageStart() <= 0) {
            request.setPageStart(0);
        }
        if (request.getPageSize() <= 0) {
            request.setPageSize(Integer.MAX_VALUE);
        }
        LOG.debug("Searching parties with parameters:" +
                        " name [{}], endPoint [{}], partyId [{}], processName [{}], pageStart [{}], pageSize [{}]",
                request.getName(), request.getEndPoint(), request.getPartyId(), request.getProcess(), request.getPageStart(), request.getPageSize());

        return partyExtService.getParties(request.getName(),
                request.getEndPoint(), request.getPartyId(),
                request.getProcess(), request.getPageStart(), request.getPageSize());
    }

//    @PutMapping(value = {"/update"})
//    public ResponseEntity updateParties(@RequestBody List<PartyInfoDTO> partyInfoDTOs) {
//        LOG.debug("Updating parties [{}]", Arrays.toString(partyInfoDTOs.toArray()));
//
//        List<Party> partyList = domainConverter.convert(partiesRo, Party.class);
//        LOG.debug("Updating partyList [{}]", partyList.toArray());
//
//        Map<String, String> certificates = partiesRo.stream()
//                .filter(party -> party.getCertificateContent() != null)
//                .collect(Collectors.toMap(PartyResponseRo::getName, PartyResponseRo::getCertificateContent));
//
//        try {
//            partyService.updateParties(partyList, certificates);
//            return ResponseEntity.noContent().build();
//        } catch (IllegalStateException e) {
//            StringBuilder errorMessageB = new StringBuilder();
//            for (Throwable err = e; err != null; err = err.getCause()) {
//                errorMessageB.append("\n").append(err.getMessage());
//            }
//            return ResponseEntity.badRequest().body(errorMessageB.toString());
//        }
//    }
//



//    @RequestMapping(path = "/{id}", method = RequestMethod.GET)
//    public ResponseEntity<PartyDTO> getParty(@PathVariable(value = "id") int id) {
//        LOG.debug("getParty for id=[{}] -> start", id);
//
//        HttpStatus status = HttpStatus.OK;
//        if (resource.getByteArray().length == 0) {
//            status = HttpStatus.NO_CONTENT;
//        }
//        return ResponseEntity.status(status)
//                .contentType(MediaType.parseMediaType(MediaType.APPLICATION_XML_VALUE))
//                .header("content-disposition", "attachment; filename=Pmodes.xml")
//                .body(resource);
//    }


}
