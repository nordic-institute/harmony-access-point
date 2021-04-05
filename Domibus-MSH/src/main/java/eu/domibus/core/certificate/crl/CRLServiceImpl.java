package eu.domibus.core.certificate.crl;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.cache.DomibusCacheService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_CERTIFICATE_CRL_EXCLUDED_PROTOCOLS;

@Service
public class CRLServiceImpl implements CRLService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CRLServiceImpl.class);

    public static final String CRL_EXCLUDED_PROTOCOLS = DOMIBUS_CERTIFICATE_CRL_EXCLUDED_PROTOCOLS;

    @Autowired
    protected CRLUtil crlUtil;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private DomibusCacheService domibusCacheService;

    private volatile List<String> supportedCrlProtocols;

    private Object supportedCrlProtocolsLock = new Object();

    @Override
    @Cacheable(value = DomibusCacheService.CRL_BY_CERT, key = "{#cert.issuerX500Principal.getName(), #cert.serialNumber}")
    public boolean isCertificateRevoked(X509Certificate cert) throws DomibusCRLException {
        List<String> crlDistributionPoints = getCrlDistributionPoints(cert);

        LOG.debug("CRL check for certificate: [{}]", getSubjectDN(cert));
        if (crlDistributionPoints == null || crlDistributionPoints.isEmpty()) {
            LOG.debug("No CRL distribution points found for certificate: [{}]", getSubjectDN(cert));
            return false;
        }

        List<String> supportedCrlDistributionPoints = getSupportedCrlDistributionPoints(crlDistributionPoints);
        if (supportedCrlDistributionPoints.isEmpty()) {
            LOG.debug("No supported CRL distribution point found for certificate " + getSubjectDN(cert));
            return false;
        }

        for (String crlDistributionPointUrl : supportedCrlDistributionPoints) {
            try {
                // once checked, stop checking, no matter if the outcome was true or false
                return isCertificateRevoked(cert, crlDistributionPointUrl);
            } catch (DomibusCRLException ex) {
                LOG.warn("Could not check certificate against CRL url [{}]", crlDistributionPointUrl, ex);
                continue; // for clarity: continue with the next CRL url, until one usable is found
            }
        }

        throw new DomibusCRLException("Could not check certificate " + getSubjectDN(cert) + " against any CRL distribution point");
    }

    /**
     * Extracts all CRL distribution point URLs from the "CRL Distribution Point" extension of X.509 pki.
     * If the CRL distribution point extension is unavailable, returns an empty list.
     *
     * @param cert a X509 certificate
     * @return the list of CRL urls of this certificate
     */
    public List<String> getCrlDistributionPoints(X509Certificate cert) {
        byte[] crldpExt = cert.getExtensionValue(org.bouncycastle.asn1.x509.Extension.cRLDistributionPoints.getId());
        if (crldpExt == null) {
            return new ArrayList<>();
        }

        ASN1Primitive derObjCrlDP = null;
        try (ASN1InputStream oAsnInStream = new ASN1InputStream(new ByteArrayInputStream(crldpExt))) {
            derObjCrlDP = oAsnInStream.readObject();
        } catch (IOException e) {
            throw new DomibusCRLException("Error while extracting CRL distribution point URLs", e);
        }

        DEROctetString dosCrlDP = (DEROctetString) derObjCrlDP;
        byte[] crldpExtOctets = dosCrlDP.getOctets();

        ASN1Primitive derObj2 = null;
        try (ASN1InputStream oAsnInStream2 = new ASN1InputStream(new ByteArrayInputStream(crldpExtOctets))) {
            derObj2 = oAsnInStream2.readObject();
        } catch (IOException e) {
            throw new DomibusCRLException("Error while extracting CRL distribution point URLs", e);
        }

        CRLDistPoint distPoint = CRLDistPoint.getInstance(derObj2);
        List<String> crlUrls = new ArrayList<>();
        for (DistributionPoint dp : distPoint.getDistributionPoints()) {
            DistributionPointName dpn = dp.getDistributionPoint();
            // Look for URIs in fullName
            if (dpn != null && dpn.getType() == DistributionPointName.FULL_NAME) {
                GeneralName[] genNames = GeneralNames.getInstance(dpn.getName()).getNames();
                // Look for an URI
                for (int index = 0; index < genNames.length; index++) {
                    if (genNames[index].getTagNo() == GeneralName.uniformResourceIdentifier) {
                        String url = DERIA5String.getInstance(genNames[index].getName()).getString();
                        crlUrls.add(url);
                    }
                }
            }
        }
        return crlUrls;
    }

    protected String getSubjectDN(X509Certificate cert) {
        if (cert != null && cert.getSubjectDN() != null) {
            return cert.getSubjectDN().getName();
        }
        return null;
    }

    protected List<String> getSupportedCrlDistributionPoints(List<String> crlDistributionPoints) {
        List<String> result = new ArrayList<>();
        if (crlDistributionPoints == null || crlDistributionPoints.isEmpty()) {
            return result;
        }

        for (String crlDistributionPoint : crlDistributionPoints) {
            if (isURLSupported(crlDistributionPoint)) {
                result.add(crlDistributionPoint);
            } else {
                LOG.debug("The protocol of the distribution endpoint is not supported: " + crlDistributionPoint);
            }
        }

        return result;
    }

    protected boolean isCertificateRevoked(X509Certificate cert, String crlDistributionPointURL) {
        X509CRL crl = crlUtil.downloadCRL(crlDistributionPointURL);
        LOG.debug("Downloaded CRL is [{}]", crl.getIssuerDN().getName());
        if (crl.isRevoked(cert)) {
            LOG.warn("The certificate is revoked by CRL: " + crlDistributionPointURL);
            return true;
        }
        return false;
    }

    private boolean isURLSupported(final String crlURL) {
        if (!CRLUrlType.isURLSupported(crlURL)) {
            return false;
        }
        for (String crlProtocol : getSupportedCrlProtocols()) {
            if (crlURL.toLowerCase().startsWith(crlProtocol)) {
                return true;
            }
        }
        return false;
    }

    private List<String> getSupportedCrlProtocols() {
        if (supportedCrlProtocols == null) {
            synchronized (supportedCrlProtocolsLock) {
                if (supportedCrlProtocols == null) {
                    List<String> list = Arrays.stream(CRLUrlType.values()).map(c -> c.getPrefix()).collect(Collectors.toList());
                    final String excludedProtocolsList = domibusPropertyProvider.getProperty(CRL_EXCLUDED_PROTOCOLS);
                    if (!StringUtils.isEmpty(excludedProtocolsList)) {
                        List<String> excluded = Arrays.stream(excludedProtocolsList.split(",")).map(p -> p.trim() + "://").collect(Collectors.toList());
                        list.removeAll(excluded);
                    }
                    supportedCrlProtocols = list;
                }
            }
        }
        return supportedCrlProtocols;
    }

    public void resetCacheCrlProtocols() {
        LOG.debug("Clearing supported Crl protocols and cache.");
        this.supportedCrlProtocols = null;
        this.domibusCacheService.clearCache(DomibusCacheService.CRL_BY_CERT);
    }

}
