package eu.domibus.core.ebms3;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;

/**
 * @author François Gautier
 * @since 4.2
 */
public class EbMS3ExceptionBuilder {

    private ErrorCode.EbMS3ErrorCode ebMS3ErrorCode;
    private Throwable cause;
    private String errorDetail;
    private String refToMessageId;
    private MSHRole mshRole;
    private boolean recoverable = true;
    private String signalMessageId;

    public static EbMS3ExceptionBuilder getInstance() {
        return new EbMS3ExceptionBuilder();
    }

    public EbMS3Exception build() {
        EbMS3Exception ebMS3Exception = new EbMS3Exception(ebMS3ErrorCode, errorDetail, refToMessageId, cause);
        ebMS3Exception.setMshRole(mshRole);
        ebMS3Exception.setRecoverable(recoverable);
        ebMS3Exception.setSignalMessageId(signalMessageId);
        return ebMS3Exception;
    }

    public Throwable getCause() {
        return cause;
    }

    public EbMS3ExceptionBuilder withCause(Throwable cause) {
        this.cause = cause;
        return this;
    }

    public ErrorCode.EbMS3ErrorCode getEbMS3ErrorCode() {
        return ebMS3ErrorCode;
    }

    public EbMS3ExceptionBuilder withEbMS3ErrorCode(ErrorCode.EbMS3ErrorCode ebMS3ErrorCode) {
        this.ebMS3ErrorCode = ebMS3ErrorCode;
        return this;
    }

    public String getErrorDetail() {
        return errorDetail;
    }

    public EbMS3ExceptionBuilder withErrorDetail(String errorDetail) {
        this.errorDetail = errorDetail;
        return this;
    }

    public String getRefToMessageId() {
        return refToMessageId;
    }

    public EbMS3ExceptionBuilder withRefToMessageId(String refToMessageId) {
        this.refToMessageId = refToMessageId;
        return this;
    }

    public MSHRole getMshRole() {
        return mshRole;
    }

    public EbMS3ExceptionBuilder withMshRole(MSHRole mshRole) {
        this.mshRole = mshRole;
        return this;
    }

    public boolean isRecoverable() {
        return recoverable;
    }

    public EbMS3ExceptionBuilder withRecoverable(boolean recoverable) {
        this.recoverable = recoverable;
        return this;
    }

    public String getSignalMessageId() {
        return signalMessageId;
    }

    public EbMS3ExceptionBuilder withSignalMessageId(String signalMessageId) {
        this.signalMessageId = signalMessageId;
        return this;
    }
}
