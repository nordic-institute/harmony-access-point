package eu.domibus.core.earchive;

import java.util.List;

public class BatchEArchiveBasicDTO {
    String batchId;
    String requestType;
    String status;
    String timestamp;
    String manifestChecksum;
    List<String> messages;
    String errorCode;
    String errorDescription;

    public BatchEArchiveBasicDTO() {
    }

    public BatchEArchiveBasicDTO(String batchId, String requestType) {
        this.batchId = batchId;
        this.requestType = requestType;
    }

    public BatchEArchiveBasicDTO(String batchId,
                                 String requestType,
                                 String status,
                                 String timestamp,
                                 String manifestChecksum,
                                 List<String> messages,
                                 String errorCode,
                                 String errorDescription

    ) {
        this.batchId = batchId;
        this.requestType = requestType;
        this.status = status;
        this.timestamp = timestamp;
        this.manifestChecksum = manifestChecksum;
        this.messages = messages;
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
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
                ", batchId='" + batchId + '\'' +
                ", requestType='" + requestType + '\'' +
                ", status='" + status + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", manifestChecksum='" + manifestChecksum + '\'' +
                ", messages=" + messages +
                ", errorCode='" + errorCode + '\'' +
                ", errorDescription='" + errorDescription + '\'' +
                '}';
    }
}
