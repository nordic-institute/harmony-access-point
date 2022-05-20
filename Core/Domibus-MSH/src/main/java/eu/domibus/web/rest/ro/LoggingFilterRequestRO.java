package eu.domibus.web.rest.ro;

import java.io.Serializable;

/**
 * @author Ion Perpegel
 * @since 4.1
 */

public class LoggingFilterRequestRO implements Serializable {

    private int page = 0;
    private int pageSize = 10;
    private String orderBy;
    private Boolean asc = true;

    private String loggerName = "eu.domibus";
    private boolean showClasses = false;

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

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public Boolean getAsc() {
        return asc;
    }

    public void setAsc(Boolean asc) {
        this.asc = asc;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    public boolean isShowClasses() {
        return showClasses;
    }

    public void setShowClasses(boolean showClasses) {
        this.showClasses = showClasses;
    }
}
