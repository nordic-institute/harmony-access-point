package eu.domibus.ext.domain.archive;

import java.util.Date;

/**
 * @author Joze Rihtarsic
 * @since 5.0
 */
public class BatchStatusDTO {

    String batchId;
    Date timestamp;
    ExportedBatchStatusType status;
    String message;

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public ExportedBatchStatusType getStatus() {
        return status;
    }

    public void setStatus(ExportedBatchStatusType status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
