package eu.domibus.web.rest;

import eu.domibus.api.pmode.*;
import eu.domibus.api.util.MultiPartFileUtil;
import eu.domibus.core.audit.AuditService;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.csv.CsvServiceImpl;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.pmode.validation.PModeValidationHelper;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.web.rest.ro.PModeResponseRO;
import eu.domibus.web.rest.ro.ValidationResponseRO;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
@RunWith(JMockit.class)
public class PModeResourceTest {

    private static final String PMODE_FILE_HAS_BEEN_SUCCESSFULLY_UPLOADED = "PMode file has been successfully uploaded.";
    @Tested
    private PModeResource pModeResource;

    @Injectable
    private PModeProvider pModeProvider;

    @Injectable
    private DomainCoreConverter domainConverter;

    @Injectable
    private CsvServiceImpl csvServiceImpl;

    @Injectable
    private AuditService auditService;

    @Injectable
    PModeService pModeService;

    @Injectable
    PModeValidationHelper pModeValidationHelper;

    @Injectable
    MultiPartFileUtil multiPartFileUtil;

    @Test
    public void testDownloadPmodes() {
        // Given
        final byte[] byteA = new byte[]{1, 0, 1};
        new Expectations() {{
            pModeProvider.getPModeFile(0);
            result = byteA;
        }};

        // When
        ResponseEntity<? extends Resource> responseEntity = pModeResource.downloadPmode(0, true, false);

        // Then
        validateResponseEntity(responseEntity, HttpStatus.OK);
    }

    @Test
    public void testDownloadPModesNoContent() {
        // Given
        final byte[] byteA = new byte[]{};
        new Expectations() {{
            pModeProvider.getPModeFile(0);
            result = byteA;
        }};

        // When
        ResponseEntity<? extends Resource> responseEntity = pModeResource.downloadPmode(0, true, false);

        // Then
        validateResponseEntity(responseEntity, HttpStatus.NO_CONTENT);
    }

    @Test
    public void testDownloadPModesAudit() {
        // Given
        final byte[] byteA = new byte[]{1, 0, 1};
        new Expectations() {{
            pModeProvider.getPModeFile(0);
            result = byteA;
        }};
        ResponseEntity<? extends Resource> responseEntity = pModeResource.downloadPmode(0, true, false);
        validateResponseEntity(responseEntity, HttpStatus.OK);

        new Verifications() {{
            // add audit must be called
            auditService.addPModeDownloadedAudit(0);
            times = 0;

        }};

        responseEntity = pModeResource.downloadPmode(0, false, false);
        validateResponseEntity(responseEntity, HttpStatus.OK);

        new Verifications() {{
            // add audit must be called
            auditService.addPModeDownloadedAudit(0);
            times = 1;

        }};
    }

    @Test
    public void testDownloadPModesArchive() {
        // Given
        final byte[] byteA = new byte[]{1, 0, 1};
        new Expectations() {{
            pModeProvider.getPModeFile(0);
            result = byteA;
        }};

        ResponseEntity<? extends Resource> responseEntity = pModeResource.downloadPmode(0, false, true);
        validateResponseEntity(responseEntity, HttpStatus.OK);

        new Verifications() {{
            // add audit must be called
            auditService.addPModeDownloadedAudit(0);
            times = 1;
            auditService.addPModeArchiveDownloadedAudit(0);
            times = 1;
        }};
    }

    private void validateResponseEntity(ResponseEntity<? extends Resource> responseEntity, HttpStatus httpStatus) {
        Assert.assertNotNull(responseEntity);
        Assert.assertEquals(httpStatus, responseEntity.getStatusCode());
        Assert.assertEquals("attachment; filename=Pmodes.xml", responseEntity.getHeaders().get("content-disposition").get(0));
        Assert.assertEquals("Byte array resource [resource loaded from byte array]", responseEntity.getBody().getDescription());
    }

    @Test
    public void testUploadPmodesSuccess() throws IOException {
        // Given
        MultipartFile file = new MockMultipartFile("filename", new byte[]{1, 0, 1});

        new Expectations() {{
            pModeValidationHelper.getValidationResponse(new ArrayList<>(), PMODE_FILE_HAS_BEEN_SUCCESSFULLY_UPLOADED);
            result = new ValidationResponseRO(PMODE_FILE_HAS_BEEN_SUCCESSFULLY_UPLOADED);
        }};

        // When
        ValidationResponseRO response = pModeResource.uploadPMode(file, "description");

        // Then
        Assert.assertNotNull(response);
        Assert.assertEquals(0, response.getIssues().size());
        Assert.assertEquals(PMODE_FILE_HAS_BEEN_SUCCESSFULLY_UPLOADED, response.getMessage());
    }

    @Test
    public void testUploadPmodesIssues() throws XmlProcessingException {
        // Given
        MultipartFile file = new MockMultipartFile("filename", new byte[]{1, 0, 1});
        List<ValidationIssue> issues = Arrays.asList(new ValidationIssue("issue1"));

        new Expectations() {{
            pModeService.updatePModeFile((byte[]) any, anyString);
            result = issues;
            pModeValidationHelper.getValidationResponse(issues, PMODE_FILE_HAS_BEEN_SUCCESSFULLY_UPLOADED);
            result = new ValidationResponseRO(PMODE_FILE_HAS_BEEN_SUCCESSFULLY_UPLOADED, issues);
        }};

        // When
        ValidationResponseRO response = pModeResource.uploadPMode(file, "description");

        // Then
        Assert.assertNotNull(response);
        Assert.assertEquals(1, response.getIssues().size());
        Assert.assertEquals("issue1", response.getIssues().get(0).getMessage());
        Assert.assertEquals(PMODE_FILE_HAS_BEEN_SUCCESSFULLY_UPLOADED, response.getMessage());
    }

    @Test()
    public void testUploadPModesXmlProcessingException() throws XmlProcessingException, IOException {
        // Given
        MultipartFile file = new MockMultipartFile("filename", new byte[]{1, 0, 1});
        List<ValidationIssue> issues = Arrays.asList(new ValidationIssue("issue1"));

        new Expectations() {{
            pModeService.updatePModeFile((byte[]) any, anyString);
            result = new PModeValidationException(issues);
        }};

        // When
        try {
            pModeResource.uploadPMode(file, "description");
        } catch (PModeException ex) {
//         Then
            Assert.assertTrue(ex instanceof PModeValidationException);
            PModeValidationException pex = (PModeValidationException) ex;
            Assert.assertTrue(pex.getIssues().size() == 1);
            Assert.assertEquals("issue1", pex.getIssues().get(0).getMessage());
        }
    }

    @Test
    public void testDeletePmodesEmptyList() {
        // Given
        final ArrayList<String> emptyList = new ArrayList<>();

        // When
        final ResponseEntity<String> response = pModeResource.deletePModes(emptyList);

        // Then
        Assert.assertNotNull(response);
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assert.assertEquals("Failed to delete PModes since the list of ids was empty.", response.getBody());
    }

    @Test
    public void testDeletePmodesSuccess() {
        // Given
        List<String> stringList = new ArrayList<>();
        stringList.add("1");
        stringList.add("2");

        // When
        final ResponseEntity<String> response = pModeResource.deletePModes(stringList);

        // Then
        Assert.assertNotNull(response);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals("PModes were deleted\n", response.getBody());
    }

    @Test
    public void testDeletePmodesException() {
        // Given
        final Exception exception = new Exception("Mocked exception");
        List<String> stringList = new ArrayList<>();
        stringList.add("1");

        new Expectations(pModeResource) {{
            pModeProvider.removePMode(anyInt);
            result = exception;
        }};

        // When
        final ResponseEntity<String> response = pModeResource.deletePModes(stringList);

        // Then
        Assert.assertNotNull(response);
        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Assert.assertEquals("Impossible to delete PModes due to \nMocked exception", response.getBody());
    }

//    @Test
//    public void testRestorePmodeSuccess() {
//        // Given
//        // When
//        final ResponseEntity<ValidationResponseRO> response = pModeResource.restorePmode(1);
//
//        // Then
//        Assert.assertNotNull(response);
//        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
//        Assert.assertEquals("PMode file has been successfully uploaded", response.getBody().getMessage());
//    }

//    @Test
//    public void testRestorePmodeException() throws XmlProcessingException {
//        // Given
//        final Exception exception = new Exception("Mocked exception");
//        new Expectations(pModeResource) {{
//            pModeService.updatePModeFile((byte[]) any, anyString);
//            result = exception;
//        }};
//
//        // When
//        final ResponseEntity<ValidationResponseRO> response = pModeResource.restorePmode(1);
//
//        // Then
//        Assert.assertNotNull(response);
//        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
//        Assert.assertEquals("Failed to upload the PMode file due to: Exception: Mocked exception", response.getBody().getMessage());
//    }

//    @Test
//    public void testRestorePmodeNestedException() throws XmlProcessingException {
//        // Given
//        final Exception exception = new Exception(new Exception("Nested mocked exception"));
//
//        new Expectations(pModeResource) {{
//            pModeService.updatePModeFile((byte[]) any, anyString);
//            result = exception;
//        }};
//
//        // When
//        final ResponseEntity<ValidationResponseRO> response = pModeResource.restorePmode(1);
//
//        // Then
//        Assert.assertNotNull(response);
//        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
//        Assert.assertEquals("Failed to upload the PMode file due to: Exception: Nested mocked exception", response.getBody().getMessage());
//    }

//    @Test
//    public void testRestorePmodeIssues() throws XmlProcessingException {
//        // Given
//        List<String> issues = new ArrayList<>();
//        issues.add("issue1");
//        new Expectations(pModeResource) {{
//            pModeService.updatePModeFile((byte[]) any, anyString);
//            result = issues;
//        }};
//
//        // When
//        final ResponseEntity<ValidationResponseRO> response = pModeResource.restorePmode(1);
//
//        // Then
//        Assert.assertNotNull(response);
//        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
//        Assert.assertEquals("PMode file has been successfully uploaded but some issues were detected:", response.getBody().getMessage());
//        Assert.assertEquals(1, response.getBody().getIssues().size());
//    }

    @Test
    public void testPmodeList() {
        // Given
        final Date date = new Date();
        final String username = "username";
        final String description = "description";

        PModeResponseRO pModeResponseRO = new PModeResponseRO();
        pModeResponseRO.setId(1);
        pModeResponseRO.setUsername(username);
        pModeResponseRO.setDescription(description);
        pModeResponseRO.setConfigurationDate(date);
        pModeResponseRO.setCurrent(true);

        ArrayList<PModeResponseRO> pModeResponseROArrayList = new ArrayList<>();
        pModeResponseROArrayList.add(pModeResponseRO);

        new Expectations(pModeResource) {{
            domainConverter.convert((List<PModeArchiveInfo>) any, PModeResponseRO.class);
            result = pModeResponseROArrayList;
        }};

        // When
        final List<PModeResponseRO> pModeResponseROSGot = pModeResource.pmodeList();

        // Then
        Assert.assertEquals(1, pModeResponseROSGot.size());
        Assert.assertEquals(1, pModeResponseRO.getId());
        Assert.assertEquals(date, pModeResponseRO.getConfigurationDate());
        Assert.assertEquals(username, pModeResponseRO.getUsername());
        Assert.assertEquals(description, pModeResponseRO.getDescription());
        Assert.assertTrue(pModeResponseRO.isCurrent());

    }

    @Test
    public void testGetCsv() throws EbMS3Exception {
        // Given
        Date date = new Date();
        List<PModeArchiveInfo> pModeArchiveInfoList = new ArrayList<>();
        PModeArchiveInfo pModeArchiveInfo1 = new PModeArchiveInfo(1, date, "user1", "description1");
        PModeArchiveInfo pModeArchiveInfo2 = new PModeArchiveInfo(2, date, "user2", "description2");
        pModeArchiveInfoList.add(pModeArchiveInfo1);
        pModeArchiveInfoList.add(pModeArchiveInfo2);

        List<PModeResponseRO> pModeResponseROList = new ArrayList<>();
        PModeResponseRO pModeResponseRO1 = new PModeResponseRO(1, date, "user1", "description1");
        PModeResponseRO pModeResponseRO2 = new PModeResponseRO(2, date, "user2", "description2");
        pModeResponseROList.add(pModeResponseRO1);
        pModeResponseROList.add(pModeResponseRO2);
        new Expectations() {{
            pModeProvider.getRawConfigurationList();
            result = pModeArchiveInfoList;
            domainConverter.convert(pModeArchiveInfoList, PModeResponseRO.class);
            result = pModeResponseROList;
            csvServiceImpl.exportToCSV(pModeResponseROList, PModeResponseRO.class, (Map<String, String>) any, (List<String>) any);
            result = "Configuration Date, Username, Description" + System.lineSeparator() +
                    date + ", user1, description1" + System.lineSeparator() +
                    date + ", user2, description2" + System.lineSeparator();
        }};

        // When
        final ResponseEntity<String> csv = pModeResource.getCsv();

        // Then
        Assert.assertEquals(HttpStatus.OK, csv.getStatusCode());
        Assert.assertEquals("Configuration Date, Username, Description" + System.lineSeparator() +
                        date + ", user1, description1" + System.lineSeparator() +
                        date + ", user2, description2" + System.lineSeparator(),
                csv.getBody());
    }
}
