package eu.domibus.core.util;

import eu.domibus.api.exceptions.RequestValidationException;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Tested;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author Ion Perpegel
 * @since 4.2
 */
public class MultiPartFileUtilImplTest {

    @Tested
    MultiPartFileUtilImpl multiPartFileUtil;

    @Test(expected = RequestValidationException.class)
    public void sanitiseFileUpload_empty(final @Mocked MultipartFile file) throws IOException {
        new Expectations() {{
            file.isEmpty();
            result = true;
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