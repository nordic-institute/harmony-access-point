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

    String messageStartId;

    String messageEndId;

    String manifestChecksum;

    List<String> messages;

    public BatchEArchiveDTO() {
    }

    public BatchEArchiveDTO(String batchId, String requestType) {
        this.batchId = batchId;
        this.requestType = requestType;
    }

    public BatchEArchiveDTO(String batchId,
                            String requestType,
                            String status,
                            String timestamp,
                            String messageStartId,
                            String messageEndId
                            ) {
        this.batchId = batchId;
        this.requestType = requestType;
        this.status = status;
        this.timestamp = timestamp;
        this.messageStartId = messageStartId;
        this.messageEndId = messageEndId;
    }

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

    public String getMessageStartId() {
        return messageStartId;
    }

    public void setMessageStartId(String messageStartId) {
        this.messageStartId = messageStartId;
    }

    public String getMessageEndId() {
        return messageEndId;
    }

    public void setMessageEndId(String messageEndId) {
        this.messageEndId = messageEndId;
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

    @Override
    public String toString() {
        return "BatchEArchiveDTO{" +
                "version='" + version + '\'' +
                ", batchId='" + batchId + '\'' +
                ", requestType='" + requestType + '\'' +
                ", status='" + status + '\'' +
                ", errorCode='" + errorCode + '\'' +
                ", errorDescription='" + errorDescription + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", messageStartDate='" + messageStartId + '\'' +
                ", messageEndDate='" + messageEndId + '\'' +
                ", manifestChecksum='" + manifestChecksum + '\'' +
                '}';
    }
}
