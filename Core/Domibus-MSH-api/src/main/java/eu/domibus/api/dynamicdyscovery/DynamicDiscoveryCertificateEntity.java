package eu.domibus.api.dynamicdyscovery;

import eu.domibus.api.model.AbstractBaseEntity;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Cosmin Baciu
 * @since 5.1.1
 */
@NamedQueries({
        @NamedQuery(name = "DynamicDiscoveryCertificateEntity.findByCertificateCN", query = "select cert from DynamicDiscoveryCertificateEntity cert where cert.cn=:CERT_CN"),
        @NamedQuery(name = "DynamicDiscoveryCertificateEntity.findCertificatesNotDiscoveredInTheLastPeriod", query = "select cert from DynamicDiscoveryCertificateEntity cert where cert.dynamicDiscoveryTime < :DDC_TIME"),
        @NamedQuery(name = "DynamicDiscoveryCertificateEntity.deleteCertificateByCN", query = "delete from DynamicDiscoveryCertificateEntity cert where cert.cn=:CERT_CN"),
})
@Entity
@Table(name = "TB_DDC_CERTIFICATE")
public class DynamicDiscoveryCertificateEntity extends AbstractBaseEntity {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DynamicDiscoveryCertificateEntity.class);

    @Column(name = "ISSUER_SUBJECT")
    protected String issuerSubject;

    @Column(name = "CN")
    protected String cn;

    @Column(name = "SUBJECT")
    protected String subject;

    @Column(name = "SERIAL")
    protected String serial;

    @Column(name = "FINGERPRINT")
    protected String fingerprint;

    /**
     * The last time when the certificate was used for sending a message with dynamic discovery
     */
    @Column(name = "DDC_TIME")
    protected Date dynamicDiscoveryTime;

    public String getIssuerSubject() {
        return issuerSubject;
    }

    public void setIssuerSubject(String issuerSubject) {
        this.issuerSubject = issuerSubject;
    }

    public String getCn() {
        return cn;
    }

    public void setCn(String cn) {
        this.cn = cn;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public Date getDynamicDiscoveryTime() {
        return dynamicDiscoveryTime;
    }

    public void setDynamicDiscoveryTime(Date dynamicDiscoveryUpdateTime) {
        this.dynamicDiscoveryTime = dynamicDiscoveryUpdateTime;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("issuerSubject", issuerSubject)
                .append("cn", cn)
                .append("subject", subject)
                .append("serial", serial)
                .append("fingerprint", fingerprint)
                .append("dynamicDiscoveryUpdateTime", dynamicDiscoveryTime)
                .toString();
    }
}
