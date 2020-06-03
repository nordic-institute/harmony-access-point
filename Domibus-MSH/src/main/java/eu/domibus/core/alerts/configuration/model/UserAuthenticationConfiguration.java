package eu.domibus.core.alerts.configuration.model;

public interface UserAuthenticationConfiguration {
    /**
     * true if we should check about external authentication enabled
     * @return boolean
     */
    boolean shouldCheckExtAuthEnabled();
}
