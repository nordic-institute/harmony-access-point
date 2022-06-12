package eu.domibus.api.earchive;


import java.util.Date;
import java.util.List;

/**
 * @author Joze Rihtarsic
 * @since 5.0
 */
public class EArchiveBatchRequestDTO {

    String version;

    String batchId;

    String requestType;

    String status;

    String errorCode;

    String errorDescription;

    Date timestamp;

    Long messageStartId;

    Long messageEndId;

    String manifestChecksum;

    List<String> messages;

    public EArchiveBatchRequestDTO() {
    }

    public EArchiveBatchRequestDTO(String batchId,
                                   Date timestamp
    ) {

        this.batchId = batchId;
        this.timestamp = timestamp;
    }

    public EArchiveBatchRequestDTO(String batchId,
                                   String requestType,
                                   String status,
                                   String errorCode,
                                   String errorDescription,
                                   Date timestamp,
                                   Long messageStartId,
                                   Long messageEndId
                            ) {
        this.batchId = batchId;
        this.requestType = requestType;
        this.status = status;
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
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

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Long getMessageStartId() {
        return messageStartId;
    }

    public void setMessageStartId(Long messageStartId) {
        this.messageStartId = messageStartId;
    }

    public Long getMessageEndId() {
        return messageEndId;
    }

    public void setMessageEndId(Long messageEndId) {
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
                ", messages=" + messages +
                '}';
    }
}
