package eu.domibus.core.property;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.web.rest.ro.PropertyRO;
import org.springframework.beans.factory.annotation.Autowired;

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
        Domain currentDomain = null; // TODO
        for (PropMeta p : this.getKnownProperties().values()) {

            PropertyRO prop = new PropertyRO();
            prop.setName(p.getName());

            if (p.getUsage() == UsageType.DOMAIN_PROPERTY_WITH_FALLBACK) {
                String value = domibusPropertyProvider.getDomainProperty(p.getName());
                prop.setValue(value);
                list.add(prop);
            } else if (p.getUsage() == UsageType.DOMAIN_PROPERTY) {
                String value = domibusPropertyProvider.getProperty(currentDomain, p.getName());
                prop.setValue(value);
                list.add(prop);
            } else if (p.getUsage() == UsageType.GLOBAL_PROPERTY) {
                // TODO
            }
        }

        return list;
    }

    private Map<String, PropMeta> getKnownProperties() {
        return Arrays.stream(new PropMeta[] {
                new PropMeta("domibus.UI.title.name"),
                new PropMeta("domibus.passwordPolicy.pattern"),
                new PropMeta("domibus.userInput.blackList", UsageType.GLOBAL_PROPERTY) ,
                new PropMeta("domibus.security.keystore.password", UsageType.DOMAIN_PROPERTY)
        }).collect(Collectors.toMap(x -> x.getName(), x -> x));
    }

    class PropMeta {
        private String name;
        private UsageType usage;
        private String type; // numeric, cronexp, regexp, string, concurrency

        public PropMeta(String name) {
            this(name, UsageType.DOMAIN_PROPERTY_WITH_FALLBACK);
        }

        public PropMeta(String name, UsageType usage) {
            this.name = name;
            this.usage = usage;
        }

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
        GLOBAL_PROPERTY, // GLOBAL Property ??? Global AP Property ???
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
