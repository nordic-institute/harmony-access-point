package eu.domibus.ext.domain;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * It is the plugin api equivalent of the DomibusPropertyMetadata
 * Class that encapsulates the properties of a domibus configuration property;
 */
public class DomibusPropertyMetadataDTO {
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

    private int type;

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

    private boolean writable;

    private boolean encrypted;

    public DomibusPropertyMetadataDTO() {
    }

    public DomibusPropertyMetadataDTO(String name, String module, boolean writable, int type, boolean withFallback, boolean clusterAware, boolean encrypted) {
        this.name = name;
        this.writable = writable;
        this.type = type;
        this.withFallback = withFallback;
        this.clusterAware = clusterAware;
        this.module = module;
        this.encrypted = encrypted;
    }

    public DomibusPropertyMetadataDTO(String name, String module, int type) {
        this(name, module, true, type, false, true, false);
    }

    public DomibusPropertyMetadataDTO(String name, String module, int type, boolean withFallback) {
        this(name, module, true, type, withFallback, true, false);
    }

    public DomibusPropertyMetadataDTO(String name, int type, boolean withFallback) {
        this(name, Module.MSH, true, type, withFallback, true, false);
    }

    public DomibusPropertyMetadataDTO(String name) {
        this(name, Module.MSH, true, Type.DOMAIN, true, true, false);
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
}
