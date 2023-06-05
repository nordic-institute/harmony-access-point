package eu.domibus.core.pmode.provider.dynamicdiscovery;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.pki.DomibusCertificateException;
import eu.domibus.api.pki.MultiDomainCryptoService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.proxy.DomibusProxy;
import eu.domibus.api.proxy.DomibusProxyService;
import eu.domibus.api.security.SecurityProfile;
import eu.domibus.api.security.X509CertificateService;
import eu.domibus.common.DomibusCacheConstants;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.europa.ec.dynamicdiscovery.DynamicDiscovery;
import eu.europa.ec.dynamicdiscovery.DynamicDiscoveryBuilder;
import eu.europa.ec.dynamicdiscovery.core.fetcher.impl.DefaultURLFetcher;
import eu.europa.ec.dynamicdiscovery.core.locator.impl.DefaultBDXRLocator;
import eu.europa.ec.dynamicdiscovery.core.reader.impl.DefaultBDXRReader;
import eu.europa.ec.dynamicdiscovery.core.security.impl.DefaultProxy;
import eu.europa.ec.dynamicdiscovery.core.security.impl.DefaultSignatureValidator;
import eu.europa.ec.dynamicdiscovery.exception.ConnectionException;
import eu.europa.ec.dynamicdiscovery.exception.TechnicalException;
import eu.europa.ec.dynamicdiscovery.model.DocumentIdentifier;
import eu.europa.ec.dynamicdiscovery.model.ParticipantIdentifier;
import eu.europa.ec.dynamicdiscovery.model.ServiceMetadata;
import org.apache.commons.lang3.StringUtils;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.EndpointType;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.ProcessType;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static eu.domibus.api.cache.DomibusLocalCacheService.DYNAMIC_DISCOVERY_ENDPOINT;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;
import static org.apache.commons.lang3.StringUtils.trim;

/**
 * Service to query a compliant eDelivery SMP profile based on the OASIS BDX Service Metadata Publishers
 * (SMP) to extract the required information about the unknown receiver AP.
 * The SMP Lookup is done using an SMP Client software, with the following input:
 * The End Receiver Participant ID (C4)
 * The Document ID
 * The Process ID
 * <p>
 * Upon a successful lookup, the result contains the endpoint address and also the public
 * certificate of the receiver.
 */
@Service
@Qualifier("dynamicDiscoveryServiceOASIS")
public class DynamicDiscoveryServiceOASIS extends AbstractDynamicDiscoveryService implements DynamicDiscoveryService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DynamicDiscoveryServiceOASIS.class);

    protected static final Map<SecurityProfile, String> SECURITY_PROFILE_TRANSPORT_PROFILE_MAP = new HashMap<>();

    static {
        SECURITY_PROFILE_TRANSPORT_PROFILE_MAP.put(SecurityProfile.RSA, "bdxr-transport-ebms3-as4-v1p0");
        SECURITY_PROFILE_TRANSPORT_PROFILE_MAP.put(SecurityProfile.ECC, "bdxr-transport-ebms3-as4-EC-sample");
    }

    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("^(?<scheme>.+?)::(?<value>.+)$");

    private final DomibusPropertyProvider domibusPropertyProvider;

    private final DomainContextProvider domainProvider;

    private final MultiDomainCryptoService multiDomainCertificateProvider;

    private final CertificateService certificateService;

    private final DomibusProxyService domibusProxyService;

    private final DomibusHttpRoutePlanner domibusHttpRoutePlanner;

    private X509CertificateService x509CertificateService;

    private final ObjectProvider<DocumentIdentifier> documentIdentifiers;

    private final ObjectProvider<ParticipantIdentifier> participantIdentifiers;

    private final ObjectProvider<DefaultProxy> proxies;

    private final ObjectProvider<DefaultBDXRLocator> bdxrLocators;

    private final ObjectProvider<DomibusCertificateValidator> domibusCertificateValidators;

    private final ObjectProvider<DefaultURLFetcher> urlFetchers;

    private final ObjectProvider<DefaultBDXRReader> bdxrReaders;

    private final ObjectProvider<DefaultSignatureValidator> signatureValidators;

    private final ObjectProvider<EndpointInfo> endpointInfos;

    private final DynamicDiscoveryUtil dynamicDiscoveryUtil;

    public DynamicDiscoveryServiceOASIS(DomibusPropertyProvider domibusPropertyProvider,
                                        DomainContextProvider domainProvider,
                                        MultiDomainCryptoService multiDomainCertificateProvider,
                                        CertificateService certificateService,
                                        DomibusProxyService domibusProxyService,
                                        DomibusHttpRoutePlanner domibusHttpRoutePlanner,
                                        X509CertificateService x509CertificateService,
                                        ObjectProvider<DocumentIdentifier> documentIdentifiers,
                                        ObjectProvider<ParticipantIdentifier> participantIdentifiers,
                                        ObjectProvider<DefaultProxy> proxies,
                                        ObjectProvider<DefaultBDXRLocator> bdxrLocators,
                                        ObjectProvider<DomibusCertificateValidator> domibusCertificateValidators,
                                        ObjectProvider<DefaultURLFetcher> urlFetchers,
                                        ObjectProvider<DefaultBDXRReader> bdxrReaders,
                                        ObjectProvider<DefaultSignatureValidator> signatureValidators,
                                        ObjectProvider<EndpointInfo> endpointInfos,
                                        DynamicDiscoveryUtil dynamicDiscoveryUtil) {
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.domainProvider = domainProvider;
        this.multiDomainCertificateProvider = multiDomainCertificateProvider;
        this.certificateService = certificateService;
        this.domibusProxyService = domibusProxyService;
        this.domibusHttpRoutePlanner = domibusHttpRoutePlanner;
        this.x509CertificateService = x509CertificateService;
        this.documentIdentifiers = documentIdentifiers;
        this.participantIdentifiers = participantIdentifiers;
        this.proxies = proxies;
        this.bdxrLocators = bdxrLocators;
        this.domibusCertificateValidators = domibusCertificateValidators;
        this.urlFetchers = urlFetchers;
        this.bdxrReaders = bdxrReaders;
        this.signatureValidators = signatureValidators;
        this.endpointInfos = endpointInfos;
        this.dynamicDiscoveryUtil = dynamicDiscoveryUtil;
    }


    @Override
    protected DomibusLogger getLogger() {
        return LOG;
    }

    @Override
    protected DynamicDiscoveryUtil getDynamicDiscoveryUtil() {
        return dynamicDiscoveryUtil;
    }

    @Override
    protected String getPartyIdTypePropertyName() {
        return DOMIBUS_DYNAMICDISCOVERY_OASISCLIENT_PARTYID_TYPE;
    }

    @Override
    protected String getPartyIdResponderRolePropertyName() {
        return DOMIBUS_DYNAMICDISCOVERY_OASISCLIENT_PARTYID_RESPONDER_ROLE;
    }

    @Cacheable(cacheManager = DomibusCacheConstants.CACHE_MANAGER, value = DYNAMIC_DISCOVERY_ENDPOINT, key = "#domain + #participantId + #participantIdScheme + #documentId + #processId + #processIdScheme")
    public EndpointInfo lookupInformation(final String domain,
                                          final String participantId,
                                          final String participantIdScheme,
                                          final String documentId,
                                          final String processId,
                                          final String processIdScheme) throws EbMS3Exception {

        LOG.info("[OASIS SMP] Do the lookup by: [{}] [{}] [{}] [{}] [{}]", participantId,
                participantIdScheme,
                documentId,
                processId,
                processIdScheme);

        try {
            ServiceMetadata serviceMetadata = getServiceMetadata(participantId, participantIdScheme, documentId, processId, processIdScheme);

            LOG.debug("ServiceMetadata Response: [{}]" + serviceMetadata.getResponseBody());

            List<ProcessType> processes = serviceMetadata.getOriginalServiceMetadata().getServiceMetadata().getServiceInformation().getProcessList().getProcess();

            List<String> transportProfiles = dynamicDiscoveryUtil.retrieveTransportProfilesFromProcesses(processes);

            //retrieve the transport profile available for the highest ranking Security Profile
            String transportProfile = dynamicDiscoveryUtil.getAvailableTransportProfileForHighestRankingSecurityProfile(transportProfiles, SECURITY_PROFILE_TRANSPORT_PROFILE_MAP);
            LOG.debug("Get the endpoint for [{}]", transportProfile);

            final EndpointType endpoint = getEndpoint(processes, processId, processIdScheme, transportProfile);
            LOG.debug("Endpoint for transport profile [{}] -  [{}]", transportProfile, endpoint);
            if (endpoint == null || endpoint.getEndpointURI() == null) {
                throw new ConfigurationException("Could not fetch metadata for: " + participantId + " " + participantIdScheme + " " + documentId +
                        " " + processId + " " + processIdScheme + " using the AS4 Protocol " + transportProfile);
            }

            X509Certificate certificate = getCertificateFromEndpoint(endpoint, documentId, processId);
            x509CertificateService.validateClientX509Certificates(certificate);
            return endpointInfos.getObject(endpoint.getEndpointURI(), certificate);

        } catch (TechnicalException exc) {
            String msg = "Could not fetch metadata from SMP for documentId " + documentId + " processId " + processId;
            // log error, because cause in ConfigurationException is consumed..
            LOG.error(msg, exc);
            throw new ConfigurationException(msg, exc);
        }
    }

    protected ServiceMetadata getServiceMetadata(String participantId, String participantIdScheme, String documentId, String processId, String processIdScheme) throws EbMS3Exception, TechnicalException {
        DynamicDiscovery smpClient = createDynamicDiscoveryClient();

        LOG.debug("Getting Request details for ServiceMetadata");
        final ParticipantIdentifier participantIdentifier = participantIdentifiers.getObject(participantId, participantIdScheme);
        final DocumentIdentifier documentIdentifier = createDocumentIdentifier(documentId);
        LOG.debug("ServiceMetadata request contains Participant Identifier [{}] and scheme [{}], Document Identifier [{}] and scheme [{}], Process Identifier [{}] and scheme [{}]",
                participantIdentifier.getIdentifier(),
                participantIdentifier.getScheme(),
                documentIdentifier.getIdentifier(),
                documentIdentifier.getScheme(),
                processId,
                processIdScheme);
        return smpClient.getServiceMetadata(participantIdentifier, documentIdentifier);
    }

    protected X509Certificate getCertificateFromEndpoint(EndpointType endpoint, String documentId, String processId) {
        try {
            return certificateService.loadCertificate(endpoint.getCertificate());
        } catch (DomibusCertificateException exc) {
            String msg = "Invalid endpoint metadata for documentId " + documentId + " processId " + processId;
            // log error, because cause in ConfigurationException is consumed..
            LOG.error(msg, exc);
            throw new ConfigurationException(msg, exc);
        }
    }

    protected DynamicDiscovery createDynamicDiscoveryClient() {
        final String smlInfo = domibusPropertyProvider.getProperty(DOMIBUS_SMLZONE);
        if (StringUtils.isBlank(smlInfo)) {
            throw new ConfigurationException("SML Zone missing. Configure property [" + DOMIBUS_SMLZONE + "] in domibus configuration!");
        }

        final String certRegex = domibusPropertyProvider.getProperty(DOMIBUS_DYNAMICDISCOVERY_OASISCLIENT_REGEX_CERTIFICATE_SUBJECT_VALIDATION);
        if (StringUtils.isBlank(certRegex)) {
            LOG.debug("The value for property [{}] is empty.", DOMIBUS_DYNAMICDISCOVERY_OASISCLIENT_REGEX_CERTIFICATE_SUBJECT_VALIDATION);
        }

        final List<String> allowedCertificatePolicyIDs = getAllowedSMPCertificatePolicyOIDs();

        LOG.debug("Load truststore for the smpClient");
        KeyStore trustStore = multiDomainCertificateProvider.getTrustStore(domainProvider.getCurrentDomain());
        try {
            DefaultProxy defaultProxy = getConfiguredProxy();
            DomibusCertificateValidator domibusSMPCertificateValidator = domibusCertificateValidators.getObject(certificateService, trustStore, certRegex, allowedCertificatePolicyIDs);

            LOG.debug("Creating SMP client [{}] proxy.", (defaultProxy != null ? "with" : "without"));
            return DynamicDiscoveryBuilder.newInstance()
                    .fetcher(urlFetchers.getObject(domibusHttpRoutePlanner, defaultProxy))
                    .locator(bdxrLocators.getObject(smlInfo))
                    .reader(bdxrReaders.getObject(signatureValidators.getObject(domibusSMPCertificateValidator)))
                    .build();
        } catch (TechnicalException exc) {
            throw new ConfigurationException("Could not create smp client to fetch metadata from SMP", exc);
        }
    }

    protected DocumentIdentifier createDocumentIdentifier(String documentId) {
        try {
            String scheme = extract(documentId, "scheme");
            String value = extract(documentId, "value");
            return documentIdentifiers.getObject(value, scheme);
        } catch (IllegalStateException ise) {
            LOG.debug("Could not extract @scheme and @value from [{}], DocumentIdentifier will be created with empty scheme", documentId, ise);
            return documentIdentifiers.getObject(documentId, "");
        }
    }

    protected String extract(String doubleColonDelimitedId, String groupName) {
        Matcher m = IDENTIFIER_PATTERN.matcher(doubleColonDelimitedId);
        m.matches();
        return m.group(groupName);
    }

    protected DefaultProxy getConfiguredProxy() throws ConnectionException {
        if (!domibusProxyService.useProxy()) {
            return null;
        }
        DomibusProxy domibusProxy = domibusProxyService.getDomibusProxy();
        if (StringUtils.isBlank(domibusProxy.getHttpProxyUser())) {
            LOG.debug("Creating a proxy without credentials using the following details: [{}]", domibusProxy);
            return proxies.getObject(domibusProxy.getHttpProxyHost(), domibusProxy.getHttpProxyPort(), null, null, domibusProxy.getNonProxyHosts());
        }
        LOG.debug("Creating a proxy with credentials using the following details: [{}]", domibusProxy);
        return proxies.getObject(domibusProxy.getHttpProxyHost(), domibusProxy.getHttpProxyPort(), domibusProxy.getHttpProxyUser(), domibusProxy.getHttpProxyPassword(), domibusProxy.getNonProxyHosts());
    }

    /**
     * Return valid endpoint from processes for process identifier and  transport profiles
     *
     * @param processes        - list of processes
     * @param processId        target process identifier
     * @param processIdScheme  target process identifier scheme
     * @param transportProfile list of targeted transport profiles
     * @return valid endpoint
     * @throws ConfigurationException
     */
    public EndpointType getEndpoint(List<ProcessType> processes, String processId, String processIdScheme, String transportProfile) {

        if (StringUtils.isBlank(transportProfile)) {
            throw new ConfigurationException("Unable to find endpoint information: transport profile not found or empty");
        }

        if (StringUtils.isBlank(processId)) {
            throw new ConfigurationException("Unable to find endpoint information for null process identifier!");
        }

        LOG.debug("Search for a valid Endpoint for process  id: [{}], process scheme [{}] and TransportProfile: [{}]]!", processId, processIdScheme, transportProfile);
        List<ProcessType> filteredProcesses = processes.stream()
                .filter(this::isValidProcess)
                .filter(processType -> isValidProcessIdentifier(processType, processId, processIdScheme))
                .collect(Collectors.toList());
        LOG.debug("Got [{}] processes for processes with  id: [{}] and scheme [{}] !", filteredProcesses.size(), processId, processIdScheme);

        return filteredProcesses.stream().map(process -> process.getServiceEndpointList().getEndpoint().stream()
                .filter(endpointType -> isValidEndpointTransport(endpointType, transportProfile))
                .filter(this::isValidEndpoint)
                .findFirst()).filter(Optional::isPresent).findFirst().map(Optional::get).orElse(null);
    }

    /**
     * This method exists to be used to filter invalid ProcessTypes from list of ProcessType. Methods validates if process
     * has defined endpoints.
     *
     * @param processType
     * @return true if endpoint's is valid
     */
    protected boolean isValidProcess(ProcessType processType) {
        boolean emptyList = processType.getServiceEndpointList() == null || processType.getServiceEndpointList().getEndpoint().isEmpty();
        if (emptyList) {
            LOG.warn("Found process id: [{}] and scheme [{}] with empty endpoint list!", processType.getProcessIdentifier().getValue(), processType.getProcessIdentifier().getScheme());
        }
        return !emptyList;

    }

    /**
     * This method exists to be used to filter out ProcessTypes which do not match searched criteria
     *
     * @param processType
     * @param filterProcessId
     * @param filterProcessIdScheme
     * @return true if endpoint's is valid
     */
    protected boolean isValidProcessIdentifier(ProcessType processType, String filterProcessId, String filterProcessIdScheme) {
        boolean match = StringUtils.equals(processType.getProcessIdentifier().getValue(), filterProcessId)
                && StringUtils.equals(processType.getProcessIdentifier().getScheme(), filterProcessIdScheme);

        LOG.debug("Search for process id [{}] with scheme [{}], found: [{}] with scheme [{}] which match [{}] to the search parameters!",
                filterProcessId,
                filterProcessIdScheme,
                processType.getProcessIdentifier().getValue(),
                processType.getProcessIdentifier().getScheme(),
                match);

        return match;
    }

    /**
     * This method exists to be used to filter invalid EndpointTypes from list of endpointType.
     *
     * @param endpointType
     * @return true if endpoint's is valid
     */
    protected boolean isValidEndpoint(EndpointType endpointType) {
        return isValidEndpoint(endpointType.getServiceActivationDate() == null ? null : endpointType.getServiceActivationDate().getTime(),
                endpointType.getServiceExpirationDate() == null ? null : endpointType.getServiceExpirationDate().getTime());

    }

    /**
     * This method exists to be used to filter list of endpointType for particular transportProfile.
     *
     * @param endpointType
     * @param transportProfile
     * @return true if endpoint's transport equals to search transport identifier
     */
    protected boolean isValidEndpointTransport(EndpointType endpointType, String transportProfile) {
        boolean isValidTransport = StringUtils.equals(trim(endpointType.getTransportProfile()), trim(transportProfile));
        if (!isValidTransport) {
            LOG.debug("Search for endpoint with transport [{}], but found [{}]", transportProfile, endpointType.getTransportProfile());
        }
        return isValidTransport;
    }
}
