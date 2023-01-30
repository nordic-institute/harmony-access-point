package eu.domibus.core.property;

import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_DATABASE_GENERAL_SCHEMA;

/**
 * Utilities for reading property values from the environment and from properties files.
 *
 * @author Sebastian-Ion TINCU
 * @since 5.1
 */
public class PropertyUtils {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PropertyUtils.class);

    /**
     * Returns a property value by searching it using its property name first in the environment variables, then in the
     * system properties.
     *
     * @param propertyName the property name
     * @return the property value or {@value StringUtils#EMPTY} when not found.
     */
    public static String getPropertyValue(String propertyName) {
        return getPropertyValue(propertyName, Optional.empty());
    }

    /**
     * Returns a property value by searching it using its property name first in the environment variables, then in the
     * system properties and optionally, when providing a path, in the properties files located at that path.
     *
     * @param propertyName the property name
     * @param propertiesFilePath an optional path to a properties file
     * @return the property value or {@value StringUtils#EMPTY} when not found.
     */
    public static String getPropertyValue(String propertyName, Optional<Path> propertiesFilePath) {
        LOG.info("Looking for the value of a property having the name of [{}]", propertyName);

        String environment = System.getenv(propertyName);
        if (StringUtils.isNotBlank(environment)) {
            LOG.info("Found an environment variable having the value of [{}]", environment);
            return environment;
        }

        String propertyValue = System.getProperty(propertyName);
        if (StringUtils.isNotBlank(propertyValue)) {
            LOG.info("Found a system property having the value of [{}]", propertyValue);
            return propertyValue;
        }

        if (propertiesFilePath.isPresent()) {
            Path path = propertiesFilePath.get();
            LOG.info("Searching inside the properties file at [{}]", path);
            if (!Files.isRegularFile(path)) {
                throw new DomibusPropertyException("The properties file is missing at [" + path + "]");
            }

            Properties properties = new Properties();
            try (Reader reader = Files.newBufferedReader(path)) {
                properties.load(reader);
                if(properties.containsKey(propertyName)) {
                    propertyValue = properties.getProperty(propertyName);
                    LOG.info("Found a file property having the value of [{}]", propertyValue);
                    return propertyValue;
                }
            } catch (IOException e) {
                throw new DomibusPropertyException("An error occurred while loading the property from the file located at [" + path + "]", e);
            }
        }

        if (!StringUtils.equalsIgnoreCase(propertyName, DOMIBUS_DATABASE_GENERAL_SCHEMA)) {
            LOG.warn("No system environment variables, system properties nor file properties found matching the name of [{}]: returning a default empty value", propertyName);
        }

        return StringUtils.EMPTY;
    }
}
