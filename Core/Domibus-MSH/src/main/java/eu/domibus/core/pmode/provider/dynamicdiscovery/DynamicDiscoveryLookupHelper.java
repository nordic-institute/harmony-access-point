package eu.domibus.core.pmode.provider.dynamicdiscovery;

import eu.domibus.api.dynamicdyscovery.DynamicDiscoveryLookupEntity;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Cosmin Baciu
 * @since 5.1.1
 */
@Service
public class DynamicDiscoveryLookupHelper {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DynamicDiscoveryLookupHelper.class);

    protected DynamicDiscoveryLookupDao dynamicDiscoveryLookupDao;

    public DynamicDiscoveryLookupHelper(DynamicDiscoveryLookupDao dynamicDiscoveryLookupDao) {
        this.dynamicDiscoveryLookupDao = dynamicDiscoveryLookupDao;
    }

    @Transactional
    public List<DynamicDiscoveryLookupEntity> deleteFromDatabaseExpiredDdcFinalRecipients(Date dateLimit) {
        LOG.info("Getting the DDC final recipients which were not discovered more recently than [{}]", dateLimit);

        final List<DynamicDiscoveryLookupEntity> finalRecipientsNotDiscoveredInTheLastPeriod = dynamicDiscoveryLookupDao.findFinalRecipientsNotDiscoveredInTheLastPeriod(dateLimit);
        final List<String> finalRecipients = finalRecipientsNotDiscoveredInTheLastPeriod.stream().map(discoveryLookupEntity -> discoveryLookupEntity.getFinalRecipientValue()).collect(Collectors.toList());

        LOG.info("Deleting [{}] from database the DDC final recipients not discovered more recently than [{}] with the following final recipients: [{}]", finalRecipients.size(), dateLimit, finalRecipients);
        dynamicDiscoveryLookupDao.deleteAll(finalRecipientsNotDiscoveredInTheLastPeriod);

        return finalRecipientsNotDiscoveredInTheLastPeriod;
    }

}
