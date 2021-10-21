package eu.domibus.ext.domain.archive;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * @author Joze Rihtarsic
 * @since 5.0
 */
public class QueuedBatchDTO {

    String batchId;
    BatchRequestType requestType;
    ZonedDateTime enqueuedTimestamp;
    Long messageStartDate;
    Long messageEndDate;
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

    public ZonedDateTime getEnqueuedTimestamp() {
        return enqueuedTimestamp;
    }

    public void setEnqueuedTimestamp(ZonedDateTime enqueuedTimestamp) {
        this.enqueuedTimestamp = enqueuedTimestamp;
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

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }
}
