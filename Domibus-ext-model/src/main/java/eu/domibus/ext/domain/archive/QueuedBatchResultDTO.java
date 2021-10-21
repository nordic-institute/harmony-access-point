package eu.domibus.ext.domain.archive;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Joze Rihtarsic
 * @since 5.0
 */
public class QueuedBatchResultDTO {

    Integer lastCountRequests;
    Integer maxCountResults;
    BatchRequestType requestType;
    OffsetDateTime startDate;
    OffsetDateTime endDate;
    List<QueuedBatchDTO> queuedBatches;

    public QueuedBatchResultDTO() {
    }

    public QueuedBatchResultDTO(Integer lastCountRequests, Integer maxCountResults, BatchRequestType requestType, OffsetDateTime startDate, OffsetDateTime endDate) {
        this.lastCountRequests = lastCountRequests;
        this.maxCountResults = maxCountResults;
        this.requestType = requestType;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Integer getLastCountRequests() {
        return lastCountRequests;
    }

    public void setLastCountRequests(Integer lastCountRequests) {
        this.lastCountRequests = lastCountRequests;
    }

    public Integer getMaxCountResults() {
        return maxCountResults;
    }

    public void setMaxCountResults(Integer maxCountResults) {
        this.maxCountResults = maxCountResults;
    }

    public BatchRequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(BatchRequestType requestType) {
        this.requestType = requestType;
    }

    public OffsetDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(OffsetDateTime startDate) {
        this.startDate = startDate;
    }

    public OffsetDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(OffsetDateTime endDate) {
        this.endDate = endDate;
    }

    public List<QueuedBatchDTO> getQueuedBatches() {
        if (queuedBatches == null) {
            queuedBatches = new ArrayList<>();
        }
        return queuedBatches;
    }
}
