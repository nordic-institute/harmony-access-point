package eu.domibus.web.rest;

import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.exceptions.RequestValidationException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.KeyStoreContentInfo;
import eu.domibus.api.pki.KeystorePersistenceService;
import eu.domibus.api.pki.MultiDomainCryptoService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.api.util.MultiPartFileUtil;
import eu.domibus.core.audit.AuditService;
import eu.domibus.core.certificate.CertificateHelper;
import eu.domibus.core.converter.PartyCoreMapper;
import eu.domibus.core.csv.CsvServiceImpl;
import eu.domibus.web.rest.error.ErrorHandlerService;
import eu.domibus.web.rest.ro.TrustStoreRO;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static eu.domibus.web.rest.TruststoreResource.ERROR_MESSAGE_EMPTY_TRUSTSTORE_PASSWORD;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@RunWith(JMockit.class)
public class TruststoreResourceBaseTest {

    @Tested
    TruststoreResourceBase truststoreResourceBase;

    @Injectable
    PartyCoreMapper partyCoreConverter;

    @Injectable
    CsvServiceImpl csvServiceImpl;

    @Injectable
    ErrorHandlerService errorHandlerService;

    @Injectable
    MultiPartFileUtil multiPartFileUtil;

    @Injectable
    private AuditService auditService;

    @Injectable
    DomainContextProvider domainContextProvider;

    @Injectable
    DomibusConfigurationService domibusConfigurationService;

    @Injectable
    CertificateHelper certificateHelper;

    @Injectable
    KeystorePersistenceService keystorePersistenceService;

    @Test
    public void replaceTruststoreOK(@Injectable KeyStoreContentInfo storeInfo) {
        byte[] content = {1, 0, 1};
        String filename = "filename";
        String pass = "pass";
        MultipartFile multiPartFile = new MockMultipartFile("name", filename, "octetstream", content);

        new Expectations(truststoreResourceBase) {{
            multiPartFileUtil.validateAndGetFileContent(multiPartFile);
            result = content;
            truststoreResourceBase.doUploadStore(storeInfo);
        }};

        truststoreResourceBase.uploadStore(multiPartFile, pass);

        new Verifications() {{
            truststoreResourceBase.doUploadStore(storeInfo);
        }};
    }

    @Test
    public void testUploadTruststoreEmpty() {
        MultipartFile emptyFile = new MockMultipartFile("truststore", new byte[]{});

        new Expectations() {{
            multiPartFileUtil.validateAndGetFileContent(emptyFile);
            result = new RequestValidationException("Failed to upload the truststore file since it was empty.");
        }};

        try {
            truststoreResourceBase.uploadStore(emptyFile, "pass");
            Assert.fail();
        } catch (RequestValidationException ex) {
            Assert.assertTrue(ex.getMessage().contains("Failed to upload the truststore file since it was empty."));
        }
    }

    @Test(expected = CryptoException.class)
    public void testUploadTruststoreException(@Injectable KeyStoreContentInfo storeInfo) {
        MultipartFile multiPartFile = new MockMultipartFile("filename", new byte[]{1, 0, 1});

        new Expectations() {{
            multiPartFileUtil.validateAndGetFileContent(multiPartFile);
            result = new byte[]{1, 0, 1};

            truststoreResourceBase.doUploadStore(storeInfo);
            result = new CryptoException("Password is incorrect");
        }};

        truststoreResourceBase.uploadStore(multiPartFile, "pass");
    }

    @Test
    public void testTrustStoreEntries() {
        Date date = new Date();
        List<TrustStoreEntry> trustStoreEntryList = new ArrayList<>();
        TrustStoreEntry trustStoreEntry = new TrustStoreEntry("Name", "Subject", "Issuer", date, date);
        trustStoreEntryList.add(trustStoreEntry);

        new Expectations(truststoreResourceBase) {{
            truststoreResourceBase.doGetStoreEntries();
            result = trustStoreEntryList;
            partyCoreConverter.trustStoreEntryListToTrustStoreROList(trustStoreEntryList);
            result = getTestTrustStoreROList(date);
        }};

        final List<TrustStoreRO> trustStoreROList = truststoreResourceBase.getTrustStoreEntries();

        Assert.assertEquals(getTestTrustStoreROList(date), trustStoreROList);
    }

    @Test
    public void testGetCsv() {
        Date date = new Date();
        List<TrustStoreRO> trustStoreROList = getTestTrustStoreROList(date);
        new Expectations(truststoreResourceBase) {{
            truststoreResourceBase.getTrustStoreEntries();
            result = trustStoreROList;

            csvServiceImpl.exportToCSV(trustStoreROList, null, (Map<String, String>) any, (List<String>) any);
            result = "Name, Subject, Issuer, Valid From, Valid Until" + System.lineSeparator() +
                    "Name, Subject, Issuer, " + date + ", " + date + System.lineSeparator();
        }};

        final ResponseEntity<String> csv = truststoreResourceBase.getEntriesAsCSV("moduleName");

        Assert.assertEquals(HttpStatus.OK, csv.getStatusCode());
        Assert.assertEquals("Name, Subject, Issuer, Valid From, Valid Until" + System.lineSeparator() +
                        "Name, Subject, Issuer, " + date + ", " + date + System.lineSeparator(),
                csv.getBody());
    }

    @Test(expected = RequestValidationException.class)
    public void testGetCsv_validationException() {
        Date date = new Date();
        List<TrustStoreRO> trustStoreROList = getTestTrustStoreROList2(date);
        new Expectations(truststoreResourceBase) {{
            truststoreResourceBase.getTrustStoreEntries();
            result = trustStoreROList;
            csvServiceImpl.validateMaxRows(trustStoreROList.size());
            result = new RequestValidationException("");
        }};

        final ResponseEntity<String> csv = truststoreResourceBase.getEntriesAsCSV("truststore");
    }

    @Test
    public void uploadTruststoreFile_rejectsWhenNoPasswordProvided(@Injectable MultipartFile multipartFile) {
        final String emptyPassword = "";

        try {
            truststoreResourceBase.uploadStore(multipartFile, emptyPassword);
            Assert.fail();
        } catch (RequestValidationException ex) {
            Assert.assertTrue("Should have returned the correct error message", ex.getMessage().contains(ERROR_MESSAGE_EMPTY_TRUSTSTORE_PASSWORD));
        }
    }

    @Test
    public void testDownload(@Injectable KeyStoreContentInfo contentInfo, @Injectable String storeName) {

        final byte[] fileContent = new byte[]{1, 0, 1};
        String fileName = "fileName";
        new Expectations(truststoreResourceBase) {{
            truststoreResourceBase.getTrustStoreContent();
            result = contentInfo;
            contentInfo.getContent();
            result = fileContent;
            truststoreResourceBase.getStoreName();
            result = storeName;
            truststoreResourceBase.getFileName((KeyStoreContentInfo) any);
            result = fileName;
        }};

        // When
        ResponseEntity<ByteArrayResource> responseEntity = truststoreResourceBase.downloadTruststoreContent();

        // Then
        validateResponseEntity(responseEntity, HttpStatus.OK);

        new Verifications() {{
            auditService.addKeystoreDownloadedAudit(storeName);
        }};

    }

    @Test
    public void testDownload_MultiTenancy(@Injectable KeyStoreContentInfo contentInfo, @Injectable String storeName) {

        final byte[] fileContent = new byte[]{1, 0, 1};
        String fileName = "fileName";
        new Expectations(truststoreResourceBase) {{
            truststoreResourceBase.getTrustStoreContent();
            result = contentInfo;

            contentInfo.getContent();
            result = fileContent;

            truststoreResourceBase.getStoreName();
            result = storeName;

            truststoreResourceBase.getFileName((KeyStoreContentInfo) any);
            result = fileName;
        }};

        ResponseEntity<ByteArrayResource> responseEntity = truststoreResourceBase.downloadTruststoreContent();

        validateResponseEntity(responseEntity, HttpStatus.OK);
        Assert.assertTrue(responseEntity.getHeaders().getContentDisposition().getFilename().contains(fileName));
        new Verifications() {{
            auditService.addKeystoreDownloadedAudit(storeName);
        }};

    }

    @Test
    public void getTrustStoreEntries(@Injectable MultiDomainCryptoService multiDomainCertificateProvider, @Mocked Domain domain,
                                     @Mocked KeyStore store, @Mocked List<TrustStoreEntry> trustStoreEntries, @Mocked List<TrustStoreRO> entries) {

        new Expectations(truststoreResourceBase) {{
            truststoreResourceBase.doGetStoreEntries();
            result = trustStoreEntries;
            partyCoreConverter.trustStoreEntryListToTrustStoreROList(trustStoreEntries);
            result = entries;
        }};

        List<TrustStoreRO> res = truststoreResourceBase.getTrustStoreEntries();

        Assert.assertEquals(entries, res);
    }

    private List<TrustStoreRO> getTestTrustStoreROList(Date date) {
        List<TrustStoreRO> trustStoreROList = new ArrayList<>();
        TrustStoreRO trustStoreRO = new TrustStoreRO();
        trustStoreRO.setName("Name");
        trustStoreRO.setSubject("Subject");
        trustStoreRO.setIssuer("Issuer");
        trustStoreRO.setValidFrom(date);
        trustStoreRO.setValidUntil(date);
        trustStoreROList.add(trustStoreRO);
        return trustStoreROList;
    }

    private List<TrustStoreRO> getTestTrustStoreROList2(Date date) {
        List<TrustStoreRO> trustStoreROList = getTestTrustStoreROList(date);
        TrustStoreRO trustStoreRO = new TrustStoreRO();
        trustStoreRO.setName("Name2");
        trustStoreRO.setSubject("Subject2");
        trustStoreRO.setIssuer("Issuer2");
        trustStoreRO.setValidFrom(date);
        trustStoreRO.setValidUntil(date);
        trustStoreROList.add(trustStoreRO);
        return trustStoreROList;
    }

    private void validateResponseEntity(ResponseEntity<? extends Resource> responseEntity, HttpStatus httpStatus) {
        Assert.assertNotNull(responseEntity);
        Assert.assertEquals(httpStatus, responseEntity.getStatusCode());
        Assert.assertTrue(responseEntity.getHeaders().get("content-disposition").get(0).contains("attachment; filename="));
        Assert.assertEquals("Byte array resource [resource loaded from byte array]", responseEntity.getBody().getDescription());
    }
}
