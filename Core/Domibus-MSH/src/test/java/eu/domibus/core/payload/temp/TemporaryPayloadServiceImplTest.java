package eu.domibus.core.payload.temp;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;

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
@RunWith(JMockit.class)
public class TemporaryPayloadServiceImplTest {

    @Tested
    TemporaryPayloadServiceImpl temporaryPayloadService;

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Test
    public void cleanTemporaryPayloads(@Injectable File directory,
                                       @Injectable File file1,
                                       @Injectable Domain domain) {
        final Collection<File> filesToClean = new ArrayList<>();
        filesToClean.add(file1);

        new Expectations(temporaryPayloadService) {{
            temporaryPayloadService.getFilesToClean(directory);
            result = filesToClean;

            temporaryPayloadService.deleteFileSafely((File) any);

        }};

        temporaryPayloadService.cleanTemporaryPayloads(directory);

        new Verifications() {{
            temporaryPayloadService.deleteFileSafely(file1);
        }};
    }

    @Test
    public void deleteFileSafely(@Injectable File file) {
        temporaryPayloadService.deleteFileSafely(file);

        new Verifications() {{
            file.delete();
        }};
    }

    @Test
    public void deleteFileSafelyWhenExceptionIsThrown(@Injectable File file) {
        new Expectations() {{
            file.delete();
            result = new RuntimeException();
        }};

        temporaryPayloadService.deleteFileSafely(file);

        new Verifications() {{
            file.delete();
        }};
    }


    @Test
    public void getRegexFileFilter(@Mocked Pattern regexPattern,
                                   @Mocked FileFilterUtils fileFilterUtils,
                                   @Mocked RegexIOFileFilter regexIOFileFilter,
                                   @Injectable Domain domain) {
        String excludeRegex = "regexExpression";

        new Expectations() {{
            domibusPropertyProvider.getProperty(DOMIBUS_PAYLOAD_TEMP_JOB_RETENTION_EXCLUDE_REGEX);
            result = excludeRegex;

            Pattern.compile(excludeRegex);
            result = regexPattern;

            new RegexIOFileFilter(regexPattern);
            result = regexIOFileFilter;
        }};

        temporaryPayloadService.getRegexFileFilter();

        new Verifications() {{
            FileFilterUtils.notFileFilter(regexIOFileFilter);
        }};
    }

    @Test
    public void getAgeFileFilter(@Mocked System system,
                                 @Mocked FileFilterUtils fileFilterUtils,
                                 @Mocked LoggerFactory loggerFactory,
                                 @Injectable Domain domain) {
        int expirationThresholdInMinutes = 5;
        long currentTimeMillis = 6 * 60 * 1000;

        new Expectations() {{
            domibusPropertyProvider.getIntegerProperty(DOMIBUS_PAYLOAD_TEMP_JOB_RETENTION_EXPIRATION);
            result = expirationThresholdInMinutes;

            System.currentTimeMillis();
            result = currentTimeMillis;
        }};

        temporaryPayloadService.getAgeFileFilter();

        new Verifications() {{
            FileFilterUtils.ageFileFilter(60000);
        }};
    }

    @Test
    public void getTemporaryLocations(@Injectable File dir1,
                                      @Injectable File dir2,
                                      @Injectable Domain domain) {
        String directories = "dir1,dir2";

        new Expectations(temporaryPayloadService) {{
            domibusPropertyProvider.getProperty(DOMIBUS_PAYLOAD_TEMP_JOB_RETENTION_DIRECTORIES);
            result = directories;

            temporaryPayloadService.getDirectory("dir1");
            result = dir1;

            temporaryPayloadService.getDirectory("dir2");
            result = dir2;
        }};

        final List<File> temporaryLocations = temporaryPayloadService.getTemporaryLocations();
        Assert.assertEquals(2, temporaryLocations.size());
        Assert.assertTrue(temporaryLocations.iterator().next() == dir1);
        Assert.assertTrue(temporaryLocations.iterator().next() == dir1);
    }

    @Test
    public void getDirectory(@Injectable File directoryFile) {
        String directory = "dir1";

        new Expectations(temporaryPayloadService) {{
            temporaryPayloadService.getDirectoryIfExists(directory);
            result = directoryFile;
        }};

        final File payloadServiceDirectory = temporaryPayloadService.getDirectory(directory);
        Assert.assertSame(payloadServiceDirectory, directoryFile);

        new Verifications() {{
            domibusPropertyProvider.getProperty(anyString);
            times = 0;
        }};
    }

    @Test
    public void getDirectoryFromDomibusProperties(@Injectable File directoryFile) {
        String directory = "dir1";

        new Expectations(temporaryPayloadService) {{
            temporaryPayloadService.getDirectoryIfExists(directory);
            result = null;

            domibusPropertyProvider.getProperty(directory);
            result = "myprop";

            temporaryPayloadService.getDirectoryIfExists("myprop");
            result = directoryFile;
        }};

        final File payloadServiceDirectory = temporaryPayloadService.getDirectory(directory);
        Assert.assertSame(payloadServiceDirectory, directoryFile);
    }

    @Test
    public void getDirectoryIfExists(@Mocked File file,
                                     @Mocked LoggerFactory loggerFactory) {
        String directory = "dir1";

        new Expectations() {{
            new File(directory);
            result = file;

            file.exists();
            result = true;

            file.getPath();
            result = "/mypath";
        }};

        final File result = temporaryPayloadService.getDirectoryIfExists(directory);
        Assert.assertEquals(result.getPath(), file.getPath());
    }
}