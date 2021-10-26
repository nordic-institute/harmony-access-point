package eu.domibus.api.earchive;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Joze Rihtarsic
 * @since 5.0
 */
public class EArchiveBatchFilter {

    String requestType;
    Date startDate;
    Date endDate;
    Long messageStartDate;
    Long messageEndDate;
    Boolean showReExported;
    Integer pageStart;
    Integer pageSize;
    List<EArchiveBatchStatus> statusList;

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

    public Long getMessageStarId() {
        return messageStartDate;
    }

    public void setMessageStartId(Long messageStartDate) {
        this.messageStartDate = messageStartDate;
    }

    public Long getMessageEndDate() {
        return messageEndDate;
    }

    public void setMessageEndDate(Long messageEndDate) {
        this.messageEndDate = messageEndDate;
    }

    public Boolean getShowReExported() {
        return showReExported;
    }

    public void setShowReExported(Boolean showReExported) {
        this.showReExported = showReExported;
    }

    public List<EArchiveBatchStatus> getStatusList() {
        if (statusList == null) {
            statusList = new ArrayList<>();
        }
        return statusList;
    }

    @Override
    public String toString() {
        return "EArchiveBatchFilter{" +
                "requestType='" + requestType + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", messageStartDate=" + messageStartDate +
                ", messageEndDate=" + messageEndDate +
                ", reExport=" + showReExported +
                ", pageStart=" + pageStart +
                ", pageSize=" + pageSize +
                ", statusList=" + statusList +
                '}';
    }
}
