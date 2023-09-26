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
public class DynamicDiscoveryDeletePmodePartiesCommandTask implements CommandTask {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DynamicDiscoveryDeletePmodePartiesCommandTask.class);


    protected DynamicDiscoveryLookupService dynamicDiscoveryLookupService;

    public DynamicDiscoveryDeletePmodePartiesCommandTask(DynamicDiscoveryLookupService dynamicDiscoveryLookupService) {
        this.dynamicDiscoveryLookupService = dynamicDiscoveryLookupService;
    }

    @Override
    public boolean canHandle(String command) {
        return StringUtils.equalsIgnoreCase(Command.DELETE_PMODE_PARTIES, command);
    }

    @Override
    public void execute(Map<String, String> properties) {
        LOGGER.debug("Deleting PMode parties command");

        final String pmodePartyNamesValue = properties.get(CommandProperty.PMODE_PARTY_NAMES);
        final String[] pmodePartyNamesArray = StringUtils.split(pmodePartyNamesValue, ",");
        if (ArrayUtils.isEmpty(pmodePartyNamesArray)) {
            LOGGER.debug("PMode parties are empty. Nothing to delete");
            return;
        }
        final List<String> pmodePartyNames = Arrays.asList(pmodePartyNamesArray);
        dynamicDiscoveryLookupService.deletePartiesFromPMode(pmodePartyNames);
    }
}
