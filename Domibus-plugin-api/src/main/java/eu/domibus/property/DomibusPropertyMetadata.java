package eu.domibus.property;

public class DomibusPropertyMetadata {
    private String name;
    private PropertyUsageType usage;
    private String type; // numeric, cronexp, regexp, string, concurrency

    public DomibusPropertyMetadata(String name) {
        this(name, PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK);
    }

    public DomibusPropertyMetadata(String name, PropertyUsageType usage) {
        this.name = name;
        this.usage = usage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PropertyUsageType getUsage() {
        return usage;
    }

    public void setUsage(PropertyUsageType usage) {
        this.usage = usage;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
