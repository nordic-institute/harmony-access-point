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

    Long messageStartDate;

    Long messageEndDate;

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
                                   Long messageStartDate,
                                   Long messageEndDate
                            ) {
        this.batchId = batchId;
        this.requestType = requestType;
        this.status = status;
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
        this.timestamp = timestamp;
        this.messageStartDate = messageStartDate;
        this.messageEndDate = messageEndDate;
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
                ", messageStartDate='" + messageStartDate + '\'' +
                ", messageEndDate='" + messageEndDate + '\'' +
                ", manifestChecksum='" + manifestChecksum + '\'' +
                ", messages=" + messages +
                '}';
    }
}
