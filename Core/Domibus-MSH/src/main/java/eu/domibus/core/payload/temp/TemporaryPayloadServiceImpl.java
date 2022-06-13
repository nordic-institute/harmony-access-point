package eu.domibus.core.payload.temp;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
@Service
public class TemporaryPayloadServiceImpl implements TemporaryPayloadService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TemporaryPayloadServiceImpl.class);

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Override
    public void cleanTemporaryPayloads(Domain domain) {
        final List<File> temporaryLocations = getTemporaryLocations();
        if (CollectionUtils.isEmpty(temporaryLocations)) {
            LOG.debug("Nothing to clean:no temporary locations defined for domain [{}]", domain);
            return;
        }

        LOG.debug("Cleaning temporaryLocations [{}]", temporaryLocations);

        for (File temporaryLocation : temporaryLocations) {
            cleanTemporaryPayloads(temporaryLocation);
        }
        LOG.debug("Finished cleaning temporaryLocations [{}]", temporaryLocations);
    }

    protected void cleanTemporaryPayloads(File directory) {
        LOG.debug("Cleaning temporary directory [{}]", directory);

        final Collection<File> filesToClean = getFilesToClean(directory);
        LOG.debug("Found the following files for deletion [{}]", filesToClean);

        for (File file : filesToClean) {
            deleteFileSafely(file);
        }
        LOG.debug("Finished cleaning temporary directory [{}]", directory);
    }

    protected void deleteFileSafely(File file) {
        try {
            final boolean deleted = file.delete();
            LOG.debug("File [{}] deleted [{}] ?", file, deleted);
        } catch (Exception e) {
            LOG.warn("Error deleting file [{}]", file, e);
        }
    }

    protected Collection<File> getFilesToClean(File directory) {
        final IOFileFilter regexFileFilter = getRegexFileFilter();
        final IOFileFilter ageFileFilter = getAgeFileFilter();
        final IOFileFilter fileFilter = FileFilterUtils.and(regexFileFilter, ageFileFilter);

        return FileUtils.listFiles(directory, fileFilter, TrueFileFilter.INSTANCE);
    }

    protected IOFileFilter getRegexFileFilter() {
        final String excludeRegex = domibusPropertyProvider.getProperty(DOMIBUS_PAYLOAD_TEMP_JOB_RETENTION_EXCLUDE_REGEX);
        LOG.debug("Using temp payload retention regex [{}]", excludeRegex);
        Pattern pattern = Pattern.compile(excludeRegex);
        return FileFilterUtils.notFileFilter(new RegexIOFileFilter(pattern));
    }

    protected IOFileFilter getAgeFileFilter() {
        final int expirationThresholdInMinutes = domibusPropertyProvider.getIntegerProperty(DOMIBUS_PAYLOAD_TEMP_JOB_RETENTION_EXPIRATION);
        LOG.debug("Using temp payload retention expiration threshold in minutes [{}]", expirationThresholdInMinutes);
        long cutoff = System.currentTimeMillis() - (expirationThresholdInMinutes * 60 * 1000);
        return FileFilterUtils.ageFileFilter(cutoff);
    }

    protected List<File> getTemporaryLocations() {
        final String directories = domibusPropertyProvider.getProperty(DOMIBUS_PAYLOAD_TEMP_JOB_RETENTION_DIRECTORIES);
        if (StringUtils.isEmpty(directories)) {
            LOG.debug("No configured payload temporary directories to clean");
            return new ArrayList<>();
        }
        LOG.debug("Configured payload temporary directories [{}]", directories);

        List<File> result = new ArrayList<>();
        final String[] directoryList = StringUtils.split(directories, ",");
        for (String directoryValue : directoryList) {
            final File directory = getDirectory(directoryValue);
            if (directory != null) {
                result.add(directory);
            }
        }
        return result;
    }

    protected File getDirectory(String directoryValue) {
        final File directory = getDirectoryIfExists(directoryValue);
        if (directory != null) {
            return directory;
        }

        LOG.debug("Getting directory [{}] from Domibus properties", directoryValue);
        final String property = domibusPropertyProvider.getProperty(directoryValue);
        if (StringUtils.isBlank(property)) {
            LOG.debug("No value defined for property [{}]", directoryValue);
            return null;
        }

        return getDirectoryIfExists(property);
    }

    protected File getDirectoryIfExists(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        File directory = new File(value);
        if (directory.exists()) {
            LOG.debug("Directory [{}] exists", directory);
            return directory;
        }
        return null;
    }

}
