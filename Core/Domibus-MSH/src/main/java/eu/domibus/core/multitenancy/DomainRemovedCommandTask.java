package eu.domibus.core.multitenancy;

import eu.domibus.api.cluster.Command;
import eu.domibus.api.cluster.CommandProperty;
import eu.domibus.core.clustering.CommandTask;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
public class DomainRemovedCommandTask implements CommandTask {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomainRemovedCommandTask.class);

    final DynamicDomainManagementService dynamicDomainManagementService;

    public DomainRemovedCommandTask(DynamicDomainManagementService dynamicDomainManagementService) {
        this.dynamicDomainManagementService = dynamicDomainManagementService;
    }

    @Override
    public boolean canHandle(String command) {
        return StringUtils.equalsIgnoreCase(Command.DOMAIN_REMOVED, command);
    }

    @Override
    public void execute(Map<String, String> properties) {
        String domainCode = properties.get(CommandProperty.UPDATED_DOMAIN);
        LOG.debug("Executing remove domain command for domain [{}]", domainCode);
        dynamicDomainManagementService.removeDomain(domainCode, false);
    }
}
