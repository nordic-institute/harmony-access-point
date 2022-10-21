package eu.domibus.core.util.backup;

import eu.domibus.api.util.DateUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * Utility service used to back-up files before updating them.
 */
@Service
public class BackupServiceImpl implements BackupService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(BackupServiceImpl.class);

    protected static final String BACKUP_EXT = ".backup-";
    protected static final DateTimeFormatter BACKUP_FILE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH_mm_ss.SSS");

    @Autowired
    protected DateUtil dateUtil;

    /**
     * {@inheritDoc}
     */
    @Override
    public void backupFile(File originalFile) throws IOException {
        final File backupFile = getBackupFile(originalFile);
        copyBackUpFile(originalFile, backupFile);
    }

    @Override
    public void backupFileInLocation(File originalFile, String trustStoreBackupLocation) throws IOException {
        final File backupFile = createBackupFileInLocation(originalFile, trustStoreBackupLocation);
        copyBackUpFile(originalFile, backupFile);
    }

    @Override
    public void backupFileIfOlderThan(File originalFile, Integer period) throws IOException {
        if (period == 0) {
            LOG.debug("Min backup period is 0 so backing up file [{}]", originalFile.getName());
            backupFile(originalFile);
            return;
        }

        List<File> backups = getBackupFilesOf(originalFile);
        if (CollectionUtils.isEmpty(backups)) {
            LOG.debug("No backups found so backing up file [{}]", originalFile.getName());
            backupFile(originalFile);
            return;
        }

        long elapsed = new Date().toInstant().toEpochMilli() - backups.get(0).lastModified();
        if (elapsed < period * 60 * 60 * 1000) {
            LOG.debug("No minimum period of time elapsed since the last backup so NO backing up file [{}]", originalFile.getName());
            return;
        }

        backupFile(originalFile);
    }

    @Override
    public void deleteBackupsIfMoreThan(File originalFile, Integer maxFilesToKeep) throws IOException {
        if (maxFilesToKeep == 0) {
            LOG.debug("Maximum backup history is 0 so exiting");
            return;
        }

        List<File> backups = getBackupFilesOf(originalFile);
        if (backups.size() <= maxFilesToKeep) {
            LOG.debug("Maximum number of allowed backups [{}] has not been reached for file [{}], so exiting.", maxFilesToKeep, originalFile.getName());
            return;
        }

        List<String> exceptions = new ArrayList<>();
        backups.subList(maxFilesToKeep, backups.size())
                .forEach(file -> {
                    try {
                        LOG.debug("Deleting backup file [{}].", originalFile.getName());
                        FileUtils.delete(file);
                    } catch (IOException e) {
                        exceptions.add(String.format("Could not delete backup file [%s] due to [%s].", file.getName(), e.getMessage()));
                    }
                });
        if (!CollectionUtils.isEmpty(exceptions)) {
            throw new IOException(String.join("\n", exceptions));
        }
    }

    private List<File> getBackupFilesOf(File originalFile) {
        return Arrays.stream(originalFile.getParentFile().listFiles())
                .filter(file -> file.getName().startsWith(originalFile.getName() + BACKUP_EXT))
                .sorted(Comparator.comparing(File::lastModified).reversed())
                .collect(Collectors.toList());
    }

    protected void copyBackUpFile(File originalFile, File backupFile) throws IOException {
        LOG.debug("Backing up file [{}] to file [{}]", originalFile, backupFile);
        try {
            FileUtils.copyFile(originalFile, backupFile);
        } catch (IOException e) {
            throw new IOException(String.format("Could not back up file [%s] to [%s]", originalFile, backupFile), e);
        }
    }

    protected File getBackupFile(File originalFile) {
        String backupFileName = originalFile.getName() + BACKUP_EXT + dateUtil.getCurrentTime(BACKUP_FILE_FORMATTER);
        return new File(originalFile.getParent(), backupFileName);
    }

    protected File createBackupFileInLocation(File originalFile, String backupLocation) throws IOException {
        File backupFile = new File(backupLocation);
        if (!Files.exists(Paths.get(backupLocation).normalize())) {
            LOG.debug("Creating backup directory [{}]", backupLocation);
            try {
                FileUtils.forceMkdir(backupFile);
            } catch (IOException e) {
                throw new IOException("Could not create backup directory", e);
            }
        }
        return getBackupFile(originalFile, backupFile);
    }

    protected File getBackupFile(File originalFile, File backupFile) {
        String backupFileName = originalFile.getName() + BACKUP_EXT + dateUtil.getCurrentTime(BACKUP_FILE_FORMATTER);
        return new File(backupFile, backupFileName);
    }
}
