package eu.domibus.web.rest.ro;

import java.io.Serializable;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 */

public class PropertyFilterRequestRO implements Serializable {
    private int page = 0;
    private int pageSize = 10;
    private String name;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
