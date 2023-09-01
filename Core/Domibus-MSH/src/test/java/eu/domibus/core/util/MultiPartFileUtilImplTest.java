package eu.domibus.core.util;

import eu.domibus.api.exceptions.RequestValidationException;
import eu.domibus.core.property.DomibusPropertyProviderImpl;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_FILE_UPLOAD_MAX_SIZE;

/**
 * @author Ion Perpegel
 * @since 4.2
 */
public class MultiPartFileUtilImplTest {
    @Injectable
    DomibusPropertyProviderImpl domibusPropertyProvider;

    @Tested
    MultiPartFileUtilImpl multiPartFileUtil;

    @Test(expected = RequestValidationException.class)
    public void sanitiseFileUpload_empty(final @Mocked MultipartFile file) {
        new Expectations() {{
            file.isEmpty();
            result = true;
        }};

        //tested
        multiPartFileUtil.validateAndGetFileContent(file);
    }

    @Test(expected = RequestValidationException.class)
    public void sanitiseFileUpload_maxSize(final @Mocked MultipartFile file) {
        new Expectations() {{
            file.isEmpty();
            result = false;
            file.getSize();
            result = 100;
            domibusPropertyProvider.getIntegerProperty(DOMIBUS_FILE_UPLOAD_MAX_SIZE);
            result = 25;
        }};

        //tested
        multiPartFileUtil.validateAndGetFileContent(file);
    }

    @Test(expected = RequestValidationException.class)
    public void sanitiseFileUpload_IOException(final @Mocked MultipartFile file) throws IOException {
        new Expectations() {{
            file.isEmpty();
            result = false;
            file.getBytes();
            result = new IOException();
        }};

        //tested
        multiPartFileUtil.validateAndGetFileContent(file);
    }

    @Test()
    public void sanitiseFileUpload(final @Mocked MultipartFile file) throws IOException {
        byte[] bytes = new byte[]{1, 2, 3};

        new Expectations() {{
            file.isEmpty();
            result = false;
            file.getBytes();
            result = bytes;
        }};

        //tested
        byte[] result = multiPartFileUtil.validateAndGetFileContent(file);

        Assert.assertTrue(result == bytes);
    }

}