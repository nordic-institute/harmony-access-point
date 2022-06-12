package eu.domibus.ext.domain.archive;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Joze Rihtarsic
 * @since 5.0
 */
public class ExportedBatchFilterDTO {

    protected Long messageStartDate;
    protected Long messageEndDate;
    protected List<ExportedBatchStatusType> statuses;
    Boolean includeReExportedBatches;

    public ExportedBatchFilterDTO() {
    }

    public ExportedBatchFilterDTO(Long messageStartDate, Long messageEndDate, List<ExportedBatchStatusType> statuses, Boolean includeReExportedBatches) {
        this.messageStartDate = messageStartDate;
        this.messageEndDate = messageEndDate;
        this.statuses = statuses;
        this.includeReExportedBatches = includeReExportedBatches;
    }

    public Boolean getIncludeReExportedBatches() {
        return includeReExportedBatches;
    }

    public void setIncludeReExportedBatches(Boolean includeReExportedBatches) {
        this.includeReExportedBatches = includeReExportedBatches;
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

    public List<ExportedBatchStatusType> getStatuses() {
        if (statuses ==null) {
            statuses = new ArrayList<>();
        }
        return statuses;
    }



    @Override
    public String toString() {
        return "ExportedBatchFilterDTO{" +
                "messageStartDate=" + messageStartDate +
                ", messageEndDate=" + messageEndDate +
                ", statuses=" + statuses +
                ", includeReExportedBatches=" + includeReExportedBatches +
                '}';
    }
}
