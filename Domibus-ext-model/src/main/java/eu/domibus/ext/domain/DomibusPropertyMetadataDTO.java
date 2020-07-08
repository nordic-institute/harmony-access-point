package eu.domibus.ext.domain;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * It is the plugin api equivalent of the DomibusPropertyMetadata
 * Class that encapsulates the properties of a domibus configuration property;
 */
public class DomibusPropertyMetadataDTO {
    public boolean isGlobal() {
        return (getUsage() & Usage.GLOBAL) == Usage.GLOBAL;
    }

    public boolean isSuper() {
        return (getUsage() & Usage.SUPER) == Usage.SUPER;
    }

    public boolean isDomain() {
        return (getUsage() & Usage.DOMAIN) == Usage.DOMAIN;
    }

    /**
     * The name of the property, corresponds to the value in the domibus.properties files
     * ex: domibus.ui.replication.sync.cron, domibus.ui.replication.sync.cron.max.rows
     */
    private String name;

    /**
     * The technical type of the property which dictates the way it is handled at runtime
     * ex:  numeric, cron expr, regexp, string, concurrency
     */
    private String type;

    /**
     * When false, it means global property. When true, it means domain property.
     * In single tenancy mode, a global property can be changed by regular admins.
     * In multi tenancy mode, a global property can be changed only by AP admins.
     */
    private boolean domainSpecific;

    /**
     * When GLOBAL, it means global property. When DOMAIN, it means domain property, when SUPER, it means for super-users
     * In single tenancy mode, a global property can be changed by regular admins.
     * In multi tenancy mode, a global property can be changed only by AP admins.
     */
    private int usage;

    /**
     * If it can be suffixed with different sufixes
     */
    private boolean isComposable;

    /**
     * For domain properties, this flag specifies whether the value is read
     * from the default domain if not found in the current domain.
     * This is subject to change in the near future.
     */
    private boolean withFallback;

    /**
     * When true, the property must be changed across the cluster machines, so a notification is raised
     */
    private boolean clusterAware;

    /**
     * Corresponds to the existing sections in domibus.properties files: Cluster, GUI, Keystore/Truststore
     */
    private String section;

    /**
     * Corresponds to the existing descriptions of properties found in domibus.properties files
     * ex: #The location of the keystore
     */
    private String description;

    /**
     * One of Module class constants: MSH, WS_PLUGIN, JMS_PLUGIN, FS_PLUGIN but can take other values for new plugins or external modules
     */
    private String module;

    private boolean writable;

    private boolean encrypted;

    /**
     * It is used to mark that the the value is stored in its own property bag or in the core property bag
     * In case of true, the get/set property method delegate to the domibus property provider(ex: jms plugin and dss module_
     * In case of false, the value is set/get using its own property bag( ex: ws and fs plugins)
     */
    private boolean storedGlobally = true;

    public DomibusPropertyMetadataDTO() {
    }

    public DomibusPropertyMetadataDTO(String name, String module, boolean writable, int usage, boolean withFallback, boolean clusterAware, boolean encrypted, boolean isComposable) {
        this(name, Type.STRING, module, writable, usage, withFallback, clusterAware, encrypted, isComposable);
    }

    public DomibusPropertyMetadataDTO(String name, String type, String module, boolean writable, int usage, boolean withFallback, boolean clusterAware, boolean encrypted, boolean isComposable) {
        this.name = name;
        this.type = type;
        this.writable = writable;
        this.usage = usage;
        this.withFallback = withFallback;
        this.clusterAware = clusterAware;
        this.module = module;
        this.encrypted = encrypted;
        this.isComposable = isComposable;
    }

    public DomibusPropertyMetadataDTO(String name, String module, int usage) {
        this(name, module, true, usage, false, true, false, false);
    }

    public DomibusPropertyMetadataDTO(String name, String type, String module, int usage) {
        this(name, type, module, true, usage, false, true, false, false);
    }

    public DomibusPropertyMetadataDTO(String name, String module, int usage, boolean withFallback) {
        this(name, module, true, usage, withFallback, true, false, false);
    }

    public DomibusPropertyMetadataDTO(String name, String type, String module, int usage, boolean withFallback) {
        this(name, type, module, true, usage, withFallback, true, false, false);
    }

    public DomibusPropertyMetadataDTO(String name, int usage, boolean withFallback) {
        this(name, Module.MSH, true, usage, withFallback, true, false, false);
    }

    public DomibusPropertyMetadataDTO(String name) {
        this(name, Module.MSH, true, Usage.DOMAIN, true, true, false, false);
    }

    @Deprecated
    public DomibusPropertyMetadataDTO(String name, String module, boolean domainSpecific, boolean withFallback) {
        this(name, module, domainSpecific ? DomibusPropertyMetadataDTO.Usage.DOMAIN : Usage.GLOBAL, withFallback);
    }

    @Deprecated
    public DomibusPropertyMetadataDTO(String name, String module, boolean domainSpecific) {
        this(name, module, domainSpecific ? DomibusPropertyMetadataDTO.Usage.DOMAIN : Usage.GLOBAL, false);
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

    /**
     * @deprecated Use instead {@link eu.domibus.ext.domain.DomibusPropertyMetadataDTO#getUsage() }
     */
    @Deprecated
    public boolean isDomainSpecific() {
        return domainSpecific;
    }

    /**
     * @deprecated Use instead {@link eu.domibus.ext.domain.DomibusPropertyMetadataDTO#setUsage(int) }
     */
    @Deprecated
    public void setDomainSpecific(boolean domainSpecific) {
        this.domainSpecific = domainSpecific;
    }

    public int getUsage() {
        return usage;
    }

    public void setUsage(int usage) {
        this.usage = usage;
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

    public boolean isStoredGlobally() {
        return storedGlobally;
    }

    public void setStoredGlobally(boolean storedGlobally) {
        this.storedGlobally = storedGlobally;
    }

    public class Usage {
        public static final int GLOBAL = 1;
        public static final int DOMAIN = 2;
        public static final int SUPER = 4;
        public static final int GLOBAL_AND_DOMAIN = GLOBAL | DOMAIN;
        public static final int DOMAIN_AND_SUPER = DOMAIN | SUPER;
    }

    /**
     * Metadata being used by third-party modules, here we added the type just as a string and not an enum
     */
    public class Type {
        public static final String NUMERIC = "NUMERIC";
        public static final String BOOLEAN = "BOOLEAN";
        public static final String STRING = "STRING";
        public static final String CRON = "CRON";
        public static final String CONCURRENCY = "CONCURRENCY";
        public static final String EMAIL = "EMAIL";

        public static final String PASSWORD = "PASSWORD"; //NOSONAR 
        public static final String REGEXP = "REGEXP";
        public static final String URI = "URI";
        public static final String CLASS = "CLASS";
        public static final String HYPHENED_NAME = "HYPHENED_NAME";
        public static final String COMMA_SEPARATED_LIST = "COMMA_SEPARATED_LIST";
        public static final String JNDI = "JNDI";
    }
}
