package eu.domibus.core.property;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 */

public class DomibusPropertiesFilter {

    private String name;

    private boolean showDomain = true;

    private String type;

    private String module;

    private String value;

    private Boolean writable;

    private String sortAttribute;

    private Boolean sortAscending;

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

    public String getSortAttribute() {
        return sortAttribute;
    }

    public void setSortAttribute(String sortAttribute) {
        this.sortAttribute = sortAttribute;
    }

    public Boolean getSortAscending() {
        return sortAscending;
    }

    public void setSortAscending(Boolean sortAscending) {
        this.sortAscending = sortAscending;
    }
}
