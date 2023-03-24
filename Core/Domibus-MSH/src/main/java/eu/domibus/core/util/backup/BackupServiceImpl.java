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
import java.time.Duration;
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
    protected static final String BACKUP_FOLDER_NAME = "backups";

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
    public void backupFile(File originalFile, String subFolder) throws IOException {
        File parentFile = originalFile.getParentFile();
        if (parentFile == null) {
            LOG.warn("Could not get parent file of [{}]; no backing up.", originalFile);
            return;
        }
        String backupLocation = Paths.get(parentFile.getPath(), subFolder).toString();
        backupFileInLocation(originalFile, backupLocation);
    }

    @Override
    public void backupFileInLocation(File originalFile, String location) throws IOException {
        final File backupFile = createBackupFileInLocation(originalFile, location);
        copyBackUpFile(originalFile, backupFile);
    }

    @Override
    public void backupFileIfOlderThan(File originalFile, String subFolder, Integer periodInHours) throws IOException {
        String backupLocation = Paths.get(originalFile.getParentFile().getPath(), subFolder).toString();

        if (periodInHours == 0) {
            LOG.debug("Min backup period is 0 so backing up file [{}]", originalFile.getName());
            backupFileInLocation(originalFile, backupLocation);
            return;
        }

        File backupsFile = Paths.get(backupLocation, originalFile.getName() + BACKUP_EXT).toFile();
        List<File> backups = getBackupFilesOf(backupsFile);
        if (CollectionUtils.isEmpty(backups)) {
            LOG.debug("No backups found so backing up file [{}]", backupsFile.getName());
            backupFileInLocation(originalFile, backupLocation);
            return;
        }

        long elapsed = new Date().toInstant().toEpochMilli() - backups.get(0).lastModified();
        if (elapsed < Duration.ofHours(periodInHours).toMillis()) {
            LOG.debug("No minimum period of time elapsed since the last backup so NO backing up file [{}]", backupsFile.getName());
            return;
        }

        backupFileInLocation(originalFile, backupLocation);
    }

    @Override
    public void deleteBackupsIfMoreThan(File originalFile, Integer maxFilesToKeep) throws IOException {
        if (maxFilesToKeep == 0) {
            LOG.debug("Maximum backup history is 0 so exiting");
            return;
        }

        File backupsFile = Paths.get(originalFile.getParentFile().getPath(), BACKUP_FOLDER_NAME, originalFile.getName() + BACKUP_EXT).toFile();

        List<File> backups = getBackupFilesOf(backupsFile);

        if (backups.size() <= maxFilesToKeep) {
            LOG.debug("Maximum number of allowed backups [{}] has not been reached for file [{}], so exiting.", maxFilesToKeep, originalFile.getName());
            return;
        }

        List<String> exceptions = new ArrayList<>();
        backups.subList(maxFilesToKeep, backups.size())
                .forEach(file -> {
                    try {
                        LOG.debug("Deleting backup file [{}].", file.getName());
                        FileUtils.delete(file);
                    } catch (IOException e) {
                        exceptions.add(String.format("Could not delete backup file [%s] due to [%s].", file.getName(), e.getMessage()));
                    }
                });
        if (!CollectionUtils.isEmpty(exceptions)) {
            throw new IOException(String.join("\n", exceptions));
        }
    }

    private List<File> getBackupFilesOf(File backupFile) {
        File[] files = backupFile.getParentFile().listFiles();
        if (files == null) {
            LOG.info("File [{}] does not exist.", backupFile.getParentFile());
            return Collections.emptyList();
        }
        return Arrays.stream(files)
                .filter(file -> file.getName().startsWith(backupFile.getName()))
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
