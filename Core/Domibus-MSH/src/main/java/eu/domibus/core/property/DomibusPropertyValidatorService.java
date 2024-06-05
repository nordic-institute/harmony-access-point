package eu.domibus.core.property;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.util.WarningUtil;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * @author François Gautier
 * @since 5.0
 */
@Service
public class DomibusPropertyValidatorService {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(DomibusPropertyValidatorService.class);

    private final DomibusPropertyProvider domibusPropertyProvider;
    private final DomibusConfigurationService domibusConfigurationService;
    private final DomainService domainService;
    private final DomainTaskExecutor domainTaskExecutor;
    private final GlobalPropertyMetadataManager globalPropertyMetadataManager;

    public DomibusPropertyValidatorService(DomibusPropertyProvider domibusPropertyProvider, DomibusConfigurationService domibusConfigurationService,
                                           DomainService domainService, DomainTaskExecutor domainTaskExecutor,
                                           GlobalPropertyMetadataManager globalPropertyMetadataManager) {
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.domibusConfigurationService = domibusConfigurationService;
        this.domainService = domainService;
        this.domainTaskExecutor = domainTaskExecutor;
        this.globalPropertyMetadataManager = globalPropertyMetadataManager;
    }

    public void enforceValidation() {
        if (domibusConfigurationService.isMultiTenantAware()) {
            validationEArchiveAndRetentionForAllDomains();

            validateUnsecureLoginAllowed();
        } else {
            validationEArchiveAndRetention();
        }
        validatePropertiesPasswordPolicy();
    }

    private void validationEArchiveAndRetentionForAllDomains() {
        final List<Domain> domains = domainService.getDomains();
        for (Domain domain : domains) {
            domainTaskExecutor.submit(this::validationEArchiveAndRetention, domain);
        }
    }

    private void validateUnsecureLoginAllowed() {
        Boolean unsecureLoginAllowed = domibusPropertyProvider.getBooleanProperty(DOMIBUS_AUTH_UNSECURE_LOGIN_ALLOWED);
        if (unsecureLoginAllowed) {
            LOG.warn(WarningUtil.warnOutput(DOMIBUS_AUTH_UNSECURE_LOGIN_ALLOWED + " property is true but it has no effect in multi-tenancy environment!"));
        }
    }
    private void validationEArchiveAndRetention() {
        Boolean eArchive = domibusPropertyProvider.getBooleanProperty(DOMIBUS_EARCHIVE_ACTIVE);
        Boolean deleteOnSuccess = domibusPropertyProvider.getBooleanProperty(DOMIBUS_SEND_MESSAGE_SUCCESS_DELETE_PAYLOAD);

        if (BooleanUtils.isTrue(eArchive) && BooleanUtils.isTrue(deleteOnSuccess)) {
            LOG.warn(WarningUtil.warnOutput("Delete payload on success is active AND Earchive is active -> force disable Delete payload on success "));
            domibusPropertyProvider.setProperty(DOMIBUS_SEND_MESSAGE_SUCCESS_DELETE_PAYLOAD, "false");
        }
    }

    public void validatePropertiesPasswordPolicy() {
        LOG.debug("Validating password policy for all the properties of type password.");
        final Pattern passwordPolicyPattern = Pattern.compile(domibusPropertyProvider.getProperty(DOMIBUS_PROPERTIES_PASSWORD_POLICY_PATTERN));
        final boolean enforcePropertiesPasswordPolicy = BooleanUtils.isTrue(domibusPropertyProvider.getBooleanProperty(DOMIBUS_PROPERTIES_PASSWORD_POLICY_ENFORCE));

        final Map<String, DomibusPropertyMetadata> allProperties = globalPropertyMetadataManager.getAllProperties();
        final List<DomibusPropertyMetadata> allPasswordProperties = allProperties.values().stream()
                .filter(prop -> StringUtils.equals(prop.getType(), DomibusPropertyMetadata.Type.PASSWORD.name()))
                .collect(Collectors.toList());

        boolean problemsFound = false;
        for (DomibusPropertyMetadata property : allPasswordProperties) {
            if (!propertyMatchesPasswordPolicy(property, passwordPolicyPattern)) {
                problemsFound = true;
            }
        }

        if (enforcePropertiesPasswordPolicy && problemsFound) {
            throw new DomibusPropertyException("When [" + DOMIBUS_PROPERTIES_PASSWORD_POLICY_ENFORCE + "] is set to true, all password properties must match [" + DOMIBUS_PROPERTIES_PASSWORD_POLICY_PATTERN + "].");
        }
    }

    private boolean propertyMatchesPasswordPolicy(DomibusPropertyMetadata property, Pattern passwordPolicyPattern) {
        boolean result = true;
        // the property can be Global and/or Domain
        if (property.isGlobal() && !globalPropertyMatchesPasswordPolicy(property, passwordPolicyPattern)) {
            result = false;
        }
        if (property.isDomain() && !domainPropertyMatchesPasswordPolicy(property, passwordPolicyPattern)) {
            result = false;
        }
        return result;
    }

    private boolean globalPropertyMatchesPasswordPolicy(DomibusPropertyMetadata property, Pattern passwordPolicyPattern) {
        LOG.debug("Validating password policy for global property [{}].", property.getName());
        final String password = domibusPropertyProvider.getProperty(property.getName());
        if (passwordMatchesPasswordPolicy(password, passwordPolicyPattern)) {
            return true;
        }
        LOG.warn(WarningUtil.warnOutput("Password property [" + property.getName() + "] doesn't match the password policy pattern [" + DOMIBUS_PROPERTIES_PASSWORD_POLICY_PATTERN + "]."));
        return false;
    }

    private boolean domainPropertyMatchesPasswordPolicy(DomibusPropertyMetadata property, Pattern passwordPolicyPattern) {
        LOG.debug("Validating password policy for domain property [{}].", property.getName());
        boolean result = true;
        final List<Domain> domains = domainService.getDomains();
        for (Domain domain : domains) {
            final String password = domibusPropertyProvider.getProperty(domain, property.getName());
            if (!passwordMatchesPasswordPolicy(password, passwordPolicyPattern)) {
                LOG.warn(WarningUtil.warnOutput("Password property [" + property.getName() + "] doesn't match the password policy pattern [" + DOMIBUS_PROPERTIES_PASSWORD_POLICY_PATTERN + "] on domain [" + domain.getName() + "]."));
                result = false;
            }
        }
        return result;
    }

    public boolean passwordMatchesPasswordPolicy(String password, Pattern passwordPolicyPattern) {
        if (StringUtils.isBlank(password)) {
            return true;
        }
        return passwordPolicyPattern.matcher(password).matches();
    }
}
