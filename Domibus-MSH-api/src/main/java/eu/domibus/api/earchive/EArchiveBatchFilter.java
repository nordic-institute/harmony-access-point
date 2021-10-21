package eu.domibus.api.earchive;

import java.time.ZonedDateTime;
import java.util.Date;

/**
 * @author Joze Rihtarsic
 * @since 5.0
 */
public class EArchiveBatchFilter {

    String requestType;
    Date startDate;
    Date endDate;
    Integer pageStart;
    Integer pageSize;

    public EArchiveBatchFilter() {
    }

    public EArchiveBatchFilter(String requestType, Date startDate, Date endDate, Integer pageStart, Integer pageSize) {
        this.requestType = requestType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.pageStart = pageStart;
        this.pageSize = pageSize;
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

    public Integer getPageStart() {
        return pageStart;
    }

    public void setPageStart(Integer pageStart) {
        this.pageStart = pageStart;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    @Override
    public String toString() {
        return "EArchiveBatchFilter{" +
                "requestType='" + requestType + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", pageStart=" + pageStart +
                ", pageSize=" + pageSize +
                '}';
    }
}
