package eu.domibus.core.crypto.spi;

import java.security.cert.X509Certificate;

/**
 * @author Thomas Dussart
 * @since 4.1
 * <p>
 * Just a mapper class for core CertificateEntry class.
 */
public class CertificateEntrySpi {

    private String alias;

    private X509Certificate certificate;

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void setCertificate(X509Certificate certificate) {
        this.certificate = certificate;
    }

    public CertificateEntrySpi() {
    }

    public CertificateEntrySpi(String alias, X509Certificate certificate) {
        this.alias = alias;
        this.certificate = certificate;
    }

    public String getAlias() {
        return this.alias;
    }

    public X509Certificate getCertificate() {
        return this.certificate;
    }
}
