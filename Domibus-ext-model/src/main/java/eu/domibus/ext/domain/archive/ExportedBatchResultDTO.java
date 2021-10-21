package eu.domibus.ext.domain.archive;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Joze Rihtarsic
 * @since 5.0
 */
public class ExportedBatchResultDTO {


    OffsetDateTime startDate;
    OffsetDateTime endDate;
    String status;
    Boolean reExport;
    Integer pageStart;
    Integer pageSize;
    List<ExportedBatchDTO> exportedBatches;
    Integer count;

    public ExportedBatchResultDTO() {
    }

    public ExportedBatchResultDTO(OffsetDateTime startDate, OffsetDateTime endDate, String status, Boolean reExport, Integer pageStart, Integer pageSize) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.reExport = reExport;
        this.pageStart = pageStart;
        this.pageSize = pageSize;
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

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public List<ExportedBatchDTO> getExportedBatches() {
        if (exportedBatches == null) {
            exportedBatches = new ArrayList<>();
        }
        return exportedBatches;
    }
}
