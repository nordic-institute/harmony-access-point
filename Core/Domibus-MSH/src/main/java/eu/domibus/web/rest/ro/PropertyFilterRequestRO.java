package eu.domibus.web.rest.ro;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 */

public class PropertyFilterRequestRO {

    private int page = 0;

    private int pageSize = 10;

    private String name;

    private boolean showDomain = true;

    private String type;

    private String module;

    private String value;

    private Boolean writable;

    private String orderBy;

    private boolean asc;

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

    public boolean isShowDomain() {
        return showDomain;
    }

    public void setShowDomain(boolean showDomain) {
        this.showDomain = showDomain;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Boolean isWritable() {
        return writable;
    }

    public void setWritable(Boolean isWritable) {
        this.writable = isWritable;
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
}
