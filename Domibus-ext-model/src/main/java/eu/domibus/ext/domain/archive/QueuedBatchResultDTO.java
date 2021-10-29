package eu.domibus.ext.domain.archive;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Joze Rihtarsic
 * @since 5.0
 */
public class QueuedBatchResultDTO {
    QueuedBatchFilterDTO filter;
    PaginationDTO pagination;
    List<QueuedBatchDTO> queuedBatches;

    public QueuedBatchResultDTO() {
    }

    public QueuedBatchResultDTO(QueuedBatchFilterDTO filter, Integer pageStart, Integer pageSize) {
        this.filter = filter;
        this.pagination = new PaginationDTO(pageStart, pageSize);
    }
    public QueuedBatchResultDTO(Integer lastCountRequests, BatchRequestTypeParameter requestType, Date startDate, Date endDate, Integer pageStart, Integer pageSize) {
        this.filter = new QueuedBatchFilterDTO(lastCountRequests, requestType, startDate, endDate);
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
