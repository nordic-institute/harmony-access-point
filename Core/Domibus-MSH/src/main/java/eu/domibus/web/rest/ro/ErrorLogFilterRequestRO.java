package eu.domibus.web.rest.ro;

import eu.domibus.common.ErrorCode;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.validators.CustomWhiteListed;

import java.io.Serializable;

/**
 * @author Ion Perpegel
 * @since 4.1
 */

public class ErrorLogFilterRequestRO implements Serializable {
    private int page = 0;

    private int pageSize = 10;

    private Boolean asc = true;

    private String orderBy;

    private String errorSignalMessageId;

    private MSHRole mshRole;

    private String messageInErrorId;

    private ErrorCode errorCode;

    @CustomWhiteListed(permitted = ":/=?&")
    private String errorDetail;

    private String timestampFrom;

    private String timestampTo;

    private String notifiedFrom;

    private String notifiedTo;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public Boolean getAsc() {
        return asc;
    }

    public void setAsc(Boolean asc) {
        this.asc = asc;
    }

    public String getErrorSignalMessageId() {
        return errorSignalMessageId;
    }

    public void setErrorSignalMessageId(String errorSignalMessageId) {
        this.errorSignalMessageId = errorSignalMessageId;
    }

    public MSHRole getMshRole() {
        return mshRole;
    }

    public void setMshRole(MSHRole mshRole) {
        this.mshRole = mshRole;
    }

    public String getMessageInErrorId() {
        return messageInErrorId;
    }

    public void setMessageInErrorId(String messageInErrorId) {
        this.messageInErrorId = messageInErrorId;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorDetail() {
        return errorDetail;
    }

    public void setErrorDetail(String errorDetail) {
        this.errorDetail = errorDetail;
    }

    public String getTimestampFrom() {
        return timestampFrom;
    }

    public void setTimestampFrom(String timestampFrom) {
        this.timestampFrom = timestampFrom;
    }

    public String getTimestampTo() {
        return timestampTo;
    }

    public void setTimestampTo(String timestampTo) {
        this.timestampTo = timestampTo;
    }

    public String getNotifiedFrom() {
        return notifiedFrom;
    }

    public void setNotifiedFrom(String notifiedFrom) {
        this.notifiedFrom = notifiedFrom;
    }

    public String getNotifiedTo() {
        return notifiedTo;
    }

    public void setNotifiedTo(String notifiedTo) {
        this.notifiedTo = notifiedTo;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }
}
