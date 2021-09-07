package eu.domibus.core.earchive;

import java.util.List;

/**
 * @author François Gautier
 * @since 5.0
 */
public class BatchEArchiveDTO {

    String version;

    String batchId;

    String requestType;

    String status;

    String errorCode;

    String errorDescription;

    String timestamp;

    String messageStartDate;

    String messageEndDate;

    String manifestChecksum;

    List<String> messages;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessageStartDate() {
        return messageStartDate;
    }

    public void setMessageStartDate(String messageStartDate) {
        this.messageStartDate = messageStartDate;
    }

    public String getMessageEndDate() {
        return messageEndDate;
    }

    public void setMessageEndDate(String messageEndDate) {
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
