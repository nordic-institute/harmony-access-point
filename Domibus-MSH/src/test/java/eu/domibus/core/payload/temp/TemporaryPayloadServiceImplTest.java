package eu.domibus.core.payload.temp;

import eu.domibus.api.property.DomibusPropertyProvider;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

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
                                       @Injectable File file1) {
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
                                   @Mocked RegexIOFileFilter regexIOFileFilter) {
        String excludeRegex = "regexExpression";

        new Expectations() {{
            domibusPropertyProvider.getProperty(TemporaryPayloadServiceImpl.DOMIBUS_PAYLOAD_TEMP_JOB_RETENTION_EXCLUDE_REGEX);
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
                                 @Mocked LoggerFactory loggerFactory) {
        int expirationThresholdInMinutes = 5;
        long currentTimeMillis = 6 * 60 * 1000;

        new Expectations() {{
            domibusPropertyProvider.getIntegerProperty(TemporaryPayloadServiceImpl.DOMIBUS_PAYLOAD_TEMP_JOB_RETENTION_EXPIRATION);
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
    public void getTemporaryLocations() {
    }

    @Test
    public void getDirectory() {
    }

    @Test
    public void getDirectoryIfExists() {
    }
}