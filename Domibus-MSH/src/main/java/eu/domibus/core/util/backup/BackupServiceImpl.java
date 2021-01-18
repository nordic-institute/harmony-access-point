package eu.domibus.core.util.backup;

import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.DateUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_SECURITY_TRUSTSTORE_BACKUP_LOCATION;


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

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected DomainContextProvider domainProvider;

    /**
     * {@inheritDoc}
     */
    @Override
    public void backupFile(File originalFile) throws IOException {
        final File backupFile = getBackupFile(originalFile);

        LOG.debug("Backing up file [{}] to file [{}]", originalFile, backupFile);
        try {
            FileUtils.copyFile(originalFile, backupFile);
        } catch (IOException e) {
            throw new IOException(String.format("Could not back up file [%s] to [%s]", originalFile, backupFile), e);
        }
    }

    protected File getBackupFile(File originalFile) {
        String trustStoreFileValue = getTrustStoreBackUpLocation();
        LOG.debug("TrustStoreLocation is: [{}]", trustStoreFileValue);
        File trustStoreFile = new File(trustStoreFileValue);
        if (!trustStoreFile.getParentFile().exists()) {
            LOG.debug("Creating directory [" + trustStoreFile.getParentFile() + "]");
            try {
                FileUtils.forceMkdir(trustStoreFile.getParentFile());
            } catch (IOException e) {
                throw new CryptoException("Could not create parent directory for truststore", e);
            }
        }
        String backupFileName = trustStoreFile.getName() + BACKUP_EXT + dateUtil.getCurrentTime(BACKUP_FILE_FORMATTER);
        return new File(originalFile.getParent(), backupFileName);
    }

    protected String getTrustStoreBackUpLocation() {
        return domibusPropertyProvider.getProperty(domainProvider.getCurrentDomain(), DOMIBUS_SECURITY_TRUSTSTORE_BACKUP_LOCATION);
    }

}
