package eu.domibus.ext.domain.archive;

import java.util.Date;

public class ExportedBatchFilterRequestDTO {

    Date startDate;
    Date endDate;
    String status;
    Boolean reExport;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getReExport() {
        return reExport;
    }

    public void setReExport(Boolean reExport) {
        this.reExport = reExport;
    }
}
