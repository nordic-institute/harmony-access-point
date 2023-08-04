package eu.domibus.core.pmode.provider.dynamicdiscovery;

import eu.domibus.AbstractIT;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.pki.MultiDomainCryptoService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthenticationException;
import eu.domibus.api.security.X509CertificateService;
import eu.domibus.core.certificate.CertificateTestUtils;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.proxy.ProxyUtil;
import network.oxalis.vefa.peppol.common.lang.PeppolLoadingException;
import network.oxalis.vefa.peppol.common.lang.PeppolParsingException;
import network.oxalis.vefa.peppol.common.model.*;
import network.oxalis.vefa.peppol.lookup.api.LookupException;
import network.oxalis.vefa.peppol.security.lang.PeppolSecurityException;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.net.URI;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import static eu.domibus.core.crypto.MultiDomainCryptoServiceImpl.DOMIBUS_KEYSTORE_NAME;
import static eu.domibus.core.crypto.MultiDomainCryptoServiceImpl.DOMIBUS_TRUSTSTORE_NAME;

/**
 * @author François Gautier
 * @version 5.1
 * @since 27-12-22
 */
public class DynamicDiscoveryServicePEPPOLIT extends AbstractIT {


    private static ServiceMetadata serviceMetadata;
    private static ServiceMetadata expired_serviceMetadata;

    @Configuration
    static class ContextConfiguration {
        @Primary
        @Bean
        public DynamicDiscoveryServicePEPPOL dynamicDiscoveryServicePEPPOL(DomibusPropertyProvider domibusPropertyProvider,
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
            return new DynamicDiscoveryServicePEPPOL(domibusPropertyProvider,
                    multiDomainCertificateProvider,
                    domainProvider,
                    proxyUtil,
                    certificateService,
                    domibusHttpRoutePlanner,
                    x509CertificateService,
                    domibusCertificateValidators,
                    busdoxLocators,
                    domibusApacheFetchers,
                    endpointInfos,
                    dynamicDiscoveryUtil) {
                @Override
                protected network.oxalis.vefa.peppol.common.model.ServiceMetadata getServiceMetadata(
                        String participantId,
                        String participantIdScheme,
                        String documentId,
                        String smlInfo,
                        String mode,
                        DomibusCertificateValidator domibusSMPCertificateValidator) throws PeppolLoadingException, LookupException, PeppolSecurityException, PeppolParsingException {
                    if (StringUtils.contains(participantId, "expired")) {
                        return expired_serviceMetadata;
                    }
                    return serviceMetadata;
                }

            };
        }
    }

    @Autowired
    private DynamicDiscoveryServicePEPPOL dynamicDiscoveryServicePEPPOL;

    @BeforeClass
    public static void beforeClass() {
        serviceMetadata = buildServiceMetadata(CertificateTestUtils.loadCertificateFromJKSFile("keystores/gateway_keystore.jks", "red_gw", "test123"));
        expired_serviceMetadata = buildServiceMetadata(CertificateTestUtils.loadCertificateFromJKSFile("keystores/expired_gateway_keystore.jks", "red_gw", "test123"));
    }

    @Before
    public void setUp() throws Exception {
        uploadPmode(SERVICE_PORT);
        createStore(DOMIBUS_TRUSTSTORE_NAME, "keystores/gateway_truststore.jks");
        createStore(DOMIBUS_KEYSTORE_NAME, "keystores/gateway_keystore.jks");
    }

    @Test(expected = AuthenticationException.class)
    public void lookupInformation_expired() throws EbMS3Exception {
        dynamicDiscoveryServicePEPPOL.lookupInformation("domain",
                "participantId_expired",
                "participantIdScheme",
                "scheme::value",
                "urn:epsosPatientService::List",
                "ehealth-procid-qns");
    }

    @Test
    public void lookupInformation() throws EbMS3Exception {
        dynamicDiscoveryServicePEPPOL.lookupInformation("domain",
                "participantId",
                "participantIdScheme",
                "scheme::value",
                "urn:epsosPatientService::List",
                "ehealth-procid-qns");
    }

    private static ServiceMetadata buildServiceMetadata(X509Certificate certificate) {

        ProcessIdentifier processIdentifier;
        try {
            processIdentifier = ProcessIdentifier.parse("urn:epsosPatientService::List");
        } catch (PeppolParsingException e) {
            return null;
        }

        Endpoint endpoint = Endpoint.of(TransportProfile.of(TransportProfile.AS4.getIdentifier()), URI.create("http://localhost:9090/anonymous/msh"), certificate);

        List<ProcessMetadata<Endpoint>> processes = new ArrayList<>();
        ProcessMetadata<Endpoint> process = ProcessMetadata.of(processIdentifier, endpoint);
        processes.add(process);

        return ServiceMetadata.of(ServiceInformation.of(null, null, processes));
    }
}
