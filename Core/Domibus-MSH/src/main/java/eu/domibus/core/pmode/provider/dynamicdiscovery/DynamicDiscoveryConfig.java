package eu.domibus.core.pmode.provider.dynamicdiscovery;

import eu.domibus.api.pki.CertificateService;
import eu.europa.ec.dynamicdiscovery.core.extension.IExtension;
import eu.europa.ec.dynamicdiscovery.core.fetcher.IMetadataFetcher;
import eu.europa.ec.dynamicdiscovery.core.fetcher.impl.DefaultURLFetcher;
import eu.europa.ec.dynamicdiscovery.core.locator.impl.DefaultBDXRLocator;
import eu.europa.ec.dynamicdiscovery.core.provider.impl.DefaultProvider;
import eu.europa.ec.dynamicdiscovery.core.reader.IMetadataReader;
import eu.europa.ec.dynamicdiscovery.core.reader.impl.DefaultBDXRReader;
import eu.europa.ec.dynamicdiscovery.core.security.impl.DefaultProxy;
import eu.europa.ec.dynamicdiscovery.core.security.impl.DefaultSignatureValidator;
import eu.europa.ec.dynamicdiscovery.enums.DNSLookupType;
import eu.europa.ec.dynamicdiscovery.exception.ConnectionException;
import eu.europa.ec.dynamicdiscovery.model.SMPTransportProfile;
import eu.europa.ec.dynamicdiscovery.model.identifiers.SMPDocumentIdentifier;
import eu.europa.ec.dynamicdiscovery.model.identifiers.SMPParticipantIdentifier;
import eu.europa.ec.dynamicdiscovery.model.identifiers.SMPProcessIdentifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * A configuration containing definitions for dynamic discovery services.
 *
 * @author Sebastian-Ion TINCU
 * @since 5.0
 */
@Configuration
public class DynamicDiscoveryConfig {

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public DomibusCertificateValidator domibusCertificateValidator(CertificateService certificateService, KeyStore trustStore, String certRegex, List<String> allowedCertificatePolicyOIDs) {
        return new DomibusCertificateValidator(certificateService, trustStore, certRegex, allowedCertificatePolicyOIDs);
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public DefaultBDXRLocator bdxrLocator(String smlInfo, List<DNSLookupType> dnsLookupTypes) {
        return new DefaultBDXRLocator.Builder()
                .addDnsLookupTypes(dnsLookupTypes)
                .addTopDnsDomain(smlInfo)
                .build();
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public DefaultProvider defaultProvider(IMetadataFetcher metadataFetcher, IMetadataReader metadataReader, List<String> wildcardSchemes) {
        return new DefaultProvider.Builder()
                .metadataFetcher(metadataFetcher)
                .metadataReader(metadataReader)
                .wildcardSchemes(wildcardSchemes)
                .build();
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public DefaultURLFetcher urlFetcher(DomibusHttpRoutePlanner domibusHttpRoutePlanner, DefaultProxy proxy) {
        return new DefaultURLFetcher(domibusHttpRoutePlanner, proxy);
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public DefaultBDXRReader bdxrReader(DefaultSignatureValidator defaultSignatureValidator, List<IExtension> extensions) {
        return new DefaultBDXRReader.Builder()
                .addExtensions(extensions)
                .signatureValidator(defaultSignatureValidator)
                .build();
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public DefaultSignatureValidator defaultSignatureValidator(DomibusCertificateValidator domibusCertificateValidator) {
        return new DefaultSignatureValidator(domibusCertificateValidator);
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public SMPDocumentIdentifier documentIdentifier(String identifier, String scheme) {
        return new SMPDocumentIdentifier(identifier, scheme);
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public SMPParticipantIdentifier participantIdentifier(String identifier, String scheme) {
        return new SMPParticipantIdentifier(identifier, scheme);
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public SMPProcessIdentifier processIdentifier(String identifier, String scheme) {
        return new SMPProcessIdentifier(identifier, scheme);
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public SMPTransportProfile transportProfile(String identifier) {
        return new SMPTransportProfile(identifier);
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public DefaultProxy proxy(String serverAddress, int serverPort, String user, String password, String nonProxyHosts) throws ConnectionException {
        return new DefaultProxy(serverAddress, serverPort, user, password, nonProxyHosts);
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public EndpointInfo endpointInfo(String address, X509Certificate certificate) {
        return new EndpointInfo(address, certificate);
    }


}
