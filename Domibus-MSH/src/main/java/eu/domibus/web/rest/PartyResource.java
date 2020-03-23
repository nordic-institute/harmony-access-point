package eu.domibus.web.rest;

import com.google.common.collect.Lists;
import eu.domibus.api.party.Party;
import eu.domibus.api.party.PartyService;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.pki.DomibusCertificateException;
import eu.domibus.api.pmode.ValidationIssue;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.party.*;
import eu.domibus.core.pmode.validation.PModeValidationHelper;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.ro.PartyFilterRequestRO;
import eu.domibus.web.rest.ro.TrustStoreRO;
import eu.domibus.web.rest.ro.ValidationResponseRO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.KeyStoreException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RestController
@RequestMapping(value = "/rest/party")
@Validated
public class PartyResource extends BaseResource {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PartyResource.class);
    private static final String DELIMITER = ", ";

    @Autowired
    private DomainCoreConverter domainConverter;

    @Autowired
    private PartyService partyService;

    @Autowired
    private CertificateService certificateService;

    @Autowired
    PModeValidationHelper pModeValidationHelper;

    @GetMapping(value = {"/list"})
    public List<PartyResponseRo> listParties(@Valid PartyFilterRequestRO request) {
        // basic user input sanitizing; pageSize = 0 means no pagination.
        if (request.getPageStart() <= 0) {
            request.setPageStart(0);
        }
        if (request.getPageSize() <= 0) {
            request.setPageSize(Integer.MAX_VALUE);
        }
        LOG.debug("Searching party with parameters name [{}], endPoint [{}], partyId [{}], processName [{}], pageStart [{}], pageSize [{}]",
                request.getName(), request.getEndPoint(), request.getPartyId(), request.getProcess(), request.getPageStart(), request.getPageSize());

        List<PartyResponseRo> partyResponseRos = domainConverter.convert(
                partyService.getParties(request.getName(), request.getEndPoint(), request.getPartyId(), request.getProcess(), request.getPageStart(), request.getPageSize()),
                PartyResponseRo.class);

        flattenIdentifiers(partyResponseRos);

        flattenProcesses(partyResponseRos);

        partyResponseRos.forEach(partyResponseRo -> {
            final List<ProcessRo> processesWithPartyAsInitiator = partyResponseRo.getProcessesWithPartyAsInitiator();
            final List<ProcessRo> processesWithPartyAsResponder = partyResponseRo.getProcessesWithPartyAsResponder();

            final Set<ProcessRo> processRos = new HashSet<>(processesWithPartyAsInitiator);
            processRos.addAll(processesWithPartyAsResponder);

            processRos
                    .stream()
                    .map(item -> new PartyProcessLinkRo(item.getName(), processesWithPartyAsInitiator.contains(item), processesWithPartyAsResponder.contains(item)))
                    .collect(Collectors.toSet());
        });

        return partyResponseRos;
    }

    /**
     * This method returns a CSV file with the contents of Party table
     *
     * @return CSV file with the contents of Party table
     */
    @GetMapping(path = "/csv")
    public ResponseEntity<String> getCsv(@Valid PartyFilterRequestRO request) {
        request.setPageStart(0);
        request.setPageSize(getMaxNumberRowsToExport());
        final List<PartyResponseRo> partyResponseRoList = listParties(request);

        return exportToCSV(partyResponseRoList,
                PartyResponseRo.class,
                new HashMap<String, String>() {{
                    put("Name".toUpperCase(), "Party name");
                    put("EndPoint".toUpperCase(), "End point");
                    put("JoinedIdentifiers".toUpperCase(), "Party id");
                    put("JoinedProcesses".toUpperCase(), "Process");
                }},
                Arrays.asList("entityId", "identifiers", "userName", "processesWithPartyAsInitiator", "processesWithPartyAsResponder", "certificateContent"),
                "pmodeparties");
    }

    @PutMapping(value = {"/update"})
    public ValidationResponseRO updateParties(@RequestBody List<PartyResponseRo> partiesRo) {
        LOG.debug("Updating parties [{}]", Arrays.toString(partiesRo.toArray()));

        List<Party> partyList = domainConverter.convert(partiesRo, Party.class);
        LOG.debug("Updating partyList [{}]", partyList.toArray());

        Map<String, String> certificates = partiesRo.stream()
                .filter(party -> party.getCertificateContent() != null)
                .collect(Collectors.toMap(PartyResponseRo::getName, PartyResponseRo::getCertificateContent));

        List<ValidationIssue> pModeUpdateIssues = partyService.updateParties(partyList, certificates);

        return pModeValidationHelper.getValidationResponse(pModeUpdateIssues, "PMode parties have been successfully updated.");
    }

    /**
     * Flatten the list of identifiers of each party into a comma separated list
     * for displaying in the console.
     *
     * @param partyResponseRos the list of party to be adapted.
     */
    protected void flattenIdentifiers(List<PartyResponseRo> partyResponseRos) {
        partyResponseRos.forEach(
                partyResponseRo -> {
                    String joinedIdentifiers = partyResponseRo.getIdentifiers().
                            stream().
                            map(IdentifierRo::getPartyId).
                            sorted().
                            collect(Collectors.joining(DELIMITER));
                    partyResponseRo.setJoinedIdentifiers(joinedIdentifiers);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Flatten identifiers for [{}]=[{}]", partyResponseRo.getName(), partyResponseRo.getJoinedIdentifiers());
                    }
                });
    }

    /**
     * Flatten the list of processes of each party into a comma separated list
     * for displaying in the console.
     *
     * @param partyResponseRos the list of party to be adapted.
     */
    protected void flattenProcesses(List<PartyResponseRo> partyResponseRos) {
        partyResponseRos.forEach(
                partyResponseRo -> {

                    List<ProcessRo> processesWithPartyAsInitiator = partyResponseRo.getProcessesWithPartyAsInitiator();
                    List<ProcessRo> processesWithPartyAsResponder = partyResponseRo.getProcessesWithPartyAsResponder();

                    List<ProcessRo> processesWithPartyAsInitiatorAndResponder
                            = processesWithPartyAsInitiator.
                            stream().
                            filter(processesWithPartyAsResponder::contains).
                            collect(Collectors.toList());

                    List<ProcessRo> processWithPartyAsInitiatorOnly = processesWithPartyAsInitiator
                            .stream()
                            .filter(processRo -> !processesWithPartyAsInitiatorAndResponder.contains(processRo))
                            .collect(Collectors.toList());

                    List<ProcessRo> processWithPartyAsResponderOnly = processesWithPartyAsResponder
                            .stream()
                            .filter(processRo -> !processesWithPartyAsInitiatorAndResponder.contains(processRo))
                            .collect(Collectors.toList());

                    String joinedProcessesWithMeAsInitiatorOnly = processWithPartyAsInitiatorOnly.
                            stream().
                            map(ProcessRo::getName).
                            map(name -> name.concat("(I)")).
                            collect(Collectors.joining(DELIMITER));

                    String joinedProcessesWithMeAsResponderOnly = processWithPartyAsResponderOnly.
                            stream().
                            map(ProcessRo::getName).
                            map(name -> name.concat("(R)")).
                            collect(Collectors.joining(DELIMITER));

                    String joinedProcessesWithMeAsInitiatorAndResponder = processesWithPartyAsInitiatorAndResponder.
                            stream().
                            map(ProcessRo::getName).
                            map(name -> name.concat("(IR)")).
                            collect(Collectors.joining(DELIMITER));

                    List<String> joinedProcess = Lists.newArrayList();

                    if (StringUtils.isNotEmpty(joinedProcessesWithMeAsInitiatorOnly)) {
                        joinedProcess.add(joinedProcessesWithMeAsInitiatorOnly);
                    }

                    if (StringUtils.isNotEmpty(joinedProcessesWithMeAsResponderOnly)) {
                        joinedProcess.add(joinedProcessesWithMeAsResponderOnly);
                    }

                    if (StringUtils.isNotEmpty(joinedProcessesWithMeAsInitiatorAndResponder)) {
                        joinedProcess.add(joinedProcessesWithMeAsInitiatorAndResponder);
                    }

                    partyResponseRo.setJoinedProcesses(
                            StringUtils.join(joinedProcess, DELIMITER));
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Flatten processes for [{}]=[{}]", partyResponseRo.getName(), partyResponseRo.getJoinedProcesses());
                    }
                });
    }

    @GetMapping(value = {"/processes"})
    public List<ProcessRo> listProcesses() {
        return domainConverter.convert(partyService.getAllProcesses(), ProcessRo.class);
    }

    @GetMapping(value = "/{partyName}/certificate")
    public ResponseEntity<TrustStoreRO> getCertificateForParty(@PathVariable(name = "partyName") String partyName) {
        try {
            TrustStoreEntry cert = certificateService.getPartyCertificateFromTruststore(partyName);
            if (cert == null) {
                return ResponseEntity.notFound().build();
            }
            TrustStoreRO res = domainConverter.convert(cert, TrustStoreRO.class);
            return ResponseEntity.ok(res);
        } catch (KeyStoreException e) {
            LOG.error("Failed to get certificate from truststore", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping(value = "/{partyName}/certificate")
    public TrustStoreRO convertCertificateContent(@PathVariable(name = "partyName") String partyName,
                                                  @RequestBody CertificateContentRo certificate) {
        if (certificate == null) {
            throw new IllegalArgumentException("Certificate parameter must be provided");
        }

        String content = certificate.getContent();
        LOG.debug("certificate base 64 received [{}] ", content);

        TrustStoreEntry cert = null;
        try {
            cert = certificateService.convertCertificateContent(content);
        } catch (DomibusCertificateException e) {
            throw new IllegalArgumentException("Certificate could not be parsed", e);
        }
        if (cert == null) {
            throw new IllegalArgumentException("Certificate could not be parsed");
        }

        return domainConverter.convert(cert, TrustStoreRO.class);
    }

}
