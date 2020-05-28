
package eu.domibus.core.pmode.provider;

import com.google.common.collect.Lists;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.pmode.PModeValidationException;
import eu.domibus.api.pmode.ValidationIssue;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.common.model.configuration.*;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.core.message.MessageExchangeConfiguration;
import eu.domibus.core.message.pull.PullMessageService;
import eu.domibus.core.pmode.ProcessPartyExtractorProvider;
import eu.domibus.core.pmode.ProcessTypePartyExtractor;
import eu.domibus.ebms3.common.model.AgreementRef;
import eu.domibus.ebms3.common.model.MessageExchangePattern;
import eu.domibus.ebms3.common.model.PartyId;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.plugin.BackendConnector;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author Cosmin Baciu, Thomas Dussart, Ioana Dragusanu
 */
public class CachingPModeProvider extends PModeProvider {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CachingPModeProvider.class);

    //Don't access directly, use getter instead
    private volatile Configuration configuration;

    @Autowired
    private ProcessPartyExtractorProvider processPartyExtractorProvider;

    @Autowired
    PullMessageService pullMessageService;

    //pull processes cache.
    private Map<Party, List<Process>> pullProcessesByInitiatorCache = new HashMap<>();

    private Map<String, List<Process>> pullProcessByMpcCache = new HashMap<>();

    protected Domain domain;

    private Object configurationLock = new Object();

    public CachingPModeProvider(Domain domain) {
        this.domain = domain;
    }

    public Configuration getConfiguration() {
        if (this.configuration == null) {
            synchronized (configurationLock) {
                if (this.configuration == null) {
                    this.init();
                }
            }
        }
        return this.configuration;
    }

    @Override
    public Party getGatewayParty() {
        return getConfiguration().getParty();
    }

    @Override
    protected void init() {
        if (!this.configurationDAO.configurationExists()) {
            throw new IllegalStateException("No processing modes found. To exchange messages, upload configuration file through the web gui.");
        }
        LOG.debug("Initialising the configuration");
        this.configuration = this.configurationDAO.readEager();
        LOG.trace("Configuration initialized: [{}]", this.configuration.getEntityId());

        initPullProcessesCache();
    }

    private void initPullProcessesCache() {
        final Set<Mpc> mpcs = getConfiguration().getMpcs();
        for (Mpc mpc : mpcs) {
            final String qualifiedName = mpc.getQualifiedName();
            final List<Process> pullProcessByMpc = getPullProcessByMpc(qualifiedName);
            pullProcessByMpcCache.put(qualifiedName, pullProcessByMpc);
        }

        Set<Party> initiatorsForPullProcesses = getInitiatorsForPullProcesses();
        for (Party initiator : initiatorsForPullProcesses) {
            final List<Process> pullProcessesByInitiator = getPullProcessesWithInitiator(initiator);
            pullProcessesByInitiatorCache.put(initiator, pullProcessesByInitiator);
        }
    }

    protected List<Process> getPullProcessesWithInitiator(Party initiator) {
        final List<Process> pullProcesses = getAllPullProcesses();
        return pullProcesses.stream()
                .filter(process -> hasInitiatorParty(process, initiator.getName()))
                .collect(Collectors.toList());
    }

    protected List<Process> getAllPullProcesses() {
        final List<Process> processes = getConfiguration().getBusinessProcesses().getProcesses();
        return processes.stream()
                .filter(process -> isPullProcess(process))
                .collect(Collectors.toList());
    }

    protected Set<Party> getInitiatorsForPullProcesses() {
        Set<Party> initiators = new HashSet<>();
        final List<Process> pullProcesses = getAllPullProcesses();
        pullProcesses.stream()
                .map(process -> process.getInitiatorParties())
                .forEach(parties -> initiators.addAll(parties));
        return initiators;
    }

    protected List<Process> getPullProcessByMpc(final String mpcQualifiedName) {
        List<Process> result = new ArrayList<>();

        final List<Process> pullProcesses = getAllPullProcesses();
        for (Process process : pullProcesses) {
            if (isProcessMatchingMpcLeg(process, mpcQualifiedName)) {
                LOG.debug("Matched pull process [{}] with mpc [{}]", process.getName(), mpcQualifiedName);
                result.add(process);
            }
        }
        return result;
    }

    protected boolean isProcessMatchingMpcLeg(Process process, final String mpcQualifiedName) {
        Set<LegConfiguration> legConfigurations = process.getLegs();
        if (legConfigurations == null) {
            return false;
        }
        return legConfigurations.stream()
                .anyMatch(legConfiguration -> StringUtils.equals(legConfiguration.getDefaultMpc().getQualifiedName(), mpcQualifiedName));
    }


    /**
     * The match means that either has an Agreement and its name matches the Agreement name found previously
     * or it has no Agreement configured and the Agreement name was not indicated in the submitted message.
     *
     * @param process       the process containing the agreement
     * @param agreementName the agreement name
     */
    protected boolean matchAgreement(Process process, String agreementName) {
        return (process.getAgreement() != null && StringUtils.equalsIgnoreCase(process.getAgreement().getName(), agreementName)
                || (StringUtils.equalsIgnoreCase(agreementName, OPTIONAL_AND_EMPTY) && process.getAgreement() == null)
                // Please notice that this is only for backward compatibility and will be removed ASAP!
                || (StringUtils.equalsIgnoreCase(agreementName, OPTIONAL_AND_EMPTY) && process.getAgreement() != null && StringUtils.isEmpty(process.getAgreement().getValue()))
        );
    }

    /**
     * The match means that either there is no initiator and it is allowed
     * by configuration OR the initiator name matches
     *
     * @param process                   the process containing the initiators
     * @param processTypePartyExtractor the extractor that provides the senderParty
     */
    protected boolean matchInitiator(final Process process, final ProcessTypePartyExtractor processTypePartyExtractor) {
        if (CollectionUtils.isEmpty(process.getInitiatorParties())) {
            if (pullMessageService.allowDynamicInitiatorInPullProcess()) {
                return true;
            }
            return false;
        }

        for (final Party party : process.getInitiatorParties()) {
            if (StringUtils.equalsIgnoreCase(party.getName(), processTypePartyExtractor.getSenderParty())) {
                return true;
            }
        }
        return false;
    }

    /**
     * The match requires that the responder exists in the process
     *
     * @param process                   the process containing the responder
     * @param processTypePartyExtractor the extractor that provides the receiverParty
     */
    protected boolean matchResponder(final Process process, final ProcessTypePartyExtractor processTypePartyExtractor) {
        //Responder is always required for this method to return true
        if (CollectionUtils.isEmpty(process.getResponderParties())) {
            return false;
        }

        for (final Party party : process.getResponderParties()) {
            if (StringUtils.equalsIgnoreCase(party.getName(), processTypePartyExtractor.getReceiverParty())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String findPullLegName(final String agreementName, final String senderParty,
                                  final String receiverParty, final String service, final String action, final String mpc) throws EbMS3Exception {
        final List<LegConfiguration> candidates = new ArrayList<>();
        ProcessTypePartyExtractor processTypePartyExtractor = processPartyExtractorProvider.getProcessTypePartyExtractor(
                MessageExchangePattern.ONE_WAY_PULL.getUri(), senderParty, receiverParty);
        List<Process> processes = this.getConfiguration().getBusinessProcesses().getProcesses();
        processes = processes.stream().filter(process -> matchAgreement(process, agreementName))
                .filter(process -> MessageExchangePattern.ONE_WAY_PULL.getUri().equals(process.getMepBinding().getValue()))
                .filter(process -> matchInitiator(process, processTypePartyExtractor))
                .filter(process -> matchResponder(process, processTypePartyExtractor)).collect(Collectors.toList());

        processes.stream().forEach(process -> candidates.addAll(process.getLegs()));
        if (candidates.isEmpty()) {
            LOG.businessError(DomibusMessageCode.BUS_LEG_NAME_NOT_FOUND, agreementName, senderParty, receiverParty, service, action);
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0001, "No Candidates for Legs found", null, null);
        }
        Optional<LegConfiguration> optional = candidates.stream()
                .filter(candidate -> candidateMatches(candidate, service, action, mpc))
                .findFirst();
        String pullLegName = optional.isPresent() ? optional.get().getName() : null;
        if (pullLegName != null) {
            return pullLegName;
        }
        LOG.businessError(DomibusMessageCode.BUS_LEG_NAME_NOT_FOUND, agreementName, senderParty, receiverParty, service, action);
        throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0001, "No matching leg found", null, null);
    }

    protected boolean candidateMatches(LegConfiguration candidate, String service, String action, String mpc) {
        if (StringUtils.equalsIgnoreCase(candidate.getService().getName(), service)
                && StringUtils.equalsIgnoreCase(candidate.getAction().getName(), action)
                && StringUtils.equalsIgnoreCase(candidate.getDefaultMpc().getQualifiedName(), mpc)) {
            return true;
        }
        return false;
    }

    @Override
    //FIXME: only works for the first leg, as sender=initiator
    public String findLegName(final String agreementName, final String senderParty, final String receiverParty,
                              final String service, final String action) throws EbMS3Exception {
        final List<LegConfiguration> candidates = new ArrayList<>();
        //TODO Refactor the nested for loop and conditions
        for (final Process process : this.getConfiguration().getBusinessProcesses().getProcesses()) {
            final ProcessTypePartyExtractor processTypePartyExtractor = processPartyExtractorProvider.getProcessTypePartyExtractor(process.getMepBinding().getValue(), senderParty, receiverParty);
            for (final Party party : process.getInitiatorParties()) {
                if (StringUtils.equalsIgnoreCase(party.getName(), processTypePartyExtractor.getSenderParty())) {
                    for (final Party responder : process.getResponderParties()) {
                        if (StringUtils.equalsIgnoreCase(responder.getName(), processTypePartyExtractor.getReceiverParty())) {
                            if (process.getAgreement() != null && StringUtils.equalsIgnoreCase(process.getAgreement().getName(), agreementName)
                                    || (StringUtils.equalsIgnoreCase(agreementName, OPTIONAL_AND_EMPTY) && process.getAgreement() == null)
                                    // Please notice that this is only for backward compatibility and will be removed ASAP!
                                    || (StringUtils.equalsIgnoreCase(agreementName, OPTIONAL_AND_EMPTY) && process.getAgreement() != null && StringUtils.isEmpty(process.getAgreement().getValue()))
                            ) {
                                /**
                                 * The Process is a candidate because either has an Agreement and its name matches the Agreement name found previously
                                 * or it has no Agreement configured and the Agreement name was not indicated in the submitted message.
                                 **/
                                candidates.addAll(process.getLegs());
                            }
                        }
                    }
                }
            }
        }
        if (candidates.isEmpty()) {
            LOG.businessError(DomibusMessageCode.BUS_LEG_NAME_NOT_FOUND, agreementName, senderParty, receiverParty, service, action);
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0001, "No Candidates for Legs found", null, null);
        }
        for (final LegConfiguration candidate : candidates) {
            if (StringUtils.equalsIgnoreCase(candidate.getService().getName(), service) && StringUtils.equalsIgnoreCase(candidate.getAction().getName(), action)) {
                return candidate.getName();
            }
        }
        LOG.businessError(DomibusMessageCode.BUS_LEG_NAME_NOT_FOUND, agreementName, senderParty, receiverParty, service, action);
        throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0001, "No matching leg found", null, null);
    }

    @Override
    public String findActionName(final String action) throws EbMS3Exception {
        for (final Action action1 : this.getConfiguration().getBusinessProcesses().getActions()) {
            if (StringUtils.equalsIgnoreCase(action1.getValue(), action)) {
                return action1.getName();
            }
        }
        throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0001, "No matching action found", null, null);
    }

    @Override
    public Mpc findMpc(final String mpcValue) throws EbMS3Exception {
        for (final Mpc mpc : this.getConfiguration().getMpcs()) {
            if (StringUtils.equalsIgnoreCase(mpc.getQualifiedName(), mpcValue)) {
                return mpc;
            }
        }
        throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0001, "No matching mpc found", null, null);
    }

    @Override
    public String findServiceName(final eu.domibus.ebms3.common.model.Service service) throws EbMS3Exception {
        if (service == null) {
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0001, "Service is not found in the message", null, null);
        }

        for (final Service service1 : this.getConfiguration().getBusinessProcesses().getServices()) {
            if ((StringUtils.equalsIgnoreCase(service1.getServiceType(), service.getType()) || (!StringUtils.isNotEmpty(service1.getServiceType()) && !StringUtils.isNotEmpty(service.getType()))))
                if (StringUtils.equalsIgnoreCase(service1.getValue(), service.getValue())) {
                    return service1.getName();
                }
        }
        throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0001, "No matching service found", null, null);
    }

    @Override
    public String findPartyName(final Collection<PartyId> partyId) throws EbMS3Exception {
        String partyIdType = StringUtils.EMPTY;
        String partyIdEx = StringUtils.EMPTY;
        final EbMS3Exception ex;
        for (final Party party : this.getConfiguration().getBusinessProcesses().getParties()) {
            for (final PartyId id : partyId) {
                partyIdEx = id.getValue();
                for (final Identifier identifier : party.getIdentifiers()) {
                    if (id.getType() != null) {
                        partyIdType = id.getType();
                        try {
                            URI.create(partyIdType);
                        } catch (final IllegalArgumentException e) {
                            ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0003, "No matching party found", null, e);
                            ex.setErrorDetail("PartyIdType of PartyId " + id.getValue() + " is not a valid URI [CORE].");
                            LOG.trace("No matching party found! PartyIdType [{}] of PartyId [{}] is not a valid URI [CORE].", partyIdType, id.getValue());
                            throw ex;
                        }
                    }
                    String identifierPartyIdType = StringUtils.EMPTY;
                    if (identifier.getPartyIdType() != null) {
                        identifierPartyIdType = identifier.getPartyIdType().getValue();
                    }
                    LOG.trace("Find party with type:[{}] and identifier:[{}] by comparing with pmode id type:[{}] and pmode identifier:[{}]", partyIdType, id.getValue(), identifierPartyIdType, identifier.getPartyId());
                    if (StringUtils.equalsIgnoreCase(partyIdType, identifierPartyIdType) && StringUtils.equalsIgnoreCase(id.getValue(), identifier.getPartyId())) {
                        LOG.trace("Found Party with matching PartyId:[{}] and type:[{}] with PartyName:[{}] ", id.getValue(), partyIdType, party.getName());
                        return party.getName();
                    }
                }
            }
        }
        throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0003, "No matching party found with PartyId " + partyIdEx, null, null);
    }

    @Override
    public String findAgreement(final AgreementRef agreementRef) throws EbMS3Exception {
        if (agreementRef == null || agreementRef.getValue() == null || agreementRef.getValue().isEmpty()) {
            return OPTIONAL_AND_EMPTY; // AgreementRef is optional
        }

        for (final Agreement agreement : this.getConfiguration().getBusinessProcesses().getAgreements()) {
            if ((StringUtils.isEmpty(agreementRef.getType()) || StringUtils.equalsIgnoreCase(agreement.getType(), agreementRef.getType()))
                    && StringUtils.equalsIgnoreCase(agreementRef.getValue(), agreement.getValue())) {
                return agreement.getName();
            }
        }
        throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0001, "No matching agreement found", null, null);
    }

    @Override
    public Party getPartyByIdentifier(String partyIdentifier) {
        for (final Party party : this.getConfiguration().getBusinessProcesses().getParties()) {
            final Set<Identifier> identifiers = party.getIdentifiers();
            for (Identifier identifier : identifiers) {
                if (StringUtils.equalsIgnoreCase(identifier.getPartyId(), partyIdentifier)) {
                    return party;
                }
            }
        }
        return null;
    }

    @Override
    public Party getSenderParty(final String pModeKey) {
        final String partyKey = this.getSenderPartyNameFromPModeKey(pModeKey);
        for (final Party party : this.getConfiguration().getBusinessProcesses().getParties()) {
            if (StringUtils.equalsIgnoreCase(party.getName(), partyKey)) {
                return party;
            }
        }
        throw new ConfigurationException("no matching sender party found with name: " + partyKey);
    }

    @Override
    public Party getReceiverParty(final String pModeKey) {
        final String partyKey = this.getReceiverPartyNameFromPModeKey(pModeKey);
        for (final Party party : this.getConfiguration().getBusinessProcesses().getParties()) {
            if (StringUtils.equalsIgnoreCase(party.getName(), partyKey)) {
                return party;
            }
        }
        throw new ConfigurationException("no matching receiver party found with name: " + partyKey);
    }

    @Override
    public Service getService(final String pModeKey) {
        final String serviceKey = this.getServiceNameFromPModeKey(pModeKey);
        for (final Service service : this.getConfiguration().getBusinessProcesses().getServices()) {
            if (StringUtils.equalsIgnoreCase(service.getName(), serviceKey)) {
                return service;
            }
        }
        throw new ConfigurationException("no matching service found with name: " + serviceKey);
    }

    @Override
    public Action getAction(final String pModeKey) {
        final String actionKey = this.getActionNameFromPModeKey(pModeKey);
        for (final Action action : this.getConfiguration().getBusinessProcesses().getActions()) {
            if (StringUtils.equalsIgnoreCase(action.getName(), actionKey)) {
                return action;
            }
        }
        throw new ConfigurationException("no matching action found with name: " + actionKey);
    }

    @Override
    public Agreement getAgreement(final String pModeKey) {
        final String agreementKey = this.getAgreementRefNameFromPModeKey(pModeKey);
        for (final Agreement agreement : this.getConfiguration().getBusinessProcesses().getAgreements()) {
            if (StringUtils.equalsIgnoreCase(agreement.getName(), agreementKey)) {
                return agreement;
            }
        }
        throw new ConfigurationException("no matching agreement found with name: " + agreementKey);
    }

    @Override
    public LegConfiguration getLegConfiguration(final String pModeKey) {
        final String legKey = this.getLegConfigurationNameFromPModeKey(pModeKey);
        for (final LegConfiguration legConfiguration : this.getConfiguration().getBusinessProcesses().getLegConfigurations()) {
            if (StringUtils.equalsIgnoreCase(legConfiguration.getName(), legKey)) {
                return legConfiguration;
            }
        }
        throw new ConfigurationException("no matching legConfiguration found with name: " + legKey);
    }

    @Override
    public boolean isMpcExistant(final String mpc) {
        for (final Mpc mpc1 : this.getConfiguration().getMpcs()) {
            if (StringUtils.equalsIgnoreCase(mpc1.getName(), mpc)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getRetentionDownloadedByMpcName(final String mpcName) {
        for (final Mpc mpc1 : this.getConfiguration().getMpcs()) {
            if (StringUtils.equalsIgnoreCase(mpc1.getName(), mpcName)) {
                return mpc1.getRetentionDownloaded();
            }
        }

        LOG.error("No MPC with name: [{}] found. Assuming message retention of 0 for downloaded messages.", mpcName);

        return 0;
    }

    @Override
    public int getRetentionDownloadedByMpcURI(final String mpcURI) {
        for (final Mpc mpc1 : this.getConfiguration().getMpcs()) {
            if (StringUtils.equalsIgnoreCase(mpc1.getQualifiedName(), mpcURI)) {
                return mpc1.getRetentionDownloaded();
            }
        }

        LOG.error("No MPC with name: [{}] found. Assuming message retention of 0 for downloaded messages.", mpcURI);

        return 0;
    }

    @Override
    public int getRetentionUndownloadedByMpcName(final String mpcName) {
        for (final Mpc mpc1 : this.getConfiguration().getMpcs()) {
            if (StringUtils.equalsIgnoreCase(mpc1.getName(), mpcName)) {
                return mpc1.getRetentionUndownloaded();
            }
        }

        LOG.error("No MPC with name: [{}] found. Assuming message retention of -1 for undownloaded messages.", mpcName);

        return -1;
    }

    @Override
    public int getRetentionUndownloadedByMpcURI(final String mpcURI) {
        for (final Mpc mpc1 : this.getConfiguration().getMpcs()) {
            if (StringUtils.equalsIgnoreCase(mpc1.getQualifiedName(), mpcURI)) {
                return mpc1.getRetentionUndownloaded();
            }
        }

        LOG.error("No MPC with name: [{}] found. Assuming message retention of -1 for undownloaded messages.", mpcURI);

        return -1;
    }

    @Override
    public List<String> getMpcList() {
        final List<String> result = new ArrayList<>();
        for (final Mpc mpc : this.getConfiguration().getMpcs()) {
            result.add(mpc.getName());
        }
        return result;
    }

    @Override
    public List<String> getMpcURIList() {
        final List<String> result = new ArrayList<>();
        for (final Mpc mpc : this.getConfiguration().getMpcs()) {
            result.add(mpc.getQualifiedName());
        }
        return result;
    }

    @Override
    public Role getBusinessProcessRole(String roleValue) {
        for (Role role : this.getConfiguration().getBusinessProcesses().getRoles()) {
            if (StringUtils.equalsIgnoreCase(role.getValue(), roleValue)) {
                return role;
            }
        }
        LOG.businessError(DomibusMessageCode.BUS_PARTY_ROLE_NOT_FOUND, roleValue);
        return null;
    }

    @Override
    public void refresh() {
        synchronized (configurationLock) {
            this.configuration = null;

            this.pullProcessByMpcCache.clear();
            this.pullProcessesByInitiatorCache.clear();

            this.init(); //reloads the config
        }
    }

    @Override
    public boolean isConfigurationLoaded() {
        if (this.configuration != null) return true;
        return configurationDAO.configurationExists();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public List<ValidationIssue> updatePModes(final byte[] bytes, String description) throws XmlProcessingException, PModeValidationException {
        List<ValidationIssue> issues = super.updatePModes(bytes, description);
        return issues;
    }

    @Override
    public List<Process> findPullProcessesByMessageContext(final MessageExchangeConfiguration messageExchangeConfiguration) {
        List<Process> allProcesses = findAllProcesses();
        List<Process> result = new ArrayList<>();
        for (Process process : allProcesses) {
            boolean pullProcess = isPullProcess(process);
            if (!pullProcess) {
                continue;
            }

            boolean hasLeg = hasLeg(process, messageExchangeConfiguration.getLeg());
            if (!hasLeg) {
                continue;
            }
            boolean hasInitiatorParty = hasInitiatorParty(process, messageExchangeConfiguration.getReceiverParty());
            if (!hasInitiatorParty) {
                continue;
            }
            boolean hasResponderParty = hasResponderParty(process, messageExchangeConfiguration.getSenderParty());
            if (!hasResponderParty) {
                continue;
            }
            result.add(process);
        }
        return result;
    }

    protected boolean isPullProcess(Process process) {
        if (process.getMepBinding() == null) {
            return false;
        }
        return StringUtils.equals(BackendConnector.Mode.PULL.getFileMapping(), process.getMepBinding().getValue());
    }

    protected boolean hasLeg(Process process, String legName) {
        return process.getLegs().stream().anyMatch(leg -> StringUtils.equals(leg.getName(), legName));
    }

    protected boolean hasInitiatorParty(Process process, String partyName) {
        Set<Party> initiatorParties = process.getInitiatorParties();
        return matchesParty(initiatorParties, partyName);
    }

    protected boolean hasResponderParty(Process process, String partyName) {
        Set<Party> responderParties = process.getResponderParties();
        return matchesParty(responderParties, partyName);
    }

    protected boolean matchesParty(Set<Party> parties, String partyName) {
        return parties.stream().anyMatch(initiatorParty -> StringUtils.equals(initiatorParty.getName(), partyName));
    }

    @Override
    public List<Process> findPullProcessesByInitiator(final Party party) {
        final List<Process> processes = pullProcessesByInitiatorCache.get(party);
        if (processes == null) {
            return Lists.newArrayList();
        }
        // return list with no duplicates
        return Lists.newArrayList(new HashSet<>(processes));
    }

    @Override
    public List<Process> findPullProcessByMpc(final String mpc) {
        List<Process> processes = pullProcessByMpcCache.get(mpc);
        if (processes == null) {
            return Lists.newArrayList();
        }
        return processes;
    }

    @Override
    public List<Process> findAllProcesses() {
        try {
            return Lists.newArrayList(getConfiguration().getBusinessProcesses().getProcesses());
        } catch (IllegalArgumentException e) {
            return Lists.newArrayList();
        }
    }

    @Override
    public List<Party> findAllParties() {
        try {
            return Lists.newArrayList(getConfiguration().getBusinessProcesses().getParties());
        } catch (IllegalArgumentException e) {
            return Lists.newArrayList();
        }
    }

    @Override
    public List<String> findPartyIdByServiceAndAction(final String service, final String action, final List<MessageExchangePattern> meps) {
        List result = new ArrayList<String>();
        List<Process> processes = filterProcessesByMep(meps);
        for (Process process : processes) {
            for (LegConfiguration legConfiguration : process.getLegs()) {
                LOG.trace("Find Party in leg [{}]", legConfiguration.getName());
                result.addAll(handleLegConfiguration(legConfiguration, process, service, action));
            }
        }
        return result;
    }

    protected List<Process> filterProcessesByMep(final List<MessageExchangePattern> meps) {
        List<Process> processes = this.getConfiguration().getBusinessProcesses().getProcesses();
        processes = processes.stream().filter(process -> isMEPMatch(process, meps)).collect(Collectors.toList());

        return processes;
    }

    protected boolean isMEPMatch(Process process, final List<MessageExchangePattern> meps) {
        if (CollectionUtils.isEmpty(meps)) { // process can have any mep
            return true;
        }

        if (process == null || process.getMepBinding() == null  // invalid process
                || process.getMepBinding().getValue() == null) {
            return false;
        }

        for (MessageExchangePattern mep : meps) {
            if (mep.getUri().equals(process.getMepBinding().getValue())) {
                LOG.trace("Found match for mep [{}]", mep.getUri());
                return true;
            }
        }

        return false;
    }

    private List<String> handleLegConfiguration(LegConfiguration legConfiguration, Process process, String
            service, String action) {
        List result = new ArrayList<String>();
        if (StringUtils.equalsIgnoreCase(legConfiguration.getService().getValue(), service) && StringUtils.equalsIgnoreCase(legConfiguration.getAction().getValue(), action)) {
            handleProcessParties(process, result);
        }
        return result;
    }

    protected void handleProcessParties(Process process, List result) {
        Comparator<Identifier> comp = (Identifier a, Identifier b) -> StringUtils.compare(a.getPartyId(), b.getPartyId());
        for (Party party : process.getResponderParties()) {
            // add only one id for the party, not all aliases
            List<Identifier> ids = party.getIdentifiers().stream().sorted(comp).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(ids)) {
                LOG.warn("No party ids for party [{}]", party.getName());
                continue;
            }
            String id = ids.get(0).getPartyId();
            LOG.trace("Add matching party [{}] from process [{}]", id, process.getName());
            result.add(id);
        }
    }

    @Override
    public String getPartyIdType(String partyIdentifier) {
        for (Party party : getConfiguration().getBusinessProcesses().getParties()) {
            String partyIdTypeHandleParty = getPartyIdTypeHandleParty(party, partyIdentifier);
            if (partyIdTypeHandleParty != null) {
                return partyIdTypeHandleParty;
            }
        }
        return null;
    }

    private String getPartyIdTypeHandleParty(Party party, String partyIdentifier) {
        for (Identifier identifier : party.getIdentifiers()) {
            if (StringUtils.equalsIgnoreCase(identifier.getPartyId(), partyIdentifier)) {
                return identifier.getPartyIdType().getValue();
            }
        }
        return null;
    }

    @Override
    public String getServiceType(String serviceValue) {
        for (Service service : getConfiguration().getBusinessProcesses().getServices()) {
            if (StringUtils.equalsIgnoreCase(service.getValue(), serviceValue)) {
                return service.getServiceType();
            }
        }
        return null;
    }

    protected List<Process> getProcessFromService(String serviceValue) {
        List<Process> result = new ArrayList<>();
        for (Process process : getConfiguration().getBusinessProcesses().getProcesses()) {
            for (LegConfiguration legConfiguration : process.getLegs()) {
                if (StringUtils.equalsIgnoreCase(legConfiguration.getService().getValue(), serviceValue)) {
                    result.add(process);
                }
            }
        }
        return result;
    }

    @Override
    public String getRole(String roleType, String serviceValue) {
        for (Process found : getProcessFromService(serviceValue)) {
            String roleHandleProcess = getRoleHandleProcess(found, roleType);
            if (roleHandleProcess != null) {
                return roleHandleProcess;
            }
        }
        return null;
    }

    @Nullable
    private String getRoleHandleProcess(Process found, String roleType) {
        for (Process process : getConfiguration().getBusinessProcesses().getProcesses()) {
            if (StringUtils.equalsIgnoreCase(process.getName(), found.getName())) {
                if (roleType.equalsIgnoreCase("initiator")) {
                    return process.getInitiatorRole().getValue();
                }
                if (roleType.equalsIgnoreCase("responder")) {
                    return process.getResponderRole().getValue();
                }
            }
        }
        return null;
    }

    @Override
    public String getAgreementRef(String serviceValue) {
        for (Process found : getProcessFromService(serviceValue)) {
            String agreementRefHandleProcess = getAgreementRefHandleProcess(found);
            if (agreementRefHandleProcess != null) {
                return agreementRefHandleProcess;
            }
        }
        return null;
    }

    public String findMpcUri(final String mpcName) throws EbMS3Exception {
        for (final Mpc mpc : this.getConfiguration().getMpcs()) {
            if (StringUtils.equalsIgnoreCase(mpc.getName(), mpcName)) {
                return mpc.getQualifiedName();
            }
        }
        throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0001, "No matching mpc found", null, null);
    }

    @Nullable
    private String getAgreementRefHandleProcess(Process found) {
        for (Process process : getConfiguration().getBusinessProcesses().getProcesses()) {
            if (StringUtils.equalsIgnoreCase(process.getName(), found.getName())) {
                Agreement agreement = process.getAgreement();
                if (agreement != null) {
                    return agreement.getValue();
                }
            }
        }
        return null;
    }
}
