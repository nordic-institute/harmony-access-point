package eu.domibus.plugin.fs.worker;

import org.springframework.stereotype.Service;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.fs.FSFilesManager;

/**
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
@Service
public class FSPurgeFailedService extends FSAbstractPurgeService {
    
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSPurgeFailedService.class);
    
    @Override
    public void purgeMessages() {
        LOG.debug("Purging failed file system messages...");

        super.purgeMessages();
    }

    @Override
    protected String getTargetFolderName() {
        return FSFilesManager.FAILED_FOLDER;
    }

    @Override
    protected int getExpirationLimit(String domain) {
        return fsPluginProperties.getFailedPurgeExpired(domain);
    }

}
