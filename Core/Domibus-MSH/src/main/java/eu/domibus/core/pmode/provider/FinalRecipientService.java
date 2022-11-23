package eu.domibus.core.pmode.provider;

import eu.domibus.api.model.participant.FinalRecipientEntity;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.core.participant.FinalRecipientDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Cosmin Baciu
 * @since 5.0.2
 */
@Service
public class FinalRecipientService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FinalRecipientService.class);

    private final Map<String, String> finalRecipientAccessPointUrls = new ConcurrentHashMap<>();

    @Autowired
    protected FinalRecipientDao finalRecipientDao;

    public String getEndpointURL(String finalRecipient) {
        String finalRecipientAPUrl = finalRecipientAccessPointUrls.get(finalRecipient);

        if (StringUtils.isNotBlank(finalRecipientAPUrl)) {
            LOG.debug("Getting from cache the endpoint URL for final recipient [{}]", finalRecipient);
            return finalRecipientAPUrl;
        }
        LOG.debug("Checking from database the endpoint URL for final recipient [{}]", finalRecipient);
        final FinalRecipientEntity finalRecipientEntity = finalRecipientDao.findByFinalRecipient(finalRecipient);
        if (finalRecipientEntity == null) {
            LOG.debug("No endpoint URL found in the database for final recipient [{}]", finalRecipient);
            return null;
        }
        finalRecipientAPUrl = finalRecipientEntity.getEndpointURL();
        LOG.debug("Updating the cache from database for final recipient [{}] with endpoint URL [{}]", finalRecipient, finalRecipientAPUrl);
        finalRecipientAccessPointUrls.putIfAbsent(finalRecipient, finalRecipientAPUrl);
        return finalRecipientAPUrl;
    }

    @Transactional
    public void saveFinalRecipientEndpoint(String finalRecipient, String finalRecipientEndpointUrl, Domain domain) {
        synchronized (ConfigurationLockContainer.getForDomain(domain)) {
            FinalRecipientEntity finalRecipientEntity = finalRecipientDao.findByFinalRecipient(finalRecipient);
            if (finalRecipientEntity == null) {
                LOG.debug("Creating final recipient instance for [{}]", finalRecipient);
                finalRecipientEntity = new FinalRecipientEntity();
                finalRecipientEntity.setFinalRecipient(finalRecipient);
            }
            LOG.debug("Updating in database the endpoint URL to [{}] for final recipient [{}]", finalRecipientEndpointUrl, finalRecipient);
            finalRecipientEntity.setEndpointURL(finalRecipientEndpointUrl);
            finalRecipientDao.createOrUpdate(finalRecipientEntity);

            //update the cache
            finalRecipientAccessPointUrls.put(finalRecipient, finalRecipientEndpointUrl);
        }
    }

    public void clearFinalRecipientAccessPointUrls(Domain domain) {
        synchronized (ConfigurationLockContainer.getForDomain(domain)) {
            finalRecipientAccessPointUrls.clear();
        }
    }

    @Transactional
    public void deleteFinalRecipients(List<FinalRecipientEntity> finalRecipients, Domain domain) {
        if (CollectionUtils.isEmpty(finalRecipients)) {
            LOG.debug("There are no FinalRecipients to delete");
            return;
        }
        synchronized (ConfigurationLockContainer.getForDomain(domain)) {
            finalRecipientDao.deleteAll(finalRecipients);
            for (FinalRecipientEntity finalRecipient : finalRecipients) {
                finalRecipientAccessPointUrls.remove(finalRecipient.getFinalRecipient());
            }
        }
    }

    @Transactional(readOnly = true)
    public List<FinalRecipientEntity> getFinalRecipientsOlderThan(int numberOfDays) {
        if(numberOfDays < 0){
            LOG.debug("The number of days after which FinalRecipients are deleted should be a positive number (numberOfDays=[{}])", numberOfDays);
            return Collections.emptyList();
        }
        return finalRecipientDao.findFinalRecipientsOlderThan(numberOfDays);
    }
}