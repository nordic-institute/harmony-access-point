package eu.domibus.core.alerts.configuration.common;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@FunctionalInterface
public interface ConfigurationReader<E> {
    E readConfiguration();
}
