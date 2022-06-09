package eu.domibus.core.multitenancy;

import eu.domibus.api.cluster.Command;
import eu.domibus.api.cluster.CommandProperty;
import eu.domibus.core.clustering.CommandTask;
import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
public class DomainAddedCommandTask implements CommandTask {

    private static final IDomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DomainAddedCommandTask.class);

    final DynamicDomainManagementService dynamicDomainManagementService;

    public DomainAddedCommandTask(DynamicDomainManagementService dynamicDomainManagementService) {
        this.dynamicDomainManagementService = dynamicDomainManagementService;
    }

    @Override
    public boolean canHandle(String command) {
        return StringUtils.equalsIgnoreCase(Command.DOMAIN_ADDED, command);
    }

    @Override
    public void execute(Map<String, String> properties) {
        String domainCode = properties.get(CommandProperty.UPDATED_DOMAIN);
        LOGGER.debug("Executing add domain command for domain [{}]", domainCode);
        dynamicDomainManagementService.addDomain(domainCode, false);
    }
}
