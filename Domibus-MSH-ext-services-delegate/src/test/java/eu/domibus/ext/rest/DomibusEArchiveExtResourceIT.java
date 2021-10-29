package eu.domibus.ext.rest;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import eu.domibus.api.earchive.DomibusEArchiveService;
import eu.domibus.api.earchive.EArchiveBatchRequestDTO;
import eu.domibus.ext.delegate.mapper.EArchiveExtMapper;
import eu.domibus.ext.delegate.mapper.TestMapperContextConfiguration;
import eu.domibus.ext.delegate.services.earchive.DomibusEArchiveServiceDelegate;
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
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
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
    public static final String TEST_ENDPOINT_QUEUED_BATCHES = TEST_ENDPOINT_RESOURCE + "/batches/queued";
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
    static class ContextConfiguration
            //        implements WebMvcConfigurer
    {
        /*
        @Override
        public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
            Jackson2ObjectMapperBuilder builder = jacksonBuilder();
            converters.add(new MappingJackson2HttpMessageConverter(builder.build()));
        }
        /
         */

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
        mockMvc = MockMvcBuilders.standaloneSetup(testInstance).
                build();
        Mockito.reset(mockDomibusEArchiveService);

        /*mockMvc = MockMvcBuilders
                .webAppContextSetup(webAppContext).
                build();
*/
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
        MvcResult result = mockMvc.perform(get(TEST_ENDPOINT_QUEUED_BATCHES)
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
        MvcResult result = mockMvc.perform(get(TEST_ENDPOINT_QUEUED_BATCHES)
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

}