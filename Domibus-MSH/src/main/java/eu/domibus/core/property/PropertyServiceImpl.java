package eu.domibus.core.property;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.web.rest.ro.PropertyRO;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 */
public class PropertyServiceImpl implements PropertyService {

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    public List<PropertyRO> getProperties(String name) {
        List<PropertyRO> list = new ArrayList<>();

        return list;
    }

    private Map<String, PropMeta> getKnownProperties() {

        return Arrays.stream(new PropMeta[] {
            new PropMeta()
        }).collect(Collectors.toList());
    }

    class PropMeta {
        private String name;
        private UsageType usage;
        private String type; // numeric, cronexp, regexp, string, concurrency

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public UsageType getUsage() {
            return usage;
        }

        public void setUsage(UsageType usage) {
            this.usage = usage;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    enum UsageType {
        AP_PROPERTY, // GLOBAL Property ??? Global AP Property ???
        // proprietate care are efect asupra intregului AP;
        // se citeste cu getProperty
        // poate fi modificata doar in mod single tenancy sau de catre super admin

        // e.g.  domibus.alert.sender.smtp.user


        DOMAIN_PROPERTY, // specific domain property , no fallback
        // proprietate care are efect asupra domeniului curent.
        // se citeste NUMAI din domeniul curent   ,   cu getProperty(domain, name)

        // e.g. domibus.security.keystore.type


        DOMAIN_PROPERTY_WITH_FALLBACK, // overridable property
        // it can be defined in the default domain and overwritten in another)


        // e.g.  domibus.UI.title.name   ->   domain_name.domibus.UI.title.name


    }
}
