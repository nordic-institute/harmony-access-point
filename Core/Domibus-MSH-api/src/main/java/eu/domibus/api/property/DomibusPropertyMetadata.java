package eu.domibus.api.property;

import eu.domibus.api.property.validators.CronValidator;
import eu.domibus.api.property.validators.DomibusPropertyValidator;
import eu.domibus.api.property.validators.RegexpValidator;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Arrays;
import java.util.List;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * Class that encapsulates the properties of a domibus configuration property;
 */
public class DomibusPropertyMetadata {

    public static final String NAME_SEPARATOR = ".";

    public boolean isOnlyGlobal() {
        return usage == Usage.GLOBAL;
    }

    public boolean isGlobal() {
        return (usage.getValue() & Usage.GLOBAL.getValue()) == Usage.GLOBAL.getValue();
    }

    public boolean isSuper() {
        return (usage.getValue() & Usage.SUPER.getValue()) == Usage.SUPER.getValue();
    }

    public boolean isDomain() {
        return (usage.getValue() & Usage.DOMAIN.getValue()) == Usage.DOMAIN.getValue();
    }

    /**
     * The name of the property, corresponds to the value in the domibus.properties files
     */
    private String name;

    /**
     * The technical type of the property which dictates the way it is handled at runtime
     * ex:  numeric, cron expr, regexp, string, concurrency
     */
    private String type;

    /**
     * When GLOBAL, it means global property. When DOMAIN, it means domain property, when SUPER, it means for super-users
     * In single tenancy mode, a global property can be changed by regular admins.
     * In multi tenancy mode, a global property can be changed only by AP admins.
     */
    private Usage usage;

    /**
     * If it can be suffixed with different sufixes
     */
    private boolean isComposable;

    /**
     * For domain properties(which also means super properties), this flag specifies whether the value is read
     * from the global properties file(which contains also default values for domain properties) if not found in the current domain.
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
     * Used here to copy the corresponding info from the DomibusPropertyMetadataDTO class, aka the external modules metadata
     * It is used to specify if the value is stored in its own (external module) property bag or in the core property bag
     */
    private boolean storedGlobally = true;

    public static DomibusPropertyMetadata getOnTheFlyProperty(String propertyName) {
        DomibusPropertyMetadata domibusPropertyMetadata = new DomibusPropertyMetadata(propertyName, Usage.ANY, true);
        domibusPropertyMetadata.setModule(Module.UNKNOWN);
        domibusPropertyMetadata.setWritable(false);
        return domibusPropertyMetadata;
    }

    public static DomibusPropertyMetadata getGlobalProperty(String name) {
        return getGlobalProperty(name, Type.STRING);
    }

    public static DomibusPropertyMetadata getGlobalProperty(String name, Type type) {
        DomibusPropertyMetadata domibusPropertyMetadata = new DomibusPropertyMetadata(name, Usage.GLOBAL, false);
        domibusPropertyMetadata.setType(type.name());
        return domibusPropertyMetadata;
    }

    public static DomibusPropertyMetadata getGlobalProperty(String name, Type type, boolean encrypted) {
        DomibusPropertyMetadata domibusPropertyMetadata = new DomibusPropertyMetadata(name, Usage.GLOBAL, false, encrypted);
        domibusPropertyMetadata.setType(type.name());
        return domibusPropertyMetadata;
    }

    public static DomibusPropertyMetadata getReadOnlyGlobalProperty(String name) {
        return getReadOnlyGlobalProperty(name, Type.STRING);
    }

    public static DomibusPropertyMetadata getReadOnlyGlobalProperty(String name, Type type) {
        DomibusPropertyMetadata domibusPropertyMetadata = new DomibusPropertyMetadata(name, Module.MSH, false, Usage.GLOBAL, false, false, false, false);
        domibusPropertyMetadata.setType(type.name());
        return domibusPropertyMetadata;
    }

    public static DomibusPropertyMetadata getReadOnlyGlobalProperty(String name, Type type, boolean encrypted) {
        return new DomibusPropertyMetadata(name, type, Module.MSH, false, Usage.GLOBAL, false, false, encrypted, false);
    }

    public static DomibusPropertyMetadata getReadOnlyGlobalProperty(String name, String module) {
        return new DomibusPropertyMetadata(name, module, false, Usage.GLOBAL, false, false, false, false);
    }

    public static DomibusPropertyMetadata getReadOnlyGlobalProperty(String name, Type type, String module) {
        return new DomibusPropertyMetadata(name, type, module, false, Usage.GLOBAL, false, false, false, false);
    }

    public DomibusPropertyMetadata() {
    }

    public DomibusPropertyMetadata(String name, String module, boolean writable, Usage usage, boolean withFallback, boolean clusterAware, boolean encrypted, boolean isComposable) {
        this(name, Type.STRING, module, writable, usage, withFallback, clusterAware, encrypted, isComposable);
    }

    public DomibusPropertyMetadata(String name, Type type, String module, boolean writable, Usage usage, boolean withFallback, boolean clusterAware, boolean encrypted, boolean isComposable) {
        this.name = name;
        this.type = type.name();
        this.writable = writable;
        this.usage = usage;
        this.withFallback = withFallback;
        this.clusterAware = clusterAware;
        this.module = module;
        this.encrypted = encrypted;
        this.isComposable = isComposable;
    }

    public DomibusPropertyMetadata(String name, Type type, Usage usage, boolean withFallback) {
        this(name, type, Module.MSH, true, usage, withFallback, true, false, false);
    }

    public DomibusPropertyMetadata(String name, Usage usage, boolean withFallback) {
        this(name, Module.MSH, true, usage, withFallback, true, false, false);
    }

    public DomibusPropertyMetadata(String name, Usage usage, boolean withFallback, boolean encrypted) {
        this(name, Module.MSH, true, usage, withFallback, true, encrypted, false);
    }

    public DomibusPropertyMetadata(String name, Type type, boolean writable, Usage usage, boolean withFallback) {
        this(name, type, Module.MSH, writable, usage, withFallback, true, false, false);
    }

    public DomibusPropertyMetadata(String name, boolean writable, Usage usage, boolean withFallback) {
        this(name, Module.MSH, writable, usage, withFallback, true, false, false);
    }

    public DomibusPropertyMetadata(String name, boolean writable, Usage usage, boolean withFallback, boolean encrypted) {
        this(name, Module.MSH, writable, usage, withFallback, true, encrypted, false);
    }

    public DomibusPropertyMetadata(String name, Type type, boolean writable, Usage usage, boolean withFallback, boolean encrypted) {
        this(name, type, Module.MSH, writable, usage, withFallback, true, encrypted, false);
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

    public Type getTypeAsEnum() {
        return Type.valueOf(type);
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getUsage() {
        return usage.getValue();
    }

    public void setUsage(int usage) {
        Usage val = Arrays.stream(Usage.values())
                .filter(el -> el.getValue() == usage).findFirst()
                .orElseThrow(() -> new DomibusPropertyException("Invalid domibus property usage: " + usage));
        this.usage = val;
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

    public String getUsageText() {
        return usage.getText();
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

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("name", this.name)
                .append("module", this.module)
                .append("type", this.type)
                .append("usage", this.getUsageText())
                .append("withFallback", this.withFallback)
                .append("encrypted", this.encrypted)
                .append("isComposable", this.isComposable)
                .append("clusterAware", this.clusterAware)
                .append("writable", this.writable)
                .toString();
    }

    /**
     * States if a property is used as a global, domain, super or a valid combination of them
     */
    public enum Usage {
        GLOBAL(1, "Global"),
        DOMAIN(2, "Domain"),
        SUPER(4, "Super"),
        GLOBAL_AND_DOMAIN(GLOBAL.getValue() | DOMAIN.getValue(), GLOBAL.getText() + " and " + DOMAIN.getText()),
        DOMAIN_AND_SUPER(DOMAIN.getValue() | SUPER.getValue(), DOMAIN.getText() + " and " + SUPER.getText()),
        ANY(GLOBAL.getValue() | DOMAIN.getValue() | SUPER.getValue(), GLOBAL.getText() + " and " + DOMAIN.getText() + " and " + SUPER.getText());

        private int value;
        private String text;

        Usage(int value, String text) {
            this.value = value;
            this.text = text;
        }

        public int getValue() {
            return value;
        }

        public String getText() {
            return text;
        }
    }

    /**
     * Metadata being an internal class, we control everything so we added the type as an enum as it has some convenience methods and type-safety
     * More types can be added later without any breaking changes
     */
    public enum Type {
        POSITIVE_INTEGER("^[0-9]+$"),
        POSITIVE_DECIMAL("^[0-9]+.[0-9]{1,2}$"),
        NUMERIC("^-?[0-9]+(\\.[0-9]{1,2})?$"),
        BOOLEAN("^(true|false)$"),

        CONCURRENCY("^(\\d+(\\-\\d+)*)$"),
        EMAIL("^(([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+.[a-zA-Z]{1,})[\\s\\;]*)*$"),
        CRON(new CronValidator()),

        // added so that the default black-list validation is skipped and the type validation is properly done
        PASSWORD(".*"),
        REGEXP(".*"),
        URI("^[ a-zA-Z0-9._%+\\-:/\\\\?=&\\+~\\!@#$%\\^\\&\\(\\)_\\[\\]{}\\;\\'\\,`]*$"),
        CLASS("^[a-zA-Z0-9_\\.]*$"),
        JNDI("^[a-zA-Z0-9_!\\.\\/\\:]*$"),
        HYPHENED_NAME("^[a-zA-Z0-9_\\-]*$"),
        COMMA_SEPARATED_LIST("^[ a-zA-Z0-9_>\\.\\-\\,]*$"),
        FREE_TEXT("^[\\x20-\\x7D]*$"),

        // no type validation for String except for the default black-list validation
        STRING();

        // properties
        private String regularExpression;

        private DomibusPropertyValidator validator;

        Type() {
        }

        Type(String regularExpression) {
            this.regularExpression = regularExpression;
            this.validator = new RegexpValidator(regularExpression);
        }

        Type(DomibusPropertyValidator validator) {
            this.validator = validator;
        }

        public String getRegularExpression() {
            return regularExpression;
        }

        public DomibusPropertyValidator getValidator() {
            return validator;
        }

        public boolean isNumeric() {
            return getNumericTypes().contains(this);
        }

        public boolean isBoolean() {
            return this == BOOLEAN;
        }

        private List<Type> getNumericTypes() {
            return Arrays.asList(POSITIVE_INTEGER, POSITIVE_DECIMAL, NUMERIC);
        }
    }

}
