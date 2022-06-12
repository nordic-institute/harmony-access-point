package eu.domibus.core.property;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DomibusRawPropertyProviderImpl implements DomibusRawPropertyProvider {

    @Autowired
    DomibusPropertyProviderImpl domibusPropertyProvider;

    @Override
    public String getRawPropertyValue(String propertyName) throws DomibusPropertyException {
        return domibusPropertyProvider.getRawPropertyValue(propertyName, null);
    }

    @Override
    public String getRawPropertyValue(Domain domain, String propertyName) throws DomibusPropertyException {
        if (domain == null) {
            throw new DomibusPropertyException("Property " + propertyName + " cannot be retrieved without a domain");
        }

        return domibusPropertyProvider.getRawPropertyValue(propertyName, domain);
    }
}
