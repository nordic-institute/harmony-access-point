package eu.domibus.core.pmode.provider.dynamicdiscovery;

import eu.domibus.api.pki.CertificateService;
import eu.domibus.core.proxy.ProxyUtil;
import eu.europa.ec.dynamicdiscovery.core.fetcher.impl.DefaultURLFetcher;
import eu.europa.ec.dynamicdiscovery.core.locator.impl.DefaultBDXRLocator;
import eu.europa.ec.dynamicdiscovery.core.reader.impl.DefaultBDXRReader;
import eu.europa.ec.dynamicdiscovery.core.security.impl.DefaultProxy;
import eu.europa.ec.dynamicdiscovery.core.security.impl.DefaultSignatureValidator;
import eu.europa.ec.dynamicdiscovery.exception.ConnectionException;
import eu.europa.ec.dynamicdiscovery.model.*;
import no.difi.vefa.peppol.lookup.locator.BusdoxLocator;
import no.difi.vefa.peppol.mode.Mode;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.security.KeyStore;
import java.security.cert.X509Certificate;

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
    public DomibusApacheFetcher domibusApacheFetcher(Mode mode, ProxyUtil proxyUtil, DomibusHttpRoutePlanner domibusHttpRoutePlanner) {
        return new DomibusApacheFetcher(mode, proxyUtil, domibusHttpRoutePlanner);
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public BusdoxLocator busdoxLocator(String smlInfo) {
        return new BusdoxLocator(smlInfo);
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public DomibusCertificateValidator domibusCertificateValidator(CertificateService certificateService, KeyStore trustStore, String certRegex) {
        return new DomibusCertificateValidator(certificateService, trustStore, certRegex);
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public DefaultBDXRLocator bdxrLocator(String smlInfo) {
        return new DefaultBDXRLocator(smlInfo);
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public DefaultURLFetcher urlFetcher(DomibusHttpRoutePlanner domibusHttpRoutePlanner, DefaultProxy proxy) {
        return new DefaultURLFetcher(domibusHttpRoutePlanner, proxy);
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public DefaultBDXRReader bdxrReader(DefaultSignatureValidator defaultSignatureValidator) {
        return new DefaultBDXRReader(defaultSignatureValidator);
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public DefaultSignatureValidator defaultSignatureValidator(DomibusCertificateValidator domibusCertificateValidator) {
        return new DefaultSignatureValidator(domibusCertificateValidator);
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public DocumentIdentifier documentIdentifier(String identifier, String scheme) {
        return new DocumentIdentifier(identifier, scheme);
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public ParticipantIdentifier participantIdentifier(String identifier, String scheme) {
        return new ParticipantIdentifier(identifier, scheme);
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public ProcessIdentifier processIdentifier(String identifier, String scheme) {
        return new ProcessIdentifier(identifier, scheme);
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public TransportProfile transportProfile(String identifier) {
        return new TransportProfile(identifier);
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
