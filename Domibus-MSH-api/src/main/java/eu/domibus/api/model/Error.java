package eu.domibus.api.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@Entity
@Table(name = "TB_ERROR")
public class Error extends AbstractBaseEntity {

    @Embedded
    protected Description description; //NOSONAR

    @Column(name = "ERROR_DETAIL")
    @Lob
    @Basic(fetch = FetchType.EAGER)
    protected String errorDetail;

    @Column(name = "CATEGORY")
    protected String category;

    @Column(name = "REF_TO_MESSAGE_ID")
    protected String refToMessageInError;

    @Column(name = "ERROR_CODE")
    protected String errorCode;

    @Column(name = "ORIGIN")
    protected String origin;

    @Column(name = "SEVERITY")
    protected String severity;

    @Column(name = "SHORT_DESCRIPTION")
    protected String shortDescription;

    public Description getDescription() {
        return this.description;
    }

    public void setDescription(final Description value) {
        this.description = value;
    }

    public String getErrorDetail() {
        return this.errorDetail;
    }

    public void setErrorDetail(final String value) {
        this.errorDetail = value;
    }

    public String getCategory() {
        return this.category;
    }

    public void setCategory(final String value) {
        this.category = value;
    }

    public String getRefToMessageInError() {
        return this.refToMessageInError;
    }

    public void setRefToMessageInError(final String value) {
        this.refToMessageInError = value;
    }

    public String getErrorCode() {
        return this.errorCode;
    }

    public void setErrorCode(final String value) {
        this.errorCode = value;
    }

    public String getOrigin() {
        return this.origin;
    }

    public void setOrigin(final String value) {
        this.origin = value;
    }

    public String getSeverity() {
        return this.severity;
    }

    public void setSeverity(final String value) {
        this.severity = value;
    }

    public String getShortDescription() {
        return this.shortDescription;
    }

    public void setShortDescription(final String value) {
        this.shortDescription = value;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("description", description)
                .append("errorDetail", errorDetail)
                .append("category", category)
                .append("refToMessageInError", refToMessageInError)
                .append("errorCode", errorCode)
                .append("origin", origin)
                .append("severity", severity)
                .append("shortDescription", shortDescription)
                .toString();
    }
}
