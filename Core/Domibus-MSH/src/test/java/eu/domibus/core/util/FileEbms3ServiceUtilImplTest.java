package eu.domibus.core.util;

import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Catalin Enache
 * @since 4.1.4
 */
@RunWith(JMockit.class)
public class FileEbms3ServiceUtilImplTest {

    @Tested
    FileServiceUtilImpl fileServiceUtil;

    @Test
    public void test_sanitizeFileName() {

        String baseFileName = "content.xml";
        String fileName = baseFileName;

        String sanitizedFileName = fileServiceUtil.sanitizeFileName(fileName);
        Assert.assertEquals(baseFileName, sanitizedFileName);

        fileName = "./../../../" + baseFileName;
        sanitizedFileName = fileServiceUtil.sanitizeFileName(fileName);
        Assert.assertEquals(baseFileName, sanitizedFileName);

        fileName = "./../../../../..\\..\\" + baseFileName;
        sanitizedFileName = fileServiceUtil.sanitizeFileName(fileName);
        Assert.assertEquals(baseFileName, sanitizedFileName);
    }
}