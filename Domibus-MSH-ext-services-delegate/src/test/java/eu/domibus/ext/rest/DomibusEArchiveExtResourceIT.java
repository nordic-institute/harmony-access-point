package eu.domibus.ext.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.domibus.api.earchive.*;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.ext.delegate.mapper.EArchiveExtMapper;
import eu.domibus.ext.delegate.mapper.TestMapperContextConfiguration;
import eu.domibus.ext.delegate.services.earchive.DomibusEArchiveServiceDelegate;
import eu.domibus.ext.domain.archive.ExportedBatchResultDTO;
import eu.domibus.ext.domain.archive.QueuedBatchResultDTO;
import eu.domibus.ext.rest.spring.DomibusExtWebConfiguration;
import eu.domibus.ext.services.*;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        DomibusEArchiveExtResourceIT.ContextConfiguration.class,
        TestMapperContextConfiguration.class,
        DomibusExtWebConfiguration.class
})
@WebAppConfiguration
public class DomibusEArchiveExtResourceIT {
    // The endpoints to test
    public static final String TEST_ENDPOINT_RESOURCE = "/ext/archive";
    public static final String TEST_ENDPOINT_QUEUED = TEST_ENDPOINT_RESOURCE + "/batches/queued";
    public static final String TEST_ENDPOINT_BATCH = TEST_ENDPOINT_RESOURCE + "/batches/{batchId}";
    public static final String TEST_ENDPOINT_EXPORTED = TEST_ENDPOINT_RESOURCE + "/batches/exported";
    public static final String TEST_ENDPOINT_SANITY_DATE = TEST_ENDPOINT_RESOURCE + "/sanity-mechanism/start-date";
    public static final String TEST_ENDPOINT_CONTINUOUS_DATE = TEST_ENDPOINT_RESOURCE + "/continuous-mechanism/start-date";
    public static final String TEST_ENDPOINT_EXPORTED_BATCHID_MESSAGES = TEST_ENDPOINT_EXPORTED + "/{batchId}/messages";

    public ObjectMapper objectMapper = new ObjectMapper();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Autowired
    private DomibusEArchiveExtResource testInstance;

    @Autowired
    private DomibusExtWebConfiguration domibusExtWebConfiguration;

    private static final DomibusEArchiveService mockDomibusEArchiveService = mock(DomibusEArchiveService.class);

    private MockMvc mockMvc;

    @Configuration
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    static class ContextConfiguration {

        @Bean
        public AuthenticationExtService beanAuthenticationExtService() {
            return Mockito.mock(AuthenticationExtService.class);
        }

        @Bean
        public CacheExtService beanCacheExtService() {
            return Mockito.mock(CacheExtService.class);
        }

        @Bean
        public MessageMonitorExtService beanMessageMonitorExtService() {
            return Mockito.mock(MessageMonitorExtService.class);
        }

        @Bean
        public PartyExtService beanPartyExtService() {
            return Mockito.mock(PartyExtService.class);
        }

        @Bean
        public PModeExtService beanPModeExtService() {
            return Mockito.mock(PModeExtService.class);
        }

        @Bean
        public UserMessageExtService beanUserMessageExtService() {
            return Mockito.mock(UserMessageExtService.class);
        }

        @Bean
        public MessageAcknowledgeExtService beanMessageAcknowledgeExtService() {
            return Mockito.mock(MessageAcknowledgeExtService.class);
        }

        @Bean
        public DomibusMonitoringExtService beanDomibusMonitoringExtService() {
            return Mockito.mock(DomibusMonitoringExtService.class);
        }


        @Bean
        public DomibusEArchiveServiceDelegate beanDomibusEArchiveExtService(EArchiveExtMapper eArchiveExtMapper) {
            return new DomibusEArchiveServiceDelegate(mockDomibusEArchiveService, eArchiveExtMapper);
        }

    }
    public List<HttpMessageConverter<?>> mappingJackson2HttpMessageConverter() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        List<HttpMessageConverter<?>> converter = Collections.singletonList(new MappingJackson2HttpMessageConverter(builder.build()));
        domibusExtWebConfiguration.extendMessageConverters(converter);
        return converter;
    }

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(testInstance).setMessageConverters(mappingJackson2HttpMessageConverter().get(0))
                .build();

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
        verify(mockDomibusEArchiveService, times(1)).updateStartDateSanityArchive(resultDate);
    }

    @Test
    public void testGetBatchMessageIdsNoResultFound() throws Exception {
        final String batchId = "0";

        when(mockDomibusEArchiveService.getBatchUserMessageListCount(batchId))
                .thenThrow(new DomibusEArchiveException(DomibusCoreErrorCode.DOM_009,"EArchive batch not found batchId: [" + batchId + "]"));

        mockMvc.perform(MockMvcRequestBuilders.get(TEST_ENDPOINT_EXPORTED_BATCHID_MESSAGES)
                .param("batchId", batchId)
        )
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andReturn();
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
        verify(mockDomibusEArchiveService, times(1)).updateStartDateContinuousArchive(resultDate);
    }

    @Test
    public void testGetQueuedBatchRequestsForNoResults() throws Exception {
        // given
        Integer lastCountRequests = 10;
        when(mockDomibusEArchiveService.getBatchRequestListCount(any())).thenReturn(0L);
        // when
        MvcResult result = mockMvc.perform(get(TEST_ENDPOINT_QUEUED)
                .param("lastCountRequests", lastCountRequests + ""))
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
        assertEquals(0, response.getBatches().size());
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
        assertEquals(2, response.getBatches().size());
        assertEquals(lastCountRequests, response.getFilter().getLastCountRequests());
    }

    @Test
    public void testHistoryOfTheExportedBatches() throws Exception {
        // given
        Long messageStartDate = 211005L;
        Long messageEndDate = 211015L;
        ArgumentCaptor<EArchiveBatchFilter> filterCaptor = ArgumentCaptor.forClass(EArchiveBatchFilter.class);
        ArgumentCaptor<EArchiveBatchFilter> filterCaptorCount = ArgumentCaptor.forClass(EArchiveBatchFilter.class);

        when(mockDomibusEArchiveService.getBatchRequestListCount(filterCaptorCount.capture())).thenReturn(2L);
        when(mockDomibusEArchiveService.getBatchRequestList(filterCaptor.capture())).thenReturn(Arrays.asList(
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
        assertEquals(2, response.getBatches().size());
        assertEquals(response.getPagination().getPageSize(), filterCaptor.getValue().getPageSize());
        assertEquals(response.getPagination().getPageStart(), filterCaptor.getValue().getPageStart());
        assertNull(filterCaptorCount.getValue().getPageSize());
        assertNull(filterCaptorCount.getValue().getPageStart());
        assertEquals(0, response.getFilter().getStatuses().size());
        assertEquals(1, filterCaptor.getValue().getStatusList().size());
        assertEquals(1, filterCaptorCount.getValue().getStatusList().size());
        // the default status
        assertEquals(EArchiveBatchStatus.EXPORTED, filterCaptor.getValue().getStatusList().get(0));
        assertEquals(EArchiveBatchStatus.EXPORTED, filterCaptorCount.getValue().getStatusList().get(0));

        assertEquals(messageStartDate, response.getFilter().getMessageStartDate());
        assertEquals(messageEndDate, response.getFilter().getMessageEndDate());
        // false by default
        assertEquals(Boolean.FALSE, response.getFilter().getIncludeReExportedBatches());
        assertEquals(Boolean.FALSE, filterCaptor.getValue().getIncludeReExportedBatches());
        assertEquals(Boolean.FALSE, filterCaptorCount.getValue().getIncludeReExportedBatches());
    }

    @Test
    public void testHistoryOfTheExportedBatches2() throws Exception {
        // given

        ArgumentCaptor<EArchiveBatchFilter> filterCaptor = ArgumentCaptor.forClass(EArchiveBatchFilter.class);
        ArgumentCaptor<EArchiveBatchFilter> filterCaptorCount = ArgumentCaptor.forClass(EArchiveBatchFilter.class);

        when(mockDomibusEArchiveService.getBatchRequestListCount(filterCaptorCount.capture())).thenReturn(2L);
        when(mockDomibusEArchiveService.getBatchRequestList(filterCaptor.capture())).thenReturn(Arrays.asList(
                new EArchiveBatchRequestDTO() {{
                    setBatchId("Batch1");
                }},
                new EArchiveBatchRequestDTO() {{
                    setBatchId("Batch2");
                }}
        ));

        Long messageStartDate = 21100500L;
        Long messageEndDate = 21123100L;
        int pageStart = 2;
        int pageSize = 10;
        Boolean reExported = Boolean.TRUE;
        // when
        MvcResult result = mockMvc.perform(get(TEST_ENDPOINT_EXPORTED)
                .param("messageStartDate", messageStartDate + "")
                .param("messageEndDate", messageEndDate + "")
                .param("statuses", "EXPORTED,ARCHIVED,ARCHIVE_FAILED,EXPIRED,DELETED")
                .param("reExport", reExported.toString())
                .param("pageStart", pageStart + "")
                .param("pageSize", pageSize + "")

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
        assertEquals(2, response.getBatches().size());
        assertEquals(Integer.valueOf(pageSize), response.getPagination().getPageSize());
        assertEquals(Integer.valueOf(pageStart), response.getPagination().getPageStart());
        assertEquals(response.getPagination().getPageSize(), filterCaptor.getValue().getPageSize());
        assertEquals(response.getPagination().getPageStart(), filterCaptor.getValue().getPageStart());
        assertNull(filterCaptorCount.getValue().getPageSize());
        assertNull(filterCaptorCount.getValue().getPageStart());

        assertEquals(5, response.getFilter().getStatuses().size());
        assertEquals(5, filterCaptor.getValue().getStatusList().size());
        assertEquals(0, filterCaptorCount.getValue().getRequestTypes().size());
        assertEquals(reExported, filterCaptor.getValue().getIncludeReExportedBatches());
        assertEquals(reExported, filterCaptorCount.getValue().getIncludeReExportedBatches());
        assertEquals(reExported, response.getFilter().getIncludeReExportedBatches());
    }

    @Test
    public void testDateFormattingForHistoryOfTheExportedBatches() throws Exception {
        // given
        Long messageStartDate = 211005L;
        Long messageEndDate = 211015L;
        Date date = Calendar.getInstance().getTime();
        ArgumentCaptor<EArchiveBatchFilter> filterCaptor = ArgumentCaptor.forClass(EArchiveBatchFilter.class);
        ArgumentCaptor<EArchiveBatchFilter> filterCaptorCount = ArgumentCaptor.forClass(EArchiveBatchFilter.class);

        when(mockDomibusEArchiveService.getBatchRequestListCount(filterCaptorCount.capture())).thenReturn(2L);
        when(mockDomibusEArchiveService.getBatchRequestList(filterCaptor.capture())).thenReturn(Arrays.asList(
                new EArchiveBatchRequestDTO() {{
                    setBatchId("Batch1");
                    setTimestamp(date);
                }},
                new EArchiveBatchRequestDTO() {{
                    setBatchId("Batch2");
                    setTimestamp(date);
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
        //dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"))

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        assertThat(content, CoreMatchers.containsString("\"enqueuedTimestamp\":\"" + sdf.format(date) + "\""));

    }

}