package eu.domibus.ext.delegate.services.property;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.ext.delegate.converter.DomainExtConverter;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.DomibusPropertyExtService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Service
public class DomibusPropertyServiceDelegate implements DomibusPropertyExtService {

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected DomainService domainService;

    @Autowired
    protected DomainExtConverter domainConverter;

    @Autowired
    DomainContextExtService domainContextService;

    @Override
    public String getProperty(String propertyName) {
        return domibusPropertyProvider.getProperty(propertyName);
    }

    @Override
    public String getProperty(DomainDTO domain, String propertyName) {
        return getDomainProperty(domain, propertyName);
    }

    @Override
    public Set<String> filterPropertiesName(Predicate<String> predicate) {
        return domibusPropertyProvider.filterPropertiesName(predicate);
    }

    @Override
    public List<String> getNestedProperties(String prefix) {
        return domibusPropertyProvider.getNestedProperties(prefix);
    }

    @Override
    public String getDomainProperty(DomainDTO domain, String propertyName) {
        final Domain domibusDomain = domainConverter.convert(domain, Domain.class);
        return domibusPropertyProvider.getProperty(domibusDomain, propertyName);
    }

    @Override
    public void setDomainProperty(DomainDTO domain, String propertyName, String propertyValue) {
        final Domain domibusDomain = domainConverter.convert(domain, Domain.class);
        domibusPropertyProvider.setProperty(domibusDomain, propertyName, propertyValue);
    }

    @Override
    public void setProperty(String propertyName, String propertyValue) {
        DomainDTO currentDomain = domainContextService.getCurrentDomainSafely();
        Domain domibusDomain = domainConverter.convert(currentDomain, Domain.class);

        domibusPropertyProvider.setProperty(domibusDomain, propertyName, propertyValue);
    }

    @Override
    public boolean containsDomainPropertyKey(DomainDTO domainDTO, String propertyName) {
        final Domain domain = domainConverter.convert(domainDTO, Domain.class);
        return domibusPropertyProvider.containsDomainPropertyKey(domain, propertyName);
    }

    @Override
    public boolean containsPropertyKey(String propertyName) {
        return domibusPropertyProvider.containsPropertyKey(propertyName);
    }

    @Override
    public String getDomainProperty(DomainDTO domainCode, String propertyName, String defaultValue) {
        final Domain domain = domainConverter.convert(domainCode, Domain.class);
        String value = domibusPropertyProvider.getProperty(domain, propertyName);
        if (StringUtils.isEmpty(value)) {
            value = defaultValue;
        }
        return value;
    }

    @Override
    public String getDomainResolvedProperty(DomainDTO domainCode, String propertyName) {
        return getDomainProperty(domainCode, propertyName);
    }

    @Override
    public String getResolvedProperty(String propertyName) {
        return getProperty(propertyName);
    }
}
