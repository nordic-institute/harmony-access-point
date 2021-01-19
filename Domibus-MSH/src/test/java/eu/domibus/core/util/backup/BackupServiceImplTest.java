package eu.domibus.core.util.backup;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.DateUtil;
import eu.domibus.core.util.DateUtilImpl;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_SECURITY_TRUSTSTORE_BACKUP_LOCATION;
import static eu.domibus.core.util.backup.BackupServiceImpl.BACKUP_EXT;
import static eu.domibus.core.util.backup.BackupServiceImpl.BACKUP_FILE_FORMATTER;
import static org.junit.Assert.*;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 */
@RunWith(JMockit.class)
public class BackupServiceImplTest {

    @Tested
    BackupServiceImpl backupService;

    @Injectable
    DateUtil dateUtil;

    @Tested
    DateUtilImpl dateUtilImpl;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    DomainContextProvider domainProvider;

    @Test
    public void testBackupFile() throws IOException {
        File originalFile = new File("testfile");
        new Expectations(FileUtils.class) {{
            FileUtils.copyFile((File) any, (File) any);
        }};

        backupService.backupFile(originalFile);

        new Verifications() {{
            File backupFile;
            backupService.getBackupFile(originalFile);
            FileUtils.copyFile(originalFile, backupFile = withCapture());
            assertFalse(backupFile.getName().equalsIgnoreCase(originalFile.getName()));
            assertTrue(backupFile.getName().contains(originalFile.getName()));
        }};
    }

    @Test
    public void testBackupFileName() {
        String timePart = "2019-07-15_23_01_01.111";
        final String originalFileName = "domibus.properties";
        final String parentDirectory = "home";

        new Expectations() {{
            dateUtil.getCurrentTime(BACKUP_FILE_FORMATTER);
            result = timePart;
        }};

        final File originalFile = new File(parentDirectory, originalFileName);
        final File backupFile = backupService.getBackupFile(originalFile);

        assertEquals(originalFileName + BACKUP_EXT + timePart, backupFile.getName());
        assertEquals(parentDirectory, backupFile.getParent());
    }

    @Test
    public void testTimestampFormatter() {
        final LocalDateTime now = LocalDateTime.of(2019, 9, 2, 15, 1, 55, 123 * 1000000);
        final String expectedValue = "2019-09-02_15_01_55.123";

        new Expectations(LocalDateTime.class) {{
            LocalDateTime.now();
            result = now;
        }};

        String value = dateUtilImpl.getCurrentTime(BACKUP_FILE_FORMATTER);
        assertEquals(expectedValue, value);
    }

    @Test
    public void backupTrustStoreFile() throws IOException {
        File originalFile = new File("testfile");
        File backupFile = new File("backup_testfile");

        new Expectations(FileUtils.class, backupService) {{
            backupService.getTrustStoreBackupFile();
            result = backupFile;
            FileUtils.copyFile((File) any, (File) any);
        }};

        backupService.backupTrustStoreFile(originalFile);

        new Verifications() {{
            File backupFile;
            backupService.backupTrustStoreFile(originalFile);
            FileUtils.copyFile(originalFile, backupFile = withCapture());
            assertFalse(backupFile.getName().equalsIgnoreCase(originalFile.getName()));
            assertTrue(backupFile.getName().contains(originalFile.getName()));
        }};
    }

    @Test
    public void getTrustStoreBackupFile() {
        final String trustStoreBackupFileValue = "testBackupFile";
        File trustStoreFile = new File(trustStoreBackupFileValue);

        new MockUp<File>() {
            @Mock
            private File getParentFile() {
                return trustStoreFile;
            }
        };

        new Expectations(backupService, FileUtils.class) {{
            backupService.getTrustStoreBackUpLocation();
            result = trustStoreBackupFileValue;
        }};

        backupService.getTrustStoreBackupFile();

        new Verifications() {{
            backupService.getBackupFile(trustStoreFile);
        }};
    }

    @Test
    public void getTrustStoreBackUpLocation() {
        final String trustStoreBackupLocation = "backupTrustStore";

        new Expectations() {{
            domibusPropertyProvider.getProperty(domainProvider.getCurrentDomain(), DOMIBUS_SECURITY_TRUSTSTORE_BACKUP_LOCATION);
            result = trustStoreBackupLocation;
        }};

        String location = backupService.getTrustStoreBackUpLocation();
        assertEquals(location, trustStoreBackupLocation);
    }
}