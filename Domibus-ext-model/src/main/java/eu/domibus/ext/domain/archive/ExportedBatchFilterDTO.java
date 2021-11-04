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
    protected List<BatchRequestType> requestTypes;

    public ExportedBatchFilterDTO() {
    }

    public ExportedBatchFilterDTO(Long messageStartDate, Long messageEndDate, List<ExportedBatchStatusType> statuses, List<BatchRequestType> requestTypes) {
        this.messageStartDate = messageStartDate;
        this.messageEndDate = messageEndDate;
        this.statuses = statuses;
        this.requestTypes = requestTypes;
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

    public List<BatchRequestType> getRequestTypes() {
        if (requestTypes ==null) {
            requestTypes = new ArrayList<>();
        }
        return requestTypes;
    }

    @Override
    public String toString() {
        return "ExportedBatchFilterDTO{" +
                "messageStartDate=" + messageStartDate +
                ", messageEndDate=" + messageEndDate +
                ", statuses=" + statuses +
                ", requestTypes=" + requestTypes +
                '}';
    }
}
