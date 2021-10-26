package eu.domibus.ext.domain.archive;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Joze Rihtarsic
 * @since 5.0
 */
public class ExportedBatchResultDTO {

    PaginationDTO pagination;
    ExportedBatchFilterDTO filter;
    List<ExportedBatchDTO> exportedBatches;

    public ExportedBatchResultDTO(ExportedBatchFilterDTO filter, Integer pageStart, Integer pageSize) {
        this.filter = filter;
        this.pagination = new PaginationDTO(pageStart, pageSize);
    }


    public ExportedBatchResultDTO(Long messageStartDate, Long messageEndDate, ExportedBatchStatusTypeParameter status, Boolean reExport, Integer pageStart, Integer pageSize) {
        this.filter = new ExportedBatchFilterDTO(messageStartDate, messageEndDate, status, reExport);
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

    public List<ExportedBatchDTO> getExportedBatches() {
        if (exportedBatches == null) {
            exportedBatches = new ArrayList<>();
        }
        return exportedBatches;
    }
}
