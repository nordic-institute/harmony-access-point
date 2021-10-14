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
public class DomainAddedCommandTask implements CommandTask {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DomainAddedCommandTask.class);

    final DynamicDomainManagementService dynamicDomainManagementService ;

    public DomainAddedCommandTask(DynamicDomainManagementService dynamicDomainManagementService){
        this.dynamicDomainManagementService = dynamicDomainManagementService;
    }

    @Override
    public boolean canHandle(String command) {
        return StringUtils.equalsIgnoreCase(Command.DOMAIN_ADDED, command);
    }

    @Override
    public void execute(Map<String, String> properties) {
        LOGGER.debug("Checking and handling domains added");

        String domainCode = properties.get(MessageConstants.DOMAIN);
        dynamicDomainManagementService.addDomain(domainCode);
    }
}
