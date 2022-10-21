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
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
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
    public void backupFileIfOlderThan(File originalFile, Integer period, Integer maxFilesToKeep) throws IOException {
        List<File> backups = Arrays.stream(originalFile.getParentFile().listFiles())
                .filter(file -> file.getName().startsWith(originalFile.getName() + BACKUP_EXT))
                .sorted(Comparator.comparing(File::lastModified).reversed())
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(backups)) {
            backupFile(originalFile);
            return;
        }

        long elapsed = new Date().toInstant().toEpochMilli() - backups.get(0).lastModified();
        if (elapsed < period * 60 * 60 * 1000) {
            return;
        }

        backupFile(originalFile);
        if (backups.size() < maxFilesToKeep) {
            return;
        }

        backups.subList(maxFilesToKeep - 1, backups.size())
                .forEach(file -> {
                    try {
                        FileUtils.delete(file);
                    } catch (IOException e) {
                        LOG.info("Could not delete backup file [{}].", file, e);
                    }
                });
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
