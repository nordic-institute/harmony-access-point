package eu.domibus.ext.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;

/**
 * @author Ion Perpegel
 * @since 5.1
 */
public class TrustStoreEntryDTO {

    private String name;
    private String subject;
    private String issuer;
    private String fingerprints;
    private String validFrom;
    private String validUntil;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getFingerprints() {
        return fingerprints;
    }

    public void setFingerprints(String fingerprints) {
        this.fingerprints = fingerprints;
    }

    public String getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(String validFrom) {
        this.validFrom = validFrom;
    }

    public String getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(String validUntil) {
        this.validUntil = validUntil;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("name", name)
                .append("subject", subject)
                .append("issuer", issuer)
                .append("validFrom", validFrom)
                .append("validUntil", validUntil)
                .append("fingerprints", fingerprints)
                .toString();
    }
}
