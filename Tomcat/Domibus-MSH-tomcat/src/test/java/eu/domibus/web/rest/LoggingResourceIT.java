package eu.domibus.web.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import eu.domibus.AbstractIT;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.core.converter.DomibusCoreMapper;
import eu.domibus.core.logging.LoggingEntry;
import eu.domibus.core.logging.LoggingService;
import eu.domibus.web.rest.ro.LoggingFilterRequestRO;
import org.apache.commons.lang3.BooleanUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.NestedServletException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * @author François Gautier
 * @since 4.2
 */
public class LoggingResourceIT extends AbstractIT {

    @Autowired
    private DomibusCoreMapper coreMapper;

    @Autowired
    private LoggingService loggingService;

    @Autowired
    private LoggingResource loggingResource;

    @Autowired
    protected AuthUtils authUtils;

    private MockMvc mockMvc;

    @Configuration
    static class ContextConfiguration {
        @Primary
        @Bean
        public AuthUtils authUtils() {
            return Mockito.mock(AuthUtils.class);
        }

        @Primary
        @Bean
        public LoggingService loggingService() {
            return Mockito.mock(LoggingService.class);
        }

    }

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(loggingResource).build();
    }

    @Test(expected = NestedServletException.class)
    @WithMockUser
    public void getLogLevel_accessDenied() throws Exception {
        // the order of the items are not checked
        mockMvc.perform(get("/rest/logging/loglevel"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void getLogLevel_ok() throws Exception {
        final List<LoggingEntry> loggingEntryList = new ArrayList<>();
        LoggingEntry loggingLevelRO1 = new LoggingEntry();
        loggingLevelRO1.setLevel("INFO");
        loggingLevelRO1.setName("eu.domibus");
        loggingEntryList.add(loggingLevelRO1);
        LoggingEntry loggingLevelRO2 = new LoggingEntry();
        loggingLevelRO2.setLevel("DEBUG");
        loggingLevelRO2.setName("eu.domibus.common");
        loggingEntryList.add(loggingLevelRO2);
        LoggingEntry loggingLevelRO3 = new LoggingEntry();
        loggingLevelRO3.setLevel("TRACE");
        loggingLevelRO3.setName("eu.domibus.common.model");
        loggingEntryList.add(loggingLevelRO3);

        LoggingFilterRequestRO loggingFilterRequestRO = new LoggingFilterRequestRO();
        loggingFilterRequestRO.setAsc(Boolean.TRUE);
        loggingFilterRequestRO.setLoggerName("eu.domibus2");
        loggingFilterRequestRO.setOrderBy("");
        loggingFilterRequestRO.setPageSize(20);
        loggingFilterRequestRO.setPage(0);
        loggingFilterRequestRO.setShowClasses(true);

        Mockito.when(loggingService.getLoggingLevel(loggingFilterRequestRO.getLoggerName(), loggingFilterRequestRO.isShowClasses())).thenReturn(loggingEntryList);

        // the order of the items are not checked
        mockMvc.perform(get("/rest/logging/loglevel")
                .param("page", loggingFilterRequestRO.getPage() + "")
                .param("loggerName", loggingFilterRequestRO.getLoggerName())
                .param("pageSize", loggingFilterRequestRO.getPageSize() + "")
                .param("orderBy", loggingFilterRequestRO.getOrderBy())
                .param("asc", BooleanUtils.toStringTrueFalse(loggingFilterRequestRO.getAsc()))
                .param("showClasses", BooleanUtils.toStringTrueFalse(loggingFilterRequestRO.isShowClasses()))
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.filter.loggerName").value(loggingFilterRequestRO.getLoggerName()))
                .andExpect(jsonPath("$.filter.showClasses").value(loggingFilterRequestRO.isShowClasses()))
                .andExpect(jsonPath("$.pageSize").value(loggingFilterRequestRO.getPageSize()))
                .andExpect(jsonPath("$.page").value(loggingFilterRequestRO.getPage()))
                .andExpect(jsonPath("$.loggingEntries.[*].name").value(hasItems(
                        "eu.domibus",
                        "eu.domibus.common",
                        "eu.domibus.common.model"
                )))
                .andExpect(jsonPath("$.loggingEntries.[*].level").value(hasItems(
                        "INFO",
                        "DEBUG",
                        "TRACE"
                )))
        ;
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