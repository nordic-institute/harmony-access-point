package eu.domibus.ext.domain.archive;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Joze Rihtarsic
 * @since 5.0
 */
public class QueuedBatchFilterDTO {

    protected Integer lastCountRequests;
    protected List<BatchRequestType> requestTypes;
    protected Date startDate;
    protected Date endDate;

    public QueuedBatchFilterDTO() {
    }

    public QueuedBatchFilterDTO(Integer lastCountRequests) {
        this.lastCountRequests = lastCountRequests;
    }
    public QueuedBatchFilterDTO(List<BatchRequestType> requestTypes, Date startDate, Date endDate) {
        this.requestTypes = requestTypes;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Integer getLastCountRequests() {
        return lastCountRequests;
    }

    public void setLastCountRequests(Integer lastCountRequests) {
        this.lastCountRequests = lastCountRequests;
    }

    public List<BatchRequestType> getRequestTypes() {
        if (requestTypes == null) {
            requestTypes = new ArrayList<>();
        }
        return requestTypes;
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

    @Override
    public String toString() {
        return "QueuedBatchFilterDTO{" +
                "lastCountRequests=" + lastCountRequests +
                ", requestTypes=" + requestTypes +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}
