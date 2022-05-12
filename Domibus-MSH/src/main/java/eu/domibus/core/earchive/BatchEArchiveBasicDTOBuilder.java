package eu.domibus.core.earchive;

import java.util.List;

/**
 * @author Soumya Chandran
 * @since 5.0
 */
public class BatchEArchiveBasicDTOBuilder {
    private String batchId;
    private String requestType;
    private String status;
    private String timestamp;
    private String manifestChecksum;
    private List<String> messages;
    private String errorCode;
    private String errorDescription;

    public BatchEArchiveBasicDTOBuilder batchId(String batchId) {
        this.batchId = batchId;
        return this;
    }

    public BatchEArchiveBasicDTOBuilder requestType(String requestType) {
        this.requestType = requestType;
        return this;
    }

    public BatchEArchiveBasicDTOBuilder status(String status) {
        this.status = status;
        return this;
    }

    public BatchEArchiveBasicDTOBuilder errorCode(String errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    public BatchEArchiveBasicDTOBuilder errorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
        return this;
    }

    public BatchEArchiveBasicDTOBuilder timestamp(String timestamp) {
        this.timestamp = timestamp;
        return this;
    }


    public BatchEArchiveBasicDTOBuilder manifestChecksum(String manifestChecksum) {
        this.manifestChecksum = manifestChecksum;
        return this;
    }

    public BatchEArchiveBasicDTOBuilder messages(List<String> messages) {
        this.messages = messages;
        return this;
    }

    public BatchEArchiveBasicDTO createBatchEArchiveBasicDTO() {
        BatchEArchiveBasicDTO batchEArchiveBasicDTO = new BatchEArchiveBasicDTO(batchId, requestType);
        batchEArchiveBasicDTO.setStatus(status);
        batchEArchiveBasicDTO.setTimestamp(timestamp);
        batchEArchiveBasicDTO.setManifestChecksum(manifestChecksum);
        batchEArchiveBasicDTO.setMessages(messages);
        batchEArchiveBasicDTO.setErrorCode(errorCode);
        batchEArchiveBasicDTO.setErrorDescription(errorDescription);
        return batchEArchiveBasicDTO;
    }
}
