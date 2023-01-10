package eu.domibus.web.rest.ro;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * REST resource class for getting the current value of a domibus property along with its metadata attributes
 */

public class DomibusPropertyRO {

    private String name;

    private String type;

    private String description;

    private String module;

    private String section;

    private String usageText;

    private boolean withFallback;

    private boolean writable;

    private boolean encrypted;

    private boolean clusterAware;

    private boolean isComposable;

    private String value;

    private String usedValue;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getUsedValue() {
        return usedValue;
    }

    public void setUsedValue(String usedValue) {
        this.usedValue = usedValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUsageText() {
        return usageText;
    }

    public void setUsageText(String usageText) {
        this.usageText = usageText;
    }

    public boolean isComposable() {
        return isComposable;
    }

    public void setComposable(boolean composable) {
        isComposable = composable;
    }

    public boolean isWithFallback() {
        return withFallback;
    }

    public void setWithFallback(boolean withFallback) {
        this.withFallback = withFallback;
    }

    public boolean isClusterAware() {
        return clusterAware;
    }

    public void setClusterAware(boolean clusterAware) {
        this.clusterAware = clusterAware;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public boolean isWritable() {
        return writable;
    }

    public void setWritable(boolean writable) {
        this.writable = writable;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }
}
