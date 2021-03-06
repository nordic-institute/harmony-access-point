package eu.domibus.web.rest.ro;

import java.io.Serializable;
import java.util.List;

/**
 * @since 4.2
 * @author Soumya Chandran
 */
public class UserResultRO implements Serializable {

    private List<UserResponseRO> entries;
    private Long count;
    private Integer page;
    private Integer pageSize;

    public List<UserResponseRO> getEntries() {
        return entries;
    }

    public void setEntries(List<UserResponseRO> entries) {
        this.entries = entries;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

}