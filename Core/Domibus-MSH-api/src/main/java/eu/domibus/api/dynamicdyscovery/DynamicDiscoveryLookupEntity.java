package eu.domibus.api.dynamicdyscovery;

import eu.domibus.api.model.AbstractBaseEntity;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 5.1.1
 */
@NamedQueries({
        @NamedQuery(name = "DynamicDiscoveryLookupEntity.findByFinalRecipient", query = "select lookup from DynamicDiscoveryLookupEntity lookup where lookup.finalRecipientValue=:FINAL_RECIPIENT"),
        @NamedQuery(name = "DynamicDiscoveryLookupEntity.findCertificatesNotDiscoveredInTheLastPeriod", query = "select lookup.cn from DynamicDiscoveryLookupEntity lookup group by lookup.cn having max(lookup.dynamicDiscoveryTime) < :DDC_TIME"),
        @NamedQuery(name = "DynamicDiscoveryLookupEntity.findPartiesNotDiscoveredInTheLastPeriod", query = "select lookup.partyName from DynamicDiscoveryLookupEntity lookup group by lookup.partyName having max(lookup.dynamicDiscoveryTime) < :DDC_TIME"),
        @NamedQuery(name = "DynamicDiscoveryLookupEntity.findFinalRecipientsNotDiscoveredInTheLastPeriod", query = "select lookup from DynamicDiscoveryLookupEntity lookup  where lookup.dynamicDiscoveryTime < :DDC_TIME"),
})
@Entity
@Table(name = "TB_DDC_LOOKUP")
public class DynamicDiscoveryLookupEntity extends AbstractBaseEntity {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DynamicDiscoveryLookupEntity.class);
    public static final String PROCESS_SEPARATOR = ",";

    @Column(name = "FINAL_RECIPIENT")
    protected String finalRecipientValue;

    @Column(name = "ENDPOINT_URL")
    protected String finalRecipientUrl;

    @Column(name = "PARTY_NAME")
    protected String partyName;

    @Column(name = "PARTY_TYPE")
    protected String partyType;

    @Column(name = "PARTY_PROCESSES")
    protected String partyProcesses;

    @Column(name = "CERT_ISSUER_SUBJECT")
    protected String issuerSubject;

    @Column(name = "CERT_CN")
    protected String cn;

    @Column(name = "CERT_SUBJECT")
    protected String subject;

    @Column(name = "CERT_SERIAL")
    protected String serial;

    @Column(name = "CERT_FINGERPRINT")
    protected String fingerprint;

    /**
     * The last time when the certificate was used for sending a message with dynamic discovery
     */
    @Column(name = "DDC_LOOKUP_TIME")
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

    public String getFinalRecipientValue() {
        return finalRecipientValue;
    }

    public void setFinalRecipientValue(String finalRecipientValue) {
        this.finalRecipientValue = finalRecipientValue;
    }

    public String getFinalRecipientUrl() {
        return finalRecipientUrl;
    }

    public void setFinalRecipientUrl(String finalRecipientUrl) {
        this.finalRecipientUrl = finalRecipientUrl;
    }

    public String getPartyName() {
        return partyName;
    }

    public void setPartyName(String partyName) {
        this.partyName = partyName;
    }

    public String getPartyType() {
        return partyType;
    }

    public void setPartyType(String partyType) {
        this.partyType = partyType;
    }

    public List<String> getPartyProcesses() {
        final String[] processArray = StringUtils.split(partyProcesses, PROCESS_SEPARATOR);
        if (processArray == null) {
            return null;
        }
        return Arrays.asList(processArray);
    }

    public void setPartyProcesses(List<String> partyProcessNames) {
        final String partyProcessesAsString = StringUtils.join(partyProcessNames, PROCESS_SEPARATOR);
        this.partyProcesses = partyProcessesAsString;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("entityId", entityId)
                .append("finalRecipientValue", finalRecipientValue)
                .append("finalRecipientUrl", finalRecipientUrl)
                .append("partyName", partyName)
                .append("partyType", partyType)
                .append("partyProcesses", partyProcesses)
                .append("issuerSubject", issuerSubject)
                .append("cn", cn)
                .append("subject", subject)
                .append("serial", serial)
                .append("fingerprint", fingerprint)
                .append("dynamicDiscoveryTime", dynamicDiscoveryTime)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        DynamicDiscoveryLookupEntity that = (DynamicDiscoveryLookupEntity) o;

        return new EqualsBuilder().appendSuper(super.equals(o)).append(finalRecipientValue, that.finalRecipientValue).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).appendSuper(super.hashCode()).append(finalRecipientValue).toHashCode();
    }
}
