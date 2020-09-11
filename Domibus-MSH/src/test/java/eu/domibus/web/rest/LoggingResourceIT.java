package eu.domibus.web.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.logging.LoggingEntry;
import eu.domibus.core.logging.LoggingService;
import eu.domibus.ext.delegate.services.security.SecurityDefaultService;
import eu.domibus.web.rest.ro.LoggingFilterRequestRO;
import eu.domibus.web.rest.ro.LoggingLevelRO;
import mockit.*;
import org.apache.commons.lang3.BooleanUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * @author François Gautier
 * @since 4.2
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
//@WebAppConfiguration
public class LoggingResourceIT {

    @Mocked
    private DomainCoreConverter domainConverter;
    @Mocked
    private LoggingService loggingService;
    @Autowired
    private LoggingResource loggingResource;
    private MockMvc mockMvc;

    @Configuration
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    static class ContextConfiguration {
        @Bean
        public LoggingResource loggingResource() {
            return new LoggingResource(null,
                    null,
                    null);
        }

        @Bean
        public SecurityDefaultService securityDefaultService() {
            return new SecurityDefaultService(null, null);
        }
    }

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(loggingResource).build();
        ReflectionTestUtils.setField(loggingResource, "domainConverter", domainConverter);
        ReflectionTestUtils.setField(loggingResource, "loggingService", loggingService);
    }

    @Test(expected = NestedServletException.class)
    @WithMockUser
    public void getLogLevel_accessDenied() throws Exception {

        new Expectations() {{
            new MockUp<SecurityDefaultService>() {
                @Mock
                public boolean isAdminMultiAware() {
                    return false;
                }
            };
        }};

        // the order of the items are not checked
        mockMvc.perform(get("/rest/logging/loglevel"));

        new FullVerifications() {
        };
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void getLogLevel_ok() throws Exception {
        List<LoggingEntry> loggingEntryList = new ArrayList<>();
        loggingEntryList.add(new LoggingEntry());

        final List<LoggingLevelRO> loggingLevelROList = new ArrayList<>();
        LoggingLevelRO loggingLevelRO1 = new LoggingLevelRO();
        loggingLevelRO1.setLevel("INFO");
        loggingLevelRO1.setName("eu.domibus");
        loggingLevelROList.add(loggingLevelRO1);
        LoggingLevelRO loggingLevelRO2 = new LoggingLevelRO();
        loggingLevelRO2.setLevel("DEBUG");
        loggingLevelRO2.setName("eu.domibus.common");
        loggingLevelROList.add(loggingLevelRO2);
        LoggingLevelRO loggingLevelRO3 = new LoggingLevelRO();
        loggingLevelRO3.setLevel("TRACE");
        loggingLevelRO3.setName("eu.domibus.common.model");
        loggingLevelROList.add(loggingLevelRO3);

        LoggingFilterRequestRO loggingFilterRequestRO = new LoggingFilterRequestRO();
        loggingFilterRequestRO.setAsc(Boolean.TRUE);
        loggingFilterRequestRO.setLoggerName("eu.domibus2");
        loggingFilterRequestRO.setOrderBy("");
        loggingFilterRequestRO.setPageSize(20);
        loggingFilterRequestRO.setPage(0);
        loggingFilterRequestRO.setShowClasses(true);

        new Expectations() {{
            new MockUp<SecurityDefaultService>() {
                @Mock
                public boolean isAdminMultiAware() {
                    return true;
                }
            };

            loggingService.getLoggingLevel(loggingFilterRequestRO.getLoggerName(), loggingFilterRequestRO.isShowClasses());
            result = loggingEntryList;
            times = 1;

            domainConverter.convert(loggingEntryList, LoggingLevelRO.class);
            result = loggingLevelROList;
            times = 1;
        }};

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

        new FullVerifications() {
        };
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