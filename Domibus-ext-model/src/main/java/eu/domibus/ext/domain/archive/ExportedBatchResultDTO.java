package eu.domibus.ext.domain.archive;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Joze Rihtarsic
 * @since 5.0
 */
public class ExportedBatchResultDTO {

    protected PaginationDTO pagination;
    protected ExportedBatchFilterDTO filter;
    protected List<BatchDTO> batches;

    public ExportedBatchResultDTO() {
    }

    public ExportedBatchResultDTO(ExportedBatchFilterDTO filter, Integer pageStart, Integer pageSize) {
        this.filter = filter;
        this.pagination = new PaginationDTO(pageStart, pageSize);
    }

    public ExportedBatchResultDTO(Long messageStartDate, Long messageEndDate, List<ExportedBatchStatusType> statuses, Boolean includeReExportedBatches, Integer pageStart, Integer pageSize) {
        this.filter = new ExportedBatchFilterDTO(messageStartDate, messageEndDate, statuses, includeReExportedBatches);
        this.pagination = new PaginationDTO(pageStart, pageSize);
    }

    public ExportedBatchFilterDTO getFilter() {
        return filter;
    }

    public void setFilter(ExportedBatchFilterDTO filter) {
        this.filter = filter;
    }

    public PaginationDTO getPagination() {
        return pagination;
    }

    public void setPagination(PaginationDTO paginationDTO) {
        this.pagination = paginationDTO;
    }

    public List<BatchDTO> getBatches() {
        if (batches == null) {
            batches = new ArrayList<>();
        }
        return batches;
    }
}
