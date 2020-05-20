package eu.domibus.core.crypto.spi;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.RegexUtil;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.core.message.MessageExchangeService;
import eu.domibus.core.message.pull.PullContext;
import eu.domibus.core.crypto.spi.model.AuthorizationError;
import eu.domibus.core.crypto.spi.model.AuthorizationException;
import eu.domibus.core.crypto.spi.model.UserMessagePmodeData;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.ext.domain.PullRequestDTO;
import eu.domibus.ext.domain.UserMessageDTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManager.*;

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
    private CertificateService certificateService;

    @Autowired
    private MessageExchangeService messageExchangeService;

    @Autowired
    private PModeProvider pModeProvider;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private DomainContextProvider domainContextProvider;

    @Autowired
    private RegexUtil regexUtil;

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
    @Override
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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIdentifier() {
        return DEFAULT_IAM_AUTHORIZATION_IDENTIFIER;
    }

    protected void authorizeAgainstTruststoreAlias(X509Certificate signingCertificate, String alias) {
        if (!domibusPropertyProvider.getBooleanProperty(DOMIBUS_SENDER_TRUST_VALIDATION_TRUSTSTORE_ALIAS)) {
            LOG.debug("Sender certificate verification is disabled");
            return;
        }

        LOG.debug("Authorize against certificate extracted based on the alias [{}] from the truststore", alias);
        if (signingCertificate == null) {
            LOG.debug("Signing certificate is not provided.");
            return;
        }
        LOG.debug("Signing certificate: [%s]", signingCertificate);
        try {
            X509Certificate cert = certificateService.getPartyX509CertificateFromTruststore(alias);
            if (cert == null) {
                LOG.warn("Failed to get the certificate based on the partyName [{}]. No further authorization against truststore is performed.", alias);
                return;
            }
            LOG.debug("Truststore certificate: [%s]", cert);

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
}
