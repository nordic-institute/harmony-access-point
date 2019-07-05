package eu.domibus.ext.rest;

import eu.domibus.ext.domain.PModeArchiveInfoDTO;
import eu.domibus.ext.services.PModeExtService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Catalin Enache
 * @since 4.1.1
 */
@RunWith(JMockit.class)
public class PModeFileResourceTest {

    @Tested
    PModeFileResource pModeFileResource;

    @Injectable
    PModeExtService pModeExtService;


    @Test
    public void test_downloadPMode(final @Mocked byte[] bytes, @Mocked ResponseEntity responseEntity) {
        final int pModeId = 1;

        new Expectations() {{
            pModeExtService.getPModeFile(pModeId);
            result = bytes;
        }};

        //tested method
        pModeFileResource.downloadPMode(pModeId);

        new Verifications() {{

            ResponseEntity.status(anyInt).
                    contentType(MediaType.parseMediaType(MediaType.APPLICATION_XML_VALUE))
                    .header("content-disposition", "attachment; filename=Pmodes.xml")
                    .body((ByteArrayResource)any);
        }};
    }

    @Test
    public void test_GetCurrentPMode(final @Mocked PModeArchiveInfoDTO pModeArchiveInfoDTO) {

        final int pModeId = 2;

        new Expectations() {{
            pModeExtService.getCurrentPmode();
            result = pModeArchiveInfoDTO;

            pModeArchiveInfoDTO.getId();
            result = pModeId;
        }};

        //tested method
        final PModeArchiveInfoDTO result = pModeFileResource.getCurrentPMode();
        Assert.assertNotNull(result);
        Assert.assertEquals(pModeId, result.getId());

    }

    @Test
    public void test_uploadPMode(final @Mocked MultipartFile pMode, final @Mocked byte[] bytes) throws Exception {
        final String description = "test upload";
        final List<String> uploadResult = new ArrayList<>();

        new Expectations() {{
            pMode.getBytes();
            result = bytes;

            pModeExtService.updatePModeFile(bytes, description);
            result = uploadResult;
        }};

        //tested
        pModeFileResource.uploadPMode(pMode, description);
    }
}