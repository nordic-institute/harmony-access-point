package eu.domibus.core.pmode.provider.dynamicdiscovery;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.pki.MultiDomainCryptoService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.core.proxy.ProxyUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import network.oxalis.vefa.peppol.common.lang.EndpointNotFoundException;
import network.oxalis.vefa.peppol.common.lang.PeppolLoadingException;
import network.oxalis.vefa.peppol.common.lang.PeppolParsingException;
import network.oxalis.vefa.peppol.common.model.*;
import network.oxalis.vefa.peppol.lookup.LookupClient;
import network.oxalis.vefa.peppol.lookup.LookupClientBuilder;
import network.oxalis.vefa.peppol.lookup.api.LookupException;
import network.oxalis.vefa.peppol.lookup.locator.BusdoxLocator;
import network.oxalis.vefa.peppol.mode.Mode;
import network.oxalis.vefa.peppol.security.lang.PeppolSecurityException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.security.KeyStore;
import java.util.List;

import static eu.domibus.core.cache.DomibusCacheService.DYNAMIC_DISCOVERY_ENDPOINT;
import static org.apache.commons.lang3.StringUtils.trim;

/**
 * Service to query the SMP to extract the required information about the unknown receiver AP.
 * The SMP Lookup is done using an SMP Client software, with the following input:
 * The End Receiver Participant ID (C4)
 * The Document ID
 * The Process ID
 * <p>
 * Upon a successful lookup, the result contains the endpoint address and also public certificate of the receiver.
 */
@Service
@Qualifier("dynamicDiscoveryServicePEPPOL")
public class DynamicDiscoveryServicePEPPOL extends AbstractDynamicDiscoveryService implements DynamicDiscoveryService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DynamicDiscoveryServicePEPPOL.class);

    private static final String DEFAULT_RESPONDER_ROLE = "urn:fdc:peppol.eu:2017:roles:ap:as4";

    private static final String DEFAULT_PARTY_TYPE = "urn:fdc:peppol.eu:2017:identifiers:ap";

    public static final String SCHEME_DELIMITER = "::";

    private final DomibusPropertyProvider domibusPropertyProvider;

    private final MultiDomainCryptoService multiDomainCertificateProvider;

    private final DomainContextProvider domainProvider;

    private final ProxyUtil proxyUtil;

    private final CertificateService certificateService;

    private final DomibusHttpRoutePlanner domibusHttpRoutePlanner;

    private final ObjectProvider<DomibusCertificateValidator> domibusCertificateValidators;

    private final ObjectProvider<BusdoxLocator> busdoxLocators;

    private final ObjectProvider<DomibusApacheFetcher> domibusApacheFetchers;

    private final ObjectProvider<EndpointInfo> endpointInfos;

    public DynamicDiscoveryServicePEPPOL(DomibusPropertyProvider domibusPropertyProvider,
                                         MultiDomainCryptoService multiDomainCertificateProvider,
                                         DomainContextProvider domainProvider,
                                         ProxyUtil proxyUtil,
                                         CertificateService certificateService,
                                         DomibusHttpRoutePlanner domibusHttpRoutePlanner,
                                         ObjectProvider<DomibusCertificateValidator> domibusCertificateValidators,
                                         ObjectProvider<BusdoxLocator> busdoxLocators,
                                         ObjectProvider<DomibusApacheFetcher> domibusApacheFetchers,
                                         ObjectProvider<EndpointInfo> endpointInfos) {
        this.domibusPropertyProvider = domibusPropertyProvider;

        this.multiDomainCertificateProvider = multiDomainCertificateProvider;
        this.domainProvider = domainProvider;
        this.proxyUtil = proxyUtil;
        this.certificateService = certificateService;
        this.domibusHttpRoutePlanner = domibusHttpRoutePlanner;
        this.domibusCertificateValidators = domibusCertificateValidators;
        this.busdoxLocators = busdoxLocators;
        this.domibusApacheFetchers = domibusApacheFetchers;
        this.endpointInfos = endpointInfos;
    }

    protected DomibusLogger getLogger() {
        return LOG;
    }

    protected String getTrimmedDomibusProperty(String propertyName) {
        return trim(domibusPropertyProvider.getProperty(propertyName));
    }

    protected String getDefaultDiscoveryPartyIdType() {
        return DEFAULT_PARTY_TYPE;
    }

    protected String getDefaultResponderRole() {
        return DEFAULT_RESPONDER_ROLE;
    }

    @Cacheable(value = DYNAMIC_DISCOVERY_ENDPOINT, key = "#domain + #participantId + #participantIdScheme + #documentId + #processId + #processIdScheme")
    public EndpointInfo lookupInformation(final String domain, final String participantId, final String participantIdScheme, final String documentId, final String processId, final String processIdScheme) {

        LOG.info("[PEPPOL SMP] Do the lookup by: [{}] [{}] [{}] [{}] [{}]", participantId, participantIdScheme, documentId, processId, processIdScheme);
        final String smlInfo = domibusPropertyProvider.getProperty(SMLZONE_KEY);
        if (StringUtils.isBlank(smlInfo)) {
            throw new ConfigurationException("SML Zone missing. Configure property [" + SMLZONE_KEY + "] in domibus configuration!");
        }
        String mode = domibusPropertyProvider.getProperty(DYNAMIC_DISCOVERY_MODE);
        if (StringUtils.isBlank(mode)) {
            mode = Mode.TEST;
        }

        final String certRegex = domibusPropertyProvider.getProperty(DYNAMIC_DISCOVERY_CERT_PEPPOL_REGEX);
        if (StringUtils.isBlank(certRegex)) {
            LOG.warn("The value for property [{}] is empty.", DYNAMIC_DISCOVERY_CERT_PEPPOL_REGEX);
        }

        List<String> allowedCertificatePolicyIDs = getAllowedSMPCertificatePolicyOIDs();

        LOG.debug("Load truststore for the smpClient");
        KeyStore trustStore = multiDomainCertificateProvider.getTrustStore(domainProvider.getCurrentDomain());

        try {
            // create certificate validator
            DomibusCertificateValidator domibusSMPCertificateValidator = domibusCertificateValidators.getObject(certificateService, trustStore, certRegex, allowedCertificatePolicyIDs);

            final LookupClientBuilder lookupClientBuilder = LookupClientBuilder.forMode(mode);
            lookupClientBuilder.locator(busdoxLocators.getObject(smlInfo));
            lookupClientBuilder.fetcher(domibusApacheFetchers.getObject(Mode.of(mode), proxyUtil, domibusHttpRoutePlanner));
            lookupClientBuilder.certificateValidator(domibusSMPCertificateValidator);
            final LookupClient smpClient = lookupClientBuilder.build();
            final ParticipantIdentifier participantIdentifier = ParticipantIdentifier.of(participantId, Scheme.of(participantIdScheme));
            final DocumentTypeIdentifier documentIdentifier = getDocumentTypeIdentifier(documentId);

            final ProcessIdentifier processIdentifier = getProcessIdentifier(processId);
            LOG.debug("Getting the ServiceMetadata");
            final ServiceMetadata sm = smpClient.getServiceMetadata(participantIdentifier, documentIdentifier);


            String transportProfileAS4 = domibusPropertyProvider.getProperty(DYNAMIC_DISCOVERY_TRANSPORTPROFILEAS4);
            LOG.debug("Get the Endpoint from ServiceMetadata with transport profile [{}]", transportProfileAS4);
            final Endpoint endpoint = getEndpoint(sm.getServiceInformation().getProcesses(),  processIdentifier, TransportProfile.of(transportProfileAS4));

            if (endpoint == null || endpoint.getAddress() == null) {
                throw new ConfigurationException("Received incomplete metadata from the SMP for documentId " + documentId + " processId " + processId);
            }

            return endpointInfos.getObject(endpoint.getAddress().toString(), endpoint.getCertificate());
        } catch (final PeppolParsingException | PeppolLoadingException | PeppolSecurityException | LookupException | EndpointNotFoundException | IllegalStateException e) {
            String msg = "Could not fetch metadata from SMP for documentId " + documentId + " processId " + processId;
            // log error, because cause in ConfigurationException is consumed..
            LOG.error(msg, e);
            throw new ConfigurationException(msg, e);
        }
    }

    /**
     * Parse document identifier string and return DocumentTypeIdentifier entity.
     *
     * @param documentId
     * @return
     * @throws PeppolParsingException
     */
    protected DocumentTypeIdentifier getDocumentTypeIdentifier(String documentId) throws PeppolParsingException {
        DocumentTypeIdentifier result;
        if (StringUtils.contains(documentId, DocumentTypeIdentifier.DEFAULT_SCHEME.getIdentifier())) {
            getLogger().debug("Getting DocumentTypeIdentifier by parsing the document Id [{}]", documentId);
            result = DocumentTypeIdentifier.parse(documentId);
        } else {
            getLogger().debug("Getting DocumentTypeIdentifier for the document Id [{}]", documentId);
            result = DocumentTypeIdentifier.of(documentId);
        }
        return result;
    }

    /**
     * Parse peppol process identifier string and return ProcessIdentifier entity
     *
     * @param processId string representation of the process identifier
     * @return ProcessIdentifier entity
     * @throws PeppolParsingException
     */
    protected ProcessIdentifier getProcessIdentifier(String processId) throws PeppolParsingException {
        ProcessIdentifier result;
        if (StringUtils.contains(processId, DynamicDiscoveryServicePEPPOL.SCHEME_DELIMITER)) {
            getLogger().debug("Getting ProcessIdentifier by parsing the process Id [{}]", processId);
            result = ProcessIdentifier.parse(processId);
        } else {
            getLogger().debug("Getting ProcessIdentifier for process Id [{}]", processId);
            result = ProcessIdentifier.of(processId);
        }
        return result;
    }

    /**
     * Return valid endpoint from processes for process identifier and  transport profiles
     *
     * @param processes         - list of processes
     * @param processIdentifier target process identifier
     * @param transportProfile  list of targeted transport profiles
     * @return valid endpoint
     * @throws EndpointNotFoundException
     */
    public Endpoint getEndpoint(List<ProcessMetadata<Endpoint>> processes, ProcessIdentifier processIdentifier, TransportProfile transportProfile)
            throws EndpointNotFoundException {
        LOG.debug("Search for a valid Endpoint for process  id: [{}] and TransportProfile: [{}]]!", processIdentifier, transportProfile);

        for (ProcessMetadata<Endpoint> processMetadata : processes)
            if (processMetadata.getProcessIdentifier().contains(processIdentifier)) {
                Endpoint endpoint = processMetadata.getEndpoint(transportProfile);
                if (endpoint.getPeriod() == null || isValidEndpoint(endpoint.getPeriod().getFrom(), endpoint.getPeriod().getTo())) {
                    return processMetadata.getEndpoint(transportProfile);
                }
            }

        String msg = "Could not found valid Endpoint for process id: [" + processIdentifier + "]  and TransportProfile: [" + transportProfile + "]!";
        LOG.error(msg);
        throw new ConfigurationException(msg);
    }

}