package eu.domibus.core.util;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Tested;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.AssertTrue;
import java.io.IOException;

public class FileUploadUtilTest {

    @Tested
    FileUploadUtil fileUploadUtil;

    @Test(expected = IllegalArgumentException.class)
    public void sanitiseFileUpload_empty(final @Mocked MultipartFile file) throws IOException {
        new Expectations() {{
            file.isEmpty();
            result = true;
        }};

        //tested
        fileUploadUtil.sanitiseFileUpload(file);
    }

    @Test(expected = IllegalArgumentException.class)
    public void sanitiseFileUpload_IOException(final @Mocked MultipartFile file) throws IOException {
        new Expectations() {{
            file.isEmpty();
            result = false;
            file.getBytes();
            result = new IOException();
        }};

        //tested
        fileUploadUtil.sanitiseFileUpload(file);
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
        byte[] result = fileUploadUtil.sanitiseFileUpload(file);

        Assert.assertTrue(result == bytes);
    }

}