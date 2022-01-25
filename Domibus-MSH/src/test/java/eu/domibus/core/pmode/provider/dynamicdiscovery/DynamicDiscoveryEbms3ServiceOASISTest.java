package eu.domibus.core.pmode.provider.dynamicdiscovery;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.pki.MultiDomainCryptoService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.core.proxy.DomibusProxy;
import eu.domibus.core.proxy.DomibusProxyService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.europa.ec.dynamicdiscovery.DynamicDiscovery;
import eu.europa.ec.dynamicdiscovery.core.fetcher.FetcherResponse;
import eu.europa.ec.dynamicdiscovery.core.fetcher.impl.DefaultURLFetcher;
import eu.europa.ec.dynamicdiscovery.core.locator.impl.DefaultBDXRLocator;
import eu.europa.ec.dynamicdiscovery.core.reader.impl.DefaultBDXRReader;
import eu.europa.ec.dynamicdiscovery.core.security.impl.DefaultProxy;
import eu.europa.ec.dynamicdiscovery.core.security.impl.DefaultSignatureValidator;
import eu.europa.ec.dynamicdiscovery.model.*;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.ServiceGroupType;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.ServiceMetadataType;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.SignedServiceMetadataType;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.test.util.ReflectionTestUtils;
import org.w3c.dom.Document;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.security.KeyStore;

import static eu.domibus.core.pmode.provider.dynamicdiscovery.DynamicDiscoveryService.DYNAMIC_DISCOVERY_TRANSPORTPROFILEAS4;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Ioana Dragusanu (idragusa)
 * @author Sebastian-Ion TINCU
 * @since 3.2.5
 */
@Ignore("EDELIVERY-8892")
@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(JMockit.class)
public class DynamicDiscoveryEbms3ServiceOASISTest {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DynamicDiscoveryEbms3ServiceOASISTest.class);

    //The (sub)domain of the SML, e.g. ehealth.acc.edelivery.tech.ec.europa.eu, connectivitytest.acc.edelivery.tech.ec.europa.eu
    private static final String TEST_SML_ZONE = "acc.edelivery.tech.ec.europa.eu";

    private static final String TEST_KEYSTORE_PASSWORD = "test123";

    private static final String TEST_RECEIVER_ID = "urn:romania:ncpb";
    private static final String TEST_RECEIVER_ID_TYPE = "ehealth-actorid-qns";
    private static final String TEST_ACTION_VALUE = "ehealth-resid-qns:urn::epsos##services:extended:epsos::107";
    private static final String TEST_SERVICE_VALUE = "urn:epsosPatientService::List";
    private static final String TEST_SERVICE_TYPE = "ehealth-procid-qns";
    private static final String TEST_INVALID_SERVICE_VALUE = "invalidServiceValue";
    private static final String DOMAIN = "default";

    private static final String ADDRESS = "http://localhost:9090/anonymous/msh";
    private static final String DYNAMICDISCOVERY_PARTYID_TYPE = "domibus.dynamicdiscovery.partyid.type";

    private static final String DYNAMICDISCOVERY_PARTYID_RESPONDER_ROLE = "domibus.dynamicdiscovery.partyid.responder.role";

    @Injectable
    private CertificateService certificateService;

    @Injectable
    private DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    private MultiDomainCryptoService multiDomainCertificateProvider;

    @Injectable
    private DomainContextProvider domainProvider;

    @Injectable
    private DomibusConfigurationService domibusConfigurationService;

    @Injectable
    private DomibusProxyService domibusProxyService;

    @Injectable
    private DomibusHttpRoutePlanner domibusHttpRoutePlanner;

    @Injectable
    private ObjectProvider<DocumentIdentifier> documentIdentifiers;

    @Injectable
    private ObjectProvider<ParticipantIdentifier> participantIdentifiers;

    @Injectable
    private ObjectProvider<ProcessIdentifier> processIdentifiers;

    @Injectable
    private ObjectProvider<TransportProfile> transportProfiles;

    @Injectable
    private ObjectProvider<DefaultProxy> proxies;

    @Injectable
    private ObjectProvider<DefaultBDXRLocator> bdxrLocators;

    @Injectable
    private ObjectProvider<DomibusCertificateValidator> domibusCertificateValidators;

    @Injectable
    private ObjectProvider<DefaultURLFetcher> urlFetchers;

    @Injectable
    private ObjectProvider<DefaultBDXRReader> bdxrReaders;

    @Injectable
    private ObjectProvider<DefaultSignatureValidator> signatureValidators;

    @Injectable
    private ObjectProvider<EndpointInfo> endpointInfos;

    @Injectable
    private DomibusCertificateValidator domibusCertificateValidator;

    @Injectable
    private DefaultURLFetcher defaultURLFetcher;

    @Injectable
    private DefaultBDXRLocator defaultBDXRLocator;

    @Injectable
    private DefaultSignatureValidator defaultSignatureValidator;

    @Injectable
    private ParticipantIdentifier participantIdentifier;

    @Injectable
    private DocumentIdentifier documentIdentifier;

    @Injectable
    private ProcessIdentifier processIdentifier;

    @Injectable
    private DefaultBDXRReader defaultBDXRReader;

    @Injectable
    private TransportProfile transportProfile;

    @Injectable
    private EndpointInfo endpointInfo;

    @Tested
    private DynamicDiscoveryServiceOASIS dynamicDiscoveryServiceOASIS;

    @Before
    public void setup() {
        new Expectations() {{
            domibusCertificateValidators.getObject(any, any, anyString);
            result = domibusCertificateValidator;

            urlFetchers.getObject(domibusHttpRoutePlanner, any);
            result = defaultURLFetcher;

            bdxrLocators.getObject(anyString);
            result = defaultBDXRLocator;

            signatureValidators.getObject(any);
            result = defaultSignatureValidator;

            bdxrReaders.getObject(any);
            result = defaultBDXRReader;

            participantIdentifiers.getObject(anyString, anyString);
            result = participantIdentifier;

            documentIdentifiers.getObject(anyString);
            result = documentIdentifier;

            processIdentifiers.getObject(anyString, anyString);
            result = processIdentifier;

            transportProfiles.getObject(anyString);
            result = transportProfile;

            endpointInfos.getObject(anyString, any);
            result = endpointInfo;
        }};
    }

    @Test
    public void testLookupInformationMock(final @Capturing DynamicDiscovery smpClient) throws Exception {
        new Expectations() {{
            domibusPropertyProvider.getProperty(DynamicDiscoveryService.SMLZONE_KEY);
            result = TEST_SML_ZONE;

            domibusPropertyProvider.getProperty(DYNAMIC_DISCOVERY_TRANSPORTPROFILEAS4);
            result = "bdxr-transport-ebms3-as4-v1p0";

            transportProfile.getIdentifier();
            result = "bdxr-transport-ebms3-as4-v1p0";

            processIdentifier.getIdentifier();
            result = TEST_SERVICE_VALUE;

            processIdentifier.getScheme();
            result = TEST_SERVICE_TYPE;

            endpointInfo.getAddress();
            result = ADDRESS;

            smpClient.getServiceMetadata(participantIdentifier, documentIdentifier);
            result =  buildServiceMetadata();
        }};

        EndpointInfo endpoint = dynamicDiscoveryServiceOASIS.lookupInformation(DOMAIN, TEST_RECEIVER_ID, TEST_RECEIVER_ID_TYPE, TEST_ACTION_VALUE, TEST_SERVICE_VALUE, TEST_SERVICE_TYPE);
        assertNotNull(endpoint);
        assertEquals(ADDRESS, endpoint.getAddress());

        new Verifications() {{
            smpClient.getServiceMetadata(participantIdentifier, documentIdentifier);
        }};
    }

    @Test
    public void testLookupInformationRegexMatch(final @Capturing DynamicDiscovery smpClient) throws Exception {
        new Expectations() {{
            domibusPropertyProvider.getProperty(DynamicDiscoveryService.SMLZONE_KEY);
            result = TEST_SML_ZONE;

            domibusPropertyProvider.getProperty(DYNAMIC_DISCOVERY_TRANSPORTPROFILEAS4);
            result = "bdxr-transport-ebms3-as4-v1p0";

            domibusPropertyProvider.getProperty(DynamicDiscoveryService.DYNAMIC_DISCOVERY_CERT_REGEX);
            result = "^.*EHEALTH_SMP.*$";

            transportProfile.getIdentifier();
            result = "bdxr-transport-ebms3-as4-v1p0";

            processIdentifier.getIdentifier();
            result = TEST_SERVICE_VALUE;

            processIdentifier.getScheme();
            result = TEST_SERVICE_TYPE;

            endpointInfo.getAddress();
            result = ADDRESS;

            ServiceMetadata sm = buildServiceMetadata();
            smpClient.getServiceMetadata(participantIdentifier, documentIdentifier);
            result = sm;
        }};

        EndpointInfo endpoint = dynamicDiscoveryServiceOASIS.lookupInformation(DOMAIN, TEST_RECEIVER_ID, TEST_RECEIVER_ID_TYPE, TEST_ACTION_VALUE, TEST_SERVICE_VALUE, TEST_SERVICE_TYPE);
        assertNotNull(endpoint);
        assertEquals(ADDRESS, endpoint.getAddress());

        new Verifications() {{
            smpClient.getServiceMetadata(participantIdentifier, documentIdentifier);
        }};
    }

    @Test
    public void testLookupInformationRegexNull(final @Capturing DynamicDiscovery smpClient) throws Exception {
        new Expectations() {{
            domibusPropertyProvider.getProperty(DynamicDiscoveryService.SMLZONE_KEY);
            result = TEST_SML_ZONE;

            domibusPropertyProvider.getProperty(DYNAMIC_DISCOVERY_TRANSPORTPROFILEAS4);
            result = "bdxr-transport-ebms3-as4-v1p0";

            domibusPropertyProvider.getProperty(DynamicDiscoveryService.DYNAMIC_DISCOVERY_CERT_REGEX);
            result = null;

            transportProfile.getIdentifier();
            result = "bdxr-transport-ebms3-as4-v1p0";

            processIdentifier.getIdentifier();
            result = TEST_SERVICE_VALUE;

            processIdentifier.getScheme();
            result = TEST_SERVICE_TYPE;

            endpointInfo.getAddress();
            result = ADDRESS;

            ServiceMetadata sm = buildServiceMetadata();
            smpClient.getServiceMetadata(participantIdentifier, documentIdentifier);
            result = sm;
        }};

        EndpointInfo endpoint = dynamicDiscoveryServiceOASIS.lookupInformation(DOMAIN, TEST_RECEIVER_ID, TEST_RECEIVER_ID_TYPE, TEST_ACTION_VALUE, TEST_SERVICE_VALUE, TEST_SERVICE_TYPE);
        assertNotNull(endpoint);
        assertEquals(ADDRESS, endpoint.getAddress());

        new Verifications() {{
            smpClient.getServiceMetadata(participantIdentifier, documentIdentifier);
        }};
    }


    @Test(expected = ConfigurationException.class)
    public void testLookupInformationNotFound(final @Capturing DynamicDiscovery smpClient) throws Exception {
        new Expectations() {{
            domibusPropertyProvider.getProperty(DynamicDiscoveryService.SMLZONE_KEY);
            result = TEST_SML_ZONE;

            ServiceMetadata sm = buildServiceMetadata();
            smpClient.getServiceMetadata(participantIdentifier, documentIdentifier);
            result = sm;
        }};

        dynamicDiscoveryServiceOASIS.lookupInformation(DOMAIN, TEST_RECEIVER_ID, TEST_RECEIVER_ID_TYPE, TEST_ACTION_VALUE, TEST_INVALID_SERVICE_VALUE, TEST_SERVICE_TYPE);
    }

    @Test
    public void testLookupInformationNotFoundMessage(final @Capturing DynamicDiscovery smpClient) throws Exception {
        new Expectations() {{
            domibusPropertyProvider.getProperty(DynamicDiscoveryService.SMLZONE_KEY);
            result = TEST_SML_ZONE;

            ServiceMetadata sm = buildServiceMetadata();
            smpClient.getServiceMetadata(participantIdentifier, documentIdentifier);
            result = sm;
        }};
        try {
            dynamicDiscoveryServiceOASIS.lookupInformation(DOMAIN, TEST_RECEIVER_ID, TEST_RECEIVER_ID_TYPE, TEST_ACTION_VALUE, TEST_INVALID_SERVICE_VALUE, TEST_SERVICE_TYPE);
        } catch (ConfigurationException cfe) {
            Assert.assertTrue(cfe.getMessage().contains("Could not fetch metadata for: urn:romania:ncpb"));
        }
    }

    private ServiceMetadata buildServiceMetadata() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("eu/domibus/core/pmode/provider/dynamicdiscovery/provider/SignedServiceMetadataResponseOASIS.xml");
        FetcherResponse fetcherResponse = new FetcherResponse(inputStream);
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        JAXBContext jaxbContext = JAXBContext.newInstance(ServiceMetadataType.class, SignedServiceMetadataType.class, ServiceGroupType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        Document document = documentBuilderFactory.newDocumentBuilder().parse(fetcherResponse.getInputStream());
        SignedServiceMetadataType signedServiceMetadataType = ((JAXBElement<SignedServiceMetadataType>) unmarshaller.unmarshal(document)).getValue();
        return new ServiceMetadata(signedServiceMetadataType, null, "");
    }

    @Test
    public void testProxyConfigured() throws Exception {
        // Given
        new MockUp<DefaultProxy>() {
            void $init(Invocation invocation, String serverAddress, int serverPort, String user, String password, String nonProxyHosts) {
                Assert.assertTrue("Should have created the correct proxy configuration when the proxy user is not empty",
                        invocation.getInvocationCount() == 1
                                && "192.168.0.0".equals(serverAddress)
                                && 1234 == serverPort
                                && "proxyUser".equals(user)
                                && "proxyPassword".equals(password)
                                && "host1,host2".equals(nonProxyHosts)
                );
            }
        };

        new Expectations(dynamicDiscoveryServiceOASIS) {{
            DomibusProxy domibusProxy = new DomibusProxy();
            domibusProxy.setEnabled(true);
            domibusProxy.setHttpProxyHost("192.168.0.0");
            domibusProxy.setHttpProxyPort(1234);
            domibusProxy.setHttpProxyUser("proxyUser");
            domibusProxy.setHttpProxyPassword("proxyPassword");
            domibusProxy.setNonProxyHosts("host1,host2");

            domibusProxyService.getDomibusProxy();
            result = domibusProxy;

            domibusProxyService.useProxy();
            result = true;

            proxies.getObject("192.168.0.0", 1234, "proxyUser", "proxyPassword", "host1,host2");
            result = new DefaultProxy("192.168.0.0", 1234, "proxyUser", "proxyPassword", "host1,host2");
        }};

        //when
        DefaultProxy defaultProxy = dynamicDiscoveryServiceOASIS.getConfiguredProxy();

        //then
        Assert.assertNotNull(defaultProxy);
    }

    @Test
    public void testProxyConfigured_emptyProxyUser() throws Exception {
        // Given
        new MockUp<DefaultProxy>() {
            void $init(Invocation invocation, String serverAddress, int serverPort, String user, String password, String nonProxyHosts) {
                Assert.assertTrue("Should have created the correct proxy configuration when the proxy user is empty",
                        invocation.getInvocationCount() == 1
                                && "192.168.0.0".equals(serverAddress)
                                && 1234 == serverPort
                                && user == null
                                && password == null
                                && "host1,host2".equals(nonProxyHosts)
                );
            }
        };

        new Expectations(dynamicDiscoveryServiceOASIS) {{
            DomibusProxy domibusProxy = new DomibusProxy();
            domibusProxy.setEnabled(true);
            domibusProxy.setHttpProxyHost("192.168.0.0");
            domibusProxy.setHttpProxyPort(1234);
            domibusProxy.setHttpProxyUser("");
            domibusProxy.setHttpProxyPassword("proxyPassword");
            domibusProxy.setNonProxyHosts("host1,host2");

            domibusProxyService.getDomibusProxy();
            result = domibusProxy;

            domibusProxyService.useProxy();
            result = true;

            proxies.getObject("192.168.0.0", 1234, null, null, "host1,host2");
            result = new DefaultProxy("192.168.0.0", 1234, null, null, "host1,host2");
        }};

        //when
        DefaultProxy defaultProxy = dynamicDiscoveryServiceOASIS.getConfiguredProxy();

        //then
        Assert.assertNotNull(defaultProxy);
    }

    @Test
    public void testProxyNotConfigured() throws Exception {
        // Given
        new Expectations(dynamicDiscoveryServiceOASIS) {{
            domibusProxyService.useProxy();
            result = false;
        }};

        //when
        DefaultProxy defaultProxy = dynamicDiscoveryServiceOASIS.getConfiguredProxy();

        //then
        Assert.assertNull(defaultProxy);
    }

    @Test
    public void testCreateDynamicDiscoveryClientWithoutProxy() throws Exception {
        // Given

        new Expectations(dynamicDiscoveryServiceOASIS) {{
            domibusProxyService.useProxy();
            result = false;

            domibusPropertyProvider.getProperty(dynamicDiscoveryServiceOASIS.SMLZONE_KEY);
            result = "domibus.domain.ec.europa.eu";

            domibusPropertyProvider.getProperty(dynamicDiscoveryServiceOASIS.DYNAMIC_DISCOVERY_CERT_REGEX);
            result = "^.*$";
        }};

        //when
        DynamicDiscovery dynamicDiscovery = dynamicDiscoveryServiceOASIS.createDynamicDiscoveryClient();
        Assert.assertNotNull(dynamicDiscovery);
        DefaultProxy defaultProxy = (DefaultProxy) ReflectionTestUtils.getField(dynamicDiscovery.getService().getMetadataFetcher(), "proxyConfiguration");
        Assert.assertNull(defaultProxy);
    }

    @Test
    public void testLookupInformation(@Injectable TransportProfile transportProfile,
                                      @Injectable DynamicDiscovery smpClient,
                                      @Injectable Endpoint endpoint,
                                      @Injectable EndpointInfo endpointInfo,
                                      @Injectable ProcessIdentifier processIdentifier,
                                      @Injectable ServiceMetadata serviceMetadata) throws Exception {
        new Expectations(dynamicDiscoveryServiceOASIS) {{

            dynamicDiscoveryServiceOASIS.createDynamicDiscoveryClient();
            result = smpClient;

            smpClient.getServiceMetadata((ParticipantIdentifier) any, (DocumentIdentifier) any);
            result = serviceMetadata;

            serviceMetadata.getEndpoint((ProcessIdentifier) any, (TransportProfile) any);
            result = endpoint;

            endpoint.getAddress();
            result = "address";

            endpoint.getProcessIdentifier();
            result = processIdentifier;

            endpointInfos.getObject(any, any);
            result = endpointInfo;

            domibusProxyService.isProxyUserSet();
            result = false;

            domibusProxyService.isNonProxyHostsSet();
            result = false;

            domibusPropertyProvider.getProperty(DynamicDiscoveryService.SMLZONE_KEY);
            result = TEST_SML_ZONE;

            KeyStore truststore;
            truststore = KeyStore.getInstance("JKS");
            truststore.load(getClass().getResourceAsStream("../ehealth_smp_acc_truststore.jks"), TEST_KEYSTORE_PASSWORD.toCharArray());

            domainProvider.getCurrentDomain();
            result = DomainService.DEFAULT_DOMAIN;

            multiDomainCertificateProvider.getTrustStore(DomainService.DEFAULT_DOMAIN);
            result = truststore;

            domibusPropertyProvider.getProperty(DYNAMIC_DISCOVERY_TRANSPORTPROFILEAS4);
            result = "profile";

            transportProfiles.getObject("profile");
            result = transportProfile;

        }};

        EndpointInfo result = dynamicDiscoveryServiceOASIS.lookupInformation(DOMAIN, "0088:270420181111", "iso6523-actorid-upis", "busdox-docid-qns::lululu", "urn:www.cenbii.eu:profile:bii04:ver1.0", "cenbii-procid-ubl");

        LOG.info(result.getAddress());
        LOG.info(result.getCertificate().toString());
        Assert.assertNotNull(result);

    }

    @Test
    public void getPartyIdTypeTestForNull() {
        final String URN_TYPE_VALUE = "urn:oasis:names:tc:ebcore:partyid-type:unregistered";
        new Expectations() {{
            domibusPropertyProvider.getProperty(DYNAMICDISCOVERY_PARTYID_TYPE);
            result = null;
            times = 1;
        }};
        String partyIdType = dynamicDiscoveryServiceOASIS.getPartyIdType();
        Assert.assertEquals(partyIdType, URN_TYPE_VALUE);
    }

    @Test
    public void getPartyIdTypeTestForEmpty() {
        new Expectations() {{
            domibusPropertyProvider.getProperty(DYNAMICDISCOVERY_PARTYID_TYPE);
            result = "";
            times = 1;
        }};
        String partyIdType = dynamicDiscoveryServiceOASIS.getPartyIdType();
        Assert.assertNull(partyIdType);
    }

    @Test
    public void getResponderRoleTest() {
        final String DEFAULT_RESPONDER_ROLE = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder";

        new Expectations() {{
            domibusPropertyProvider.getProperty(DYNAMICDISCOVERY_PARTYID_RESPONDER_ROLE);
            result = "";
            times = 1;
        }};
        String responderRole = dynamicDiscoveryServiceOASIS.getResponderRole();

        Assert.assertEquals(responderRole, DEFAULT_RESPONDER_ROLE);
    }

    @Test(expected = ConfigurationException.class)
    public void testSmlZoneEmpty() throws EbMS3Exception {
        new Expectations() {{
            domibusPropertyProvider.getProperty(DynamicDiscoveryService.SMLZONE_KEY);
            result = "";
            times = 1;
        }};
        dynamicDiscoveryServiceOASIS.lookupInformation(DOMAIN, TEST_RECEIVER_ID, TEST_RECEIVER_ID_TYPE, TEST_ACTION_VALUE, TEST_SERVICE_VALUE, TEST_SERVICE_TYPE);
    }

    @Test(expected = ConfigurationException.class)
    public void testSmlZoneNull() throws EbMS3Exception {
        new Expectations() {{
            domibusPropertyProvider.getProperty(DynamicDiscoveryService.SMLZONE_KEY);
            result = null;
            times = 1;
        }};
        dynamicDiscoveryServiceOASIS.lookupInformation(DOMAIN, TEST_RECEIVER_ID, TEST_RECEIVER_ID_TYPE, TEST_ACTION_VALUE, TEST_SERVICE_VALUE, TEST_SERVICE_TYPE);
    }

}
