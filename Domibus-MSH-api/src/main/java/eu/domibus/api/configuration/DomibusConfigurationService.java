package eu.domibus.api.configuration;

import eu.domibus.api.multitenancy.Domain;

import static eu.domibus.api.property.DomibusPropertyMetadataManager.*;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public interface DomibusConfigurationService {

    String DOMIBUS_CONFIG_LOCATION = "domibus.config.location";

    String FOURCORNERMODEL_ENABLED_KEY = DOMIBUS_FOURCORNERMODEL_ENABLED;
    String CLUSTER_DEPLOYMENT = DOMIBUS_DEPLOYMENT_CLUSTERED;
    String EXTERNAL_AUTH_PROVIDER = "domibus.security.ext.auth.provider.enabled";
    String PAYLOAD_ENCRYPTION_PROPERTY = DOMIBUS_PAYLOAD_ENCRYPTION_ACTIVE;
    String PASSWORD_ENCRYPTION_ACTIVE_PROPERTY = DOMIBUS_PASSWORD_ENCRYPTION_ACTIVE; //NOSONAR

    String getConfigLocation();

    boolean isClusterDeployment();

    DataBaseEngine getDataBaseEngine();

    boolean isMultiTenantAware();

    boolean isFourCornerEnabled();

    /**
     * Returns true if external authentication provider is enabled
     *
     * @return boolean - true if an authentication external provider is enabled
     */
    boolean isExtAuthProviderEnabled();

    boolean isPayloadEncryptionActive(Domain domain);

    boolean isPasswordEncryptionActive();

    boolean isPasswordEncryptionActive(Domain domain);

    String getConfigurationFileName();

    String getConfigurationFileName(Domain domain);

}
