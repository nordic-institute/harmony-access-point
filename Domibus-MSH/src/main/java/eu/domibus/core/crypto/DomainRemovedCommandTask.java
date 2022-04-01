package eu.domibus.core.crypto;

import eu.domibus.api.cluster.Command;
import eu.domibus.core.clustering.CommandTask;
import eu.domibus.core.multitenancy.DynamicDomainManagementService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
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
        LOG.debug("Checking and handling domain removed");

        String domainCode = properties.get(MessageConstants.DOMAIN);
        dynamicDomainManagementService.removeDomain(domainCode);
    }
}