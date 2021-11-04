package eu.domibus.ext.domain.archive;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Joze Rihtarsic
 * @since 5.0
 */
public class QueuedBatchResultDTO {
    protected QueuedBatchFilterDTO filter;
    protected PaginationDTO pagination;
    protected List<QueuedBatchDTO> queuedBatches;

    public QueuedBatchResultDTO() {
    }

    public QueuedBatchResultDTO(QueuedBatchFilterDTO filter, Integer pageStart, Integer pageSize) {
        this.filter = filter;
        this.pagination = new PaginationDTO(pageStart, pageSize);
    }

    public QueuedBatchResultDTO(Integer lastCountRequests, List<BatchRequestType> requestTypes, Date startDate, Date endDate, Integer pageStart, Integer pageSize) {
        this.filter = new QueuedBatchFilterDTO(lastCountRequests, requestTypes, startDate, endDate);
        this.pagination = new PaginationDTO(pageStart, pageSize);
    }

    public QueuedBatchFilterDTO getFilter() {
        return filter;
    }

    public void setFilter(QueuedBatchFilterDTO filter) {
        this.filter = filter;
    }

    public PaginationDTO getPagination() {
        return pagination;
    }

    public void setPagination(PaginationDTO pagination) {
        this.pagination = pagination;
    }

    public List<QueuedBatchDTO> getQueuedBatches() {
        if (queuedBatches == null) {
            queuedBatches = new ArrayList<>();
        }
        return queuedBatches;
    }
}
