package eu.domibus.core.pmode.provider.dynamicdiscovery;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.crypto.api.MultiDomainCryptoService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.security.KeyStore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static eu.domibus.core.cache.DomibusCacheService.DYNAMIC_DISCOVERY_ENDPOINT;

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
public class DynamicDiscoveryServiceOASIS implements DynamicDiscoveryService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DynamicDiscoveryServiceOASIS.class);
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("^(?<scheme>.+?)::(?<value>.+)$");
    protected static final String URN_TYPE_VALUE = "urn:oasis:names:tc:ebcore:partyid-type:unregistered";
    protected static final String DEFAULT_RESPONDER_ROLE = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder";


    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected DomainContextProvider domainProvider;

    @Autowired
    protected MultiDomainCryptoService multiDomainCertificateProvider;

    @Autowired
    DomibusConfigurationService domibusConfigurationService;

    @Autowired
    protected CertificateService certificateService;

    @Autowired
    DomibusProxyService domibusProxyService;

    @Cacheable(value = DYNAMIC_DISCOVERY_ENDPOINT, key = "#domain + #participantId + #participantIdScheme + #documentId + #processId + #processIdScheme")
    public EndpointInfo lookupInformation(final String domain,
                                          final String participantId,
                                          final String participantIdScheme,
                                          final String documentId,
                                          final String processId,
                                          final String processIdScheme) throws EbMS3Exception {

        LOG.info("[OASIS SMP] Do the lookup by: " + participantId + " " + participantIdScheme + " " + documentId +
                " " + processId + " " + processIdScheme);

        try {
            DynamicDiscovery smpClient = createDynamicDiscoveryClient();

            LOG.debug("Getting Request details for ServiceMetadata");
            final ParticipantIdentifier participantIdentifier = new ParticipantIdentifier(participantId, participantIdScheme);
            final DocumentIdentifier documentIdentifier = createDocumentIdentifier(documentId);
            final ProcessIdentifier processIdentifier = new ProcessIdentifier(processId, processIdScheme);
            LOG.debug("ServiceMetadata request contains Participant Identifier [{}] and scheme [{}], Document Identifier [{}] and scheme [{}], Process Identifier [{}] and scheme [{}]", participantIdentifier.getIdentifier(), participantIdentifier.getScheme(), documentIdentifier.getIdentifier(), documentIdentifier.getScheme(), processIdentifier.getIdentifier(), processIdentifier.getScheme());
            ServiceMetadata serviceMetadata = smpClient.getServiceMetadata(participantIdentifier, documentIdentifier);
            LOG.debug("ServiceMetadata Response: [{}]" + serviceMetadata.getResponseBody());
            String transportProfileAS4 = domibusPropertyProvider.getProperty(DYNAMIC_DISCOVERY_TRANSPORTPROFILEAS4);
            LOG.debug("Get the endpoint for " + transportProfileAS4);
            final Endpoint endpoint = serviceMetadata.getEndpoint(processIdentifier, new TransportProfile(transportProfileAS4));
            LOG.debug("Endpoint for transport profile -  [{}]", endpoint);
            if (endpoint == null || endpoint.getAddress() == null || endpoint.getProcessIdentifier() == null) {
                throw new ConfigurationException("Could not fetch metadata for: " + participantId + " " + participantIdScheme + " " + documentId +
                        " " + processId + " " + processIdScheme + " using the AS4 Protocol " + transportProfileAS4);
            }

            return new EndpointInfo(endpoint.getAddress(), endpoint.getCertificate());

        } catch (TechnicalException exc) {
            String msg = "Could not fetch metadata from SMP for documentId " + documentId + " processId " + processId;
            // log error, because cause in ConfigurationException is consumed..
            LOG.error(msg, exc);
            throw new ConfigurationException(msg, exc);
        }
    }

    protected DynamicDiscovery createDynamicDiscoveryClient() {
        final String smlInfo = domibusPropertyProvider.getProperty(SMLZONE_KEY);
        if (smlInfo == null) {
            throw new ConfigurationException("SML Zone missing. Configure in domibus-configuration.xml");
        }

        final String certRegex = domibusPropertyProvider.getProperty(DYNAMIC_DISCOVERY_CERT_REGEX);
        if (StringUtils.isEmpty(certRegex)) {
            LOG.debug("The value for property domibus.dynamicdiscovery.oasisclient.regexCertificateSubjectValidation is empty.");
        }

        LOG.debug("Load truststore for the smpClient");
        KeyStore trustStore = multiDomainCertificateProvider.getTrustStore(domainProvider.getCurrentDomain());
        try {
            DefaultProxy defaultProxy = getConfiguredProxy();
            DomibusCertificateValidator domibusSMPCertificateValidator = new DomibusCertificateValidator(certificateService, trustStore, certRegex);

            DynamicDiscoveryBuilder dynamicDiscoveryBuilder = DynamicDiscoveryBuilder.newInstance();
            dynamicDiscoveryBuilder
                    .locator(new DefaultBDXRLocator(smlInfo))
                    .reader(new DefaultBDXRReader(new DefaultSignatureValidator(domibusSMPCertificateValidator)));

            if (defaultProxy != null) {
                dynamicDiscoveryBuilder
                        .fetcher(new DefaultURLFetcher(defaultProxy));
            }

            LOG.debug("Creating SMP client " + (defaultProxy != null ? "with" : "without") + " proxy.");

            return dynamicDiscoveryBuilder.build();
        } catch (TechnicalException exc) {
            throw new ConfigurationException("Could not create smp client to fetch metadata from SMP", exc);
        }
    }

    protected DocumentIdentifier createDocumentIdentifier(String documentId) throws EbMS3Exception {
        try {
            String scheme = extract(documentId, "scheme");
            String value = extract(documentId, "value");
            return new DocumentIdentifier(value, scheme);
        } catch (IllegalStateException ise) {
            LOG.debug("Could not extract @scheme and @value from [{}], DocumentIdentifier will be created with empty scheme", documentId, ise);
            return new DocumentIdentifier(documentId);
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
            return new DefaultProxy(domibusProxy.getHttpProxyHost(), domibusProxy.getHttpProxyPort(), null, null, domibusProxy.getNonProxyHosts());
        }
        return new DefaultProxy(domibusProxy.getHttpProxyHost(), domibusProxy.getHttpProxyPort(), domibusProxy.getHttpProxyUser(), domibusProxy.getHttpProxyPassword(), domibusProxy.getNonProxyHosts());
    }

    @Override
    public String getPartyIdType() {
        String propVal = domibusPropertyProvider.getProperty(DYNAMIC_DISCOVERY_PARTYID_TYPE);
        // if is null - this means property is commented-out and default value must be set.
        // else if is empty - property is set in domibus.properties as empty string and the right value for the
        // ebMS 3.0  PartyId/@type is null value!
        if (propVal==null) {
            propVal = URN_TYPE_VALUE;
        } else if (StringUtils.isEmpty(propVal)) {
            propVal = null;
        }
        return propVal;
    }

    @Override
    public String getResponderRole() {
        String propVal = domibusPropertyProvider.getProperty(DYNAMIC_DISCOVERY_PARTYID_RESPONDER_ROLE);
        if (StringUtils.isEmpty(propVal)) {
            propVal = DEFAULT_RESPONDER_ROLE;
        }
        return propVal;
    }
}
