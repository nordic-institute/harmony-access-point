package eu.domibus.core.crypto.spi;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.pki.MultiDomainCryptoService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.RegexUtil;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.core.crypto.spi.model.AuthorizationError;
import eu.domibus.core.crypto.spi.model.AuthorizationException;
import eu.domibus.core.crypto.spi.model.UserMessagePmodeData;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.message.MessageExchangeService;
import eu.domibus.core.message.pull.PullContext;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.pmode.provider.dynamicdiscovery.DynamicDiscoveryService;
import eu.domibus.ext.domain.PullRequestDTO;
import eu.domibus.ext.domain.UserMessageDTO;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

/**
 * @author Thomas Dussart, Ioana Dragusanu
 * @since 4.1
 * <p>
 * Default authorization implementation.
 */
@Component
public class DefaultAuthorizationServiceSpiImpl implements AuthorizationServiceSpi {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultAuthorizationServiceSpiImpl.class);

    protected static final String DEFAULT_IAM_AUTHORIZATION_IDENTIFIER = "DEFAULT_AUTHORIZATION_SPI";

    @Autowired
    private MessageExchangeService messageExchangeService;

    @Autowired
    private PModeProvider pModeProvider;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private RegexUtil regexUtil;

    @Autowired
    MultiDomainCryptoService multiDomainCertificateProvider;

    @Autowired
    DomainContextProvider domainProvider;

    @Autowired
    CertificateService certificateService;

    /**
     * {@inheritDoc}
     */
    @Override
    public void authorize(List<X509Certificate> signingCertificateTrustChain, X509Certificate signingCertificate, UserMessageDTO userMessageDTO, UserMessagePmodeData userMessagePmodeData) throws AuthorizationException {
        doAuthorize(signingCertificate, userMessagePmodeData.getPartyName());
    }

    /**
     * {@inheritDoc}
     */
    public void authorize(List<X509Certificate> signingCertificateTrustChain, X509Certificate signingCertificate, PullRequestDTO pullRequestDTO, PullRequestPmodeData pullRequestPmodeData) throws AuthorizationException {
        String mpc = pullRequestPmodeData.getMpcName();
        if (mpc == null) {
            LOG.error("Mpc is null, cannot authorize against a null mpc");
            throw new AuthorizationException(AuthorizationError.AUTHORIZATION_OTHER, "Mpc is null, cannot authorize against a null mpc");
        }

        String mpcQualified;
        try {
            mpcQualified = pModeProvider.findMpcUri(mpc);
        } catch (EbMS3Exception e) {
            LOG.error("Could not find mpc [{}]", mpc, e);
            throw new AuthorizationException(AuthorizationError.AUTHORIZATION_OTHER, "Could not find mpc " + mpc, e);
        }

        PullContext pullContext = messageExchangeService.extractProcessOnMpc(mpcQualified);
        if (pullContext == null || pullContext.getProcess() == null) {
            LOG.error("Could not extract process on mpc [{}]", mpc);
            throw new AuthorizationException(AuthorizationError.AUTHORIZATION_OTHER, "Could not extract process on mpc [{}]" + mpc);
        }

        if (CollectionUtils.isEmpty(pullContext.getProcess().getInitiatorParties()) ||
                pullContext.getProcess().getInitiatorParties().size() > 1) {
            LOG.error("Default authorization of Pull Request requires one initiator per pull process");
            throw new AuthorizationException(AuthorizationError.AUTHORIZATION_REJECTED, "Default authorization of Pull Request requires one initiator per pull process");
        }
        Party initiator = pullContext.getProcess().getInitiatorParties().iterator().next();
        String initiatorName = initiator.getName();

        doAuthorize(signingCertificate, initiatorName);
    }

    protected void doAuthorize(X509Certificate signingCertificate, String initiatorName) {
        authorizeAgainstTruststoreAlias(signingCertificate, initiatorName);
        authorizeAgainstCertificateSubjectExpression(signingCertificate);
        authorizeAgainstCertificateCNMatch(signingCertificate, initiatorName);
        authorizeAgainstCertificatePolicyMatch(signingCertificate, initiatorName);
    }


    protected void authorizeAgainstTruststoreAlias(X509Certificate signingCertificate, String alias) {
        if (!domibusPropertyProvider.getBooleanProperty(DOMIBUS_SENDER_TRUST_VALIDATION_TRUSTSTORE_ALIAS)) {
            LOG.debug("Sender certificate verification is disabled");
            return;
        }

        if (BooleanUtils.isTrue(domibusPropertyProvider.getBooleanProperty(DynamicDiscoveryService.USE_DYNAMIC_DISCOVERY))) {
            LOG.debug("Sender certificate verification is disabled in dynamic discovery mode");
            return;
        }

        LOG.debug("Authorize against certificate extracted based on the alias [{}] from the truststore", alias);
        if (signingCertificate == null) {
            LOG.debug("Signing certificate is not provided.");
            return;
        }
        LOG.debug("Signing certificate: [{}]", signingCertificate);
        try {
            X509Certificate cert = multiDomainCertificateProvider.getCertificateFromTruststore(domainProvider.getCurrentDomain(), alias);
            if (cert == null) {
                LOG.error("Failed to get the certificate based on the partyName [{}]", alias);
                throw new AuthorizationException(AuthorizationError.AUTHORIZATION_REJECTED, "Could not find the certificate in the truststore");
            }
            LOG.debug("Truststore certificate: [{}]", cert);

            if (!signingCertificate.equals(cert)) {
                String excMessage = "Signing certificate and truststore certificate do not match.";
                excMessage += String.format("Signing certificate: %s", signingCertificate);
                excMessage += String.format("Truststore certificate: %s", cert);
                LOG.error(excMessage);
                throw new AuthorizationException(AuthorizationError.AUTHORIZATION_REJECTED, excMessage);
            }
        } catch (KeyStoreException e) {
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_005, "Failed to get certificate from truststore", e);
        }
    }

    protected void authorizeAgainstCertificateSubjectExpression(X509Certificate signingCertificate) {
        LOG.debug("Authorize against certificate subject");
        if (signingCertificate == null) {
            LOG.debug("Signing certificate is not provided.");
            return;
        }

        String subject = signingCertificate.getSubjectDN().getName();
        String certSubjectExpression = domibusPropertyProvider.getProperty(DOMIBUS_SENDER_TRUST_VALIDATION_EXPRESSION);
        if (StringUtils.isEmpty(certSubjectExpression)) {
            LOG.debug("[{}] is empty, verification is disabled.", DOMIBUS_SENDER_TRUST_VALIDATION_EXPRESSION);
            return;
        }
        LOG.debug("Property [{}], value [{}]", DOMIBUS_SENDER_TRUST_VALIDATION_EXPRESSION, certSubjectExpression);
        if (!regexUtil.matches(certSubjectExpression, subject)) {
            String excMessage = String.format("Certificate subject [%s] does not match the regular expression configured [%s]", subject, certSubjectExpression);
            LOG.error(excMessage);
            throw new AuthorizationException(AuthorizationError.AUTHORIZATION_REJECTED, excMessage);
        }
    }

    protected void authorizeAgainstCertificateCNMatch(X509Certificate signingCertificate, String alias) {
        if (!domibusPropertyProvider.getBooleanProperty(DOMIBUS_SENDER_CERTIFICATE_SUBJECT_CHECK)) {
            LOG.debug("Sender alias verification is disabled");
            return;
        }

        LOG.debug("Authorize against certificate CN with alias [{}]", alias);
        if (signingCertificate == null) {
            LOG.debug("Signing certificate is not provided.");
            return;
        }

        if (StringUtils.containsIgnoreCase(signingCertificate.getSubjectDN().getName(), alias)) {
            LOG.info("Sender [{}] is trusted", alias);
            return;
        }
        String excMessage = String.format("Sender alias verification failed. Signing certificate CN does not contain the alias [%s]: %s ", alias, signingCertificate);
        throw new AuthorizationException(AuthorizationError.AUTHORIZATION_REJECTED, excMessage);
    }

    protected void authorizeAgainstCertificatePolicyMatch(X509Certificate signingCertificate, String alias) {

        LOG.debug("Authorize against certificate Policy");
        if (signingCertificate == null) {
            LOG.debug("Signing certificate is not provided.");
            return;
        }

        String propertyValue = trimToEmpty(domibusPropertyProvider.getProperty(DOMIBUS_SENDER_TRUST_VALIDATION_CERTIFICATE_POLICY_OIDS));
        if (StringUtils.isBlank(propertyValue)) {
            LOG.debug("[{}] is empty, certificate policy verification is disabled.", DOMIBUS_SENDER_TRUST_VALIDATION_CERTIFICATE_POLICY_OIDS);
            return;
        }
        // allowed list
        List<String> allowedCertificatePolicyOIDList = Arrays.asList(propertyValue.split("\\s*,\\s*"));
        // certificate list
        List<String> certPolicyList = certificateService.getCertificatePolicyIdentifiers(signingCertificate);
        if (certPolicyList.isEmpty()) {
            String excMessage = String.format("Sender alias verification failed. Signing certificate for the alias [%s] has empty CertificatePolicy extension. Certificate: %s ", alias, signingCertificate);
            throw new AuthorizationException(AuthorizationError.AUTHORIZATION_REJECTED, excMessage);
        }

        Optional<String> result = certPolicyList.stream().filter(certPolicyOID -> allowedCertificatePolicyOIDList.contains(certPolicyOID)).findFirst();
        if (result.isPresent()) {
            LOG.info("Sender [{}] is trusted with certificate policy [{}]", alias, result.get());
            return;
        }
        String excMessage = String.format("Sender certificate policy verification failed. Signing certificate [%s] does not contain any of the required certificate policies: [%s]", alias, signingCertificate);
        throw new AuthorizationException(AuthorizationError.AUTHORIZATION_REJECTED, excMessage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIdentifier() {
        return DEFAULT_IAM_AUTHORIZATION_IDENTIFIER;
    }
}
