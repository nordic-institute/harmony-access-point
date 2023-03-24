package eu.domibus.core.earchive;

import java.util.List;

public class BatchEArchiveDTOBuilder {
    private String version;
    private String batchId;
    private String requestType;
    private String status;
    private String errorCode;
    private String errorDescription;
    private String timestamp;
    private String messageStartId;
    private String messageEndId;
    private String messageStartDate;
    private String messageEndDate;
    private String manifestChecksum;
    private List<String> messages;

    public BatchEArchiveDTOBuilder version(String version) {
        this.version = version;
        return this;
    }

    public BatchEArchiveDTOBuilder batchId(String batchId) {
        this.batchId = batchId;
        return this;
    }

    public BatchEArchiveDTOBuilder requestType(String requestType) {
        this.requestType = requestType;
        return this;
    }

    public BatchEArchiveDTOBuilder status(String status) {
        this.status = status;
        return this;
    }

    public BatchEArchiveDTOBuilder errorCode(String errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    public BatchEArchiveDTOBuilder errorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
        return this;
    }

    public BatchEArchiveDTOBuilder timestamp(String timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public BatchEArchiveDTOBuilder messageStartId(String messageStartId) {
        this.messageStartId = messageStartId;
        return this;
    }

    public BatchEArchiveDTOBuilder messageEndId(String messageEndId) {
        this.messageEndId = messageEndId;
        return this;
    }

    public BatchEArchiveDTOBuilder messageStartDate(String messageStartDate) {
        this.messageStartDate = messageStartDate;
        return this;
    }

    public BatchEArchiveDTOBuilder messageEndDate(String messageEndDate) {
        this.messageEndDate = messageEndDate;
        return this;
    }

    public BatchEArchiveDTOBuilder manifestChecksum(String manifestChecksum) {
        this.manifestChecksum = manifestChecksum;
        return this;
    }

    public BatchEArchiveDTOBuilder messages(List<String> messages) {
        this.messages = messages;
        return this;
    }

    public BatchEArchiveDTO createBatchEArchiveDTO() {
        BatchEArchiveDTO batchEArchiveDTO = new BatchEArchiveDTO(batchId, requestType);
        batchEArchiveDTO.setVersion(version);
        batchEArchiveDTO.setStatus(status);
        batchEArchiveDTO.setErrorCode(errorCode);
        batchEArchiveDTO.setErrorDescription(errorDescription);
        batchEArchiveDTO.setTimestamp(timestamp);
        batchEArchiveDTO.setMessageStartId(messageStartId);
        batchEArchiveDTO.setMessageEndId(messageEndId);
        batchEArchiveDTO.setMessageStartDate(messageStartDate);
        batchEArchiveDTO.setMessageEndDate(messageEndDate);
        batchEArchiveDTO.setManifestChecksum(manifestChecksum);
        batchEArchiveDTO.setMessages(messages);
        return batchEArchiveDTO;
    }
}