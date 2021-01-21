package eu.domibus.web.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.api.util.MultiPartFileUtil;
import eu.domibus.core.audit.AuditService;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.crypto.api.TLSCertificateManager;
import eu.domibus.core.csv.CsvServiceImpl;
import eu.domibus.core.security.AuthUtilsImpl;
import eu.domibus.web.rest.error.ErrorHandlerService;
import eu.domibus.web.rest.ro.TrustStoreRO;
import mockit.Expectations;
import mockit.FullVerifications;
import mockit.Mocked;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.NestedServletException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class TLSTruststoreResourceIT {

    @Autowired
    private TLSTruststoreResource tlsTruststoreResource;

    @Mocked
    private TLSCertificateManager tlsCertificateManager;

    @Mocked
    protected DomainCoreConverter domainConverter;

    @Mocked
    protected ErrorHandlerService errorHandlerService;

    @Mocked
    protected MultiPartFileUtil multiPartFileUtil;

    @Mocked
    protected AuditService auditService;

    private MockMvc mockMvc;

    @Configuration
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    static class ContextConfiguration {
        @Bean
        public TLSTruststoreResource tlsTruststoreResource() {
            return new TLSTruststoreResource(null, null, null, null, null);
        }

        @Bean
        public AuthUtilsImpl authUtils() {
            return new AuthUtilsImpl(null, null);
        }

        @Bean
        public CsvServiceImpl csvServiceImpl() {
            return Mockito.mock(CsvServiceImpl.class);
        }

    }

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(tlsTruststoreResource).build();

        ReflectionTestUtils.setField(tlsTruststoreResource, "tlsCertificateManager", tlsCertificateManager);
        ReflectionTestUtils.setField(tlsTruststoreResource, "domainConverter", domainConverter);
        ReflectionTestUtils.setField(tlsTruststoreResource, "errorHandlerService", errorHandlerService);
        ReflectionTestUtils.setField(tlsTruststoreResource, "multiPartFileUtil", multiPartFileUtil);
        ReflectionTestUtils.setField(tlsTruststoreResource, "auditService", auditService);
//        ReflectionTestUtils.setField(tlsTruststoreResource, "csvServiceImpl", csvServiceImpl);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void getEntries_ok() throws Exception {
        List<TrustStoreEntry> entries = Arrays.asList(new TrustStoreEntry());
        TrustStoreRO t1 = new TrustStoreRO();
        t1.setName("blue_gw");
        TrustStoreRO t2 = new TrustStoreRO();
        t2.setName("red_gw");
        List<TrustStoreRO> entriesRO = Arrays.asList(t1, t2);

        new Expectations() {{
            tlsCertificateManager.getTrustStoreEntries();
            result = entries;
            times = 1;

            domainConverter.convert(entries, TrustStoreRO.class);
            result = entriesRO;
            times = 1;
        }};

        mockMvc.perform(get("/rest/tlstruststore/entries"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.[*].name").value(hasItems(
                        "blue_gw", "red_gw"
                )))
        ;

        new FullVerifications() {
        };
    }

    @Test(expected = NestedServletException.class)
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void replaceTrust_EmptyPass() throws Exception {
        byte[] content = {1, 0, 1};
        String filename = "file";
        MockMultipartFile truststoreFile = new MockMultipartFile("file", filename, "octetstream", content);

        new Expectations() {{
            multiPartFileUtil.validateAndGetFileContent(truststoreFile);
            result = content;
            times = 1;
        }};

        mockMvc.perform(multipart("/rest/tlstruststore")
                .file(truststoreFile)
                .param("password", " ")
        )
                .andDo(print());

    }

    public static byte[] convertObjectToJsonBytes(Object object)
            throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        JavaTimeModule module = new JavaTimeModule();
        mapper.registerModule(module);

        return mapper.writeValueAsBytes(object);
    }

}