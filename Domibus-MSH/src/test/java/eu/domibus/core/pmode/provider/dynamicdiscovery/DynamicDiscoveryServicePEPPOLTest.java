package eu.domibus.core.pmode.provider.dynamicdiscovery;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.crypto.api.MultiDomainCryptoService;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.core.proxy.ProxyUtil;
import mockit.*;
import mockit.integration.junit4.JMockit;
import no.difi.vefa.peppol.common.lang.PeppolParsingException;
import no.difi.vefa.peppol.common.model.*;
import no.difi.vefa.peppol.lookup.LookupClient;
import no.difi.vefa.peppol.mode.Mode;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URI;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import static eu.domibus.core.certificate.CertificateTestUtils.loadCertificateFromJKSFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


@RunWith(JMockit.class)
public class DynamicDiscoveryServicePEPPOLTest {

    private static final String RESOURCE_PATH = "src/test/resources/eu/domibus/ebms3/common/dao/DynamicDiscoveryPModeProviderTest/";
    private static final String DYNAMICDISCOVERY_PARTYID_TYPE = "domibus.dynamicdiscovery.partyid.type";
    private static final String TEST_KEYSTORE = "testkeystore.jks";

    //The (sub)domain of the SML, e.g. acc.edelivery.tech.ec.europa.eu
    //private static final String TEST_SML_ZONE = "isaitb.acc.edelivery.tech.ec.europa.eu";
    private static final String TEST_SML_ZONE = "acc.edelivery.tech.ec.europa.eu";

    private static final String ALIAS_CN_AVAILABLE = "cn_available";
    private static final String TEST_KEYSTORE_PASSWORD = "1234";

    private static final String TEST_RECEIVER_ID = "0088:unknownRecipient";
    private static final String TEST_RECEIVER_ID_TYPE = "iso6523-actorid-upis";
    private static final String TEST_ACTION_VALUE = "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:www.cenbii.eu:transaction:biitrns014:ver2.0:extended:urn:www.peppol.eu:bis:peppol5a:ver2.0::2.1";
    private static final String TEST_SERVICE_VALUE = "scheme::serviceValue";
    private static final String TEST_SERVICE_TYPE = "serviceType";
    private static final String TEST_INVALID_SERVICE_VALUE = "invalidServiceValue";
    private static final String DOMAIN = "default";

    private static final String ADDRESS = "http://localhost:9090/anonymous/msh";

    @Injectable
    protected DomainContextProvider domainProvider;

    @Injectable
    private MultiDomainCryptoService multiDomainCertificateProvider;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    private CertificateService certificateService;

    @Injectable
    ProxyUtil proxyUtil;

    @Injectable
    DomibusConfigurationService domibusConfigurationService;

    @Tested
    private DynamicDiscoveryServicePEPPOL dynamicDiscoveryServicePEPPOL;

    private String transportProfileAS4;

    @Test
    public void testLookupInformationMock(final @Capturing LookupClient smpClient) throws Exception {
        new NonStrictExpectations() {{
            domibusPropertyProvider.getProperty(DynamicDiscoveryService.SMLZONE_KEY);
            result = TEST_SML_ZONE;

            domibusPropertyProvider.getProperty(DynamicDiscoveryService.DYNAMIC_DISCOVERY_MODE);
            result = Mode.TEST;

            transportProfileAS4 = TransportProfile.AS4.getIdentifier();
            ServiceMetadata sm = buildServiceMetadata();
            smpClient.getServiceMetadata((ParticipantIdentifier) any, (DocumentTypeIdentifier) any);
            result = sm;

            domibusPropertyProvider.getProperty(DynamicDiscoveryService.DYNAMIC_DISCOVERY_TRANSPORTPROFILEAS4);
            result = transportProfileAS4;
        }};

        EndpointInfo endpoint = dynamicDiscoveryServicePEPPOL.lookupInformation(DOMAIN, TEST_RECEIVER_ID, TEST_RECEIVER_ID_TYPE, TEST_ACTION_VALUE, TEST_SERVICE_VALUE, TEST_SERVICE_TYPE);
        assertNotNull(endpoint);
        assertEquals(ADDRESS, endpoint.getAddress());

        new Verifications() {{
            smpClient.getServiceMetadata((ParticipantIdentifier) any, (DocumentTypeIdentifier) any);
        }};
    }

    @Test
    public void testLookupInformationMockOtherTransportProfile(final @Capturing LookupClient smpClient) throws Exception {
        new NonStrictExpectations() {{
            domibusPropertyProvider.getProperty(DynamicDiscoveryService.SMLZONE_KEY);
            result = TEST_SML_ZONE;

            domibusPropertyProvider.getProperty(DynamicDiscoveryService.DYNAMIC_DISCOVERY_MODE);
            result = Mode.TEST;

            transportProfileAS4 = "AS4_other_transport_profile";
            ServiceMetadata sm = buildServiceMetadata();
            smpClient.getServiceMetadata((ParticipantIdentifier) any, (DocumentTypeIdentifier) any);
            result = sm;

            domibusPropertyProvider.getProperty(DynamicDiscoveryService.DYNAMIC_DISCOVERY_TRANSPORTPROFILEAS4);
            result = transportProfileAS4;
        }};

        EndpointInfo endpoint = dynamicDiscoveryServicePEPPOL.lookupInformation(DOMAIN, TEST_RECEIVER_ID, TEST_RECEIVER_ID_TYPE, TEST_ACTION_VALUE, TEST_SERVICE_VALUE, TEST_SERVICE_TYPE);
        assertNotNull(endpoint);
        assertEquals(ADDRESS, endpoint.getAddress());

        new Verifications() {{
            smpClient.getServiceMetadata((ParticipantIdentifier) any, (DocumentTypeIdentifier) any);
        }};
    }

    @Test
    public void getPartyIdTypeTestForNull() {
        final String URN_TYPE_VALUE = "urn:fdc:peppol.eu:2017:identifiers:ap";
        new Expectations() {{
            domibusPropertyProvider.getProperty(DYNAMICDISCOVERY_PARTYID_TYPE);
            result = null;
            times = 1;
        }};
        String partyIdType = dynamicDiscoveryServicePEPPOL.getPartyIdType();
        Assert.assertEquals(partyIdType, URN_TYPE_VALUE);
    }

    @Test
    public void getPartyIdTypeTestForEmpty() {
        new Expectations() {{
            domibusPropertyProvider.getProperty(DYNAMICDISCOVERY_PARTYID_TYPE);
            result = "";
            times = 1;
        }};
        String partyIdType = dynamicDiscoveryServicePEPPOL.getPartyIdType();
        Assert.assertNull(partyIdType);
    }


    @Test(expected = ConfigurationException.class)
    public void testLookupInformationNotFound(final @Capturing LookupClient smpClient) throws Exception {
        new NonStrictExpectations() {{
            domibusPropertyProvider.getProperty(DynamicDiscoveryService.SMLZONE_KEY);
            result = TEST_SML_ZONE;

            domibusPropertyProvider.getProperty(DynamicDiscoveryService.DYNAMIC_DISCOVERY_MODE);
            result = Mode.TEST;

            transportProfileAS4 = TransportProfile.AS4.getIdentifier();
            ServiceMetadata sm = buildServiceMetadata();
            smpClient.getServiceMetadata((ParticipantIdentifier) any, (DocumentTypeIdentifier) any);
            result = sm;

        }};

        dynamicDiscoveryServicePEPPOL.lookupInformation(DOMAIN, TEST_RECEIVER_ID, TEST_RECEIVER_ID_TYPE, TEST_ACTION_VALUE, TEST_INVALID_SERVICE_VALUE, TEST_SERVICE_TYPE);
    }


    private ServiceMetadata buildServiceMetadata() {

        X509Certificate testData = loadCertificateFromJKSFile(RESOURCE_PATH + TEST_KEYSTORE, ALIAS_CN_AVAILABLE, TEST_KEYSTORE_PASSWORD);
        ProcessIdentifier processIdentifier;
        try {
            processIdentifier = ProcessIdentifier.parse(TEST_SERVICE_VALUE);
        } catch (PeppolParsingException e) {
            return null;
        }

        Endpoint endpoint = Endpoint.of(TransportProfile.of(transportProfileAS4), URI.create(ADDRESS), testData);

        List<ProcessMetadata<Endpoint>> processes = new ArrayList<>();
        ProcessMetadata<Endpoint> process = ProcessMetadata.of(processIdentifier, endpoint);
        processes.add(process);

        ServiceMetadata sm = ServiceMetadata.of(null, null, processes);
        return sm;
    }

    @Test
    public void testGetDocumentTypeIdentifierWithScheme() throws PeppolParsingException {
        String documentId = "busdox-docid-qns::urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:www.cenbii.eu:transaction:biitrns010:ver2.0:extended:urn:www.peppol.eu:bis:peppol5a:ver2.0::2.1";
        dynamicDiscoveryServicePEPPOL.getDocumentTypeIdentifier(documentId);

        new Verifications() {{
            DocumentTypeIdentifier.parse(documentId);
        }};
    }

    @Test
    public void testGetDocumentTypeIdentifier() throws PeppolParsingException {
        String documentId = "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:www.cenbii.eu:transaction:biitrns010:ver2.0:extended:urn:www.peppol.eu:bis:peppol5a:ver2.0::2.1";
        dynamicDiscoveryServicePEPPOL.getDocumentTypeIdentifier(documentId);

        new Verifications() {{
            DocumentTypeIdentifier.of(documentId);
        }};
    }

    @Test
    public void testGeProcessIdentifierWithScheme() throws PeppolParsingException {
        String processId = "cenbii-procid-ubl::urn:www.cenbii.eu:profile:bii05:ver2.0";
        dynamicDiscoveryServicePEPPOL.getProcessIdentifier(processId);

        new Verifications() {{
            ProcessIdentifier.parse(processId);
        }};
    }

    @Test
    public void testGeProcessIdentifier() throws PeppolParsingException {
        String processId = "urn:www.cenbii.eu:profile:bii05:ver2.0";
        dynamicDiscoveryServicePEPPOL.getProcessIdentifier(processId);

        new Verifications() {{
            ProcessIdentifier.of(processId);
        }};
    }

    @Test(expected = ConfigurationException.class)
    public void testSmlZoneEmpty() throws EbMS3Exception {
        new Expectations() {{
            domibusPropertyProvider.getProperty(DynamicDiscoveryService.SMLZONE_KEY);
            result = "";
            times = 1;
        }};
        dynamicDiscoveryServicePEPPOL.lookupInformation(DOMAIN, TEST_RECEIVER_ID, TEST_RECEIVER_ID_TYPE, TEST_ACTION_VALUE, TEST_SERVICE_VALUE, TEST_SERVICE_TYPE);
    }

    @Test(expected = ConfigurationException.class)
    public void testSmlZoneNull() throws EbMS3Exception {
        new Expectations() {{
            domibusPropertyProvider.getProperty(DynamicDiscoveryService.SMLZONE_KEY);
            result = null;
            times = 1;
        }};
        dynamicDiscoveryServicePEPPOL.lookupInformation(DOMAIN, TEST_RECEIVER_ID, TEST_RECEIVER_ID_TYPE, TEST_ACTION_VALUE, TEST_SERVICE_VALUE, TEST_SERVICE_TYPE);
    }

}
