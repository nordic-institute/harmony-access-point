package eu.domibus.property;

public enum PropertyUsageType {

    /**
     * Global AP property.
     * It affects the entire AP.
     * In single tenancy mode, it can be changed by regular admins.
     * In multi tenancy mode, it can be changed only by AP admins.
     */
    GLOBAL_PROPERTY,

    /**
     * Domain property, no fallback to the default domain value.
     * Can be changed by either domain admins or AP admins.
     */
    DOMAIN_PROPERTY_NO_FALLBACK,

    /**
     * Domain property, with fallback to the default domain value.
     * Can be changed by either domain admins or AP admins.
     */
    DOMAIN_PROPERTY_WITH_FALLBACK,

    /**
     * Resolved domain property, no fallback to the default domain value.
     * Can be changed by either domain admins or AP admins.
     */
    DOMAIN_PROPERTY_RESOLVED,
}
