package eu.domibus.ext.domain.archive;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Joze Rihtarsic
 * @since 5.0
 */
public class ExportedBatchFilterDTO {

    Long messageStartDate;
    Long messageEndDate;
    ExportedBatchStatusTypeParameter status;
    Boolean reExport;

    public ExportedBatchFilterDTO(Long messageStartDate, Long messageEndDate, ExportedBatchStatusTypeParameter status, Boolean reExport) {
        this.messageStartDate = messageStartDate;
        this.messageEndDate = messageEndDate;
        this.status = status;
        this.reExport = reExport;
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

    public ExportedBatchStatusTypeParameter getStatus() {
        return status;
    }

    public void setStatus(ExportedBatchStatusTypeParameter status) {
        this.status = status;
    }

    public Boolean getReExport() {
        return reExport;
    }

    public void setReExport(Boolean reExport) {
        this.reExport = reExport;
    }

}
