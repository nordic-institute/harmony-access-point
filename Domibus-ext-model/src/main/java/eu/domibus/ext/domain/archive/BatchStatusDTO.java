package eu.domibus.ext.domain.archive;

import java.time.OffsetDateTime;

/**
 * @author Joze Rihtarsic
 * @since 5.0
 */
public class BatchStatusDTO {

    String batchId;
    OffsetDateTime timestamp;
    String status;
    String message;

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
