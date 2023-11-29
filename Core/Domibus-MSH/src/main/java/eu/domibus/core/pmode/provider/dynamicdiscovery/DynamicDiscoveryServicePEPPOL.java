package eu.domibus.core.pmode.provider.dynamicdiscovery;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.pki.MultiDomainCryptoService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.SecurityProfile;
import eu.domibus.api.security.X509CertificateService;
import eu.domibus.common.DomibusCacheConstants;
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
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static eu.domibus.api.cache.DomibusLocalCacheService.DYNAMIC_DISCOVERY_ENDPOINT;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

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
public class DynamicDiscoveryServicePEPPOL extends AbstractDynamicDiscoveryService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DynamicDiscoveryServicePEPPOL.class);

    protected static final Map<SecurityProfile, String> SECURITY_PROFILE_TRANSPORT_PROFILE_MAP = new HashMap<>();

    static {
        SECURITY_PROFILE_TRANSPORT_PROFILE_MAP.put(SecurityProfile.RSA, "peppol-transport-as4-v2_0");
    }

    public static final String SCHEME_DELIMITER = "::";

    private final DomibusPropertyProvider domibusPropertyProvider;

    private final MultiDomainCryptoService multiDomainCertificateProvider;

    private final DomainContextProvider domainProvider;

    private final ProxyUtil proxyUtil;

    private final CertificateService certificateService;

    private final DomibusHttpRoutePlanner domibusHttpRoutePlanner;

    private X509CertificateService x509CertificateService;
    private final ObjectProvider<DomibusCertificateValidator> domibusCertificateValidators;

    private final ObjectProvider<DomibusBusdoxLocator> busdoxLocators;

    private final ObjectProvider<DomibusApacheFetcher> domibusApacheFetchers;

    private final ObjectProvider<EndpointInfo> endpointInfos;

    private final DynamicDiscoveryUtil dynamicDiscoveryUtil;

    public DynamicDiscoveryServicePEPPOL(DomibusPropertyProvider domibusPropertyProvider,
                                         MultiDomainCryptoService multiDomainCertificateProvider,
                                         DomainContextProvider domainProvider,
                                         ProxyUtil proxyUtil,
                                         CertificateService certificateService,
                                         DomibusHttpRoutePlanner domibusHttpRoutePlanner,
                                         X509CertificateService x509CertificateService,
                                         ObjectProvider<DomibusCertificateValidator> domibusCertificateValidators,
                                         ObjectProvider<DomibusBusdoxLocator> busdoxLocators,
                                         ObjectProvider<DomibusApacheFetcher> domibusApacheFetchers,
                                         ObjectProvider<EndpointInfo> endpointInfos,
                                         DynamicDiscoveryUtil dynamicDiscoveryUtil) {
        this.domibusPropertyProvider = domibusPropertyProvider;

        this.multiDomainCertificateProvider = multiDomainCertificateProvider;
        this.domainProvider = domainProvider;
        this.proxyUtil = proxyUtil;
        this.certificateService = certificateService;
        this.domibusHttpRoutePlanner = domibusHttpRoutePlanner;
        this.x509CertificateService = x509CertificateService;
        this.domibusCertificateValidators = domibusCertificateValidators;
        this.busdoxLocators = busdoxLocators;
        this.domibusApacheFetchers = domibusApacheFetchers;
        this.endpointInfos = endpointInfos;
        this.dynamicDiscoveryUtil = dynamicDiscoveryUtil;
    }

    protected DomibusLogger getLogger() {
        return LOG;
    }

    @Override
    protected DynamicDiscoveryUtil getDynamicDiscoveryUtil() {
        return dynamicDiscoveryUtil;
    }

    protected String getPartyIdTypePropertyName() {
        return DOMIBUS_DYNAMICDISCOVERY_PEPPOLCLIENT_PARTYID_TYPE;
    }

    protected String getPartyIdResponderRolePropertyName() {
        return DOMIBUS_DYNAMICDISCOVERY_PEPPOLCLIENT_PARTYID_RESPONDER_ROLE;
    }

    @Cacheable(cacheManager = DomibusCacheConstants.CACHE_MANAGER, value = DYNAMIC_DISCOVERY_ENDPOINT, key = "#lookupKey")
    public EndpointInfo lookupInformation(final String lookupKey, final String finalRecipientValue, final String finalRecipientType, final String documentId, final String processId, final String processIdScheme) {

        LOG.info("[PEPPOL SMP] Do the lookup by: [{}] [{}] [{}] [{}] [{}]", finalRecipientValue, finalRecipientType, documentId, processId, processIdScheme);
        final String smlInfo = domibusPropertyProvider.getProperty(DOMIBUS_SMLZONE);
        if (StringUtils.isBlank(smlInfo)) {
            throw new ConfigurationException("SML Zone missing. Configure property [" + DOMIBUS_SMLZONE + "] in domibus configuration!");
        }
        String mode = domibusPropertyProvider.getProperty(DOMIBUS_DYNAMICDISCOVERY_PEPPOLCLIENT_MODE);
        if (StringUtils.isBlank(mode)) {
            mode = Mode.TEST;
        }

        final String certRegex = domibusPropertyProvider.getProperty(DOMIBUS_DYNAMICDISCOVERY_PEPPOLCLIENT_REGEX_CERTIFICATE_SUBJECT_VALIDATION);
        if (StringUtils.isBlank(certRegex)) {
            LOG.info("The value for property [{}] is empty.", DOMIBUS_DYNAMICDISCOVERY_PEPPOLCLIENT_REGEX_CERTIFICATE_SUBJECT_VALIDATION);
        }

        List<String> allowedCertificatePolicyIDs = getAllowedSMPCertificatePolicyOIDs();

        LOG.debug("Load truststore for the smpClient");
        KeyStore trustStore = multiDomainCertificateProvider.getTrustStore(domainProvider.getCurrentDomain());

        try {
            // create certificate validator
            DomibusCertificateValidator domibusSMPCertificateValidator = domibusCertificateValidators.getObject(certificateService, trustStore, certRegex, allowedCertificatePolicyIDs);

            LOG.debug("Getting the ServiceMetadata");
            final ServiceMetadata sm = getServiceMetadata(finalRecipientValue, finalRecipientType, documentId, smlInfo, mode, domibusSMPCertificateValidator);

            String transportProfileAS4 = domibusPropertyProvider.getProperty(DOMIBUS_DYNAMICDISCOVERY_TRANSPORTPROFILEAS_4);
            LOG.debug("Get the Endpoint from ServiceMetadata with transport profile [{}]", transportProfileAS4);
            final Endpoint endpoint = getEndpoint(sm.getServiceInformation().getProcesses(), getProcessIdentifier(processId), TransportProfile.of(transportProfileAS4));

            if (endpoint == null || endpoint.getAddress() == null) {
                throw new ConfigurationException("Received incomplete metadata from the SMP for documentId " + documentId + " processId " + processId);
            }

            X509Certificate certificate = endpoint.getCertificate();
            x509CertificateService.validateClientX509Certificates(certificate);
            return endpointInfos.getObject(endpoint.getAddress().toString(), certificate);
        } catch (final PeppolParsingException | PeppolLoadingException | PeppolSecurityException | LookupException |
                       EndpointNotFoundException | IllegalStateException e) {
            String msg = "Could not fetch metadata from SMP for documentId " + documentId + " processId " + processId;
            // log error, because cause in ConfigurationException is consumed..
            LOG.error(msg, e);
            throw new ConfigurationException(msg, e);
        }
    }

    protected ServiceMetadata getServiceMetadata(String participantId, String participantIdScheme, String documentId, String smlInfo, String mode, DomibusCertificateValidator domibusSMPCertificateValidator) throws PeppolLoadingException, LookupException, PeppolSecurityException, PeppolParsingException {
        final LookupClientBuilder lookupClientBuilder = LookupClientBuilder.forMode(mode);
        lookupClientBuilder.locator(busdoxLocators.getObject(smlInfo));
        lookupClientBuilder.fetcher(domibusApacheFetchers.getObject(Mode.of(mode), proxyUtil, domibusHttpRoutePlanner));
        lookupClientBuilder.certificateValidator(domibusSMPCertificateValidator);
        final LookupClient smpClient = lookupClientBuilder.build();


        return smpClient.getServiceMetadata(
                ParticipantIdentifier.of(participantId, Scheme.of(participantIdScheme)),
                getDocumentTypeIdentifier(documentId));
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
