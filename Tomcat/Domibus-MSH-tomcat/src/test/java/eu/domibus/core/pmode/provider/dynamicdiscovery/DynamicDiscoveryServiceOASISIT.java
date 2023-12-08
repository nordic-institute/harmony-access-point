package eu.domibus.core.pmode.provider.dynamicdiscovery;

import eu.domibus.AbstractIT;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.pki.MultiDomainCryptoService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.proxy.DomibusProxyService;
import eu.domibus.api.security.AuthenticationException;
import eu.domibus.api.security.X509CertificateService;
import eu.domibus.core.certificate.CertificateTestUtils;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.europa.ec.dynamicdiscovery.core.fetcher.FetcherResponse;
import eu.europa.ec.dynamicdiscovery.core.fetcher.impl.DefaultURLFetcher;
import eu.europa.ec.dynamicdiscovery.core.locator.impl.DefaultBDXRLocator;
import eu.europa.ec.dynamicdiscovery.core.reader.impl.DefaultBDXRReader;
import eu.europa.ec.dynamicdiscovery.core.security.impl.DefaultProxy;
import eu.europa.ec.dynamicdiscovery.core.security.impl.DefaultSignatureValidator;
import eu.europa.ec.dynamicdiscovery.exception.TechnicalException;
import eu.europa.ec.dynamicdiscovery.model.DocumentIdentifier;
import eu.europa.ec.dynamicdiscovery.model.ParticipantIdentifier;
import eu.europa.ec.dynamicdiscovery.model.ServiceMetadata;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.EndpointType;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.ServiceGroupType;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.ServiceMetadataType;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.SignedServiceMetadataType;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.annotation.DirtiesContext;
import org.w3c.dom.Document;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.security.cert.CertificateEncodingException;
import java.util.Calendar;
import java.util.Date;

import static eu.domibus.core.crypto.MultiDomainCryptoServiceImpl.DOMIBUS_KEYSTORE_NAME;
import static eu.domibus.core.crypto.MultiDomainCryptoServiceImpl.DOMIBUS_TRUSTSTORE_NAME;

/**
 * @author François Gautier
 * @version 5.1
 * @since 27-12-22
 */
@DirtiesContext
public class DynamicDiscoveryServiceOASISIT extends AbstractIT {


    private static ServiceMetadata serviceMetadata;
    private static ServiceMetadata expired_serviceMetadata;

    @Configuration
    static class ContextConfiguration {
        @Primary
        @Bean
        public DynamicDiscoveryServiceOASIS dynamicDiscoveryServiceOASIS(DomibusPropertyProvider domibusPropertyProvider,
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
            return new DynamicDiscoveryServiceOASIS(domibusPropertyProvider,
                    domainProvider,
                    multiDomainCertificateProvider,
                    certificateService,
                    domibusProxyService,
                    domibusHttpRoutePlanner,
                    x509CertificateService,
                    documentIdentifiers,
                    participantIdentifiers,
                    proxies,
                    bdxrLocators,
                    domibusCertificateValidators,
                    urlFetchers,
                    bdxrReaders,
                    signatureValidators,
                    endpointInfos,
                    dynamicDiscoveryUtil) {
                @Override
                protected eu.europa.ec.dynamicdiscovery.model.ServiceMetadata getServiceMetadata(
                        String participantId,
                        String participantIdScheme,
                        String documentId,
                        String processId,
                        String processIdScheme) throws EbMS3Exception, TechnicalException {
                    if (StringUtils.contains(participantId, "expired")) {
                        return expired_serviceMetadata;
                    }
                    return serviceMetadata;
                }

            };
        }
    }

    @Autowired
    private DynamicDiscoveryServiceOASIS dynamicDiscoveryServiceOASIS;

    @BeforeClass
    public static void beforeClass() throws Exception {
        serviceMetadata = buildServiceMetadata();
        expired_serviceMetadata = buildServiceMetadata();

        expired_serviceMetadata.getOriginalServiceMetadata().getServiceMetadata().getServiceInformation().getProcessList().getProcess().get(0).getServiceEndpointList().getEndpoint().remove(0);
        expired_serviceMetadata.getOriginalServiceMetadata().getServiceMetadata().getServiceInformation().getProcessList().getProcess().get(0).getServiceEndpointList().getEndpoint().add(getExpiredEndpointType());
    }

    private static EndpointType getExpiredEndpointType() throws CertificateEncodingException {
        EndpointType e = new EndpointType();
        e.setCertificate(CertificateTestUtils.loadCertificateFromJKSFile("keystores/expired_gateway_keystore.jks", "red_gw", "test123").getEncoded());
        e.setTransportProfile("bdxr-transport-ebms3-as4-v1p0");

        e.setServiceActivationDate(getCalendarAddDays(-1));
        e.setServiceExpirationDate(getCalendarAddDays(1));

        e.setEndpointURI("expired");
        return e;
    }

    private static Calendar getCalendarAddDays(int amount) {
        Calendar start = Calendar.getInstance();
        start.setTime(DateUtils.addDays(new Date(), amount));
        return start;
    }

    @Before
    public void setUp() throws Exception {
        uploadPmode(SERVICE_PORT);
        createStore(DOMIBUS_TRUSTSTORE_NAME, "keystores/gateway_truststore.jks");
        createStore(DOMIBUS_KEYSTORE_NAME, "keystores/gateway_keystore.jks");
    }

    @Test(expected = AuthenticationException.class)
    public void lookupInformation_expired() throws EbMS3Exception {
        dynamicDiscoveryServiceOASIS.lookupInformation("domain",
                "participantId_expired",
                "participantIdScheme",
                "scheme::value",
                "urn:epsosPatientService::List",
                "ehealth-procid-qns");
    }

    @Test
    @Ignore //EDELIVERY-10865
    public void lookupInformation() throws EbMS3Exception {
        dynamicDiscoveryServiceOASIS.lookupInformation("domain",
                "participantId",
                "participantIdScheme",
                "scheme::value",
                "urn:epsosPatientService::List",
                "ehealth-procid-qns");
    }

    private static ServiceMetadata buildServiceMetadata() throws Exception {
        InputStream inputStream = DynamicDiscoveryServiceOASISIT.class.getClassLoader().getResourceAsStream("dynamicDiscovery/SignedServiceMetadataResponseOASIS.xml");
        FetcherResponse fetcherResponse = new FetcherResponse(inputStream);
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        JAXBContext jaxbContext = JAXBContext.newInstance(ServiceMetadataType.class, SignedServiceMetadataType.class, ServiceGroupType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        Document document = documentBuilderFactory.newDocumentBuilder().parse(fetcherResponse.getInputStream());
        SignedServiceMetadataType signedServiceMetadataType = ((JAXBElement<SignedServiceMetadataType>) unmarshaller.unmarshal(document)).getValue();
        return new ServiceMetadata(signedServiceMetadataType, null, "");
    }
}
