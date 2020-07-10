package eu.domibus.core.party;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.party.Party;
import eu.domibus.api.party.PartyService;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.pki.DomibusCertificateException;
import eu.domibus.api.pmode.PModeArchiveInfo;
import eu.domibus.api.pmode.PModeException;
import eu.domibus.api.pmode.PModeValidationException;
import eu.domibus.api.pmode.ValidationIssue;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.common.model.configuration.*;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.crypto.api.CertificateEntry;
import eu.domibus.core.crypto.api.MultiDomainCryptoService;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.pmode.validation.PModeValidationHelper;
import eu.domibus.ebms3.common.model.MessageExchangePattern;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.XmlProcessingException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.cert.X509Certificate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_FILE_UPLOAD_MAX_SIZE;
import static java.util.stream.Collectors.*;

/**
 * @author Thomas Dussart
 * @author Ion Perpegel
 * @since 4.0
 */
@Service
public class PartyServiceImpl implements PartyService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PartyServiceImpl.class);
    private static final Predicate<Party> DEFAULT_PREDICATE = condition -> true;

    public static final String EXCEPTION_CANNOT_DELETE_PARTY_CURRENT_SYSTEM = "Cannot delete the party describing the current system.";

    @Autowired
    private DomainCoreConverter domainCoreConverter;

    @Autowired
    private PModeProvider pModeProvider;

    @Autowired
    protected MultiDomainCryptoService multiDomainCertificateProvider;

    @Autowired
    protected DomainContextProvider domainProvider;

    @Autowired
    protected CertificateService certificateService;

    @Autowired
    PModeValidationHelper pModeValidationHelper;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Party> getParties(final String name,
                                  final String endPoint,
                                  final String partyId,
                                  final String processName,
                                  final int pageStart,
                                  final int pageSize) {

        final Predicate<Party> searchPredicate = getSearchPredicate(name, endPoint, partyId, processName);
        return linkPartyAndProcesses().
                stream().
                filter(searchPredicate).
                skip(pageStart).
                limit(pageSize).
                collect(Collectors.toList());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> findPartyNamesByServiceAndAction(String service, String action) {
        return pModeProvider.findPartyIdByServiceAndAction(service, action, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> findPushToPartyNamesByServiceAndAction(String service, String action) {
        List<MessageExchangePattern> meps = new ArrayList<>();
        meps.add(MessageExchangePattern.ONE_WAY_PUSH);
        meps.add(MessageExchangePattern.TWO_WAY_PUSH_PUSH);
        meps.add(MessageExchangePattern.TWO_WAY_PUSH_PULL);
        meps.add(MessageExchangePattern.TWO_WAY_PULL_PUSH);
        return pModeProvider.findPartyIdByServiceAndAction(service, action, meps);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getGatewayPartyIdentifier() {
        String result = null;
        eu.domibus.common.model.configuration.Party gatewayParty = pModeProvider.getGatewayParty();
        // return the first identifier
        if (!gatewayParty.getIdentifiers().isEmpty()) {
            result = gatewayParty.getIdentifiers().iterator().next().getPartyId();
        }
        return result;
    }

    /**
     * In the actual configuration the link between parties and processes exists from process to party.
     * We need to reverse this association, we want to have a relation party -&gt; process I am involved in as a responder
     * or initiator.
     *
     * @return a list of party linked with their processes.
     */
    protected List<Party> linkPartyAndProcesses() {

        //Retrieve all party entities.
        List<eu.domibus.common.model.configuration.Party> allParties;
        try {
            allParties = pModeProvider.findAllParties();
        } catch (IllegalStateException e) {
            LOG.trace("findAllParties thrown exception: ", e);
            return new ArrayList<>();
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Linking party and processes for following parties: ");
            allParties.forEach(party -> LOG.debug("      Party [{}]", party));
        }

        //create a new Party to live outside the service per existing party entity in the pmode.
        List<Party> parties = domainCoreConverter.convert(allParties, Party.class);

        //transform parties to map for convenience.
        final Map<String, Party> partyMapByName =
                parties.stream().
                        collect(collectingAndThen(toMap(Party::getName, Function.identity()), ImmutableMap::copyOf));

        //retrieve all existing processes in the pmode.
        final List<Process> allProcesses =
                pModeProvider.findAllProcesses().stream().collect(collectingAndThen(toList(), ImmutableList::copyOf));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Finding all processes in the pMode: ");
            allProcesses.forEach(process -> LOG.debug("[{}]", process));
        }

        linkProcessWithPartyAsInitiator(partyMapByName, allProcesses);

        linkProcessWithPartyAsResponder(partyMapByName, allProcesses);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Finding all parties with initiators and responders: ");
            parties.forEach(party -> printPartyProcesses(party));
        }

        return parties;
    }

    protected void printPartyProcesses(Party party) {
        LOG.debug("Party [{}]", party);
        if (party == null) {
            return;
        }

        if (party.getProcessesWithPartyAsInitiator() != null) {
            LOG.debug("     initiator processes: ");
            party.getProcessesWithPartyAsInitiator().forEach(process -> LOG.debug("[{}]", process));
        }
        if (party.getProcessesWithPartyAsResponder() != null) {
            LOG.debug("     responder processes: ");
            party.getProcessesWithPartyAsResponder().forEach(process -> LOG.debug("[{}]", process));
        }
    }


    protected void linkProcessWithPartyAsInitiator(final Map<String, Party> partyMapByName, final List<Process> allProcesses) {
        allProcesses.forEach(
                processEntity -> {
                    //loop process initiators.
                    processEntity.getInitiatorParties().forEach(partyEntity -> {
                                Party party = partyMapByName.get(partyEntity.getName());
                                eu.domibus.api.process.Process process = domainCoreConverter.convert(
                                        processEntity,
                                        eu.domibus.api.process.Process.class);
                                //add the processes for which this party is initiator.
                                party.addProcessesWithPartyAsInitiator(process);
                            }
                    );
                }
        );
    }

    protected void linkProcessWithPartyAsResponder(final Map<String, Party> partyMapByName, final List<Process> allProcesses) {
        allProcesses.forEach(
                processEntity -> {
                    //loop process responder.
                    processEntity.getResponderParties().forEach(partyEntity -> {
                                Party party = partyMapByName.get(partyEntity.getName());
                                eu.domibus.api.process.Process process = domainCoreConverter.convert(
                                        processEntity,
                                        eu.domibus.api.process.Process.class);
                                //add the processes for which this party is responder.
                                party.addprocessesWithPartyAsResponder(process);
                            }
                    );
                }
        );
    }

    protected Predicate<Party> getSearchPredicate(String name, String endPoint, String partyId, String processName) {
        return namePredicate(name).
                and(endPointPredicate(endPoint)).
                and(partyIdPredicate(partyId)).
                and(processPredicate(processName));
    }

    protected Predicate<Party> namePredicate(final String name) {

        if (StringUtils.isNotEmpty(name)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("create name predicate for [{}]", name);
            }
            return party -> StringUtils.equalsIgnoreCase(party.getName(), name.toUpperCase());
        }
        return DEFAULT_PREDICATE;

    }

    protected Predicate<Party> endPointPredicate(final String endPoint) {
        if (StringUtils.isNotEmpty(endPoint)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("create endPoint predicate for [{}]", endPoint);
            }
            return party ->
                    StringUtils.equalsIgnoreCase(party.getEndpoint(), endPoint.toUpperCase());
        }
        return DEFAULT_PREDICATE;
    }

    protected Predicate<Party> partyIdPredicate(final String partyId) {
        //Search in the list of partyId to find one that match the search criteria.
        if (StringUtils.isNotEmpty(partyId)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("create partyId predicate for [{}]", partyId);
            }
            return
                    party -> {
                        long count = party.getIdentifiers().stream().
                                filter(identifier -> StringUtils.equalsIgnoreCase(identifier.getPartyId(), partyId)).count();
                        return count > 0;
                    };
        }
        return DEFAULT_PREDICATE;
    }

    protected Predicate<Party> processPredicate(final String processName) {
        //Search in the list of process for which this party is initiator and the one for which this party is a responder.
        if (StringUtils.isNotEmpty(processName)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("create process predicate for [{}]", processName);
            }
            return
                    party -> {
                        long count = party.getProcessesWithPartyAsInitiator().stream().
                                filter(process -> StringUtils.equalsIgnoreCase(process.getName(), processName)).count();
                        count += party.getProcessesWithPartyAsResponder().stream().
                                filter(process -> StringUtils.equalsIgnoreCase(process.getName(), processName)).count();
                        return count > 0;
                    };
        }

        return DEFAULT_PREDICATE;
    }

    protected static class ReplacementResult {
        private final List<eu.domibus.common.model.configuration.Party> removedParties = new ArrayList<>();

        private final Configuration updatedConfiguration;

        public ReplacementResult(Configuration updatedConfiguration, List<eu.domibus.common.model.configuration.Party> removedParties) {
            this.updatedConfiguration = updatedConfiguration;
            this.removedParties.addAll(removedParties);
        }

        public ReplacementResult(Configuration updatedConfiguration) {
            this.updatedConfiguration = updatedConfiguration;
        }

        public Configuration getUpdatedConfiguration() {
            return updatedConfiguration;
        }

        public List<eu.domibus.common.model.configuration.Party> getRemovedParties() {
            return Collections.unmodifiableList(removedParties);
        }

        public void addRemovedParty(eu.domibus.common.model.configuration.Party party) {
            this.removedParties.add(party);
        }

        public void addRemovedParties(eu.domibus.common.model.configuration.Party... parties) {
            addRemovedParties(Arrays.asList(parties));
        }

        public void addRemovedParties(List<eu.domibus.common.model.configuration.Party> parties) {
            this.removedParties.addAll(parties);
        }

        public void clearRemovedParties() {
            this.removedParties.clear();
        }
    }

    protected ReplacementResult replaceParties(List<Party> partyList, Configuration configuration) {
        List<eu.domibus.common.model.configuration.Party> newParties = domainCoreConverter.convert(partyList, eu.domibus.common.model.configuration.Party.class);

        List<eu.domibus.common.model.configuration.Party> removedParties = updateConfigurationParties(newParties, configuration);

        updatePartyIdTypes(newParties, configuration);

        updateProcessConfiguration(partyList, configuration);

        return new ReplacementResult(configuration, removedParties);
    }

    private List<eu.domibus.common.model.configuration.Party> updateConfigurationParties(List<eu.domibus.common.model.configuration.Party> newParties, Configuration configuration) {
        BusinessProcesses businessProcesses = configuration.getBusinessProcesses();
        Parties parties = businessProcesses.getPartiesXml();

        List<eu.domibus.common.model.configuration.Party> removedParties = parties.getParty().stream()
                .filter(existingP -> !newParties.stream().anyMatch(newP -> newP.getName().equals(existingP.getName())))
                .collect(toList());
        preventGatewayPartyRemoval(removedParties);

        parties.getParty().clear();
        parties.getParty().addAll(newParties);
        return removedParties;
    }

    private void preventGatewayPartyRemoval(List<eu.domibus.common.model.configuration.Party> removedParties) {
        eu.domibus.common.model.configuration.Party partyMe = pModeProvider.getGatewayParty();
        if (partyMe == null) {
            throw new PModeException(DomibusCoreErrorCode.DOM_003, "Cannot find the party describing the current system. ");
        }
        if (removedParties.stream().anyMatch(party -> party.getName().equals(partyMe.getName()))) {
            throw new PModeException(DomibusCoreErrorCode.DOM_003, EXCEPTION_CANNOT_DELETE_PARTY_CURRENT_SYSTEM);
        }
    }

    protected void updatePartyIdTypes(List<eu.domibus.common.model.configuration.Party> newParties, Configuration configuration) {
        BusinessProcesses businessProcesses = configuration.getBusinessProcesses();
        Parties parties = businessProcesses.getPartiesXml();
        PartyIdTypes partyIdTypes = parties.getPartyIdTypes();
        List<PartyIdType> partyIdType = partyIdTypes.getPartyIdType();

        newParties.forEach(party -> {
            party.getIdentifiers().forEach(identifier -> {
                if (!partyIdType.contains(identifier.getPartyIdType())) {
                    partyIdType.add(identifier.getPartyIdType());
                }
            });
        });
    }

    private void updateProcessConfiguration(List<Party> partyList, Configuration configuration) {
        BusinessProcesses businessProcesses = configuration.getBusinessProcesses();
        List<Process> processes = businessProcesses.getProcesses();

        processes.forEach(process -> {
            updateProcessConfigurationInitiatorParties(partyList, process);
            updateProcessConfigurationResponderParties(partyList, process);
        });
    }

    private void updateProcessConfigurationResponderParties(List<Party> partyList, Process process) {
        Set<String> rParties = partyList.stream()
                .filter(p -> p.getProcessesWithPartyAsResponder().stream()
                        .anyMatch(pp -> process.getName().equalsIgnoreCase(pp.getName())))
                .map(p -> p.getName())
                .collect(Collectors.toSet());

        if (process.getResponderPartiesXml() == null) {
            process.setResponderPartiesXml(new ResponderParties());
        }
        List<ResponderParty> rp = process.getResponderPartiesXml().getResponderParty();
        rp.removeIf(x -> !rParties.contains(x.getName()));
        rp.addAll(rParties.stream().filter(name -> rp.stream().noneMatch(x -> name.equalsIgnoreCase(x.getName())))
                .map(name -> {
                    ResponderParty y = new ResponderParty();
                    y.setName(name);
                    return y;
                }).collect(Collectors.toSet()));
        if (rp.isEmpty()) {
            process.setResponderPartiesXml(null);
        }
    }

    private void updateProcessConfigurationInitiatorParties(List<Party> partyList, Process process) {
        Set<String> iParties = partyList.stream()
                .filter(p -> p.getProcessesWithPartyAsInitiator().stream()
                        .anyMatch(pp -> process.getName().equalsIgnoreCase(pp.getName())))
                .map(p -> p.getName())
                .collect(Collectors.toSet());

        if (process.getInitiatorPartiesXml() == null) {
            process.setInitiatorPartiesXml(new InitiatorParties());
        }
        List<InitiatorParty> ip = process.getInitiatorPartiesXml().getInitiatorParty();
        ip.removeIf(x -> !iParties.contains(x.getName()));
        ip.addAll(iParties.stream().filter(name -> ip.stream().noneMatch(x -> name != null && name.equalsIgnoreCase(x.getName())))
                .map(name -> {
                    InitiatorParty y = new InitiatorParty();
                    y.setName(name);
                    return y;
                }).collect(Collectors.toSet()));
        if (ip.isEmpty()) {
            process.setInitiatorPartiesXml(null);
        }
    }

    @Override
    public List<ValidationIssue> updateParties(List<Party> partyList, Map<String, String> partyToCertificateMap) throws PModeValidationException {

        validatePartyCertificates(partyToCertificateMap);

        final PModeArchiveInfo currentPmode = pModeProvider.getCurrentPmode();
        if (currentPmode == null) {
            throw new PModeException(DomibusCoreErrorCode.DOM_001, "Could not update PMode parties: PMode not found!");
        }

        ConfigurationRaw rawConfiguration = pModeProvider.getRawConfiguration(currentPmode.getId());

        Configuration configuration;
        try {
            configuration = pModeProvider.getPModeConfiguration(rawConfiguration.getXml());
        } catch (XmlProcessingException e) {
            LOG.error("Error reading current PMode", e);
            throw pModeValidationHelper.getPModeValidationException(e, "Error parsing PMode due to: ");
        }

        ReplacementResult replacementResult = replaceParties(partyList, configuration);

        List<ValidationIssue> result = updateConfiguration(rawConfiguration.getConfigurationDate(), replacementResult.getUpdatedConfiguration());

        updatePartyCertificate(partyToCertificateMap, replacementResult);

        return result;
    }


    protected void validatePartyCertificates(Map<String, String> partyToCertificateMap) {
        int maxSize = domibusPropertyProvider.getIntegerProperty(DOMIBUS_FILE_UPLOAD_MAX_SIZE);

        List<Map.Entry<String, String>> wrongCertificates = partyToCertificateMap.entrySet().stream()
                .filter(entry -> entry.getValue().length() > maxSize)
                .collect(toList());
        if (wrongCertificates.size() == 0) {
            LOG.trace("All certificates pass max length validation");
            return;
        }

        List<ValidationIssue> errors = wrongCertificates.stream()
                .map(entry -> new ValidationIssue(
                                entry.getKey() + " party certificate: " + "The size " + entry.getValue().length() + " exceeds the maximum size limit of " + maxSize,
                                ValidationIssue.Level.ERROR
                        )
                )
                .collect(Collectors.toList());
        throw new PModeValidationException("Error validating party certificates.", errors);
    }

    protected List<ValidationIssue> updateConfiguration(Date configurationDate, Configuration updatedConfiguration) throws PModeValidationException {
        ZonedDateTime confDate = ZonedDateTime.ofInstant(configurationDate.toInstant(), ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ssO");
        String updatedDescription = "Updated parties to version of " + confDate.format(formatter);

        try {
            byte[] updatedPMode = pModeProvider.serializePModeConfiguration(updatedConfiguration);
            List<ValidationIssue> result = pModeProvider.updatePModes(updatedPMode, updatedDescription);
            return result;
        } catch (XmlProcessingException e) {
            LOG.error("Error writing current PMode", e);
            throw pModeValidationHelper.getPModeValidationException(e, "Error writing current PMode due to: ");
        }
    }

    /**
     * Updates certificates for parties
     *
     * @param partyToCertificateMap
     * @param replacementResult
     */
    protected void updatePartyCertificate(Map<String, String> partyToCertificateMap, ReplacementResult replacementResult) {

        List<String> aliases = getRemovedParties(replacementResult);
        removePartyCertificate(aliases);

        addPartyCertificate(partyToCertificateMap);
    }

    /**
     * Add new certificates to parties
     *
     * @param partyToCertificateMap pair of partyName and certificate content
     */
    protected void addPartyCertificate(Map<String, String> partyToCertificateMap) {
        Domain currentDomain = domainProvider.getCurrentDomain();

        List<CertificateEntry> certificates = new ArrayList<>();
        for (Map.Entry<String, String> pair : partyToCertificateMap.entrySet()) {
            if (pair.getValue() == null) {
                continue;
            }

            String partyName = pair.getKey();
            String certificateContent = pair.getValue();
            try {
                X509Certificate cert = certificateService.loadCertificateFromString(certificateContent);
                certificates.add(new CertificateEntry(partyName, cert));
            } catch (DomibusCertificateException e) {
                LOG.error("Error deserializing certificate", e);
                throw new IllegalStateException(e);
            }
        }
        if (CollectionUtils.isNotEmpty(certificates)) {
            multiDomainCertificateProvider.addCertificate(currentDomain, certificates, true);
        }
    }

    protected void removePartyCertificate(final List<String> aliases) {
        if (CollectionUtils.isNotEmpty(aliases)) {
            Domain currentDomain = domainProvider.getCurrentDomain();
            multiDomainCertificateProvider.removeCertificate(currentDomain, aliases);
        }
    }

    protected List<String> getRemovedParties(ReplacementResult replacementResult) {
        return replacementResult.getRemovedParties().stream().map(party -> party.getName()).collect(toList());
    }

    @Override
    public List<eu.domibus.api.process.Process> getAllProcesses() {
        //Retrieve all processes, needed in UI console to be able to check
        List<eu.domibus.common.model.configuration.Process> allProcesses;
        try {
            allProcesses = pModeProvider.findAllProcesses();
        } catch (IllegalStateException e) {
            return new ArrayList<>();
        }
        List<eu.domibus.api.process.Process> processes = domainCoreConverter.convert(allProcesses, eu.domibus.api.process.Process.class);
        return processes;
    }

    /**
     * {@inheritDoc}
     *
     * @param party              Party object
     * @param certificateContent certificate content in base64
     */
    @Override
    public void createParty(final Party party, final String certificateContent) throws PModeException {

        //read PMode configuration
        Configuration configuration = getConfiguration();

        //check if party exists
        final String partyName = party.getName();
        eu.domibus.common.model.configuration.Party partyFound =
                configuration.getBusinessProcesses().getPartiesXml().getParty().stream().filter(p -> p.getName().equalsIgnoreCase(partyName)).findFirst().orElse(null);
        if (partyFound != null) {
            throw new PModeException(DomibusCoreErrorCode.DOM_003, "Party with partyName=[" + partyName + "] already exists!");
        }

        eu.domibus.common.model.configuration.Party configParty = domainCoreConverter.convert(party, eu.domibus.common.model.configuration.Party.class);
        List<eu.domibus.common.model.configuration.Party> configParties = Collections.singletonList(configParty);

        //add new party to configuration
        addPartyToConfiguration(configParty, configuration);

        //update party id types
        updatePartyIdTypes(configParties, configuration);

        //update processes
        addProcessConfiguration(party, configuration);

        //update the PMode configuration
        updateConfiguration(new Date(System.currentTimeMillis()), configuration);

        //add certificate as well
        if (StringUtils.isNotBlank(certificateContent)) {
            Map<String, String> map = new HashMap<>();
            map.put(party.getName(), certificateContent);
            addPartyCertificate(map);
        }
    }

    /**
     * Add a {@code Party} to configuration
     *
     * @param configParty
     * @param configuration
     */
    protected void addPartyToConfiguration(eu.domibus.common.model.configuration.Party configParty, Configuration configuration) {
        BusinessProcesses businessProcesses = configuration.getBusinessProcesses();
        Parties parties = businessProcesses.getPartiesXml();

        parties.getParty().add(configParty);
    }


    /**
     * Make the links between current Party and Processes (Initator and Responder)
     *
     * @param party
     * @param configuration
     */
    protected void addProcessConfiguration(Party party, Configuration configuration) {
        if (party.getProcessesWithPartyAsInitiator() != null) {
            party.getProcessesWithPartyAsInitiator().forEach(process -> addProcessConfigurationInitiatorParties(party, process, configuration));
        }
        if (party.getProcessesWithPartyAsResponder() != null) {
            party.getProcessesWithPartyAsResponder().forEach(process -> addProcessConfigurationResponderParties(party, process, configuration));
        }
    }

    /**
     * @param party
     * @param process
     * @param configuration
     */
    protected void addProcessConfigurationInitiatorParties(Party party, eu.domibus.api.process.Process process, Configuration configuration) {
        Process configProcess = getProcess(process.getName(), configuration);
        if (configProcess != null) {
            if (configProcess.getInitiatorPartiesXml() == null) {
                configProcess.setInitiatorPartiesXml(new InitiatorParties());
            }
            List<InitiatorParty> initiatorPartyList = configProcess.getInitiatorPartiesXml().getInitiatorParty();
            InitiatorParty initiatorParty = new InitiatorParty();
            initiatorParty.setName(party.getName());
            initiatorPartyList.add(initiatorParty);
        }
    }

    /**
     * @param party
     * @param process
     * @param configuration
     */
    protected void addProcessConfigurationResponderParties(Party party, eu.domibus.api.process.Process process, Configuration configuration) {
        Process configProcess = getProcess(process.getName(), configuration);
        if (configProcess != null) {
            if (configProcess.getResponderPartiesXml() == null) {
                configProcess.setResponderPartiesXml(new ResponderParties());
            }
            List<ResponderParty> responderPartyList = configProcess.getResponderPartiesXml().getResponderParty();
            ResponderParty responderParty = new ResponderParty();
            responderParty.setName(party.getName());
            responderPartyList.add(responderParty);
        }
    }

    /**
     * Returns the process identified by processName
     * If not found throws an Exception
     *
     * @param processName
     * @param configuration
     * @return
     */
    protected Process getProcess(final String processName, Configuration configuration) {
        BusinessProcesses businessProcesses = configuration.getBusinessProcesses();
        Process configProcess = businessProcesses.getProcesses().stream().filter(p -> p.getName().equalsIgnoreCase(processName)).findFirst().orElse(null);
        if (configProcess == null) {
            throw new PModeException(DomibusCoreErrorCode.DOM_003, "Process name [" + processName + "] not found in PModeConfiguration");
        }
        return configProcess;
    }

    /**
     * {@inheritDoc}
     *
     * @param partyName
     */
    @Override
    public void deleteParty(String partyName) throws PModeException {

        //check if party is not in use
        checkPartyInUse(partyName);

        //read actual PMode configuration
        Configuration configuration = getConfiguration();
        initConfigurationParties(configuration);

        //get all parties
        List<eu.domibus.common.model.configuration.Party> allParties = configuration.getBusinessProcesses().getPartiesXml().getParty();

        //find the party to be deleted
        eu.domibus.common.model.configuration.Party partyToBeDeleted = getParty(partyName, allParties);

        //remove party from configuration
        removePartyFromConfiguration(partyToBeDeleted, configuration);

        //remove party id types
        removePartyIdTypes(partyToBeDeleted, configuration);

        //update processes
        removeProcessConfiguration(partyToBeDeleted, configuration);

        //update PMode configuration
        updateConfiguration(new Date(System.currentTimeMillis()), configuration);

        //remove certificate
        removePartyCertificate(Collections.singletonList(partyToBeDeleted.getName()));
    }

    /**
     * Search for a Party among the list
     *
     * @param partyName
     * @param allParties
     * @return
     */
    protected eu.domibus.common.model.configuration.Party getParty(String partyName, List<eu.domibus.common.model.configuration.Party> allParties) {
        eu.domibus.common.model.configuration.Party partyToSearch =
                allParties.stream().filter(party -> party.getName().equalsIgnoreCase(partyName)).findFirst().orElse(null);
        if (partyToSearch == null) {
            throw new PModeException(DomibusCoreErrorCode.DOM_003, "Party with partyName=[" + partyName + "] not found!");
        }
        return partyToSearch;
    }

    protected void removePartyFromConfiguration(eu.domibus.common.model.configuration.Party partyToBeDeleted, Configuration configuration) throws PModeException {
        BusinessProcesses businessProcesses = configuration.getBusinessProcesses();
        Parties parties = businessProcesses.getPartiesXml();
        if (!parties.getParty().remove(partyToBeDeleted)) {
            throw new PModeException(DomibusCoreErrorCode.DOM_003, "Party having name [" + partyToBeDeleted.getName() + "] not deleted from PMode");
        }
    }

    protected void removePartyIdTypes(eu.domibus.common.model.configuration.Party partyToBeDeleted,
                                      Configuration configuration) {
        BusinessProcesses businessProcesses = configuration.getBusinessProcesses();
        List<PartyIdType> partyIdTypeList = businessProcesses.getPartiesXml().getPartyIdTypes().getPartyIdType();

        Set<PartyIdType> toBeDeleted = partyToBeDeleted.getIdentifiers().stream().map(Identifier::getPartyIdType).collect(Collectors.toSet());
        Set<PartyIdType> existing = businessProcesses.getPartiesXml().getParty().stream().
                flatMap(party -> party.getIdentifiers().stream().map(Identifier::getPartyIdType)).collect(Collectors.toSet());

        Map<Boolean, List<PartyIdType>> filtered = toBeDeleted.stream()
                .collect(partitioningBy(existing::contains));

        List<PartyIdType> difference = filtered.get(false);

        //remove those partyIdType not used anymore
        if (!difference.isEmpty()) {
            partyIdTypeList.removeAll(difference);
        }
    }

    protected void removeProcessConfiguration(eu.domibus.common.model.configuration.Party configParty, Configuration configuration) {
        BusinessProcesses businessProcesses = configuration.getBusinessProcesses();
        List<Process> processes = businessProcesses.getProcesses();
        Party party = domainCoreConverter.convert(configParty, Party.class);

        processes.forEach(process -> removeProcessConfigurationInitiatorResponderParties(party, process));
    }

    protected void removeProcessConfigurationInitiatorResponderParties(Party party, Process process) {

        if (process.getInitiatorPartiesXml() == null) {
            process.setInitiatorPartiesXml(new InitiatorParties());
        }
        List<InitiatorParty> initiatorPartyList = process.getInitiatorPartiesXml().getInitiatorParty();
        initiatorPartyList.removeIf(x -> x.getName().equalsIgnoreCase(party.getName()));

        if (process.getResponderPartiesXml() == null) {
            process.setResponderPartiesXml(new ResponderParties());
        }
        List<ResponderParty> responderPartyList = process.getResponderPartiesXml().getResponderParty();
        responderPartyList.removeIf(x -> x.getName().equalsIgnoreCase(party.getName()));
    }


    @Override
    public void updateParty(Party party, String certificateContent) throws PModeException {

        final String partyName = party.getName();
        checkPartyInUse(partyName);

        //read actual PMode configuration
        Configuration configuration = getConfiguration();
        initConfigurationParties(configuration);

        //get all parties
        List<eu.domibus.common.model.configuration.Party> allParties = configuration.getBusinessProcesses().getPartiesXml().getParty();

        //find the party to be deleted
        eu.domibus.common.model.configuration.Party configParty = getParty(partyName, allParties);

        //remove party from configuration
        removePartyFromConfiguration(configParty, configuration);

        //remove party id types
        removePartyIdTypes(configParty, configuration);

        //remove processes
        removeProcessConfiguration(configParty, configuration);

        //remove certificate
        removePartyCertificate(Collections.singletonList(configParty.getName()));

        //add new party to configuration
        addPartyToConfiguration(configParty, configuration);

        //update party id types
        updatePartyIdTypes(allParties, configuration);

        //update processes
        addProcessConfiguration(party, configuration);

        //update the PMode configuration
        updateConfiguration(new Date(System.currentTimeMillis()), configuration);

        //add certificate as well
        if (StringUtils.isNotBlank(certificateContent)) {
            Map<String, String> map = new HashMap<>();
            map.put(party.getName(), certificateContent);
            addPartyCertificate(map);
        }

    }

    /**
     * Checks if a Party is in use
     *
     * @param partyName
     */
    protected void checkPartyInUse(String partyName) throws PModeException {
        //check if party is not in use
        String partyMe = pModeProvider.getGatewayParty().getName();
        if (partyMe.equalsIgnoreCase(partyName)) {
            throw new PModeException(DomibusCoreErrorCode.DOM_003, EXCEPTION_CANNOT_DELETE_PARTY_CURRENT_SYSTEM);
        }
    }

    /**
     * Read latest configuration from PMode
     *
     * @return Configuration object
     */
    protected Configuration getConfiguration() throws PModeException {
        //read current configuration
        final PModeArchiveInfo pModeArchiveInfo = pModeProvider.getCurrentPmode();
        if (pModeArchiveInfo == null) {
            throw new IllegalStateException("Could not create a Party: PMode not found!");
        }
        ConfigurationRaw rawConfiguration = pModeProvider.getRawConfiguration(pModeArchiveInfo.getId());
        Configuration configuration;
        try {
            configuration = pModeProvider.getPModeConfiguration(rawConfiguration.getXml());
        } catch (XmlProcessingException e) {
            String errorMsg = "Error reading current PMode";
            LOG.error(errorMsg, e);
            throw new PModeException(DomibusCoreErrorCode.DOM_003, errorMsg);
        }

        return configuration;
    }

    /**
     * It will populate PartyIdTypes from {@code Configuration} and call init for each {@code Party}
     *
     * @param configuration Configuration object
     */
    protected void initConfigurationParties(Configuration configuration) {
        configuration.getBusinessProcesses().setPartyIdTypes(new HashSet<>(configuration.getBusinessProcesses().getPartiesXml().getPartyIdTypes().getPartyIdType()));
        for (final eu.domibus.common.model.configuration.Party party : configuration.getBusinessProcesses().getPartiesXml().getParty()) {
            party.init(configuration);
        }
    }

}
