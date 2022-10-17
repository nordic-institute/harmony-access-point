package eu.domibus.core.pmode.provider;

import eu.domibus.api.model.participant.FinalRecipientEntity;
import eu.domibus.core.participant.FinalRecipientDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 5.0.2
 */
@Service
public class FinalRecipientService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FinalRecipientService.class);

    protected Map<String, String> finalRecipientAccessPointUrls = new HashMap<>();

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
        finalRecipientAccessPointUrls.put(finalRecipient, finalRecipientAPUrl);
        return finalRecipientAPUrl;
    }

    @Transactional
    public void saveFinalRecipientEndpoint(String finalRecipient, String finalRecipientEndpointUrl) {
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

    public void clearFinalRecipientAccessPointUrls() {
        finalRecipientAccessPointUrls.clear();
    }
}