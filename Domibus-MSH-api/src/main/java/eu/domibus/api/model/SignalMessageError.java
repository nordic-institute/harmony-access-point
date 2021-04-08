package eu.domibus.api.model;

public class SignalMessageError {

    protected String signalDescription;
    protected String signalDescriptionLang;
    protected String errorDetail;
    protected String category;
    protected String refToMessageInError;
    protected String errorCode;
    protected String origin;
    protected String severity;
    protected String shortDescription;

    public String getSignalDescription() {
        return signalDescription;
    }

    public void setSignalDescription(String signalDescription) {
        this.signalDescription = signalDescription;
    }

    public String getSignalDescriptionLang() {
        return signalDescriptionLang;
    }

    public void setSignalDescriptionLang(String signalDescriptionLang) {
        this.signalDescriptionLang = signalDescriptionLang;
    }

    public String getErrorDetail() {
        return errorDetail;
    }

    public void setErrorDetail(String errorDetail) {
        this.errorDetail = errorDetail;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getRefToMessageInError() {
        return refToMessageInError;
    }

    public void setRefToMessageInError(String refToMessageInError) {
        this.refToMessageInError = refToMessageInError;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }
}
