package eu.domibus.ext.domain.archive;

import java.util.Date;
import java.util.List;

/**
 * @author Joze Rihtarsic
 * @since 5.0
 */
public class ExportedBatchDTO {

    protected String batchId;
    protected BatchRequestType requestType;
    protected ExportedBatchStatusType status;
    protected String errorCode;
    protected String errorDescription;
    protected Date enqueuedTimestamp;
    // Same as message parameter max and min:  yyMMddHH
    protected Long messageStartDate;
    protected Long messageEndDate;
    protected String manifestChecksum;
    protected List<String> messages;

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public BatchRequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(BatchRequestType requestType) {
        this.requestType = requestType;
    }

    public ExportedBatchStatusType getStatus() {
        return status;
    }

    public void setStatus(ExportedBatchStatusType status) {
        this.status = status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public Date getEnqueuedTimestamp() {
        return enqueuedTimestamp;
    }

    public void setEnqueuedTimestamp(Date enqueuedTimestamp) {
        this.enqueuedTimestamp = enqueuedTimestamp;
    }

    public Long getMessageStartDate() {
        return messageStartDate;
    }

    public void setMessageStartDate(Long messageStartDate) {
        this.messageStartDate = messageStartDate;
    }

    public Long getMessageEndDate() {
        return messageEndDate;
    }

    public void setMessageEndDate(Long messageEndDate) {
        this.messageEndDate = messageEndDate;
    }

    public String getManifestChecksum() {
        return manifestChecksum;
    }

    public void setManifestChecksum(String manifestChecksum) {
        this.manifestChecksum = manifestChecksum;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }
}
