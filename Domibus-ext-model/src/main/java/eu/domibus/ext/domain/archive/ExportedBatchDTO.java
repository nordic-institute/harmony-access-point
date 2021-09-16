package eu.domibus.ext.domain.archive;

import java.util.Date;
import java.util.List;

/**
 * @author Joze Rihtarsic
 * @since 5.0
 */
public class ExportedBatchDTO {

    String batchId;
    BatchRequestType requestType;
    String errorCode;
    String errorDescription;
    Date enqueuedTimestamp;
    Date messageStartDate;
    Date messageEndDate;
    String manifestChecksum;

    List<String> messages;

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

    public Date getMessageStartDate() {
        return messageStartDate;
    }

    public void setMessageStartDate(Date messageStartDate) {
        this.messageStartDate = messageStartDate;
    }

    public Date getMessageEndDate() {
        return messageEndDate;
    }

    public void setMessageEndDate(Date messageEndDate) {
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
