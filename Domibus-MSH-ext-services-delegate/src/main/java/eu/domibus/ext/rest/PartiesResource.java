package eu.domibus.ext.rest;

import eu.domibus.ext.domain.PartyDTO;
import eu.domibus.ext.domain.PartyRequestDTO;
import eu.domibus.ext.domain.ProcessDTO;
import eu.domibus.ext.domain.TrustStoreDTO;
import eu.domibus.ext.services.PartyExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.security.KeyStoreException;
import java.util.List;

/**
 * @author Catalin Enache
 * @since 4.2
 */
@RestController
@RequestMapping(value = "/ext/party")
public class PartiesResource {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PartiesResource.class);

    @Autowired
    PartyExtService partyExtService;

    @ApiOperation(value = "Get Parties", notes = "Get Parties using certain criteria like name, endpoint, partyId, process name. " +
            "Use pageStart and pageSize for pagination purposes",
            authorizations = @Authorization(value = "basicAuth"), tags = "party")
    @GetMapping(value = {"/list"})
    public List<PartyDTO> listParties(PartyRequestDTO request) {
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

    @PostMapping(value = "/save")
    public ResponseEntity<Object> createParty(@RequestBody PartyDTO request) {

        partyExtService.createParty(request);

        final String partyId = "";
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/list?partyId={partyId}").buildAndExpand(partyId).toUri();
        return ResponseEntity.created(uri).build();
    }

    @ApiOperation(value = "Get Certificate for a Party",
            notes = "Get Certificate for a Party based on party name",
            authorizations = @Authorization(value = "basicAuth"), tags = "party")
    @GetMapping(value = "/{partyName}/certificate")
    public ResponseEntity<TrustStoreDTO> getCertificateForParty(@PathVariable(name = "partyName") String partyName) {
        try {
            TrustStoreDTO cert = partyExtService.getPartyCertificateFromTruststore(partyName);
            if (cert == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(cert);
        } catch (KeyStoreException e) {
            LOG.error("Failed to get certificate from truststore", e);
            return ResponseEntity.badRequest().build();
        }
    }

//    @PutMapping(value = "/{partyName}/certificate")
//    public ResponseEntity convertCertificateContent(@PathVariable(name = "partyName") String partyName,
//                                                  @RequestBody CertificateDTO certificate) {
//        if (certificate == null && certificate.getContent() == null) {
//            throw new IllegalArgumentException("Certificate parameter must be provided");
//        }
//
//        String content = certificate.getContent();
//        LOG.debug("certificate base 64 received [{}] ", content);
//
//        TrustStoreEntry cert = null;
//        try {
//            cert = certificateService.convertCertificateContent(content);
//        } catch (DomibusCertificateException e) {
//            throw new IllegalArgumentException("Certificate could not be parsed", e);
//        }
//        if (cert == null) {
//            throw new IllegalArgumentException("Certificate could not be parsed");
//        }
//
//        return domainConverter.convert(cert, TrustStoreRO.class);
//    }

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


    @GetMapping(value = {"/processes"})
    public List<ProcessDTO> listProcesses() {
        return partyExtService.getAllProcesses();
    }


}
