package eu.domibus.ext.domain.archive;

import java.util.Date;

public class QueuedBatchFilterRequestDTO {

    Integer lastCountRequests;
    Integer maxCountResults;
    String requestType;
    Date startDate;
    Date endDate;

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

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
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
