package eu.domibus.ext.domain.archive;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Joze Rihtarsic
 * @since 5.0
 */
public class QueuedBatchResultDTO {
    protected QueuedBatchFilterDTO filter;
    protected PaginationDTO pagination;
    protected List<BatchDTO> batches;

    public QueuedBatchResultDTO() {
    }

    public QueuedBatchResultDTO(QueuedBatchFilterDTO filter, Integer pageStart, Integer pageSize) {
        this.filter = filter;
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

    public List<BatchDTO> getBatches() {
        if (batches == null) {
            batches = new ArrayList<>();
        }
        return batches;
    }
}
