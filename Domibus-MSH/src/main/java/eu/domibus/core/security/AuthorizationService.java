package eu.domibus.core.security;

import com.google.common.collect.Lists;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.crypto.spi.AuthorizationServiceSpi;
import eu.domibus.core.crypto.spi.PullRequestPmodeData;
import eu.domibus.core.crypto.spi.model.AuthorizationError;
import eu.domibus.core.crypto.spi.model.AuthorizationException;
import eu.domibus.core.crypto.spi.model.UserMessagePmodeData;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.ebms3.common.model.PullRequest;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.core.certificate.CertificateExchangeType;
import eu.domibus.ext.domain.PullRequestDTO;
import eu.domibus.ext.domain.UserMessageDTO;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_EXTENSION_IAM_AUTHORIZATION_IDENTIFIER;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_SENDER_TRUST_VALIDATION_ONRECEIVING;

/**
 * @author Thomas Dussart
 * @since 4.1
 * <p>
 * Authorization service that will extract data from the SoapMessage before delegating the authorization
 * call to the configured SPI.
 */
@Component
public class AuthorizationService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AuthorizationService.class);

    protected static final String IAM_AUTHORIZATION_IDENTIFIER = DOMIBUS_EXTENSION_IAM_AUTHORIZATION_IDENTIFIER;

    @Autowired
    private List<AuthorizationServiceSpi> authorizationServiceSpis;

    @Autowired
    private CertificateService certificateService;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private DomainCoreConverter domainCoreConverter;

    @Autowired
    private PModeProvider pModeProvider;

    protected AuthorizationServiceSpi getAuthorizationService() {
        final String authorizationServiceIdentifier = domibusPropertyProvider.getProperty(IAM_AUTHORIZATION_IDENTIFIER);
        final List<AuthorizationServiceSpi> authorizationServiceList = this.authorizationServiceSpis.stream().
                filter(authorizationServiceSpi -> authorizationServiceIdentifier.equals(authorizationServiceSpi.getIdentifier())).
                collect(Collectors.toList());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Authorization spi:");
            authorizationServiceList.forEach(authorizationServiceSpi -> LOG.debug(" identifier:[{}] for class:[{}]", authorizationServiceSpi.getIdentifier(), authorizationServiceSpi.getClass()));
        }

        if (authorizationServiceList.size() > 1) {
            throw new AuthorizationException(AuthorizationError.AUTHORIZATION_MODULE_CONFIGURATION_ISSUE, String.format("More than one authorization service provider for identifier:[%s]", authorizationServiceIdentifier));
        }
        if (authorizationServiceList.isEmpty()) {
            throw new AuthorizationException(AuthorizationError.AUTHORIZATION_MODULE_CONFIGURATION_ISSUE, String.format("No authorisation service provider found for given identifier:[%s]", authorizationServiceIdentifier));
        }
        return authorizationServiceList.get(0);
    }

    public void authorizePullRequest(SOAPMessage request, PullRequest pullRequest) throws EbMS3Exception {
        if (!isAuthorizationEnabled(request)) {
            return;
        }
        final CertificateTrust certificateTrust = getCertificateTrust(request);
        final PullRequestPmodeData pullRequestPmodeData;
        try {
            pullRequestPmodeData = pModeProvider.getPullRequestMapping(pullRequest);
        } catch (EbMS3Exception e) {
            throw new AuthorizationException(e);
        }
        getAuthorizationService().authorize(certificateTrust.getTrustChain(), certificateTrust.getSigningCertificate(),
                domainCoreConverter.convert(pullRequest, PullRequestDTO.class), pullRequestPmodeData);
    }

    public void authorizeUserMessage(SOAPMessage request, UserMessage userMessage) throws EbMS3Exception {
        if (!isAuthorizationEnabled(request)) {
            return;
        }
        final CertificateTrust certificateTrust = getCertificateTrust(request);
        final UserMessagePmodeData userMessagePmodeData= pModeProvider.getUserMessagePmodeData(userMessage);

        getAuthorizationService().authorize(certificateTrust.getTrustChain(), certificateTrust.getSigningCertificate(),
                domainCoreConverter.convert(userMessage, UserMessageDTO.class), userMessagePmodeData);

    }

    private boolean isAuthorizationEnabled(SOAPMessage request) {
        if (!domibusPropertyProvider.getBooleanProperty(DOMIBUS_SENDER_TRUST_VALIDATION_ONRECEIVING)) {
            LOG.debug("No trust verification of sending certificate");
            return false;
        }
        final CertificateExchangeType certificateExchangeType = getCertificateExchangeTypeFromSoapMessage(request);
        if (CertificateExchangeType.NONE.equals(certificateExchangeType)) {
            LOG.debug("Message has no security configured, skipping authorization");
            return false;
        }
        return true;
    }

    private CertificateTrust getCertificateTrust(SOAPMessage request) {
        final List<X509Certificate> x509Certificates = getCertificatesFromSoapMessage(request);
        X509Certificate leafCertificate = (X509Certificate) certificateService.extractLeafCertificateFromChain(x509Certificates);
        final List<X509Certificate> signingCertificateTrustChain = Lists.newArrayList(x509Certificates);
        signingCertificateTrustChain.remove(leafCertificate);
        return new CertificateTrust(leafCertificate, signingCertificateTrustChain);
    }

    private List<X509Certificate> getCertificatesFromSoapMessage(SOAPMessage request) {
        String certificateChainValue;
        try {
            certificateChainValue = (String) request.getProperty(CertificateExchangeType.getValue());
        } catch (SOAPException e) {
            throw new AuthorizationException(AuthorizationError.AUTHORIZATION_OTHER, String.
                    format("At this stage, the property:[%s] of the soap message should contain a certificate", CertificateExchangeType.getValue()), e);
        }
        return certificateService.deserializeCertificateChainFromPemFormat(certificateChainValue);

    }

    private CertificateExchangeType getCertificateExchangeTypeFromSoapMessage(SOAPMessage request) {
        String certificateExchangeTypeValue;
        try {
            certificateExchangeTypeValue = (String) request.getProperty(CertificateExchangeType.getKey());
        } catch (SOAPException e) {
            throw new AuthorizationException(AuthorizationError.AUTHORIZATION_OTHER, String.
                    format("At this stage, the property:[%s] of the soap message should contain a certificate", CertificateExchangeType.getValue()), e);
        }

        try {
            return CertificateExchangeType.valueOf(certificateExchangeTypeValue);
        } catch (IllegalArgumentException e) {
            throw new AuthorizationException(AuthorizationError.AUTHORIZATION_OTHER, String.format("Invalid certificate exchange type:[%s]", certificateExchangeTypeValue), e);
        }
    }

}
