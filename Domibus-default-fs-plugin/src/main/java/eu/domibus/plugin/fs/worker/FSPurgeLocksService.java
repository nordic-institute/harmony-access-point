package eu.domibus.plugin.fs.worker;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.fs.FSFileNameHelper;
import eu.domibus.plugin.fs.FSFilesManager;
import eu.domibus.plugin.fs.exception.FSSetUpException;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service responsible for the purge of orphan/zombie lock files
 *
 * @author Ion Perpegel
 * @since 4.2
 */
@Service
public class FSPurgeLocksService {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSPurgeLocksService.class);

    private FSDomainService multiTenancyService;

    private FSFilesManager fsFilesManager;

    private FSFileNameHelper fsFileNameHelper;

    public FSPurgeLocksService(FSDomainService multiTenancyService, FSFilesManager fsFilesManager, FSFileNameHelper fsFileNameHelper) {
        this.multiTenancyService = multiTenancyService;
        this.fsFilesManager = fsFilesManager;
        this.fsFileNameHelper = fsFileNameHelper;
    }

    public void purge() {
        LOG.debug("Purging orphan lock files....");

        List<String> domains = multiTenancyService.getDomainsToProcess();
        for (String domain : domains) {
            if (multiTenancyService.verifyDomainExists(domain)) {
                purge(domain);
            }
        }
    }

    protected void purge(String domain) {
        FileObject[] files = null;
        try (FileObject rootDir = fsFilesManager.setUpFileSystem(domain)) {
            files = fsFilesManager.findAllDescendantFiles(rootDir);
            LOG.debug("Found files [{}]", files);

            List<String> lockedFileNames = fsFileNameHelper.filterLockedFileNames(files);
            LOG.debug("Found locked file names [{}]", lockedFileNames);

            for (String lockedFileName : lockedFileNames) {
                if (!fsFilesManager.fileExists(rootDir, lockedFileName)) {
                    LOG.debug("File [{}] does not exists so delete the corresponding lock file.", lockedFileName);
                    fsFilesManager.deleteFileByName(rootDir, fsFileNameHelper.getLockFileName(lockedFileName));
                }
            }
        } catch (FileSystemException ex) {
            LOG.error("Error purging orphan lock files", ex);
        } catch (FSSetUpException ex) {
            LOG.error("Error setting up folders for domain: " + domain, ex);
        } finally {
            if (files != null) {
                fsFilesManager.closeAll(files);
            }
        }
    }
}
