package eu.domibus.core.crypto;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.core.crypto.api.DomainCryptoService;
import eu.domibus.core.crypto.spi.DomainCryptoServiceSpi;
import eu.domibus.core.crypto.spi.model.AuthenticationError;
import eu.domibus.core.crypto.spi.model.AuthenticationException;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.xml.ws.WebServiceException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_EXTENSION_IAM_AUTHENTICATION_IDENTIFIER;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_SECURITY_TRUSTSTORE_TYPE;
import static eu.domibus.core.crypto.spi.AbstractCryptoServiceSpi.DEFAULT_AUTHENTICATION_SPI;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
public class DomainCryptoServiceImpl extends BaseDomainCryptoServiceImpl implements DomainCryptoService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomainCryptoServiceImpl.class);

    protected static final String IAM_AUTHENTICATION_IDENTIFIER = DOMIBUS_EXTENSION_IAM_AUTHENTICATION_IDENTIFIER;

    @Autowired
    private List<DomainCryptoServiceSpi> domainCryptoServiceSpiList;

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;

//    public DomainCryptoServiceImpl() {
//    }

    public DomainCryptoServiceImpl(Domain domain) {
        super(domain);
    }

    @PostConstruct
    public void init() {
        String spiIdentifier = domibusPropertyProvider.getProperty(domain, IAM_AUTHENTICATION_IDENTIFIER);
        if (spiIdentifier.equals(DEFAULT_AUTHENTICATION_SPI) && domainCryptoServiceSpiList.size() > 1) {
            LOG.warn("A custom authentication implementation has been provided but property:[{}}] is configured with default value:[{}]",
                    DOMIBUS_EXTENSION_IAM_AUTHENTICATION_IDENTIFIER, spiIdentifier);
        }
        final List<DomainCryptoServiceSpi> providerList = domainCryptoServiceSpiList.stream().
                filter(domainCryptoServiceSpi -> spiIdentifier.equals(domainCryptoServiceSpi.getIdentifier())).
                collect(Collectors.toList());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Authentication spi:");
            providerList.forEach(domainCryptoServiceSpi -> LOG.debug(" identifier:[{}] for class:[{}]", domainCryptoServiceSpi.getIdentifier(), domainCryptoServiceSpi.getClass()));
        }

        if (providerList.size() > 1) {
            throw new IllegalStateException(String.format("More than one authentication service provider for identifier:[%s]", spiIdentifier));
        }
        if (providerList.isEmpty()) {
            throw new IllegalStateException(String.format("No authentication service provider found for given identifier:[%s]", spiIdentifier));
        }

        iamProvider = providerList.get(0);
        super.init(iamProvider);

        LOG.info("Active IAM provider identifier:[{}] for domain:[{}]", iamProvider.getIdentifier(), domain.getName());
    }

    @Override
    public void verifyTrust(X509Certificate[] certs, boolean enableRevocation, Collection<Pattern> subjectCertConstraints, Collection<Pattern> issuerCertConstraints) throws WSSecurityException {
        try {
            iamProvider.verifyTrust(certs, enableRevocation, subjectCertConstraints, issuerCertConstraints);
        } catch (AuthenticationException e) {
            if (e.getCause() != null) {
                throw e;
            }
            LOG.error("Certificate validation error", e);
            AuthenticationError authenticationError = e.getAuthenticationError();
            switch (authenticationError) {
                case EBMS_0101:
                    EbMS3Exception ebMS3Ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0101, "Sender certificate is not valid or has been revoked", LOG.getMDC(DomibusLogger.MDC_MESSAGE_ID), e);
                    ebMS3Ex.setMshRole(MSHRole.RECEIVING);
                    throw new WebServiceException(ebMS3Ex);
                default:
                    throw new WebServiceException(new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0004, "unknown error occurred", LOG.getMDC(DomibusLogger.MDC_MESSAGE_ID), e));
            }
        }
    }

    @Override
    public String getTrustStoreType() {
        return domibusPropertyProvider.getProperty(domain, DOMIBUS_SECURITY_TRUSTSTORE_TYPE);
    }

    // used only in tests
    protected void setDomainCryptoServiceSpiList(List<DomainCryptoServiceSpi> domainCryptoServiceSpiList) {
        this.domainCryptoServiceSpiList = domainCryptoServiceSpiList;
    }

    @Override
    public void reset() {
        this.init();
    }
}
