package eu.domibus.core.property;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DataBaseEngine;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.io.File;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_ENTITY_MANAGER_FACTORY_JPA_PROPERTY_HIBERNATE_DIALECT;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Component
public class DefaultDomibusConfigurationService implements DomibusConfigurationService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DefaultDomibusConfigurationService.class);

    private static final String DATABASE_DIALECT = DOMIBUS_ENTITY_MANAGER_FACTORY_JPA_PROPERTY_HIBERNATE_DIALECT;

    private DataBaseEngine dataBaseEngine;

    @Autowired
    protected DomibusPropertyRetrieveManager domibusPropertyRetrieveManager;

    @Autowired
    PrimitivePropertyTypesManager primitivePropertyTypesManager;

    @Override
    public String getConfigLocation() {
        return domibusPropertyRetrieveManager.getInternalProperty(DomibusPropertyMetadataManagerSPI.DOMIBUS_CONFIG_LOCATION);
    }

    @Cacheable("multitenantCache")
    @Override
    public boolean isMultiTenantAware() {
        return StringUtils.isNotBlank(domibusPropertyRetrieveManager.getInternalProperty(DomainService.GENERAL_SCHEMA_PROPERTY));
    }

    @Override
    public boolean isSingleTenantAware() {
        return !isMultiTenantAware();
    }

    @Override
    public boolean isClusterDeployment() {
        return getBooleanProperty(CLUSTER_DEPLOYMENT);
    }

    @Override
    public DataBaseEngine getDataBaseEngine() {
        if (dataBaseEngine == null) {
            final String property = domibusPropertyRetrieveManager.getInternalProperty(DATABASE_DIALECT);
            if (property == null) {
                throw new IllegalStateException("Database dialect not configured, please set property: domibus.entityManagerFactory.jpaProperty.hibernate.dialect");
            }
            dataBaseEngine = DataBaseEngine.getDatabaseEngine(property);
            LOG.debug("Database engine:[{}]", dataBaseEngine);
        }
        return dataBaseEngine;
    }

    @Override
    public boolean isFourCornerEnabled() {
        return getBooleanProperty(FOURCORNERMODEL_ENABLED_KEY);
    }

    @Override
    public boolean isExtAuthProviderEnabled() {
        return getBooleanProperty(EXTERNAL_AUTH_PROVIDER);
    }

    @Override
    public boolean isPayloadEncryptionActive(Domain domain) {
        return getBooleanProperty(domain, PAYLOAD_ENCRYPTION_PROPERTY);
    }

    @Override
    public boolean isPasswordEncryptionActive() {
        return getBooleanProperty(PASSWORD_ENCRYPTION_ACTIVE_PROPERTY);
    }

    @Override
    public boolean isPasswordEncryptionActive(Domain domain) {
        return getBooleanProperty(domain, PASSWORD_ENCRYPTION_ACTIVE_PROPERTY);
    }

    @Override
    public String getConfigurationFileName() {
        return DomibusPropertyProvider.DOMIBUS_PROPERTY_FILE;
    }

    @Override
    public String getConfigurationFileName(Domain domain) {
        String propertyFileName = null;
        if (DomainService.DEFAULT_DOMAIN.equals(domain)) {
            String defaultDomainConfigFile = getDomainConfigurationFileName(DomainService.DEFAULT_DOMAIN);
            final String configurationFile = getConfigLocation() + File.separator + defaultDomainConfigFile;
            LOG.debug("Checking if file [{}] exists", configurationFile);
            if (new File(configurationFile).exists()) {
                LOG.debug("File [{}] exists. Using property file [{}]", configurationFile, defaultDomainConfigFile);
                propertyFileName = defaultDomainConfigFile;
            } else {
                LOG.debug("File [{}] does not exists, using [{}]", configurationFile, DomibusPropertyProvider.DOMIBUS_PROPERTY_FILE);
                propertyFileName = DomibusPropertyProvider.DOMIBUS_PROPERTY_FILE;
            }
        } else {
            propertyFileName = getDomainConfigurationFileName(domain);
            LOG.debug("Using property file [{}]", propertyFileName);
        }

        return propertyFileName;
    }

    public String getDomainConfigurationFileName(Domain domain) {
        return domain.getCode() + "-" + DomibusPropertyProvider.DOMIBUS_PROPERTY_FILE;
    }

    private Boolean getBooleanProperty(Domain domain, String propertyName) {
        String domainValue = domibusPropertyRetrieveManager.getInternalProperty(domain, propertyName);
        return primitivePropertyTypesManager.getBooleanInternal(propertyName, domainValue);
    }

    public Boolean getBooleanProperty(String propertyName) {
        String value = domibusPropertyRetrieveManager.getInternalProperty(propertyName);
        return primitivePropertyTypesManager.getBooleanInternal(propertyName, value);
    }
}
