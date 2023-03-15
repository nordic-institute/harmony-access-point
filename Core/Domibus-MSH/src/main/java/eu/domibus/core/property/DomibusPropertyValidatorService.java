package eu.domibus.core.property;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.util.WarningUtil;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public DomibusPropertyValidatorService(DomibusPropertyProvider domibusPropertyProvider, DomibusConfigurationService domibusConfigurationService,
                                           DomainService domainService, DomainTaskExecutor domainTaskExecutor) {
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.domibusConfigurationService = domibusConfigurationService;
        this.domainService = domainService;
        this.domainTaskExecutor = domainTaskExecutor;
    }

    public void enforceValidation() {
        if (domibusConfigurationService.isMultiTenantAware()) {
            validationEArchiveAndRetentionForAllDomains();

            validateUnsecureLoginAllowed();
        } else {
            validationEArchiveAndRetention();
        }
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
}
