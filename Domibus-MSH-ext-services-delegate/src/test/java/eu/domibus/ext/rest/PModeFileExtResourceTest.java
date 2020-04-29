package eu.domibus.ext.rest;

import eu.domibus.ext.delegate.converter.DomainExtConverter;
import eu.domibus.ext.domain.PModeArchiveInfoDTO;
import eu.domibus.ext.exceptions.PModeExtException;
import eu.domibus.ext.rest.error.ExtExceptionHelper;
import eu.domibus.ext.services.PModeExtService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Catalin Enache
 * @since 4.1.1
 */
@RunWith(JMockit.class)
public class PModeFileExtResourceTest {

    @Tested
    PModeFileExtResource pModeFileExtResource;

    @Injectable
    PModeExtService pModeExtService;

    @Injectable
    DomainExtConverter domainConverter;

    @Injectable
    ExtExceptionHelper extExceptionHelper;

    @Test
    public void test_downloadPMode(@Mocked ResponseEntity responseEntity) {
        final int pModeId = 1;

        final byte[] bytes = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".getBytes(StandardCharsets.UTF_8);

        new Expectations() {{
            pModeExtService.getPModeFile(pModeId);
            result = bytes;
        }};

        //tested method
        final ResponseEntity<ByteArrayResource> response = pModeFileExtResource.downloadPMode(pModeId);
        Assert.assertNotNull(response);

        new FullVerifications() {{
            ResponseEntity.status(HttpStatus.OK).
                    contentType(MediaType.parseMediaType(MediaType.APPLICATION_XML_VALUE))
                    .header("content-disposition", "attachment; filename=Pmodes.xml")
                    .body((ByteArrayResource) any);
        }};
    }

    @Test
    public void test_downloadPMode_NoContent(@Mocked ResponseEntity responseEntity) {
        final int pModeId = 1;

        final byte[] bytes = "".getBytes(StandardCharsets.UTF_8);

        new Expectations() {{
            pModeExtService.getPModeFile(pModeId);
            result = bytes;
        }};

        //tested method
        final ResponseEntity<ByteArrayResource> response = pModeFileExtResource.downloadPMode(pModeId);
        Assert.assertNotNull(response);

        new FullVerifications() {{
            ResponseEntity.status(HttpStatus.NO_CONTENT).
                    contentType(MediaType.parseMediaType(MediaType.APPLICATION_XML_VALUE))
                    .header("content-disposition", "attachment; filename=Pmodes.xml")
                    .body((ByteArrayResource) any);
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
        final PModeArchiveInfoDTO result = pModeFileExtResource.getCurrentPMode();
        Assert.assertNotNull(result);
        Assert.assertEquals(pModeId, result.getId());

    }

    @Test
    public void test_uploadPMode(final @Mocked MultipartFile pModeFile, final @Mocked byte[] bytes) throws Exception {
        final String description = "test upload";
        final List<String> uploadResult = new ArrayList<>();

        new Expectations() {{
            pModeExtService.updatePModeFile(pModeFile, description);
            result = uploadResult;
        }};

        //tested
        pModeFileExtResource.uploadPMode(pModeFile, description);
    }

    @Test
    public void test_handlePartyExtServiceException(final @Mocked PModeExtException pModeExtException) {

        //tested method
        pModeFileExtResource.handlePModeExtException(pModeExtException);

        new FullVerifications() {{
            extExceptionHelper.handleExtException(pModeExtException);
        }};
    }
}