package eu.domibus.property;

public enum PropertyUsageType {
    GLOBAL_PROPERTY, // Global AP Property ???
    // proprietate care are efect asupra intregului AP;
    // it is read with getProperty
    // poate fi modificata doar in mod single tenancy sau de catre super admin
    // e.g.  domibus.alert.sender.smtp.user

    DOMAIN_PROPERTY_NO_FALLBACK, // specific domain property , no fallback
    // proprietate care are efect asupra domeniului curent.
    // se citeste NUMAI din domeniul curent   ,   cu getProperty(domain, name)
    // e.g. domibus.security.keystore.type

    DOMAIN_PROPERTY_WITH_FALLBACK, // overridable property
    // it can be defined in the default domain and overwritten in another)
    // e.g.  domibus.UI.title.name   ->   domain_name.domibus.UI.title.name

    DOMAIN_PROPERTY_RESOLVED, // overridable property
    // it can be defined in the default domain and overwritten in another)
    // it is read with getResolvedProperty(domain, name)
    // e.g. domibus.security.keystore.location
}
