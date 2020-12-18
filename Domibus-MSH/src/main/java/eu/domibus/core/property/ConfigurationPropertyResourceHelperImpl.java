package eu.domibus.core.property;

import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.*;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.core.rest.validators.DomibusPropertyValueValidator;
import eu.domibus.core.rest.validators.FieldBlacklistValidator;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadata.NAME_SEPARATOR;

/**
 * Responsible with getting the domibus properties that can be changed at runtime, getting and setting their values
 *
 * @author Ion Perpegel
 * @since 4.1.1
 */
@Service
public class ConfigurationPropertyResourceHelperImpl implements ConfigurationPropertyResourceHelper {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(ConfigurationPropertyResourceHelperImpl.class);

    public static final String ACCEPTED_CHARACTERS_IN_PROPERTY_NAMES = NAME_SEPARATOR;

    private DomibusConfigurationService domibusConfigurationService;

    private DomibusPropertyProvider domibusPropertyProvider;

    private AuthUtils authUtils;

    private DomainTaskExecutor domainTaskExecutor;

    private GlobalPropertyMetadataManager globalPropertyMetadataManager;

    private DomibusPropertyValueValidator domibusPropertyValueValidator;

    private FieldBlacklistValidator propertyNameBlacklistValidator;

    private Map<SortMapKey, Comparator<DomibusProperty>> sortingComparatorsMap = new HashMap<>();

    public ConfigurationPropertyResourceHelperImpl(DomibusConfigurationService domibusConfigurationService,
                                                   DomibusPropertyProvider domibusPropertyProvider,
                                                   AuthUtils authUtils,
                                                   DomainTaskExecutor domainTaskExecutor,
                                                   GlobalPropertyMetadataManager globalPropertyMetadataManager,
                                                   DomibusPropertyValueValidator domibusPropertyValueValidator,
                                                   FieldBlacklistValidator propertyNameBlacklistValidator) {
        this.domibusConfigurationService = domibusConfigurationService;
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.authUtils = authUtils;
        this.domainTaskExecutor = domainTaskExecutor;
        this.globalPropertyMetadataManager = globalPropertyMetadataManager;
        this.domibusPropertyValueValidator = domibusPropertyValueValidator;
        this.propertyNameBlacklistValidator = propertyNameBlacklistValidator;
        this.propertyNameBlacklistValidator.init();

        initSortMap();
    }

    @Override
    public List<DomibusProperty> getAllProperties(DomibusPropertiesFilter filter) {
        List<DomibusPropertyMetadata> propertiesMetadata = filterProperties(globalPropertyMetadataManager.getAllProperties(), filter);

        if (CollectionUtils.isEmpty(propertiesMetadata)) {
            return new ArrayList();
        }

        List<DomibusProperty> properties;

        properties = new RetrieveProcess()
                .getByDomain(filter, propertiesMetadata)
                .filterByValue(filter.getValue())
                .sort(filter.getOrderBy(), filter.getAsc())
                .getResults();

        return properties;
    }

    @Override
    public void setPropertyValue(String propertyName, boolean isDomain, String propertyValue) throws DomibusPropertyException {
        validatePropertyValue(propertyName, propertyValue);

        if (isDomain) {
            LOG.debug("Setting the value [{}] for the domain property [{}] in the current domain.", propertyValue, propertyName);
            domibusPropertyProvider.setProperty(propertyName, propertyValue);
            LOG.info("Property [{}] updated.", propertyName);
            return;
        }
        if (!authUtils.isSuperAdmin()) {
            throw new DomibusPropertyException("Cannot set global or super properties if not a super user.");
        }
        // for non-domain properties, we set the value in the null-domain context:
        domainTaskExecutor.submit(() -> {
            LOG.debug("Setting the value [{}] for the global/super property [{}].", propertyValue, propertyName);
            domibusPropertyProvider.setProperty(propertyName, propertyValue);
            LOG.info("Property [{}] updated.", propertyName);
        });
    }

    @Override
    public DomibusProperty getProperty(String propertyName) {
        if (!globalPropertyMetadataManager.hasKnownProperty(propertyName)) {
            throw new DomibusPropertyException("Unknown property: " + propertyName);
        }

        DomibusPropertyMetadata propertyMetadata = globalPropertyMetadataManager.getPropertyMetadata(propertyName);
        return getValueAndCreateProperty(propertyMetadata);
    }

    protected List<DomibusProperty> getPropertyValues(List<DomibusPropertyMetadata> properties) {
        Map<String, DomibusProperty> result = new HashMap<>();

        for (DomibusPropertyMetadata propMeta : properties) {
            if (!propMeta.isComposable()) {
                LOG.trace("Getting property value for non-composable property [{}]", propMeta.getName());
                DomibusProperty prop = getValueAndCreateProperty(propMeta);
                addIfMissing(result, prop);
            } else {
                LOG.trace("Getting property value for composable property [{}]", propMeta.getName());
                List<DomibusProperty> props = getNestedProperties(propMeta);
                props.forEach(prop -> addIfMissing(result, prop));
            }
        }

        return new ArrayList<>(result.values());
    }

    protected List<DomibusProperty> getNestedProperties(DomibusPropertyMetadata propMeta) {
        //add parent property
        List<DomibusProperty> result = new ArrayList<>();
        result.add(getProperty(propMeta.getName()));

        //add nested
        List<String> nestedProps = domibusPropertyProvider.getNestedProperties(propMeta.getName());
        List<DomibusProperty> nested = nestedProps.stream()
                .map(nestedProp -> getProperty(propMeta.getName() + NAME_SEPARATOR + nestedProp))
                .collect(Collectors.toList());
        result.addAll(nested);

        return result;
    }

    protected void validatePropertyValue(String propertyName, String propertyValue) {
        propertyNameBlacklistValidator.validate(propertyName, ACCEPTED_CHARACTERS_IN_PROPERTY_NAMES);

        DomibusPropertyMetadata propMeta = getPropertyMetadata(propertyName);

        if (propMeta == null) {
            throw new DomibusPropertyException("Cannot set property [" + propertyName + "] because it does not exist.");
        }

        if (!propMeta.isWritable()) {
            throw new DomibusPropertyException("Cannot set property [" + propertyName + "] because it is not writable.");
        }

        if (StringUtils.equals(propMeta.getName(), propertyName) && propMeta.isComposable()) {
            throw new DomibusPropertyException("Cannot set composable property [" + propertyName + "] directly. You can only set its nested properties.");
        }

        DomibusProperty prop = createProperty(propMeta, propertyValue);

        prop.setValue(propertyValue);
        domibusPropertyValueValidator.validate(prop);
    }

    protected DomibusPropertyMetadata getPropertyMetadata(String propertyName) {
        // check property metadata is declared
        if (globalPropertyMetadataManager.hasKnownProperty(propertyName)) {
            return globalPropertyMetadataManager.getPropertyMetadata(propertyName);
        }
        // Last chance: if it is a composable property, return its 'parent'/source metadata
        return globalPropertyMetadataManager.getComposableProperty(propertyName);
    }

    protected void addIfMissing(Map<String, DomibusProperty> result, DomibusProperty prop) {
        String name = prop.getMetadata().getName();
        if (!result.containsKey(name)) {
            LOG.debug("Adding property with name [{}] to the result list", name);
            result.put(name, prop);
        }
    }

    protected DomibusProperty getValueAndCreateProperty(DomibusPropertyMetadata propMeta) {
        String propertyValue = domibusPropertyProvider.getProperty(propMeta.getName());
        return createProperty(propMeta, propertyValue);
    }

    protected DomibusProperty createProperty(DomibusPropertyMetadata propMeta, String propertyValue) {
        DomibusProperty prop = new DomibusProperty();
        prop.setMetadata(propMeta);
        prop.setValue(propertyValue);
        return prop;
    }

    protected List<DomibusPropertyMetadata> filterProperties(Map<String, DomibusPropertyMetadata> propertiesMap, DomibusPropertiesFilter filter) {
        List<DomibusPropertyMetadata> knownProps = propertiesMap.values().stream()
                .filter(prop -> filter.isWritable() == null || filter.isWritable() == prop.isWritable())
                .filter(prop -> filter.getName() == null || StringUtils.containsIgnoreCase(prop.getName(), filter.getName()))
                .filter(prop -> filter.getType() == null || StringUtils.equals(filter.getType(), prop.getType()))
                .filter(prop -> filter.getModule() == null || StringUtils.equals(filter.getModule(), prop.getModule()))
                .collect(Collectors.toList());

        if (!domibusConfigurationService.isMultiTenantAware()) {
            return knownProps;
        }

        if (filter.isShowDomain()) {
            return knownProps.stream().filter(p -> p.isDomain()).collect(Collectors.toList());
        }

        if (authUtils.isSuperAdmin()) {
            return knownProps.stream().filter(p -> p.isGlobal() || p.isSuper()).collect(Collectors.toList());
        }

        throw new DomibusPropertyException("Cannot request global and super properties if not a super user.");
    }

    protected void initSortMap() {
        addPropertyComparator("name", domibusProperty -> domibusProperty.getMetadata().getName());
        addPropertyComparator("type", domibusProperty -> domibusProperty.getMetadata().getType());
        addPropertyComparator("module", domibusProperty -> domibusProperty.getMetadata().getModule());
        addPropertyComparator("usage", domibusProperty -> domibusProperty.getMetadata().getUsageText());
    }

    protected void addPropertyComparator(String propertyName, Function<DomibusProperty, String> comparatorFunction) {
        Comparator<DomibusProperty> comparator = Comparator.comparing(comparatorFunction);
        sortingComparatorsMap.put(new SortMapKey(propertyName, true), comparator);
        Comparator<DomibusProperty> reverseComparator = comparator.reversed();
        sortingComparatorsMap.put(new SortMapKey(propertyName, false), reverseComparator);
    }

    static class SortMapKey {
        private String field;
        private boolean asc;

        SortMapKey(String field, boolean asc) {
            this.field = field;
            this.asc = asc;
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .append(field)
                    .append(asc)
                    .toHashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SortMapKey obj = (SortMapKey) o;
            return new EqualsBuilder()
                    .append(field, obj.field)
                    .append(asc, obj.asc)
                    .isEquals();
        }

    }

    class RetrieveProcess {
        List<DomibusProperty> properties;

        protected RetrieveProcess getByDomain(DomibusPropertiesFilter filter, List<DomibusPropertyMetadata> propertiesMetadata) {
            if (filter.isShowDomain()) {
                properties = getPropertyValues(propertiesMetadata);
            } else {
                // for non-domain properties, we get the values in the null-domain context:
                properties = domainTaskExecutor.submit(() -> getPropertyValues(propertiesMetadata));
            }
            return this;
        }

        protected RetrieveProcess filterByValue(String value) {
            if (value == null) {
                return this;
            }
            properties = properties.stream()
                    .filter(prop -> StringUtils.equals(value, prop.getValue()))
                    .collect(Collectors.toList());
            return this;
        }

        protected RetrieveProcess sort(String sortAttribute, boolean sortAscending) {
            Comparator<DomibusProperty> comparator = sortingComparatorsMap.get(new SortMapKey(sortAttribute, sortAscending));
            if (comparator == null) {
                return this;
            }
            properties = properties.stream().sorted(comparator).collect(Collectors.toList());
            return this;
        }

        public List<DomibusProperty> getResults() {
            return properties;
        }
    }
}
