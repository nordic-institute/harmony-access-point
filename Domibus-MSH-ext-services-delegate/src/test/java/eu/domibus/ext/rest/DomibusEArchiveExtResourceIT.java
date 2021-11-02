package eu.domibus.ext.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import eu.domibus.api.earchive.DomibusEArchiveService;
import eu.domibus.api.earchive.EArchiveBatchRequestDTO;
import eu.domibus.ext.delegate.mapper.EArchiveExtMapper;
import eu.domibus.ext.delegate.mapper.TestMapperContextConfiguration;
import eu.domibus.ext.delegate.services.earchive.DomibusEArchiveServiceDelegate;
import eu.domibus.ext.domain.archive.ExportedBatchResultDTO;
import eu.domibus.ext.domain.archive.QueuedBatchResultDTO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        DomibusEArchiveExtResourceIT.ContextConfiguration.class,
        TestMapperContextConfiguration.class,
})
@WebAppConfiguration
public class DomibusEArchiveExtResourceIT {
    // The endpoints to test
    public static final String TEST_ENDPOINT_RESOURCE = "/ext/archive";
    public static final String TEST_ENDPOINT_QUEUED = TEST_ENDPOINT_RESOURCE + "/batches/queued";
    public static final String TEST_ENDPOINT_EXPORTED = TEST_ENDPOINT_RESOURCE + "/batches/exported";
    public static final String TEST_ENDPOINT_SANITY_DATE = TEST_ENDPOINT_RESOURCE + "/sanity-mechanism/start-date";
    public static final String TEST_ENDPOINT_CONTINUOUS_DATE = TEST_ENDPOINT_RESOURCE + "/continuous-mechanism/start-date";

    public ObjectMapper objectMapper = new ObjectMapper();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Autowired
    private DomibusEArchiveExtResource testInstance;

    @Autowired
    private WebApplicationContext webAppContext;

    private static final DomibusEArchiveService mockDomibusEArchiveService = mock(DomibusEArchiveService.class);

    private MockMvc mockMvc;

    @Configuration
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    static class ContextConfiguration {
        @Bean
        public DomibusEArchiveServiceDelegate beanDomibusEArchiveExtService(EArchiveExtMapper eArchiveExtMapper) {
            return new DomibusEArchiveServiceDelegate(mockDomibusEArchiveService, eArchiveExtMapper);
        }

        @Bean
        public DomibusEArchiveExtResource beanDomibusEArchiveExtResource(DomibusEArchiveServiceDelegate domibusEArchiveExtService) {
            return new DomibusEArchiveExtResource(domibusEArchiveExtService, null);
        }

        @Bean
        public Jackson2ObjectMapperBuilder jacksonBuilder() {
            Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
            builder.propertyNamingStrategy(PropertyNamingStrategy.KEBAB_CASE);
            return builder;
        }
    }

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(testInstance).build();
        Mockito.reset(mockDomibusEArchiveService);
    }

    @Test
    public void testGetSanityArchivingStartDate() throws Exception {
        // given
        long resultDate = 21101500L;
        when(mockDomibusEArchiveService.getStartDateSanityArchive()).thenReturn(resultDate);
        // when
        MvcResult result = mockMvc.perform(get(TEST_ENDPOINT_SANITY_DATE))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        // then
        String content = result.getResponse().getContentAsString();
        Assert.assertEquals(resultDate, Long.parseLong(content));
    }

    @Test
    public void testResetSanityArchivingStartDate() throws Exception {
        // given
        long resultDate = 21101500L;
        when(mockDomibusEArchiveService.getStartDateSanityArchive()).thenReturn(resultDate);
        // when
        MvcResult result = mockMvc.perform(put(TEST_ENDPOINT_SANITY_DATE)
                .param("messageStartDate", resultDate + "")
        )
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        // then
        verify(mockDomibusEArchiveService, times(1)).updateStartDateSanityArchive(Matchers.eq(resultDate));
    }

    @Test
    public void testGetStartDateContinuousArchive() throws Exception {
        // given
        long resultDate = 21101500L;
        when(mockDomibusEArchiveService.getStartDateContinuousArchive()).thenReturn(resultDate);
        // ehwn
        MvcResult result = mockMvc.perform(get(TEST_ENDPOINT_CONTINUOUS_DATE))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        // then
        String content = result.getResponse().getContentAsString();
        Assert.assertEquals(resultDate, Long.parseLong(content));
    }

    @Test
    public void testResetStartDateContinuousArchive() throws Exception {
        // given
        long resultDate = 21101500L;
        when(mockDomibusEArchiveService.getStartDateSanityArchive()).thenReturn(resultDate);
        // when
        MvcResult result = mockMvc.perform(put(TEST_ENDPOINT_CONTINUOUS_DATE)
                .param("messageStartDate", resultDate + "")
        )
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        // then
        verify(mockDomibusEArchiveService, times(1)).updateStartDateContinuousArchive(Matchers.eq(resultDate));
    }

    @Test
    public void testGetQueuedBatchRequestsForNoResults() throws Exception {
        // given
        Integer lastCountRequests = 10;
        when(mockDomibusEArchiveService.getBatchRequestListCount(any())).thenReturn(0L);
        // when
        MvcResult result = mockMvc.perform(get(TEST_ENDPOINT_QUEUED)
                .param("lastCountRequests", lastCountRequests + "")
        )
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        // then
        String content = result.getResponse().getContentAsString();
        QueuedBatchResultDTO response = objectMapper.readValue(content, QueuedBatchResultDTO.class);

        verify(mockDomibusEArchiveService, times(1)).getBatchRequestListCount(any());
        verify(mockDomibusEArchiveService, times(0)).getBatchRequestList(any());
        assertNotNull(response.getFilter());
        assertNotNull(response.getPagination());
        assertEquals(Integer.valueOf(0), response.getPagination().getTotal());
        assertEquals(0, response.getQueuedBatches().size());
        assertEquals(lastCountRequests, response.getFilter().getLastCountRequests());
    }

    @Test
    public void testGetQueuedBatchRequestsForResults() throws Exception {
// given
        Integer lastCountRequests = 5;

        when(mockDomibusEArchiveService.getBatchRequestListCount(any())).thenReturn(2L);
        when(mockDomibusEArchiveService.getBatchRequestList(any())).thenReturn(Arrays.asList(
                new EArchiveBatchRequestDTO() {{
                    setBatchId("Batch1");
                }},
                new EArchiveBatchRequestDTO() {{
                    setBatchId("Batch2");
                }}
        ));

        // when
        MvcResult result = mockMvc.perform(get(TEST_ENDPOINT_QUEUED)
                .param("lastCountRequests", lastCountRequests + "")
        )
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        // then
        String content = result.getResponse().getContentAsString();
        QueuedBatchResultDTO response = objectMapper.readValue(content, QueuedBatchResultDTO.class);

        verify(mockDomibusEArchiveService, times(1)).getBatchRequestListCount(any());
        verify(mockDomibusEArchiveService, times(1)).getBatchRequestList(any());
        assertNotNull(response.getFilter());
        assertNotNull(response.getPagination());
        assertEquals(Integer.valueOf(2), response.getPagination().getTotal());
        assertEquals(2, response.getQueuedBatches().size());
        assertEquals(lastCountRequests, response.getFilter().getLastCountRequests());
    }

    @Test
    public void testHistoryOfTheExportedBatches() throws Exception {
// given
        Long messageStartDate = 211005L;
        Long messageEndDate = 211015L;

        when(mockDomibusEArchiveService.getBatchRequestListCount(any())).thenReturn(2L);
        when(mockDomibusEArchiveService.getBatchRequestList(any())).thenReturn(Arrays.asList(
                new EArchiveBatchRequestDTO() {{
                    setBatchId("Batch1");
                }},
                new EArchiveBatchRequestDTO() {{
                    setBatchId("Batch2");
                }}
        ));

        // when
        MvcResult result = mockMvc.perform(get(TEST_ENDPOINT_EXPORTED)
                .param("messageStartDate", messageStartDate + "")
                .param("messageEndDate", messageEndDate + "")
        )
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        // then
        String content = result.getResponse().getContentAsString();
        ExportedBatchResultDTO response = objectMapper.readValue(content, ExportedBatchResultDTO.class);

        verify(mockDomibusEArchiveService, times(1)).getBatchRequestListCount(any());
        verify(mockDomibusEArchiveService, times(1)).getBatchRequestList(any());
        assertNotNull(response.getFilter());
        assertNotNull(response.getPagination());
        assertEquals(Integer.valueOf(2), response.getPagination().getTotal());
        assertEquals(2, response.getExportedBatches().size());
        assertEquals(messageStartDate, response.getFilter().getMessageStartDate());
        assertEquals(messageStartDate, response.getFilter().getMessageStartDate());
        assertEquals(messageEndDate, response.getFilter().getMessageEndDate());
    }

}