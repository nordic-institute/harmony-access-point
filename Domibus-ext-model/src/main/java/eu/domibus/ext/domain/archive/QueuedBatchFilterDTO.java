package eu.domibus.ext.domain.archive;

import java.time.ZonedDateTime;

/**
 * @author Joze Rihtarsic
 * @since 5.0
 */
public class QueuedBatchFilterDTO {

    Integer lastCountRequests;
    BatchRequestTypeParameter requestType;
    ZonedDateTime startDate;
    ZonedDateTime endDate;


    public QueuedBatchFilterDTO(Integer lastCountRequests, BatchRequestTypeParameter requestType, ZonedDateTime startDate, ZonedDateTime endDate) {
        this.lastCountRequests = lastCountRequests;
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

    public BatchRequestTypeParameter getRequestType() {
        return requestType;
    }

    public void setRequestType(BatchRequestTypeParameter requestType) {
        this.requestType = requestType;
    }

    public ZonedDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(ZonedDateTime startDate) {
        this.startDate = startDate;
    }

    public ZonedDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(ZonedDateTime endDate) {
        this.endDate = endDate;
    }
}
