package eu.domibus.ext.domain.archive;

public class PaginationDTO {
    Integer pageStart;
    Integer pageSize;
    Integer total;

    public PaginationDTO() {
    }

    public PaginationDTO(Integer pageStart, Integer pageSize) {
        this.pageStart = pageStart;
        this.pageSize = pageSize;
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

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }
}
