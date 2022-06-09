package eu.domibus.core.pmode.provider.dynamicdiscovery;

import eu.domibus.api.multitenancy.DomainContextProvider;
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

import static eu.domibus.core.pmode.provider.dynamicdiscovery.DynamicDiscoveryService.DYNAMIC_DISCOVERY_TRANSPORTPROFILEAS4;
import static org.junit.Assert.*;

/**
 * @author Ioana Dragusanu (idragusa)
 * @author Sebastian-Ion TINCU
 * @since 3.2.5
 */
//@Ignore("EDELIVERY-8892")
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


    public void setupBasicLookupConditions() throws Exception {
        new Expectations() {{
            domibusPropertyProvider.getProperty(DynamicDiscoveryService.SMLZONE_KEY);
            result = TEST_SML_ZONE;

            domibusCertificateValidators.getObject(any, any, anyString, any);
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
        }};
    }

    public void setupLookupConditions(DynamicDiscovery smpClient) throws Exception {
        setupBasicLookupConditions();

        new Expectations() {{

            domibusPropertyProvider.getProperty(DYNAMIC_DISCOVERY_TRANSPORTPROFILEAS4);
            result = "bdxr-transport-ebms3-as4-v1p0";

            endpointInfo.getAddress();
            result = ADDRESS;

            smpClient.getServiceMetadata(participantIdentifier, documentIdentifier);
            result = buildServiceMetadata();

            endpointInfos.getObject(anyString, any);
            result = endpointInfo;
        }};
    }

    @Test
    public void testLookupInformationMock(final @Capturing DynamicDiscovery smpClient) throws Exception {
        setupLookupConditions(smpClient);

        EndpointInfo endpoint = dynamicDiscoveryServiceOASIS.lookupInformation(DOMAIN, TEST_RECEIVER_ID, TEST_RECEIVER_ID_TYPE, TEST_ACTION_VALUE, TEST_SERVICE_VALUE, TEST_SERVICE_TYPE);
        assertNotNull(endpoint);
        assertEquals(ADDRESS, endpoint.getAddress());

        new Verifications() {{
            smpClient.getServiceMetadata(participantIdentifier, documentIdentifier);
        }};
    }

    @Test
    public void testLookupInformationProcessNotExits(@Injectable DynamicDiscovery smpClient) throws Exception {
        setupBasicLookupConditions();

        new Expectations() {{
            domibusPropertyProvider.getProperty(DYNAMIC_DISCOVERY_TRANSPORTPROFILEAS4);
            result = "bdxr-transport-ebms3-as4-v1p0";

        }};

        //when
        ConfigurationException exception = assertThrows(ConfigurationException.class,
                () -> dynamicDiscoveryServiceOASIS.lookupInformation(DOMAIN, TEST_RECEIVER_ID, TEST_RECEIVER_ID_TYPE, TEST_ACTION_VALUE, "SomeService", TEST_SERVICE_TYPE));

        // then
        assertEquals("Could not fetch metadata for: urn:romania:ncpb ehealth-actorid-qns ehealth-resid-qns:urn::epsos##services:extended:epsos::107 SomeService ehealth-procid-qns using the AS4 Protocol bdxr-transport-ebms3-as4-v1p0", exception.getMessage());
    }

    @Test
    public void testLookupInformationRegexMatch(final @Capturing DynamicDiscovery smpClient) throws Exception {
        setupLookupConditions(smpClient);

        new Expectations() {{
            domibusPropertyProvider.getProperty(DynamicDiscoveryService.DYNAMIC_DISCOVERY_CERT_REGEX);
            result = "^.*EHEALTH_SMP.*$";

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
        setupLookupConditions(smpClient);

        new Expectations() {{

            domibusPropertyProvider.getProperty(DynamicDiscoveryService.DYNAMIC_DISCOVERY_CERT_REGEX);
            result = null;
        }};

        EndpointInfo endpoint = dynamicDiscoveryServiceOASIS.lookupInformation(DOMAIN, TEST_RECEIVER_ID, TEST_RECEIVER_ID_TYPE, TEST_ACTION_VALUE, TEST_SERVICE_VALUE, TEST_SERVICE_TYPE);
        assertNotNull(endpoint);
        assertEquals(ADDRESS, endpoint.getAddress());

        new Verifications() {{
            smpClient.getServiceMetadata(participantIdentifier, documentIdentifier);
        }};
    }


    @Test
    public void testLookupInformationNotFound(final @Capturing DynamicDiscovery smpClient) throws Exception {
        setupBasicLookupConditions();
        new Expectations() {{
            domibusPropertyProvider.getProperty(DYNAMIC_DISCOVERY_TRANSPORTPROFILEAS4);
            result = "bdxr-transport-ebms3-as4-v1p0";

            smpClient.getServiceMetadata(participantIdentifier, documentIdentifier);
            result = buildServiceMetadata();
        }};

        //when
        ConfigurationException exception = assertThrows(ConfigurationException.class,
                () -> dynamicDiscoveryServiceOASIS.lookupInformation(DOMAIN, TEST_RECEIVER_ID, TEST_RECEIVER_ID_TYPE, TEST_ACTION_VALUE, TEST_INVALID_SERVICE_VALUE, TEST_SERVICE_TYPE));
        // then
        assertEquals("Could not fetch metadata for: urn:romania:ncpb ehealth-actorid-qns ehealth-resid-qns:urn::epsos##services:extended:epsos::107 invalidServiceValue ehealth-procid-qns using the AS4 Protocol bdxr-transport-ebms3-as4-v1p0", exception.getMessage());

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
                                && "192.168.0.0" .equals(serverAddress)
                                && 1234 == serverPort
                                && "proxyUser" .equals(user)
                                && "proxyPassword" .equals(password)
                                && "host1,host2" .equals(nonProxyHosts)
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
                                && "192.168.0.0" .equals(serverAddress)
                                && 1234 == serverPort
                                && user == null
                                && password == null
                                && "host1,host2" .equals(nonProxyHosts)
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
    public void testCreateDynamicDiscoveryClientWithoutProxy(final @Capturing DynamicDiscovery smpClient) throws Exception {
        // Given

        new Expectations(dynamicDiscoveryServiceOASIS) {{
            domibusProxyService.useProxy();
            result = false;

            domibusPropertyProvider.getProperty(DynamicDiscoveryService.SMLZONE_KEY);
            result = TEST_SML_ZONE;

            domibusCertificateValidators.getObject(any, any, anyString, any);
            result = domibusCertificateValidator;

            urlFetchers.getObject(domibusHttpRoutePlanner, any);
            result = defaultURLFetcher;

            bdxrLocators.getObject(anyString);
            result = defaultBDXRLocator;

            signatureValidators.getObject(any);
            result = defaultSignatureValidator;

            bdxrReaders.getObject(any);
            result = defaultBDXRReader;
        }};

        //when
        DynamicDiscovery dynamicDiscovery = dynamicDiscoveryServiceOASIS.createDynamicDiscoveryClient();
        Assert.assertNotNull(dynamicDiscovery);
        DefaultProxy defaultProxy = (DefaultProxy) ReflectionTestUtils.getField(dynamicDiscovery.getService().getMetadataFetcher(), "proxyConfiguration");
        Assert.assertNull(defaultProxy);
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
