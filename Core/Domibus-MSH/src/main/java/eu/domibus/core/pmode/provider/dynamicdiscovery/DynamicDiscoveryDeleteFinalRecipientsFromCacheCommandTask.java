package eu.domibus.core.pmode.provider.dynamicdiscovery;

import eu.domibus.api.cluster.Command;
import eu.domibus.api.cluster.CommandProperty;
import eu.domibus.core.clustering.CommandTask;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 5.1.1
 */
@Service
public class DynamicDiscoveryDeleteFinalRecipientsFromCacheCommandTask implements CommandTask {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DynamicDiscoveryDeleteFinalRecipientsFromCacheCommandTask.class);


    protected DynamicDiscoveryLookupService dynamicDiscoveryLookupService;

    public DynamicDiscoveryDeleteFinalRecipientsFromCacheCommandTask(DynamicDiscoveryLookupService dynamicDiscoveryLookupService) {
        this.dynamicDiscoveryLookupService = dynamicDiscoveryLookupService;
    }

    @Override
    public boolean canHandle(String command) {
        return StringUtils.equalsIgnoreCase(Command.DELETE_FINAL_RECIPIENTS_CACHE, command);
    }

    @Override
    public void execute(Map<String, String> properties) {
        LOGGER.debug("Deleting final recipients cache command");

        final String finalRecipientsValue = properties.get(CommandProperty.FINAL_RECIPIENTS);
        final String[] finalRecipientsArray = StringUtils.split(finalRecipientsValue, ",");
        if (ArrayUtils.isEmpty(finalRecipientsArray)) {
            LOGGER.debug("Final recipients are empty. Nothing to delete");
            return;
        }
        final List<String> finalRecipients = Arrays.asList(finalRecipientsArray);
        dynamicDiscoveryLookupService.deleteFinalRecipientsFromCache(finalRecipients);
    }
}
