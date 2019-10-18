package eu.domibus.api.property;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * Class that encapsulates the properties of a domibus configuration property;
 */
public class DomibusPropertyMetadata {

    public boolean appliesForGlobal() {
        return (getType() & Type.GLOBAL) == Type.GLOBAL;
    }

    public boolean appliesForSuper() {
        return (getType() & Type.SUPER) == Type.SUPER;
    }

    public boolean appliesForDomain() {
        return (getType() & Type.DOMAIN) == Type.DOMAIN;
    }

    public class Type {
        public static final int GLOBAL = 1;
        public static final int DOMAIN = 2;
        public static final int SUPER = 4;
        public static final int GLOBAL_AND_DOMAIN = GLOBAL | DOMAIN;
        public static final int DOMAIN_AND_SUPER = DOMAIN | SUPER;
    }

    private String name;

    /**
     * When GLOBAL, it means global property. When DOMAIN_SPECIFIC, it means domain property.
     * In single tenancy mode, a global property can be changed by regular admins.
     * In multi tenancy mode, a global property can be changed only by AP admins.
     */
    private int type;

    /**
     * For domain properties(which also means super properties), this flag specifies whether the value is read
     * from the global properties file(which contains also default values for domain properties) if not found in the current domain.
     */
    private boolean withFallback;

    private boolean clusterAware;

    private String section;

    private String description;

    private String module;

    private boolean writable;

    private boolean encrypted;

    public static DomibusPropertyMetadata getGlobalProperty(String name) {
        return new DomibusPropertyMetadata(name, Type.GLOBAL, false);
    }

    public static DomibusPropertyMetadata getReadOnlyGlobalProperty(String name) {
        return new DomibusPropertyMetadata(name, Module.MSH, false, Type.GLOBAL, false, false, false);
    }

    public static DomibusPropertyMetadata getReadOnlyGlobalProperty(String name, boolean encrypted) {
        return new DomibusPropertyMetadata(name, Module.MSH, false, Type.GLOBAL, false, false, encrypted);
    }

    public DomibusPropertyMetadata() {
    }

    public DomibusPropertyMetadata(String name, String module, boolean writable, int type, boolean withFallback, boolean clusterAware, boolean encrypted) {
        this.name = name;
        this.writable = writable;
        this.type = type;
        this.withFallback = withFallback;
        this.clusterAware = clusterAware;
        this.module = module;
        this.encrypted = encrypted;
    }

    public DomibusPropertyMetadata(String name,int type, boolean withFallback) {
        this(name, Module.MSH, true, type, withFallback, true, false);
    }

    public DomibusPropertyMetadata(String name, boolean writable, int type, boolean withFallback) {
        this(name, Module.MSH, writable, type, withFallback, true, false);
    }

    public DomibusPropertyMetadata(String name, boolean writable, int type, boolean withFallback, boolean encrypted) {
        this(name, Module.MSH, writable, type, withFallback, true, encrypted);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        DomibusPropertyMetadata el = (DomibusPropertyMetadata) o;

        return new EqualsBuilder()
                .append(name, el.getName())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(name)
                .toHashCode();
    }


}
