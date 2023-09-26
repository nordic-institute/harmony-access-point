package eu.domibus.core.pmode.provider.dynamicdiscovery;

import java.security.cert.X509Certificate;


/**
 * @author Cosmin Baciu
 * @since 5.1.1
 */
public class PartyEndpointInfo {

    protected String certificateCn;
    protected X509Certificate x509Certificate;
    protected String endpointUrl;


    public PartyEndpointInfo(String certificateCn, X509Certificate x509Certificate, String endpointUrl) {
        this.certificateCn = certificateCn;
        this.x509Certificate = x509Certificate;
        this.endpointUrl = endpointUrl;
    }

    public String getCertificateCn() {
        return certificateCn;
    }

    public void setCertificateCn(String certificateCn) {
        this.certificateCn = certificateCn;
    }

    public X509Certificate getX509Certificate() {
        return x509Certificate;
    }

    public void setX509Certificate(X509Certificate x509Certificate) {
        this.x509Certificate = x509Certificate;
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }
}
