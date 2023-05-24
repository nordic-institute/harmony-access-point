package eu.domibus.ext.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;

/**
 * @author Catalin Enache
 * @since 4.2
 */
public class TrustStoreDTO {

    private String name;

    private String subject;

    private String issuer;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date validFrom;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date validUntil;

    private String fingerprints;

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

    public Date getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

    public Date getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
    }

    public String getFingerprints() {
        return fingerprints;
    }

    public void setFingerprints(String fingerprints) {
        this.fingerprints = fingerprints;
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
