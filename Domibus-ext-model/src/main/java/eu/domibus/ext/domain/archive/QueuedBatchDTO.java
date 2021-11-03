package eu.domibus.ext.domain.archive;

import java.util.Date;
import java.util.List;

/**
 * @author Joze Rihtarsic
 * @since 5.0
 */
public class QueuedBatchDTO {

    protected String batchId;
    protected BatchRequestType requestType;
    protected Date enqueuedTimestamp;
    protected Long messageStartDate;
    protected Long messageEndDate;
    protected List<String> messages;

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

    public Date getEnqueuedTimestamp() {
        return enqueuedTimestamp;
    }

    public void setEnqueuedTimestamp(Date enqueuedTimestamp) {
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
