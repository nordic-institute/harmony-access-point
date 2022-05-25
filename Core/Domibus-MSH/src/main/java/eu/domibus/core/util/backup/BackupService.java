package eu.domibus.core.util.backup;

import java.io.File;
import java.io.IOException;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * Utility service used to back-up files before updating them.
 */
public interface BackupService {

    /**
     * Creates a copy of the originalFile, in the same folder.
     * @param originalFile the file to be backed-up.
     * @throws IOException when the backup file cannot be written.
     * @implNote The backup file is named using the following convention: original_filename.backup-yyyy-MM-dd_HH_mm_ss.SSS
     */
    void backupFile(File originalFile) throws IOException;

    /**
     * Creates a copy of the originalFile, in the backup folder.
     *
     * @param originalFile   the file to be backed-up.
     * @throws IOException when the backup file cannot be written.
     * @implNote The backup file is named using the following convention: original_filename.backup-yyyy-MM-dd_HH_mm_ss.SSS
     */
    void backupFileInLocation(File originalFile, String trustStoreBackupLocation) throws IOException;

}
