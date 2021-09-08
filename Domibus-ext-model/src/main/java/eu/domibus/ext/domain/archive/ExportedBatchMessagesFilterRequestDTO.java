package eu.domibus.ext.domain.archive;

public class ExportedBatchMessagesFilterRequestDTO {

    Integer limit;
    Integer start;

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }
}
