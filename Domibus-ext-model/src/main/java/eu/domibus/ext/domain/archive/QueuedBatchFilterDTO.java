package eu.domibus.ext.domain.archive;

import java.time.ZonedDateTime;
import java.util.Date;

/**
 * @author Joze Rihtarsic
 * @since 5.0
 */
public class QueuedBatchFilterDTO {

    Integer lastCountRequests;
    BatchRequestTypeParameter requestType;
    Date startDate;
    Date endDate;


    public QueuedBatchFilterDTO(Integer lastCountRequests, BatchRequestTypeParameter requestType, Date startDate, Date endDate) {
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

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
