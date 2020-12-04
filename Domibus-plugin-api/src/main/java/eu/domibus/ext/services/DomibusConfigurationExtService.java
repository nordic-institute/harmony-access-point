package eu.domibus.ext.services;

/**
 * Service used for operations related with Domibus Configuration.
 *
 * @author Tiago Miguel
 * @since 4.0
 */
public interface DomibusConfigurationExtService {

    /**
     * Checks if Domibus runs in multi tenancy mode
     *
     * @return true if Domibus runs in multi tenancy mode
     */
    boolean isMultiTenantAware();

    boolean isSingleTenantAware();

    boolean isSecuredLoginRequired();

    String getConfigLocation();

    boolean isClusterDeployment();

}
