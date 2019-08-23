package eu.domibus.api.property;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * Class that encapsulates the properties of a domibus configuration property;
 */
public class DomibusPropertyMetadata {

    private String name;

    private String type; // numeric, cronexp, regexp, string, concurrency

    /**
     * When false, it means global property. When true, it means domain property.
     * In single tenancy mode, a global property can be changed by regular admins.
     * In multi tenancy mode, a global property can be changed only by AP admins.
     */
    private boolean domainSpecific;

    /**
     * For domain properties, this flag specifies whether the value is read
     * from the default domain if not found in the current domain.
     * This is subject to change in the near future.
     */
    private boolean withFallback;

    private boolean clusterAware;

    private String section;

    private String description;

    private String module;

    public static DomibusPropertyMetadata getGlobalProperty(String name) {
        return new DomibusPropertyMetadata(name, false, false);
    }

    public DomibusPropertyMetadata(String name, String module, boolean domainSpecific, boolean withFallback, boolean clusterAware) {
        this.name = name;
        this.domainSpecific = domainSpecific;
        this.withFallback = withFallback;
        this.clusterAware = clusterAware;
        this.module = module;
    }

    public DomibusPropertyMetadata(String name, boolean domainSpecific, boolean withFallback) {
        this(name, Module.MSH, domainSpecific, withFallback, true);
    }

    public DomibusPropertyMetadata(String name, boolean domainSpecific) {
        this(name, Module.MSH, domainSpecific, false, true);
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

    public boolean isDomainSpecific() {
        return domainSpecific;
    }

    public void setDomainSpecific(boolean domainSpecific) {
        this.domainSpecific = domainSpecific;
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
}
