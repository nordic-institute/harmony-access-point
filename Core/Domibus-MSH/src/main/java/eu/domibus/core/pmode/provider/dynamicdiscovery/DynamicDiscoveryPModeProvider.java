package eu.domibus.core.pmode.provider.dynamicdiscovery;

import eu.domibus.api.cache.DomibusLocalCacheService;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.PartyId;
import eu.domibus.api.model.PartyRole;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.pki.MultiDomainCryptoService;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.common.model.configuration.*;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.EbMS3ExceptionBuilder;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.core.message.MessageExchangeConfiguration;
import eu.domibus.core.message.UserMessageServiceHelper;
import eu.domibus.core.message.dictionary.PartyIdDictionaryService;
import eu.domibus.core.message.dictionary.PartyRoleDictionaryService;
import eu.domibus.core.pmode.provider.CachingPModeProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.ProcessingType;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.naming.InvalidNameException;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.stream.Collectors;

import static eu.domibus.api.cache.DomibusLocalCacheService.DYNAMIC_DISCOVERY_ENDPOINT;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/* This class is used for dynamic discovery of the parties participating in a message exchange.
 *
 * Dynamic discovery is activated when the pMode is configured with a dynamic
 * process (PMode.Initiator is not set and/or PMode.Responder is not set)
 *
 * The receiver of the message must be able to accept messages from previously unknown senders.
 * This requires the receiver to have one or more P-Modes configured for all registrations ii has in the SMP.
 * Therefore for each SMP Endpoint registration of the receiver with the type attribute set to 'bdxr-transport-ebms3-as4-v1p0'
 * there MUST exist a P-Mode that can handle a message with the following attributes:
 *      Service = ancestor::ServiceMetadata/ServiceInformation/Processlist/Process/ProcessIdentifier
 *      Service/@type = ancestor::ServiceMetadata/ServiceInformation/Processlist/Process/ProcessIdentifier/@scheme
 *      Action = ancestor::ServiceMetadata/ServiceInformation/DocumentIdentifier
 *
 * The sender must be able to send messages to unknown receivers. This requires that the sender performs a lookup to find
 * out the receivers details (partyId, type, endpoint address, public certificate - to encrypt the message).
 *
 * The sender may not register, it can send a message to a registered receiver even if he (the sender) is not registered.
 * Therefore, on the receiver there is no lookup for the sender. The message is accepted based on the root CA as long as the process matches.
 */

public class DynamicDiscoveryPModeProvider extends CachingPModeProvider {

    private static final String DYNAMIC_DISCOVERY_CLIENT_SPECIFICATION = DOMIBUS_DYNAMICDISCOVERY_CLIENT_SPECIFICATION;

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DynamicDiscoveryPModeProvider.class);

    @Autowired
    protected MultiDomainCryptoService multiDomainCertificateProvider;

    @Autowired
    protected DomainContextProvider domainProvider;

    @Autowired
    @Qualifier("dynamicDiscoveryServiceOASIS")
    private DynamicDiscoveryService dynamicDiscoveryServiceOASIS;

    @Autowired
    @Qualifier("dynamicDiscoveryServicePEPPOL")
    private DynamicDiscoveryService dynamicDiscoveryServicePEPPOL;

    protected DynamicDiscoveryService dynamicDiscoveryService = null;

    @Autowired
    protected CertificateService certificateService;

    @Autowired
    protected PartyRoleDictionaryService partyRoleDictionaryService;

    @Autowired
    protected PartyIdDictionaryService partyIdDictionaryService;

    @Autowired
    protected DomibusLocalCacheService domibusLocalCacheService;

    @Autowired
    protected UserMessageServiceHelper userMessageServiceHelper;

    protected Collection<eu.domibus.common.model.configuration.Process> dynamicResponderProcesses;
    protected Collection<eu.domibus.common.model.configuration.Process> dynamicInitiatorProcesses;

    // default type in eDelivery profile
    protected static final String URN_TYPE_VALUE = "urn:oasis:names:tc:ebcore:partyid-type:unregistered";
    protected static final String DEFAULT_RESPONDER_ROLE = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder";
    protected static final String MSH_ENDPOINT = "msh_endpoint";

    public DynamicDiscoveryPModeProvider(Domain domain) {
        super(domain);
    }

    @Override
    public void init() {
        load();
    }

    @Override
    protected void load() {
        super.load();

        LOG.debug("Initialising the dynamic discovery configuration.");
        dynamicResponderProcesses = findDynamicResponderProcesses();
        dynamicInitiatorProcesses = findDynamicSenderProcesses();
        if (DynamicDiscoveryClientSpecification.PEPPOL.getName().equalsIgnoreCase(domibusPropertyProvider.getProperty(DYNAMIC_DISCOVERY_CLIENT_SPECIFICATION))) {
            dynamicDiscoveryService = dynamicDiscoveryServicePEPPOL;
        } else { // OASIS client is used by default
            dynamicDiscoveryService = dynamicDiscoveryServiceOASIS;
        }
    }

    public Collection<eu.domibus.common.model.configuration.Process> getDynamicProcesses(final MSHRole mshRole) {
        // TODO investigate why the configuration is empty when these lists are initialized in the first place
        if (CollectionUtils.isEmpty(dynamicResponderProcesses) && CollectionUtils.isEmpty(dynamicInitiatorProcesses)) {
            // this is needed when the processes were not initialized in the init()
            LOG.debug("Refreshing the configuration.");
            refresh();
        }

        return MSHRole.SENDING.equals(mshRole) ? dynamicResponderProcesses : dynamicInitiatorProcesses;
    }

    protected Collection<eu.domibus.common.model.configuration.Process> findDynamicResponderProcesses() {
        final Collection<eu.domibus.common.model.configuration.Process> result = new ArrayList<>();
        for (final eu.domibus.common.model.configuration.Process process : this.getConfiguration().getBusinessProcesses().getProcesses()) {
            if (process.isDynamicResponder() && (process.isDynamicInitiator() || process.getInitiatorParties().contains(getConfiguration().getParty()))) {
                if (!process.getInitiatorParties().contains(getConfiguration().getParty())) {
                    throw new ConfigurationException(process + " does not contain self party " + getConfiguration().getParty() + " as an initiator party.");
                }
                LOG.debug("Found dynamic receiver process: " + process.getName());
                result.add(process);
            }
        }
        return result;
    }

    protected Collection<eu.domibus.common.model.configuration.Process> findDynamicSenderProcesses() {
        final Collection<eu.domibus.common.model.configuration.Process> result = new ArrayList<>();
        for (final eu.domibus.common.model.configuration.Process process : this.getConfiguration().getBusinessProcesses().getProcesses()) {
            if (process.isDynamicInitiator() && (process.isDynamicResponder() || process.getResponderParties().contains(getConfiguration().getParty()))) {
                if (!process.getResponderParties().contains(getConfiguration().getParty())) {
                    throw new ConfigurationException(process + " does not contain self party " + getConfiguration().getParty() + " as a responder party.");
                }
                LOG.debug("Found dynamic sender process: " + process.getName());
                result.add(process);
            }
        }
        return result;
    }

    /**
     * Method validates if dynamic discovery is enabled for current domain.
     *
     * @return true if domibus.dynamicdiscovery.useDynamicDiscovery is enabled for the current domain.
     */
    protected boolean useDynamicDiscovery() {
        return domibusPropertyProvider.getBooleanProperty(DOMIBUS_DYNAMICDISCOVERY_USE_DYNAMIC_DISCOVERY);
    }

    /* Method finds MessageExchangeConfiguration for given user mesage and role. If property domibus.smlzone
     * is not defined only static search is done else (if static search did not return result) also dynamic discovery is executed.
     */
    @Override
    public MessageExchangeConfiguration findUserMessageExchangeContext(final UserMessage userMessage, final MSHRole mshRole, final boolean isPull, ProcessingType processingType) throws EbMS3Exception {
        try {
            final MessageExchangeConfiguration userMessageExchangeContext = super.findUserMessageExchangeContext(userMessage, mshRole, isPull, processingType, true);

            if (useDynamicDiscovery()) {
                final String partyToValue = userMessageServiceHelper.getPartyToValue(userMessage);
                LOG.debug("Checking if public certificate for receiver party [{}] in the truststore", partyToValue);
                final X509Certificate receiverCertificateFromTruststore = getCertificateFromTruststore(partyToValue, userMessage.getMessageId());
                if (receiverCertificateFromTruststore == null) {
                    LOG.info("Could not find public certificate for receiver party [{}] in the truststore. Triggering dynamic discovery", partyToValue);
                    throw EbMS3ExceptionBuilder.getInstance()
                            .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0003)
                            .message("Could not find public certificate for receiver party [" + partyToValue + "] in the truststore. Triggering dynamic discovery")
                            .refToMessageId(userMessage.getMessageId())
                            .build();
                }
            }
            return userMessageExchangeContext;
        } catch (final EbMS3Exception e) {
            if (useDynamicDiscovery()) {
                LOG.info("PmodeKey/receiver public certificate not found, starting the dynamic discovery process: [{}]", e.getMessage());
                doDynamicDiscovery(userMessage, mshRole);
            } else {
                LOG.error("PmodeKey/receiver public certificate not found and dynamic discovery is not enabled! Check property [{}] for current domain [{}]: [{}]", DOMIBUS_SMLZONE, domainProvider.getCurrentDomain(), e.getMessage());
                throw e;
            }
        }
        LOG.debug("Recalling findUserMessageExchangeContext after the dynamic discovery");
        return super.findUserMessageExchangeContext(userMessage, mshRole, isPull, processingType, false);
    }

    protected void doDynamicDiscovery(final UserMessage userMessage, final MSHRole mshRole) throws EbMS3Exception {
        Collection<eu.domibus.common.model.configuration.Process> candidates = findCandidateProcesses(userMessage, mshRole);

        if (candidates == null || candidates.isEmpty()) {
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0010)
                    .message("No matching dynamic discovery processes found for message.")
                    .refToMessageId(userMessage.getMessageId())
                    .build();
        }

        LOG.info("Found [{}] dynamic discovery candidates: [{}]. MSHRole: [{}]", candidates.size(), getProcessNames(candidates), mshRole);

        if (MSHRole.RECEIVING.equals(mshRole)) {
            PartyId fromPartyId = getFromPartyId(userMessage);
            Party configurationParty = updateConfigurationParty(fromPartyId.getValue(), fromPartyId.getType(), null);
            updateInitiatorPartiesInPmode(candidates, configurationParty);

        } else {//MSHRole.SENDING
            final DynamicDiscoveryCheckResult dynamicDiscoveryCheckResult = checkIfDynamicDiscoveryShouldBePerformed(userMessage, candidates);

            final String finalRecipientCacheKey = dynamicDiscoveryCheckResult.getFinalRecipientCacheKey();
            if (dynamicDiscoveryCheckResult.isPerformDynamicDiscovery()) {
                // do the lookup
                lookupAndUpdateConfigurationForToPartyId(finalRecipientCacheKey, userMessage, candidates);
            } else {
                LOG.debug("Skip DDC lookup and add 'To Party' to UserMessage retrieved from the cache using key [{}]", finalRecipientCacheKey);

                final Party receiverPartyFromPmode = dynamicDiscoveryCheckResult.getPmodeReceiverParty();
                final PartyId receiverPartyTo = getPartyToIdForDynamicDiscovery(receiverPartyFromPmode.getName());

                userMessage.getPartyInfo().getTo().setToPartyId(receiverPartyTo);
                if (userMessage.getPartyInfo().getTo().getToRole() == null) {
                    String responderRoleValue = dynamicDiscoveryService.getResponderRole();
                    PartyRole partyRole = partyRoleDictionaryService.findOrCreateRole(responderRoleValue);
                    userMessage.getPartyInfo().getTo().setToRole(partyRole);
                }
            }
        }
    }

    private List<String> getProcessNames(Collection<Process> candidates) {
        return candidates.stream().map(process -> process.getName()).collect(Collectors.toList());
    }

    protected DynamicDiscoveryCheckResult checkIfDynamicDiscoveryShouldBePerformed(final UserMessage userMessage, Collection<eu.domibus.common.model.configuration.Process> foundProcesses) throws EbMS3Exception {
        DynamicDiscoveryCheckResult result = new DynamicDiscoveryCheckResult();
        String finalRecipientCacheKey = dynamicDiscoveryService.getFinalRecipientCacheKeyForDynamicDiscovery(userMessage);
        result.setFinalRecipientCacheKey(finalRecipientCacheKey);

        EndpointInfo endpointInfo = (EndpointInfo) domibusLocalCacheService.getEntryFromCache(DYNAMIC_DISCOVERY_ENDPOINT, finalRecipientCacheKey);
        result.setEndpointInfo(endpointInfo);

        //final recipient was not previously discovered
        if (endpointInfo == null) {
            LOG.debug("Dynamic discovery will be performed: could not find EndpointInfo in the cache for final recipient key [{}]", finalRecipientCacheKey);
            result.setPerformDynamicDiscovery(true);
            return result;
        }
        final PartyEndpointInfo partyEndpointInfo = getPartyEndpointInfo(endpointInfo, userMessage.getMessageId());
        final String partyNameToFind = partyEndpointInfo.getCertificateCn();
        final Party pmodeReceiverParty = findPartyInTheProcessResponderParties(foundProcesses, partyNameToFind);
        result.setPmodeReceiverParty(pmodeReceiverParty);

        //receiver party was not found in the Pmode receiver parties; it could be that the Pmode was overridden after the final recipient was discovered
        if (pmodeReceiverParty == null) {
            LOG.debug("Dynamic discovery will be performed: could not find Party [{}] in Pmode in none of the processes [{}]", partyNameToFind, getProcessNames(foundProcesses));
            result.setPerformDynamicDiscovery(true);
            return result;
        }

        X509Certificate receiverCertificateFromTruststore = getCertificateFromTruststore(partyNameToFind, userMessage.getMessageId());
        result.setReceiverCertificate(receiverCertificateFromTruststore);

        //the public certificate of the receiver was not found in the truststore; it could be that the truststore was overridden after the final recipient was discovered
        if (receiverCertificateFromTruststore == null) {
            LOG.debug("Dynamic discovery will be performed: could not find public certificate with alias [{}] in the truststore", partyNameToFind);
            result.setPerformDynamicDiscovery(true);
            return result;
        }
        LOG.debug("Dynamic discovery will be skipped: found all details in cache/pmode/truststore");
        result.setPerformDynamicDiscovery(false);
        return result;
    }

    protected X509Certificate getCertificateFromTruststore(String alias, String messageId) throws EbMS3Exception {
        Domain currentDomain = domainProvider.getCurrentDomain();
        try {
            return multiDomainCertificateProvider.getCertificateFromTruststore(currentDomain, alias);
        } catch (final KeyStoreException e) {
            LOG.error("Error while checking if public certificate for party [" + alias + "] is in the truststore", e);
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0003)
                    .message("Error while checking if public certificate for party [" + alias + "] is in the truststore")
                    .refToMessageId(messageId)
                    .cause(e)
                    .build();
        }
    }

    /**
     * Method lookups and updates pmode configuration and truststore
     *
     * @param cacheKey          cached key matches the key for lookup data
     * @param userMessage       - user message which triggered the dynamic discovery search
     * @param processCandidates for dynamic discovery
     * @throws EbMS3Exception
     */
    public void lookupAndUpdateConfigurationForToPartyId(String cacheKey, UserMessage userMessage, Collection<eu.domibus.common.model.configuration.Process> processCandidates) throws EbMS3Exception {
        //we lookup in SMP based on the combination of (domain, participantId, participantIdScheme, action, serviceValue, serviceType)
        //if the lookup was previously done, it is retrieved from cache
        //cache is domain specific
        EndpointInfo endpointInfo = lookupByFinalRecipient(cacheKey, userMessage);
        LOG.debug("Found endpoint [{}]. Configuring PMode and truststore", endpointInfo.getAddress());

        //extract the party information from the Endpoint eg X509 certificate, cn, endpoint URL
        final PartyEndpointInfo partyEndpointInfo = getPartyEndpointInfo(endpointInfo, userMessage.getMessageId());

        final X509Certificate x509Certificate = partyEndpointInfo.getX509Certificate();
        final String certificateCn = partyEndpointInfo.getCertificateCn();

        //we create or get the partyTo based on the certificate common name
        final PartyId receiverParty = getPartyToIdForDynamicDiscovery(certificateCn);

        //we add the partyTo in the UserMessage
        addPartyToInUserMessage(userMessage, receiverParty);

        //we add the certificate in the Domibus truststore, domain specific
        Domain currentDomain = domainProvider.getCurrentDomain();

        //save certificate in the truststore using synchronisation
        boolean added = multiDomainCertificateProvider.addCertificate(currentDomain, x509Certificate, certificateCn, true);
        if (added) {
            LOG.info("Added public certificate with alias [{}] to the truststore for domain [{}]: [{}] ", certificateCn, currentDomain, x509Certificate);
        }

        //update party in the Pmode with the latest discovered values; synchronized
        final String partyName = receiverParty.getValue();
        final String partyType = receiverParty.getType();
        Party configurationParty = updateConfigurationParty(partyName, partyType, partyEndpointInfo.getEndpointUrl());

        //party is added in the responder parties only if it doesn't exist; synchronized
        updateToPartyInPmodeResponderParties(processCandidates, configurationParty);

        //save the final recipient value and URL in the database
        final String finalRecipientValue = userMessageServiceHelper.getFinalRecipientValue(userMessage);
        final String receiverURL = endpointInfo.getAddress();

        final List<String> partyProcessNames = getProcessNames(processCandidates);


        if (CollectionUtils.isNotEmpty(pModeEventListeners)) {
            //we call the PMode event listeners
            pModeEventListeners.stream().forEach(pModeEventListener -> {
                try {
                    LOG.debug("Notifying listener [{}] for event afterDynamicDiscoveryLookup", pModeEventListener.getName());
                    pModeEventListener.afterDynamicDiscoveryLookup(finalRecipientValue, receiverURL, partyName, partyType, partyProcessNames, certificateCn, x509Certificate);
                } catch (Exception e) {
                    LOG.error("Error in PMode event listener [{}]: afterDynamicDiscoveryLookup", pModeEventListener.getName(), e);
                }
            });
        }
    }

    protected PartyEndpointInfo getPartyEndpointInfo(EndpointInfo endpointInfo, String messageId) throws EbMS3Exception {
        final X509Certificate x509Certificate = endpointInfo.getCertificate();
        final String certificateCn = getCommonNameFromCertificate(messageId, x509Certificate);
        return new PartyEndpointInfo(certificateCn, x509Certificate, endpointInfo.getAddress());
    }


    protected PartyId getFromPartyId(UserMessage userMessage) throws EbMS3Exception {
        PartyId from = null;
        String messageId = getMessageId(userMessage);
        if (userMessage != null &&
                userMessage.getPartyInfo() != null &&
                userMessage.getPartyInfo().getFrom() != null) {
            from = userMessage.getPartyInfo().getFrom().getFromPartyId();
        }
        if (from == null) {
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0003)
                    .message("Invalid From party identifier")
                    .refToMessageId(messageId)
                    .build();
        }

        return from;
    }

    protected String getMessageId(UserMessage userMessage) {
        if (userMessage == null) {
            return null;
        }
        return userMessage.getMessageId();
    }

    /**
     * Update party in the Pmode with the latest discovered values
     */
    protected synchronized Party updateConfigurationParty(String name, String partyType, String endpoint) {
        LOG.info("Update the configuration party with [{}] [{}] [{}]", name, partyType, endpoint);
        // get the party type from Pmode; add it if party type doesn't exist
        PartyIdType partyIdType = getOrAddPartyIdTypeInPmode(partyType);

        // search if the party exists in the pMode
        Party configurationParty = null;
        for (final Party party : getConfiguration().getBusinessProcesses().getParties()) {
            if (StringUtils.equalsIgnoreCase(party.getName(), name)) {
                LOG.debug("Party exists in the pmode: " + party.getName());
                configurationParty = party;
                break;
            }
        }

        // remove party if exists to add it with latest values for address and type
        if (configurationParty != null) {
            LOG.debug("Remove party to add with new values " + configurationParty.getName());
            getConfiguration().getBusinessProcesses().removeParty(configurationParty);
        }
        // set the new endpoint if exists, otherwise copy the old one if exists
        String newEndpoint = endpoint;
        if (newEndpoint == null) {
            newEndpoint = MSH_ENDPOINT;
            if (configurationParty != null && configurationParty.getEndpoint() != null) {
                newEndpoint = configurationParty.getEndpoint();
                LOG.debug("Setting the party endpoint from the Pmode [{}]", newEndpoint);
            }
        }

        //add the party in the Pmode

        LOG.debug("New endpoint is [{}]", newEndpoint);
        Party newConfigurationParty = buildNewConfigurationParty(name, partyIdType, newEndpoint);
        LOG.debug("Add new configuration party in Pmode [{}]", newConfigurationParty.getName());
        getConfiguration().getBusinessProcesses().addParty(newConfigurationParty);

        return newConfigurationParty;
    }

    protected Party buildNewConfigurationParty(String name, PartyIdType configurationType, String endpoint) {
        Party newConfigurationParty = new Party();
        final Identifier partyIdentifier = new Identifier();
        partyIdentifier.setPartyId(name);
        partyIdentifier.setPartyIdType(configurationType);

        newConfigurationParty.setName(partyIdentifier.getPartyId());
        newConfigurationParty.getIdentifiers().add(partyIdentifier);
        newConfigurationParty.setEndpoint(endpoint);
        return newConfigurationParty;
    }

    protected PartyIdType getOrAddPartyIdTypeInPmode(String partyType) {
        Set<PartyIdType> partyIdTypes = getConfiguration().getBusinessProcesses().getPartyIdTypes();
        if (partyIdTypes == null) {
            LOG.info("Empty partyIdTypes set");
            partyIdTypes = new HashSet<>();
        }

        PartyIdType configurationType = null;
        for (final PartyIdType t : partyIdTypes) {
            if (StringUtils.equalsIgnoreCase(t.getValue(), partyType)) {
                LOG.debug("PartyIdType exists in the pmode [{}]", partyType);
                configurationType = t;
            }
        }
        // add to partyIdType list
        if (configurationType == null) {
            LOG.debug("Add new PartyIdType [{}]", partyType);
            configurationType = new PartyIdType();
            configurationType.setName(partyType);
            configurationType.setValue(partyType);
            partyIdTypes.add(configurationType);
            this.getConfiguration().getBusinessProcesses().setPartyIdTypes(partyIdTypes);
        }
        return configurationType;
    }

    //party is added in the responder parties only if it doesn't exist
    protected synchronized void updateToPartyInPmodeResponderParties(Collection<eu.domibus.common.model.configuration.Process> candidates, Party configurationParty) {
        LOG.debug("Update Pmode processes->responderParties with party [{}]", configurationParty.getName());

        for (final Process candidate : candidates) {
            final Party responderParty = findResponderPartyInProcess(candidate, configurationParty.getName());
            if (responderParty == null) {
                LOG.info("Adding party [{}] in the process responder parties [{}]", configurationParty.getName(), candidate.getName());
                candidate.getResponderParties().add(configurationParty);
            }
        }
    }

    protected Party findPartyInTheProcessResponderParties(Collection<eu.domibus.common.model.configuration.Process> candidates, String partyName) {
        for (final Process candidate : candidates) {
            final Party responderParty = findResponderPartyInProcess(candidate, partyName);
            if (responderParty != null) {
                LOG.info("Found existing party [{}] in the process responder parties [{}]", partyName, candidate.getName());
                return responderParty;
            }
        }
        return null;
    }

    protected Party findResponderPartyInProcess(eu.domibus.common.model.configuration.Process process, String partyName) {
        for (final Party party : process.getResponderParties()) {
            if (StringUtils.equalsIgnoreCase(partyName, party.getName())) {
                LOG.debug("Party [{}] found in process [{}]", partyName, process.getName());
                return party;
            }
        }

        return null;
    }

    protected synchronized void updateInitiatorPartiesInPmode(Collection<eu.domibus.common.model.configuration.Process> candidates, Party configurationParty) {
        LOG.debug("updateInitiatorPartiesInPmode with party " + configurationParty.getName());
        for (final Process candidate : candidates) {
            boolean partyFound = false;
            for (final Party party : candidate.getInitiatorParties()) {
                if (StringUtils.equalsIgnoreCase(configurationParty.getName(), party.getName())) {
                    partyFound = true;
                    LOG.debug("partyFound in candidate: " + candidate.getName());
                    break;
                }
            }
            if (!partyFound) {
                candidate.getInitiatorParties().add(configurationParty);
            }
        }
    }

    /**
     * Set partyTo in UserMessage
     */
    protected void addPartyToInUserMessage(UserMessage userMessage, PartyId receiverParty) {
        LOG.debug("Adding partyTo in the UserMessage [{}]", receiverParty);

        userMessage.getPartyInfo().getTo().setToPartyId(receiverParty);
        if (userMessage.getPartyInfo().getTo().getToRole() == null) {
            String responderRoleValue = dynamicDiscoveryService.getResponderRole();
            LOG.debug("Adding partyTo role in the UserMessage [{}]", responderRoleValue);
            PartyRole partyRole = partyRoleDictionaryService.findOrCreateRole(responderRoleValue);
            userMessage.getPartyInfo().getTo().setToRole(partyRole);
        }
    }

    /**
     * It creates or gets the receiver party based on the certificate common name
     */
    private PartyId getPartyToIdForDynamicDiscovery(String certificateCn) {
        String type = dynamicDiscoveryService.getPartyIdType();
        LOG.debug("DDC: using configured party type [{}]", type);

        // double check not to add empty value as a type
        // because it is invalid by the oasis messaging  xsd
        if (StringUtils.isEmpty(type)) {
            type = null;
        }

        final PartyId receiverParty = partyIdDictionaryService.findOrCreateParty(certificateCn, type);
        return receiverParty;
    }

    private String getCommonNameFromCertificate(String messageId, X509Certificate certificate) throws EbMS3Exception {
        try {
            //parse certificate for common name = toPartyId
            String cn = certificateService.extractCommonName(certificate);
            LOG.debug("Extracted the common name [{}]", cn);
            return cn;
        } catch (final InvalidNameException e) {
            LOG.error("Error while extracting CommonName from certificate", e);
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0003)
                    .message("Error while extracting CommonName from certificate")
                    .refToMessageId(messageId)
                    .cause(e)
                    .build();
        }
    }

    protected EndpointInfo lookupByFinalRecipient(String lookupCacheKey, UserMessage userMessage) throws EbMS3Exception {
        final String finalRecipientValue = userMessageServiceHelper.getFinalRecipientValue(userMessage);
        final String finalRecipientType = userMessageServiceHelper.getFinalRecipientType(userMessage);

        if (StringUtils.isBlank(finalRecipientValue)) {
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0010)
                    .message("Dynamic discovery processes found for message but finalRecipient information is missing in messageProperties.")
                    .refToMessageId(userMessage.getMessageId())
                    .build();
        }
        LOG.info("Perform lookup by finalRecipient type [{}] and value [{}]", finalRecipientType, finalRecipientValue);

        //lookup sml/smp - result is cached
        final EndpointInfo endpoint = dynamicDiscoveryService.lookupInformation(lookupCacheKey, finalRecipientValue,
                finalRecipientType,
                userMessage.getActionValue(),
                userMessage.getService().getValue(),
                userMessage.getService().getType());

        // The SMP entries missing this info are not for the use of Domibus
        if (endpoint.getAddress() == null || endpoint.getCertificate() == null) {
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0010)
                    .message("Invalid endpoint metadata received from the dynamic discovery process.")
                    .refToMessageId(userMessage.getMessageId())
                    .build();
        }
        LOG.debug("Lookup successful: " + endpoint.getAddress());
        return endpoint;
    }

    /*
     * Check all dynamic processes to find candidates for dynamic discovery lookup.
     */
    protected Collection<eu.domibus.common.model.configuration.Process> findCandidateProcesses(UserMessage userMessage, final MSHRole mshRole) {
        LOG.debug("Finding candidate processes.");
        Collection<eu.domibus.common.model.configuration.Process> candidates = new HashSet<>();
        Collection<eu.domibus.common.model.configuration.Process> processes = getDynamicProcesses(mshRole);

        for (final Process process : processes) {
            if (matchProcess(process, mshRole)) {
                LOG.debug("Process matched: [{}] [{}]", process.getName(), mshRole);
                for (final LegConfiguration legConfiguration : process.getLegs()) {
                    if (StringUtils.equalsIgnoreCase(legConfiguration.getService().getValue(), userMessage.getService().getValue()) &&
                            StringUtils.equalsIgnoreCase(legConfiguration.getAction().getValue(), userMessage.getActionValue())) {
                        LOG.debug("Leg matched, adding process. Leg: " + legConfiguration.getName());
                        candidates.add(process);
                    }
                }
            }
        }

        return candidates;
    }

    /*
     * On the receiving, the initiator is unknown, on the sending side the responder is unknown.
     */
    protected boolean matchProcess(final Process process, MSHRole mshRole) {
        if (MSHRole.RECEIVING.equals(mshRole)) {
            return process.isDynamicInitiator() || process.getInitiatorParties().contains(this.getConfiguration().getParty());
        } else { // MSHRole.SENDING
            return process.isDynamicResponder() || process.getResponderParties().contains(this.getConfiguration().getParty());
        }
    }
}
