package eu.domibus.core.crypto.spi.dss;

import eu.domibus.ext.services.DomibusPropertyExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Thomas Dussart
 * @since 4.1
 * <p>
 * This class has a similar behavior than @ConfigurationProperties annotation and allows
 * to parse list of properties in the format domibus.example.name[0],domibus.example.name[1].
 * <p>
 * Subclasses implement a transform method to create the model needed <E> and the map method will then return a List<E>.
 */

public abstract class PropertyGroupMapper<E> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PropertyGroupMapper.class);

    protected final DomibusPropertyExtService domibusPropertyExtService;

    private final Pattern passwordPattern = Pattern.compile(".*password.*", Pattern.CASE_INSENSITIVE);


    public PropertyGroupMapper(final DomibusPropertyExtService domibusPropertyExtService) {
        this.domibusPropertyExtService = domibusPropertyExtService;
    }

    protected List<E> map(String propertyName) {
        List<E> elements = new ArrayList<>();
        List<String> mainPropertiesSuffixes
                = domibusPropertyExtService.getNestedProperties(propertyName);
        List<String> mainProperties = mainPropertiesSuffixes.stream().map(nestedMainProperty -> propertyName + "." + nestedMainProperty).collect(Collectors.toList());
        for (String mainProperty : mainProperties) {
            Map<String, String> keyValues = new HashMap<>();
            List<String> nestedPropertiesSuffixes
                    = domibusPropertyExtService.getNestedProperties(mainProperty);
            for (String nestedPropertySuffix : nestedPropertiesSuffixes) {
                String nestedPropertyName = mainProperty + "." + nestedPropertySuffix;
                String propertyValue = domibusPropertyExtService.getProperty(nestedPropertyName);
                if (!passwordPattern.matcher(nestedPropertyName).matches()) {
                    LOG.debug("Property:[{}] has following value:[{}]", nestedPropertyName, propertyValue);
                }
                keyValues.put(nestedPropertySuffix, propertyValue);
            }
            boolean emptyValue = keyValues.entrySet().stream().anyMatch(entry -> StringUtils.isEmpty(entry.getValue()));
            if (emptyValue || keyValues.size() == 0) {
                continue;
            }
            final E transform = transform(keyValues);
            if (transform != null) {
                elements.add(transform);
            }
        }
        return elements;
    }


    abstract E transform(Map<String, String> keyValues);

}
