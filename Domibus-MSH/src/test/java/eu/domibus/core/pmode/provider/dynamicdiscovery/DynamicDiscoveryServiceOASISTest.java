package eu.domibus.core.pmode.provider.dynamicdiscovery;

import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.core.crypto.api.MultiDomainCryptoService;
import eu.domibus.core.proxy.DomibusProxy;
import eu.domibus.core.proxy.DomibusProxyService;
import eu.europa.ec.dynamicdiscovery.DynamicDiscovery;
import eu.europa.ec.dynamicdiscovery.core.fetcher.FetcherResponse;
import eu.europa.ec.dynamicdiscovery.core.security.impl.DefaultProxy;
import eu.europa.ec.dynamicdiscovery.exception.ConnectionException;
import eu.europa.ec.dynamicdiscovery.model.DocumentIdentifier;
import eu.europa.ec.dynamicdiscovery.model.ParticipantIdentifier;
import eu.europa.ec.dynamicdiscovery.model.ServiceMetadata;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.ServiceGroupType;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.ServiceMetadataType;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.SignedServiceMetadataType;
import org.springframework.test.util.ReflectionTestUtils;
import org.w3c.dom.Document;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.security.KeyStore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/*
 * @author Ioana Dragusanu (idragusa)
 * @since 3.2.5
 */

@RunWith(JMockit.class)
public class DynamicDiscoveryServiceOASISTest {

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
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    private MultiDomainCryptoService multiDomainCertificateProvider;

    @Injectable
    protected DomainContextProvider domainProvider;

    @Injectable
    DomibusConfigurationService domibusConfigurationService;

    @Injectable
    DomibusProxyService domibusProxyService;

    @Tested
    DynamicDiscoveryServiceOASIS dynamicDiscoveryServiceOASIS;

    @Test
    public void testLookupInformationMock(final @Capturing DynamicDiscovery smpClient) throws Exception {
        new NonStrictExpectations() {{
            domibusPropertyProvider.getProperty(DynamicDiscoveryService.SMLZONE_KEY);
            result = TEST_SML_ZONE;

            domibusPropertyProvider.getProperty(DynamicDiscoveryService.DYNAMIC_DISCOVERY_TRANSPORTPROFILEAS4);
            result = "bdxr-transport-ebms3-as4-v1p0";

            ServiceMetadata sm = buildServiceMetadata();
            smpClient.getServiceMetadata((ParticipantIdentifier) any, (DocumentIdentifier) any);
            result = sm;

        }};

        EndpointInfo endpoint = dynamicDiscoveryServiceOASIS.lookupInformation(DOMAIN, TEST_RECEIVER_ID, TEST_RECEIVER_ID_TYPE, TEST_ACTION_VALUE, TEST_SERVICE_VALUE, TEST_SERVICE_TYPE);
        assertNotNull(endpoint);
        assertEquals(ADDRESS, endpoint.getAddress());

        new Verifications() {{
            smpClient.getServiceMetadata((ParticipantIdentifier) any, (DocumentIdentifier) any);
        }};
    }

    @Test
    public void testLookupInformationRegexMatch(final @Capturing DynamicDiscovery smpClient) throws Exception {
        new NonStrictExpectations() {{
            domibusPropertyProvider.getProperty(DynamicDiscoveryService.SMLZONE_KEY);
            result = TEST_SML_ZONE;

            domibusPropertyProvider.getProperty(DynamicDiscoveryService.DYNAMIC_DISCOVERY_TRANSPORTPROFILEAS4);
            result = "bdxr-transport-ebms3-as4-v1p0";

            domibusPropertyProvider.getProperty(DynamicDiscoveryService.DYNAMIC_DISCOVERY_CERT_REGEX);
            result = "^.*EHEALTH_SMP.*$";

            ServiceMetadata sm = buildServiceMetadata();
            smpClient.getServiceMetadata((ParticipantIdentifier) any, (DocumentIdentifier) any);
            result = sm;

        }};

        EndpointInfo endpoint = dynamicDiscoveryServiceOASIS.lookupInformation(DOMAIN, TEST_RECEIVER_ID, TEST_RECEIVER_ID_TYPE, TEST_ACTION_VALUE, TEST_SERVICE_VALUE, TEST_SERVICE_TYPE);
        assertNotNull(endpoint);
        assertEquals(ADDRESS, endpoint.getAddress());

        new Verifications() {{
            smpClient.getServiceMetadata((ParticipantIdentifier) any, (DocumentIdentifier) any);
        }};
    }

    @Test
    public void testLookupInformationRegexNull(final @Capturing DynamicDiscovery smpClient) throws Exception {
        new NonStrictExpectations() {{
            domibusPropertyProvider.getProperty(DynamicDiscoveryService.SMLZONE_KEY);
            result = TEST_SML_ZONE;

            domibusPropertyProvider.getProperty(DynamicDiscoveryService.DYNAMIC_DISCOVERY_TRANSPORTPROFILEAS4);
            result = "bdxr-transport-ebms3-as4-v1p0";

            domibusPropertyProvider.getProperty(DynamicDiscoveryService.DYNAMIC_DISCOVERY_CERT_REGEX);
            result = null;

            ServiceMetadata sm = buildServiceMetadata();
            smpClient.getServiceMetadata((ParticipantIdentifier) any, (DocumentIdentifier) any);
            result = sm;

        }};

        EndpointInfo endpoint = dynamicDiscoveryServiceOASIS.lookupInformation(DOMAIN, TEST_RECEIVER_ID, TEST_RECEIVER_ID_TYPE, TEST_ACTION_VALUE, TEST_SERVICE_VALUE, TEST_SERVICE_TYPE);
        assertNotNull(endpoint);
        assertEquals(ADDRESS, endpoint.getAddress());

        new Verifications() {{
            smpClient.getServiceMetadata((ParticipantIdentifier) any, (DocumentIdentifier) any);
        }};
    }


    @Test(expected = ConfigurationException.class)
    public void testLookupInformationNotFound(final @Capturing DynamicDiscovery smpClient) throws Exception {
        new NonStrictExpectations() {{
            domibusPropertyProvider.getProperty(DynamicDiscoveryService.SMLZONE_KEY);
            result = TEST_SML_ZONE;

            ServiceMetadata sm = buildServiceMetadata();
            smpClient.getServiceMetadata((ParticipantIdentifier) any, (DocumentIdentifier) any);
            result = sm;
        }};

        dynamicDiscoveryServiceOASIS.lookupInformation(DOMAIN, TEST_RECEIVER_ID, TEST_RECEIVER_ID_TYPE, TEST_ACTION_VALUE, TEST_INVALID_SERVICE_VALUE, TEST_SERVICE_TYPE);
    }

    @Test
    public void testLookupInformationNotFoundMessage(final @Capturing DynamicDiscovery smpClient) throws Exception {
        new NonStrictExpectations() {{
            domibusPropertyProvider.getProperty(DynamicDiscoveryService.SMLZONE_KEY);
            result = TEST_SML_ZONE;

            ServiceMetadata sm = buildServiceMetadata();
            smpClient.getServiceMetadata((ParticipantIdentifier) any, (DocumentIdentifier) any);
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
        Object result = ((JAXBElement) unmarshaller.unmarshal(document)).getValue();
        SignedServiceMetadataType signedServiceMetadataType = (SignedServiceMetadataType) result;
        ServiceMetadata serviceMetadata = new ServiceMetadata(signedServiceMetadataType, null, "");
        return serviceMetadata;
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
    public void testCreateDynamicDiscoveryClientWithProxy() throws Exception {
        // Given

        new Expectations(dynamicDiscoveryServiceOASIS) {{
            DomibusProxy domibusProxy = new DomibusProxy();
            domibusProxy.setEnabled(true);
            domibusProxy.setHttpProxyHost("192.168.0.0");
            domibusProxy.setHttpProxyPort(1234);
            domibusProxy.setHttpProxyUser("proxyUser");
            domibusProxy.setHttpProxyPassword("proxyPassword");

            domibusProxyService.getDomibusProxy();
            result = domibusProxy;

            domibusProxyService.useProxy();
            result = true;

            domibusPropertyProvider.getProperty(dynamicDiscoveryServiceOASIS.SMLZONE_KEY);
            result = "domibus.domain.ec.europa.eu";

            domibusPropertyProvider.getProperty(dynamicDiscoveryServiceOASIS.DYNAMIC_DISCOVERY_CERT_REGEX);
            result = "^.*$";
        }};

        //when
        DynamicDiscovery dynamicDiscovery = dynamicDiscoveryServiceOASIS.createDynamicDiscoveryClient();
        Assert.assertNotNull(dynamicDiscovery);
        DefaultProxy defaultProxy = (DefaultProxy) ReflectionTestUtils.getField(dynamicDiscovery.getService().getMetadataFetcher(), "proxyConfiguration");
        Assert.assertNotNull(defaultProxy);
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

    private void assertNullForMissingParameters() throws ConnectionException {
        //when
        DefaultProxy defaultProxy = dynamicDiscoveryServiceOASIS.getConfiguredProxy();

        //then
        Assert.assertNull(defaultProxy);
    }

    // This is not a unit tests but the code is useful to test real SMP entries.
    @Test
    @Ignore
    public void testLookupInformation() throws Exception {
        new NonStrictExpectations() {{
            domibusProxyService.isProxyUserSet();
            result = false;

            domibusProxyService.isNonProxyHostsSet();
            result = false;

            domibusPropertyProvider.getProperty(DynamicDiscoveryService.SMLZONE_KEY);
            result = TEST_SML_ZONE;

            KeyStore truststore;
            truststore = KeyStore.getInstance("JKS");
            truststore.load(getClass().getResourceAsStream("../ehealth_smp_acc_truststore.jks"), TEST_KEYSTORE_PASSWORD.toCharArray());

            multiDomainCertificateProvider.getTrustStore(DomainService.DEFAULT_DOMAIN);
            result = truststore;

        }};

        // This entry is valid
        //EndpointInfo endpointInfo = dynamicDiscoveryServiceOASIS.lookupInformation("0007:9340033829test2", "ehealth-actorid-qns", "busdox-docid-qns::urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:www.cenbii.eu:transaction:biitrns014:ver2.0:extended:urn:www.peppol.eu:bis:peppol5a:ver2.0::2.1", "urn:www.cenbii.eu:profile:bii05:ver2.0", "cenbii-procid-ubl");

        // This entry is valid but has no certificate
        //EndpointInfo endpointInfo = dynamicDiscoveryServiceOASIS.lookupInformation("0007:9340033829dev1", "ehealth-actorid-qns", "busdox-docid-qns::urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:www.cenbii.eu:transaction:biitrns010:ver2.0:extended:urn:www.peppol.eu:bis:peppol5a:ver2.0::2.2", "urn:www.cenbii.eu:profile:bii05:ver2.0", "cenbii-procid-ubl");

        //TEST Service
        //EndpointInfo endpointInfo = dynamicDiscoveryServiceOASIS.lookupInformation("0007:9340033829test2", "ehealth-actorid-qns", "busdox-docid-qns::urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:www.cenbii.eu:transaction:biitrns014:ver2.0:extended:urn:www.peppol.eu:bis:peppol5a:ver2.0::2.1", "urn:www.cenbii.eu:profile:bii05:ver2.0", "cenbii-procid-ubl");

        EndpointInfo endpointInfo = dynamicDiscoveryServiceOASIS.lookupInformation(DOMAIN, "0088:270420181111", "iso6523-actorid-upis", "busdox-docid-qns::lululu", "urn:www.cenbii.eu:profile:bii04:ver1.0", "cenbii-procid-ubl");

        // Support Issue
        //EndpointInfo endpointInfo = dynamicDiscoveryServiceOASIS.lookupInformation("dynceftestparty13gw", "connectivity-partid-qns", "connectivity-docid-qns::doc_id1", "urn:www.cenbii.eu:profile:bii04:ver1.0", "connectivity-docid-qns");

        System.out.println(endpointInfo.getAddress());
        System.out.println(endpointInfo.getCertificate());
        Assert.assertNotNull(endpointInfo);
    }

    @Test
    public void getPartyIdTypeTest() {
        final String URN_TYPE_VALUE = "urn:oasis:names:tc:ebcore:partyid-type:unregistered";
        new Expectations() {{
            domibusPropertyProvider.getProperty(DYNAMICDISCOVERY_PARTYID_TYPE);
            result = "";
            times = 1;
        }};
        String partyIdType = dynamicDiscoveryServiceOASIS.getPartyIdType();
        Assert.assertEquals(partyIdType, URN_TYPE_VALUE);
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
