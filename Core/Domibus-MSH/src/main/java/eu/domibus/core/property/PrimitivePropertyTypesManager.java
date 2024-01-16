package eu.domibus.core.property;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Service
public class PrimitivePropertyTypesManager {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PrimitivePropertyTypesManager.class);

    private final Properties domibusDefaultProperties;

    public PrimitivePropertyTypesManager(@Qualifier("domibusDefaultProperties") Properties domibusDefaultProperties) {
        this.domibusDefaultProperties = domibusDefaultProperties;
    }

    protected Integer getIntegerInternal(String propertyName, String customValue) {
        if (customValue != null) {
            try {
                return Integer.valueOf(customValue);
            } catch (final NumberFormatException e) {
                LOG.warn("Could not parse the property [" + propertyName + "] custom value [" + customValue + "] to an integer value", e);
                return getDefaultIntegerValue(propertyName);
            }
        }
        return getDefaultIntegerValue(propertyName);
    }

    protected Long getLongInternal(String propertyName, String customValue) {
        if (customValue != null) {
            try {
                return Long.valueOf(customValue);
            } catch (final NumberFormatException e) {
                LOG.warn("Could not parse the property [" + propertyName + "] custom value [" + customValue + "] to a Long value", e);
                return getDefaultLongValue(propertyName);
            }
        }
        return getDefaultLongValue(propertyName);
    }

    public double getDecimalInternal(String propertyName, String propertyValue) {
        if (propertyValue == null) {
            return getDefaultDoubleValue(propertyName);
        }

        try {
            return Double.valueOf(propertyValue);
        } catch (final NumberFormatException e) {
            LOG.warn("Could not parse the property [" + propertyName + "] value [" + propertyValue + "] to a Double value", e);
            return getDefaultDoubleValue(propertyName);
        }
    }

    protected Integer getDefaultIntegerValue(String propertyName) {
        Integer defaultValue = MapUtils.getInteger(domibusDefaultProperties, propertyName);
        return checkDefaultValue(propertyName, defaultValue);
    }

    protected Long getDefaultLongValue(String propertyName) {
        Long defaultValue = MapUtils.getLong(domibusDefaultProperties, propertyName);
        return checkDefaultValue(propertyName, defaultValue);
    }

    protected Double getDefaultDoubleValue(String propertyName) {
        Double defaultValue = MapUtils.getDouble(domibusDefaultProperties, propertyName);
        return checkDefaultValue(propertyName, defaultValue);
    }

    protected Boolean getBooleanInternal(String propertyName, String customValue) {
        if (customValue != null) {
            Boolean customBoolean = BooleanUtils.toBooleanObject(customValue);
            if (customBoolean != null) {
                return customBoolean;
            }
            LOG.warn("Could not parse the property [{}] custom value [{}] to a boolean value", propertyName, customValue);
            return getDefaultBooleanValue(propertyName);
        }
        return getDefaultBooleanValue(propertyName);
    }

    protected Boolean getDefaultBooleanValue(String propertyName) {
        // We need to fetch the Boolean value in two steps as the MapUtils#getBoolean(Properties, String) does not return "null" when the value is an invalid Boolean.
        String defaultValue = MapUtils.getString(domibusDefaultProperties, propertyName);
        Boolean defaultBooleanValue = BooleanUtils.toBooleanObject(defaultValue);
        return checkDefaultValue(propertyName, defaultBooleanValue);
    }

    private <T> T checkDefaultValue(String propertyName, T defaultValue) {
        if (defaultValue == null) {
            throw new IllegalStateException("The default property [" + propertyName + "] is required but was either not found inside the default properties or found having an invalid value");
        }
        LOG.debug("Found the property [{}] default value [{}]", propertyName, defaultValue);
        return defaultValue;
    }

}
