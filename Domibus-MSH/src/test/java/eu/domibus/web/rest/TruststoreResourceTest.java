package eu.domibus.web.rest;

import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.exceptions.RequestValidationException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.api.util.MultiPartFileUtil;
import eu.domibus.core.audit.AuditService;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.crypto.MultiDomainCryptoServiceImpl;
//import eu.domibus.core.crypto.TLSMultiDomainCryptoServiceImpl;
import eu.domibus.core.crypto.api.MultiDomainCryptoService;
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

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static eu.domibus.web.rest.TruststoreResource.ERROR_MESSAGE_EMPTY_TRUSTSTORE_PASSWORD;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
@RunWith(JMockit.class)
public class TruststoreResourceTest {

    @Tested
    TruststoreResource truststoreResource;

    @Injectable
    MultiDomainCryptoService multiDomainCertificateProvider;

    @Injectable
    MultiDomainCryptoServiceImpl multiDomainCryptoService;

//    @Injectable
//    TLSMultiDomainCryptoServiceImpl tlsMultiDomainCryptoService;

    @Injectable
    protected DomainContextProvider domainProvider;

    @Injectable
    CertificateService certificateService;

    @Injectable
    DomainCoreConverter domainConverter;

    @Injectable
    CsvServiceImpl csvServiceImpl;

    @Injectable
    ErrorHandlerService errorHandlerService;

    @Injectable
    MultiPartFileUtil multiPartFileUtil;

    @Injectable
    private AuditService auditService;

    @Test
    public void testUploadTruststoreFileSuccess() {
        // Given
        MultipartFile multiPartFile = new MockMultipartFile("filename", new byte[]{1, 0, 1});

        // When
        String response = truststoreResource.uploadTruststoreFile(multiPartFile, "pass");

        // Then
        Assert.assertNotNull(response);
        Assert.assertEquals("Truststore file has been successfully replaced.", response);
    }

    @Test
    public void testUploadTruststoreEmpty() {
        // Given
        MultipartFile emptyFile = new MockMultipartFile("truststore", new byte[]{});

        new Expectations() {{
            multiPartFileUtil.validateAndGetFileContent(emptyFile);
            result = new IllegalArgumentException("Failed to upload the truststore file since it was empty.");
        }};

        // When
        try {
            String responseEntity = truststoreResource.uploadTruststoreFile(emptyFile, "pass");
        } catch (IllegalArgumentException ex) {
            // Then
            Assert.assertEquals("Failed to upload the truststore file since it was empty.", ex.getMessage());
        }
    }

    @Test(expected = CryptoException.class)
    public void testUploadTruststoreException(@Injectable MultiDomainCryptoService certificateProvider) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        // Given
        MultipartFile multiPartFile = new MockMultipartFile("filename", new byte[]{1, 0, 1});

        final Domain domain = DomainService.DEFAULT_DOMAIN;

        new Expectations() {{
            multiPartFileUtil.validateAndGetFileContent(multiPartFile);
            result = new byte[]{1, 0, 1};

            domainProvider.getCurrentDomain();
            result = domain;

            certificateProvider.replaceTrustStore(domain, anyString, (byte[]) any, anyString);
            result = new CryptoException("Password is incorrect");
        }};

        // When
        truststoreResource.replaceTruststore(certificateProvider, multiPartFile, "pass");
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

    @Test
    public void testTrustStoreEntries(@Mocked KeyStore trustStore) {
        // Given
        Date date = new Date();
        List<TrustStoreEntry> trustStoreEntryList = new ArrayList<>();
        TrustStoreEntry trustStoreEntry = new TrustStoreEntry("Name", "Subject", "Issuer", date, date);
        trustStoreEntryList.add(trustStoreEntry);

        final Domain domain = DomainService.DEFAULT_DOMAIN;

        new Expectations() {{
            domainProvider.getCurrentDomain();
            result = domain;

            certificateService.getTrustStoreEntries(trustStore);
            result = trustStoreEntryList;
            domainConverter.convert(trustStoreEntryList, TrustStoreRO.class);
            result = getTestTrustStoreROList(date);
        }};

        // When
        final List<TrustStoreRO> trustStoreROList = truststoreResource.trustStoreEntries();

        // Then
        Assert.assertEquals(getTestTrustStoreROList(date), trustStoreROList);
    }

    @Test
    public void testGetCsv(@Injectable MultiDomainCryptoService cryptoService, @Mocked String moduleName) {
        // Given
        Date date = new Date();
        List<TrustStoreRO> trustStoreROList = getTestTrustStoreROList(date);
        new Expectations(truststoreResource) {{
            truststoreResource.getTrustStoreEntries(cryptoService);
            result = trustStoreROList;

            csvServiceImpl.exportToCSV(trustStoreROList, null, (Map<String, String>) any, (List<String>) any);
            result = "Name, Subject, Issuer, Valid From, Valid Until" + System.lineSeparator() +
                    "Name, Subject, Issuer, " + date + ", " + date + System.lineSeparator();
        }};

        // When
        final ResponseEntity<String> csv = truststoreResource.getEntriesAsCSV(cryptoService, moduleName);

        // Then
        Assert.assertEquals(HttpStatus.OK, csv.getStatusCode());
        Assert.assertEquals("Name, Subject, Issuer, Valid From, Valid Until" + System.lineSeparator() +
                        "Name, Subject, Issuer, " + date + ", " + date + System.lineSeparator(),
                csv.getBody());
    }

    @Test(expected = RequestValidationException.class)
    public void testGetCsv_validationExeption(@Injectable MultiDomainCryptoService cryptoService) {
        // Given
        Date date = new Date();
        List<TrustStoreRO> trustStoreROList = getTestTrustStoreROList2(date);
        new Expectations(truststoreResource) {{
            truststoreResource.getTrustStoreEntries(cryptoService);
            result = trustStoreROList;
            csvServiceImpl.validateMaxRows(trustStoreROList.size());
            result = new RequestValidationException("");
        }};

        // When
        final ResponseEntity<String> csv = truststoreResource.getEntriesAsCSV(cryptoService, "truststore");
    }

    @Test
    public void uploadTruststoreFile_rejectsWhenNoPasswordProvided(@Injectable MultipartFile multipartFile) throws Exception {
        // GIVEN
        final String emptyPassword = "";

        // WHEN
        try {
            String response = truststoreResource.uploadTruststoreFile(multipartFile, emptyPassword);
            Assert.fail();
        } catch (Exception ex) {
            Assert.assertTrue("Should have returned the correct error message", ex.getMessage().contentEquals(ERROR_MESSAGE_EMPTY_TRUSTSTORE_PASSWORD));
        }
    }

//    @Test
//    public void testDownload(@Injectable MultiDomainCryptoService multiDomainCertificateProvider,
//                             @Mocked Runnable auditMethod, @Mocked Domain domain) throws IOException {
//
//        final byte[] fileContent = new byte[]{1, 0, 1};
//        // Given
//        new Expectations() {{
//            domainProvider.getCurrentDomain();
//            result = domain;
//
//            multiDomainCertificateProvider.getTruststoreContent(domain);
//            result = fileContent;
//        }};
//
//        // When
//        ResponseEntity<ByteArrayResource> responseEntity = truststoreResource.downloadTruststoreContent(multiDomainCertificateProvider, auditMethod);
//
//        // Then
//        validateResponseEntity(responseEntity, HttpStatus.OK);
//
//        new Verifications() {{
//            auditMethod.run();
//        }};
//
//    }

    @Test
    public void replaceTruststore(@Injectable MultiDomainCryptoService multiDomainCertificateProvider, @Mocked Domain domain) {
        final byte[] fileContent = new byte[]{1, 0, 1};
        MultipartFile multiPartFile = new MockMultipartFile("filename", fileContent);

        new Expectations() {{
            multiPartFileUtil.validateAndGetFileContent(multiPartFile);
            result = fileContent;
            domainProvider.getCurrentDomain();
            result = domain;
        }};

        // When
        truststoreResource.replaceTruststore(multiDomainCertificateProvider, multiPartFile, "pass");

        new Verifications() {{
            multiPartFileUtil.validateAndGetFileContent(multiPartFile);
            multiDomainCertificateProvider.replaceTrustStore(domain, multiPartFile.getOriginalFilename(), fileContent, "pass");
        }};

    }

    @Test
    public void getTrustStoreEntries(@Injectable MultiDomainCryptoService multiDomainCertificateProvider, @Mocked Domain domain,
                                     @Mocked KeyStore store, @Mocked List<TrustStoreEntry> trustStoreEntries, @Mocked List<TrustStoreRO> entries) {

        new Expectations() {{
            domainProvider.getCurrentDomain();
            result = domain;
            multiDomainCertificateProvider.getTrustStore(domain);
            result = store;
            certificateService.getTrustStoreEntries(store);
            result = trustStoreEntries;
            domainConverter.convert(trustStoreEntries, TrustStoreRO.class);
            result = entries;
        }};

        List<TrustStoreRO> res = truststoreResource.getTrustStoreEntries(multiDomainCertificateProvider);

        Assert.assertEquals(entries, res);
    }

    @Test
    public void uploadTruststoreFile(@Injectable MultiDomainCryptoServiceImpl multiDomainCertificateProvider, @Mocked Domain domain) {
        String pass = "pass";
        MultipartFile truststoreFile = new MockMultipartFile("filename", new byte[]{1, 0, 1});
        new Expectations(truststoreResource) {{
            domainProvider.getCurrentDomain();
            result = domain;
            truststoreResource.replaceTruststore(multiDomainCertificateProvider, truststoreFile, pass);
            certificateService.saveCertificateAndLogRevocation(domain);
        }};

        String response = truststoreResource.uploadTruststoreFile(truststoreFile, pass);

        new Verifications() {{
            certificateService.saveCertificateAndLogRevocation(domain);
        }};

        Assert.assertEquals("Truststore file has been successfully replaced.", response);
    }

    private void validateResponseEntity(ResponseEntity<? extends Resource> responseEntity, HttpStatus httpStatus) {
        Assert.assertNotNull(responseEntity);
        Assert.assertEquals(httpStatus, responseEntity.getStatusCode());
        Assert.assertEquals("attachment; filename=TrustStore.jks", responseEntity.getHeaders().get("content-disposition").get(0));
        Assert.assertEquals("Byte array resource [resource loaded from byte array]", responseEntity.getBody().getDescription());
    }
}
