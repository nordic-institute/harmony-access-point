package eu.domibus.core.pmode.provider.dynamicdiscovery;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.pki.MultiDomainCryptoService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.core.proxy.DomibusProxy;
import eu.domibus.core.proxy.DomibusProxyService;
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
import eu.europa.ec.dynamicdiscovery.model.*;
import org.apache.commons.lang3.StringUtils;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.EndpointType;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.ProcessType;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.security.KeyStore;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static eu.domibus.core.cache.DomibusCacheService.DYNAMIC_DISCOVERY_ENDPOINT;
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

    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("^(?<scheme>.+?)::(?<value>.+)$");

    protected static final String DEFAULT_PARTY_TYPE = "urn:oasis:names:tc:ebcore:partyid-type:unregistered";

    protected static final String DEFAULT_RESPONDER_ROLE = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder";

    private final DomibusPropertyProvider domibusPropertyProvider;

    private final DomainContextProvider domainProvider;

    private final MultiDomainCryptoService multiDomainCertificateProvider;

    private final CertificateService certificateService;

    private final DomibusProxyService domibusProxyService;

    private final DomibusHttpRoutePlanner domibusHttpRoutePlanner;

    private final ObjectProvider<DocumentIdentifier> documentIdentifiers;

    private final ObjectProvider<ParticipantIdentifier> participantIdentifiers;

    private final ObjectProvider<ProcessIdentifier> processIdentifiers;

    private final ObjectProvider<TransportProfile> transportProfiles;

    private final ObjectProvider<DefaultProxy> proxies;

    private final ObjectProvider<DefaultBDXRLocator> bdxrLocators;

    private final ObjectProvider<DomibusCertificateValidator> domibusCertificateValidators;

    private final ObjectProvider<DefaultURLFetcher> urlFetchers;

    private final ObjectProvider<DefaultBDXRReader> bdxrReaders;

    private final ObjectProvider<DefaultSignatureValidator> signatureValidators;

    private final ObjectProvider<EndpointInfo> endpointInfos;

    public DynamicDiscoveryServiceOASIS(DomibusPropertyProvider domibusPropertyProvider,
                                        DomainContextProvider domainProvider,
                                        MultiDomainCryptoService multiDomainCertificateProvider,
                                        CertificateService certificateService,
                                        DomibusProxyService domibusProxyService,
                                        DomibusHttpRoutePlanner domibusHttpRoutePlanner,
                                        ObjectProvider<DocumentIdentifier> documentIdentifiers,
                                        ObjectProvider<ParticipantIdentifier> participantIdentifiers,
                                        ObjectProvider<ProcessIdentifier> processIdentifiers,
                                        ObjectProvider<TransportProfile> transportProfiles,
                                        ObjectProvider<DefaultProxy> proxies,
                                        ObjectProvider<DefaultBDXRLocator> bdxrLocators,
                                        ObjectProvider<DomibusCertificateValidator> domibusCertificateValidators,
                                        ObjectProvider<DefaultURLFetcher> urlFetchers,
                                        ObjectProvider<DefaultBDXRReader> bdxrReaders,
                                        ObjectProvider<DefaultSignatureValidator> signatureValidators,
                                        ObjectProvider<EndpointInfo> endpointInfos) {
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.domainProvider = domainProvider;
        this.multiDomainCertificateProvider = multiDomainCertificateProvider;
        this.certificateService = certificateService;
        this.domibusProxyService = domibusProxyService;
        this.domibusHttpRoutePlanner = domibusHttpRoutePlanner;
        this.documentIdentifiers = documentIdentifiers;
        this.participantIdentifiers = participantIdentifiers;
        this.processIdentifiers = processIdentifiers;
        this.transportProfiles = transportProfiles;
        this.proxies = proxies;
        this.bdxrLocators = bdxrLocators;
        this.domibusCertificateValidators = domibusCertificateValidators;
        this.urlFetchers = urlFetchers;
        this.bdxrReaders = bdxrReaders;
        this.signatureValidators = signatureValidators;
        this.endpointInfos = endpointInfos;
    }


    @Override
    DomibusLogger getLogger() {
        return LOG;
    }

    @Override
    String getTrimmedDomibusProperty(String propertyName) {
        return trim(domibusPropertyProvider.getProperty(propertyName));
    }

    @Override
    String getDefaultDiscoveryPartyIdType() {
        return DEFAULT_PARTY_TYPE;
    }

    @Override
    String getDefaultResponderRole() {
        return DEFAULT_RESPONDER_ROLE;
    }

    @Cacheable(value = DYNAMIC_DISCOVERY_ENDPOINT, key = "#domain + #participantId + #participantIdScheme + #documentId + #processId + #processIdScheme")
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
            ServiceMetadata serviceMetadata = smpClient.getServiceMetadata(participantIdentifier, documentIdentifier);

            LOG.debug("ServiceMetadata Response: [{}]" + serviceMetadata.getResponseBody());
            String transportProfileAS4 = domibusPropertyProvider.getProperty(DYNAMIC_DISCOVERY_TRANSPORTPROFILEAS4);
            LOG.debug("Get the endpoint for [{}]", transportProfileAS4);
            List<ProcessType> processes = serviceMetadata.getOriginalServiceMetadata().getServiceMetadata().getServiceInformation().getProcessList().getProcess();

            final EndpointType endpoint = getEndpoint(processes, processId, processIdScheme, transportProfileAS4);
            LOG.debug("Endpoint for transport profile [{}] -  [{}]", transportProfileAS4, endpoint);
            if (endpoint == null || endpoint.getEndpointURI() == null) {
                throw new ConfigurationException("Could not fetch metadata for: " + participantId + " " + participantIdScheme + " " + documentId +
                        " " + processId + " " + processIdScheme + " using the AS4 Protocol " + transportProfileAS4);
            }

            return endpointInfos.getObject(endpoint.getEndpointURI(), endpoint.getCertificate());

        } catch (TechnicalException exc) {
            String msg = "Could not fetch metadata from SMP for documentId " + documentId + " processId " + processId;
            // log error, because cause in ConfigurationException is consumed..
            LOG.error(msg, exc);
            throw new ConfigurationException(msg, exc);
        }
    }

    protected DynamicDiscovery createDynamicDiscoveryClient() {
        final String smlInfo = domibusPropertyProvider.getProperty(SMLZONE_KEY);
        if (StringUtils.isBlank(smlInfo)) {
            throw new ConfigurationException("SML Zone missing. Configure propertu [" + SMLZONE_KEY + "] in  domibus configuration");
        }

        final String certRegex = domibusPropertyProvider.getProperty(DYNAMIC_DISCOVERY_CERT_REGEX);
        if (StringUtils.isBlank(certRegex)) {
            LOG.debug("The value for property [" + DYNAMIC_DISCOVERY_CERT_REGEX + "] is empty.");
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

    protected DocumentIdentifier createDocumentIdentifier(String documentId) throws EbMS3Exception {
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
            throw new ConfigurationException("Unable to find endpoint information for null transport profile. Please check if property [" + DYNAMIC_DISCOVERY_TRANSPORTPROFILEAS4 + "] is set!");
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
        if (!match) {
            LOG.debug("Search for process id [{}] with scheme [{}], found: [{}] with scheme  [{}] !",
                    filterProcessId,
                    filterProcessIdScheme,
                    processType.getProcessIdentifier().getValue(),
                    processType.getProcessIdentifier().getScheme());
        }
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
