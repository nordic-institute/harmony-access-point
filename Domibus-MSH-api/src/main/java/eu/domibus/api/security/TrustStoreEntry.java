package eu.domibus.api.security;

import java.util.Date;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

public class TrustStoreEntry {

    private String name;
    private String subject;
    private String issuer;
    private Date validFrom;
    private Date validUntil;
    private String fingerprints;

    public TrustStoreEntry(String name, String subject, String issuer, Date validFrom, Date validUntil) {
        this.name = name;
        this.subject = subject;
        this.issuer = issuer;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
    }

    public TrustStoreEntry() {
    }

    public String getName() {
        return name;
    }

    public String getSubject() {
        return subject;
    }

    public String getIssuer() {
        return issuer;
    }

    public Date getValidFrom() {
        return validFrom;
    }

    public Date getValidUntil() { return validUntil; }

    public String getFingerprints() {
        return fingerprints;
    }

    public void setFingerprints(String fingerprints) {
        this.fingerprints = fingerprints;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

    public void setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
    }
}
