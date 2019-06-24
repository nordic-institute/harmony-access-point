package eu.domibus.core.payload.temp;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
@Service
public class TemporaryPayloadServiceImpl implements TemporaryPayloadService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TemporaryPayloadServiceImpl.class);

    @Override
    public void cleanTemporaryPayloads() {
        final List<File> temporaryLocations = getTemporaryLocations();
        LOG.debug("Cleaning temporaryLocations [{}]", temporaryLocations);

        for (File temporaryLocation : temporaryLocations) {
            cleanTemporaryPayloads(temporaryLocation);
        }
        LOG.debug("Finished cleaning temporaryLocations [{}]", temporaryLocations);
    }

    protected void cleanTemporaryPayloads(File directory) {
        LOG.debug("Cleaning temporary directory [{}]", directory);

        final List<File> candidateFileForDeletion = getCandidateFileForDeletion(directory);
        LOG.debug("Found the following candidate files for deletion [{}]", candidateFileForDeletion);

        for (File file : candidateFileForDeletion) {
            final boolean fileExpired = isFileExpired(file);
            if (fileExpired) {
                LOG.debug("File [{}] has expired", file);
                deleteFileSafely(file);
            }
        }
        LOG.debug("Finished cleaning temporary directory [{}]", directory);
    }

    protected void deleteFileSafely(File file) {
        try {
            //delete file
            LOG.debug("File deleted [{}]", file);
        } catch (Exception e) {
            LOG.error("Error deleting file [{}]", file);
        }
    }

    protected boolean isFileExpired(File file) {
        //TODO
        return false;
    }

    protected List<File> getCandidateFileForDeletion(File directory) {
        //list recursively files from directory by applying the configured regex property
        //TODO
        return null;
    }

    protected List<File> getTemporaryLocations() {
        //TODO
        return null;
    }
}
