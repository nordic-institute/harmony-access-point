package eu.domibus.core.pmode.provider.dynamicdiscovery;

import eu.domibus.api.model.UserMessage;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.pki.MultiDomainCryptoService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.proxy.DomibusProxy;
import eu.domibus.api.proxy.DomibusProxyService;
import eu.domibus.api.security.SecurityProfile;
import eu.domibus.api.security.X509CertificateService;
import eu.domibus.api.util.DateUtil;
import eu.domibus.common.DomibusCacheConstants;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.core.message.UserMessageServiceHelper;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.europa.ec.dynamicdiscovery.DynamicDiscovery;
import eu.europa.ec.dynamicdiscovery.DynamicDiscoveryBuilder;
import eu.europa.ec.dynamicdiscovery.core.extension.IExtension;
import eu.europa.ec.dynamicdiscovery.core.fetcher.impl.DefaultURLFetcher;
import eu.europa.ec.dynamicdiscovery.core.locator.impl.DefaultBDXRLocator;
import eu.europa.ec.dynamicdiscovery.core.provider.impl.DefaultProvider;
import eu.europa.ec.dynamicdiscovery.core.reader.impl.DefaultBDXRReader;
import eu.europa.ec.dynamicdiscovery.core.security.impl.DefaultProxy;
import eu.europa.ec.dynamicdiscovery.core.security.impl.DefaultSignatureValidator;
import eu.europa.ec.dynamicdiscovery.enums.DNSLookupType;
import eu.europa.ec.dynamicdiscovery.exception.ConnectionException;
import eu.europa.ec.dynamicdiscovery.exception.TechnicalException;
import eu.europa.ec.dynamicdiscovery.model.SMPEndpoint;
import eu.europa.ec.dynamicdiscovery.model.SMPServiceMetadata;
import eu.europa.ec.dynamicdiscovery.model.SMPTransportProfile;
import eu.europa.ec.dynamicdiscovery.model.identifiers.SMPDocumentIdentifier;
import eu.europa.ec.dynamicdiscovery.model.identifiers.SMPParticipantIdentifier;
import eu.europa.ec.dynamicdiscovery.model.identifiers.SMPProcessIdentifier;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;

import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static eu.domibus.api.cache.DomibusLocalCacheService.DYNAMIC_DISCOVERY_ENDPOINT;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;
import static org.apache.commons.lang3.StringUtils.trim;

/**
 * Abstract class implement common methods for Oasis and Peppol dynamic discovery
 *
 * @author Joze Rihtarsic
 * @author Cosmin Baciu
 * @since 5.0
 */
public abstract class AbstractDynamicDiscoveryService implements DynamicDiscoveryService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AbstractDynamicDiscoveryService.class);

    protected static final Map<SecurityProfile, String> SECURITY_PROFILE_TRANSPORT_PROFILE_MAP = new HashMap<>();

    static {
        SECURITY_PROFILE_TRANSPORT_PROFILE_MAP.put(SecurityProfile.RSA, "bdxr-transport-ebms3-as4-v1p0");
        SECURITY_PROFILE_TRANSPORT_PROFILE_MAP.put(SecurityProfile.ECC, "bdxr-transport-ebms3-as4-EC-sample");
    }

    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("^(?<scheme>.+?)::(?<value>.+)$");

    @Autowired
    protected UserMessageServiceHelper userMessageServiceHelper;

    @Autowired
    protected DomainContextProvider domainProvider;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    protected DynamicDiscoveryUtil dynamicDiscoveryUtil;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected MultiDomainCryptoService multiDomainCertificateProvider;

    @Autowired
    protected CertificateService certificateService;

    @Autowired
    protected DomibusProxyService domibusProxyService;

    @Autowired
    protected DomibusHttpRoutePlanner domibusHttpRoutePlanner;

    @Autowired
    private X509CertificateService x509CertificateService;

    @Autowired
    protected ObjectProvider<SMPDocumentIdentifier> documentIdentifiers;

    @Autowired
    protected ObjectProvider<SMPParticipantIdentifier> participantIdentifiers;

    @Autowired
    protected ObjectProvider<DefaultProxy> proxies;

    @Autowired
    protected ObjectProvider<DefaultBDXRLocator> bdxrLocators;

    @Autowired
    protected ObjectProvider<DomibusCertificateValidator> domibusCertificateValidators;

    @Autowired
    protected ObjectProvider<DefaultURLFetcher> urlFetchers;

    @Autowired
    protected ObjectProvider<DefaultBDXRReader> bdxrReaders;

    @Autowired
    protected ObjectProvider<DefaultProvider> defaultProviders;

    @Autowired
    protected ObjectProvider<DefaultSignatureValidator> signatureValidators;

    @Autowired
    protected ObjectProvider<EndpointInfo> endpointInfos;

    /**
     * Get Default Discovery partyId type specific to implementation of the dynamic discovery service
     *
     * @return discovery party type
     */
    protected abstract String getPartyIdTypePropertyName();

    /**
     * Get responder role specific to implementation of the dynamic discovery service
     *
     * @return responder role
     */
    protected abstract String getPartyIdResponderRolePropertyName();

    protected abstract String getRegexCertificateSubjectValidationPropertyName();

    protected abstract List<IExtension> getSMPDocumentExtensions();

    @Cacheable(cacheManager = DomibusCacheConstants.CACHE_MANAGER, value = DYNAMIC_DISCOVERY_ENDPOINT, key = "#lookupKey")
    public EndpointInfo lookupInformation(final String lookupKey,
                                          final String finalRecipientValue,
                                          final String finalRecipientType,
                                          final String documentId,
                                          final String processId,
                                          final String processIdScheme) throws EbMS3Exception {

        LOG.info("[SMP] Do the lookup by participant identifier [{}] and scheme [{}], document identifier [{}], process identifier [{}] and scheme [{}]",
                finalRecipientValue,
                finalRecipientType,
                documentId,
                processId,
                processIdScheme);

        try {
            SMPServiceMetadata serviceMetadata = getServiceMetadata(finalRecipientValue, finalRecipientType, documentId, processId, processIdScheme);

            LOG.debug("ServiceMetadata Response: [{}]", serviceMetadata.getEndpoints());

            List<SMPEndpoint> smpEndpoints = serviceMetadata.getEndpoints();

            List<String> transportProfiles = dynamicDiscoveryUtil.retrieveTransportProfilesFromProcesses(smpEndpoints);

            //retrieve the transport profile available for the highest ranking Security Profile
            String transportProfile = dynamicDiscoveryUtil.getAvailableTransportProfileForHighestRankingSecurityProfile(transportProfiles, SECURITY_PROFILE_TRANSPORT_PROFILE_MAP);
            LOG.debug("Getting the endpoint for [{}]", transportProfile);

            final SMPEndpoint endpoint = getEndpoint(smpEndpoints, processId, processIdScheme, transportProfile);
            LOG.debug("Endpoint for transport profile [{}] - [{}]", transportProfile, endpoint);
            if (endpoint == null || StringUtils.isBlank(endpoint.getAddress())) {
                throw new ConfigurationException("Could not fetch metadata for: " + finalRecipientValue + " " + finalRecipientType + " " + documentId +
                        " " + processId + " " + processIdScheme + " using the AS4 Protocol " + transportProfile);
            }

            X509Certificate certificate = endpoint.getCertificate();
            if (certificate == null) {
                throw new ConfigurationException("Could not validate certificate: certificate is empty for participant identifier [" + finalRecipientValue + "] and scheme [" + finalRecipientType + "]");
            }

            x509CertificateService.validateClientX509Certificates(certificate);
            return endpointInfos.getObject(endpoint.getAddress(), certificate);

        } catch (TechnicalException exc) {
            String msg = "Could not fetch metadata from SMP for documentId [" + documentId + "] and processId [" + processId + "]";
            // log error, because cause in ConfigurationException is consumed.
            LOG.error(msg, exc);
            throw new ConfigurationException(msg, exc);
        }
    }

    protected SMPServiceMetadata getServiceMetadata(String participantId, String participantIdScheme, String documentId, String processId, String processIdScheme) throws TechnicalException {
        DynamicDiscovery smpClient = createDynamicDiscoveryClient();

        LOG.debug("Getting Request details for ServiceMetadata");
        final SMPParticipantIdentifier participantIdentifier = participantIdentifiers.getObject(participantId, participantIdScheme);
        final SMPDocumentIdentifier documentIdentifier = createDocumentIdentifier(documentId);
        LOG.debug("Getting ServiceMetadata using Participant Identifier [{}] and scheme [{}], Document Identifier [{}] and scheme [{}]",
                participantIdentifier.getIdentifier(),
                participantIdentifier.getScheme(),
                documentIdentifier.getIdentifier(),
                documentIdentifier.getScheme());
        return smpClient.getServiceMetadata(participantIdentifier, documentIdentifier);
    }

    protected DynamicDiscovery createDynamicDiscoveryClient() {
        final String smlInfo = domibusPropertyProvider.getProperty(DOMIBUS_SMLZONE);
        if (StringUtils.isBlank(smlInfo)) {
            throw new ConfigurationException("SML Zone missing. Configure property [" + DOMIBUS_SMLZONE + "] in domibus configuration!");
        }


        final String regexCertificateSubjectValidationPropertyName = getRegexCertificateSubjectValidationPropertyName();
        final String certRegex = domibusPropertyProvider.getProperty(regexCertificateSubjectValidationPropertyName);
        if (StringUtils.isBlank(certRegex)) {
            LOG.debug("The value for property [{}] is empty.", regexCertificateSubjectValidationPropertyName);
        }

        final List<String> allowedCertificatePolicyIDs = getAllowedSMPCertificatePolicyOIDs();

        LOG.debug("Load truststore for the smpClient");
        KeyStore trustStore = multiDomainCertificateProvider.getTrustStore(domainProvider.getCurrentDomain());
        try {
            DefaultProxy defaultProxy = getConfiguredProxy();
            DomibusCertificateValidator domibusSMPCertificateValidator = domibusCertificateValidators.getObject(certificateService, trustStore, certRegex, allowedCertificatePolicyIDs);

            LOG.debug("Creating SMP client [{}] proxy.", (defaultProxy != null ? "with" : "without"));
            final List<DNSLookupType> dnsLookupTypes = getDnsLookupTypes();
            if (CollectionUtils.isEmpty(dnsLookupTypes)) {
                throw new ConfigurationException("DNS lookup types must be specified");
            }

            final DefaultBDXRLocator bdxrLocators = this.bdxrLocators.getObject(smlInfo, dnsLookupTypes);
            final DefaultURLFetcher urlFetcher = urlFetchers.getObject(domibusHttpRoutePlanner, defaultProxy);

            final List<IExtension> smpDocumentExtensions = getSMPDocumentExtensions();
            LOG.debug("Using SMP document extensions [{}]", smpDocumentExtensions);
            final DefaultSignatureValidator defaultSignatureValidator = signatureValidators.getObject(domibusSMPCertificateValidator);
            final DefaultBDXRReader metadataReader = bdxrReaders.getObject(defaultSignatureValidator, smpDocumentExtensions);

            final List<String> wildcardDocumentSchemes = getWildcardDocumentSchemes();
            LOG.debug("Creating the dynamic discovery client using wildcard document schemes [{}]", wildcardDocumentSchemes);
            final DefaultProvider defaultProvider = defaultProviders.getObject(urlFetcher, metadataReader, wildcardDocumentSchemes);
            return DynamicDiscoveryBuilder.newInstance()
                    .fetcher(urlFetcher)
                    .locator(bdxrLocators)
                    .reader(metadataReader)
                    .provider(defaultProvider)
                    .build();
        } catch (TechnicalException exc) {
            throw new ConfigurationException("Could not create smp client to fetch metadata from SMP", exc);
        }
    }

    protected SMPDocumentIdentifier createDocumentIdentifier(String documentId) {
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
     * @param smpEndpoints     - list of processes
     * @param processId        target process identifier
     * @param processIdScheme  target process identifier scheme
     * @param transportProfile list of targeted transport profiles
     * @return valid endpoint
     * @throws ConfigurationException
     */
    public SMPEndpoint getEndpoint(List<SMPEndpoint> smpEndpoints, String processId, String processIdScheme, String transportProfile) {

        if (StringUtils.isBlank(transportProfile)) {
            throw new ConfigurationException("Unable to find endpoint information: transport profile not found or empty");
        }

        if (StringUtils.isBlank(processId)) {
            throw new ConfigurationException("Unable to find endpoint information for null process identifier!");
        }

        LOG.debug("Search for a valid Endpoint for process  id: [{}], process scheme [{}] and TransportProfile: [{}]]!", processId, processIdScheme, transportProfile);

        List<SMPEndpoint> filteredSmpEndpoints = smpEndpoints.stream()
                .filter(this::hasValidProcessIdentifiers)
                .filter(processType -> smpEndpointMatchesProcessValues(processType, processId, processIdScheme))
                .collect(Collectors.toList());
        LOG.debug("Got [{}] Endpoints for processes with  id: [{}] and scheme [{}] !", filteredSmpEndpoints.size(), processId, processIdScheme);

        return filteredSmpEndpoints.stream()
                .filter(endpointType -> matchesEndpointTransport(endpointType, transportProfile))
                .filter(this::isValidEndpoint)
                .findFirst()
                .orElse(null);
    }

    /**
     * This method exists to be used to filter invalid ProcessTypes from list of ProcessType. Methods validates if process
     * has defined endpoints.
     *
     * @param smpEndpoint
     * @return true if endpoint's is valid
     */
    protected boolean hasValidProcessIdentifiers(SMPEndpoint smpEndpoint) {
        boolean emptyList = smpEndpoint.getProcessIdentifiers() == null || smpEndpoint.getProcessIdentifiers().isEmpty();
        if (emptyList) {
            LOG.warn("Found SMP endpoint with empty process identifiers [{}]", smpEndpoint);
        }
        return !emptyList;

    }

    /**
     * This method exists to be used to filter out ProcessTypes which do not match searched criteria
     *
     * @param smpEndpoint
     * @param filterProcessId
     * @param filterProcessIdScheme
     * @return true if endpoint's is valid
     */
    protected boolean smpEndpointMatchesProcessValues(SMPEndpoint smpEndpoint, String filterProcessId, String filterProcessIdScheme) {
        final List<SMPProcessIdentifier> matchedProcessIdentifiers = smpEndpoint.getProcessIdentifiers().stream().filter(smpProcessIdentifier -> {
            boolean match = StringUtils.equals(smpProcessIdentifier.getIdentifier(), filterProcessId)
                    && StringUtils.equals(smpProcessIdentifier.getScheme(), filterProcessIdScheme);

            LOG.debug("Search for process id [{}] with scheme [{}], found: [{}] with scheme [{}] which match [{}] to the search parameters!",
                    filterProcessId,
                    filterProcessIdScheme,
                    smpProcessIdentifier.getIdentifier(),
                    smpProcessIdentifier.getScheme(),
                    match);

            return match;
        }).collect(Collectors.toList());
        return CollectionUtils.isNotEmpty(matchedProcessIdentifiers);
    }

    /**
     * This method exists to be used to filter invalid EndpointTypes from list of endpointType.
     *
     * @param endpointType
     * @return true if endpoint's is valid
     */
    protected boolean isValidEndpoint(SMPEndpoint endpointType) {
        return isValidEndpoint(endpointType.getActivationDate(), endpointType.getExpirationDate());
    }

    /**
     * This method exists to be used to filter list of endpointType for particular transportProfile.
     *
     * @param endpointType
     * @param transportProfileValue
     * @return true if endpoint's transport equals to search transport identifier
     */
    protected boolean matchesEndpointTransport(SMPEndpoint endpointType, String transportProfileValue) {
        final SMPTransportProfile transportProfile = endpointType.getTransportProfile();
        if (transportProfile == null) {
            return false;
        }

        boolean isValidTransport = StringUtils.equals(trim(transportProfile.getIdentifier()), trim(transportProfileValue));
        if (!isValidTransport) {
            LOG.debug("Search for endpoint with transport [{}], but found [{}]", transportProfileValue, transportProfile);
        }
        return isValidTransport;
    }


    protected List<String> getWildcardDocumentSchemes() {
        final String wildcardDocumentSchemePropertyName = DOMIBUS_DYNAMICDISCOVERY_CLIENT_WILDCARD_DOCUMENT_SCHEMES;
        final List<String> wildcardSchemes = domibusPropertyProvider.getCommaSeparatedPropertyValues(wildcardDocumentSchemePropertyName);
        if (CollectionUtils.isEmpty(wildcardSchemes)) {
            LOG.debug("No wildcard schemes were configured for property [{}]", wildcardDocumentSchemePropertyName);
            return null;
        }
        return wildcardSchemes;
    }

    /**
     * get allowed SMP certificate policy OIDs
     *
     * @return list of certificate policy OIDs
     */
    protected List<String> getAllowedSMPCertificatePolicyOIDs() {
        final String allowedCertificatePolicyId = dynamicDiscoveryUtil.getTrimmedDomibusProperty(DOMIBUS_DYNAMICDISCOVERY_CLIENT_CERTIFICATE_POLICY_OID_VALIDATION);
        if (StringUtils.isBlank(allowedCertificatePolicyId)) {
            LOG.debug("The value for property [{}] is empty.", DOMIBUS_DYNAMICDISCOVERY_CLIENT_CERTIFICATE_POLICY_OID_VALIDATION);
            return Collections.emptyList();
        } else {
            return Arrays.asList(allowedCertificatePolicyId.split("\\s*,\\s*"));
        }
    }

    public String getPartyIdType() {
        String propertyName = getPartyIdTypePropertyName();
        // if is null - this means property is commented-out and default value must be set.
        // else if is empty - property is set in domibus.properties as empty string and the right value for the
        // ebMS 3.0  PartyId/@type is null value!
        return StringUtils.trimToNull(dynamicDiscoveryUtil.getTrimmedDomibusProperty(propertyName));
    }

    public String getResponderRole() {
        String propertyName = getPartyIdResponderRolePropertyName();
        return dynamicDiscoveryUtil.getTrimmedDomibusProperty(propertyName);
    }

    /**
     * Method validates serviceActivationDate and serviceExpirationDate dates.
     * A missing/null activation date is interpreted as “valid".
     * A missing/null expiration date is interpreted as “valid until eternity”.
     *
     * @param serviceActivationDateOffset activate date from element Endpoint/ServiceActivationDate
     * @param serviceExpirationDateOffset expiration date from element Endpoint/ServiceExpirationDate
     * @return true if the endpoint is valid for the current date. Else return false.
     */
    public boolean isValidEndpoint(OffsetDateTime serviceActivationDateOffset, OffsetDateTime serviceExpirationDateOffset) {
        final Date serviceActivationStartDate = dateUtil.convertOffsetDateTimeToDate(serviceActivationDateOffset);
        Date currentDate = Calendar.getInstance().getTime();
        if (serviceActivationStartDate != null && currentDate.before(serviceActivationStartDate)) {
            LOG.warn("Found endpoint which is not yet activated! Endpoint's activation date: [{}]!", DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.format(serviceActivationStartDate));
            return false;
        }

        final Date serviceExpirationDate = dateUtil.convertOffsetDateTimeToDate(serviceExpirationDateOffset);
        if (serviceExpirationDate != null && currentDate.after(serviceExpirationDate)) {
            LOG.warn("Found endpoint, which is expired! Endpoint's expiration date: [{}]!", DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.format(serviceExpirationDate));
            return false;
        }
        return true;
    }

    /**
     * Method returns cache key for dynamic discovery lookup.
     *
     * @param userMessage
     * @return cache key string with format: #domain + #participantId + #participantIdScheme + #documentId + #processId + #processIdScheme";
     */
    @Override
    public String getFinalRecipientCacheKeyForDynamicDiscovery(UserMessage userMessage) {
        final String finalRecipientValue = userMessageServiceHelper.getFinalRecipientValue(userMessage);
        final String finalRecipientType = userMessageServiceHelper.getFinalRecipientType(userMessage);

        // create key
        //"#domain + #participantId + #participantIdScheme + #documentId + #processId + #processIdScheme";
        String cacheKey = domainProvider.getCurrentDomain().getCode() +
                finalRecipientValue +
                finalRecipientType +
                userMessage.getActionValue() +
                userMessage.getService().getValue() +
                userMessage.getService().getType();
        return cacheKey;
    }

    protected List<DNSLookupType> getDnsLookupTypes() {
        String dnsLookupTypePropertyName = DOMIBUS_DYNAMICDISCOVERY_CLIENT_DNS_LOOKUP_TYPES;
        final List<String> dnsLookupTypesListString = domibusPropertyProvider.getCommaSeparatedPropertyValues(dnsLookupTypePropertyName);

        List<DNSLookupType> result = new ArrayList<>();
        dnsLookupTypesListString.stream()
                .forEach(dnsLookupTypeString -> {
                    final DNSLookupType dnsLookupType = DNSLookupType.valueOf(dnsLookupTypeString);
                    result.add(dnsLookupType);
                });
        LOG.debug("Using DNS custom lookup schemes [{}]", result);
        return result;
    }
}
