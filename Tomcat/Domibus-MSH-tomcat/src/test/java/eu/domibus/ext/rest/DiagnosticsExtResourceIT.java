package eu.domibus.ext.rest;

import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.test.AbstractIT;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Breaz Ionut
 * @since 5.1.9
 */
@Transactional
public class DiagnosticsExtResourceIT extends AbstractIT {

    // The endpoints to test
    public static final String TEST_ENDPOINT_BASE = "/ext/diagnostics";
    public static final String TEST_ENDPOINT_VERSION = TEST_ENDPOINT_BASE + "/version";
    public static final String TEST_ENDPOINT_JMS_QUEUES = TEST_ENDPOINT_BASE + "/jmsQueuesInfo";
    public static final String TEST_ENDPOINT_ALERTS = TEST_ENDPOINT_BASE + "/alerts";
    public static final String TEST_ENDPOINT_MESSAGES = TEST_ENDPOINT_BASE + "/messages";

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webAppContext;

    @Autowired
    private DomibusConfigurationService domibusConfigurationService;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext)
                .build();
    }

    @Test
    public void testGetVersionInfo() throws Exception {
        // when
        MvcResult result = mockMvc.perform(get(TEST_ENDPOINT_VERSION)
                        .with(httpBasic(TEST_PLUGIN_USERNAME, TEST_PLUGIN_PASSWORD))
                        .with(csrf()))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        // then
        String content = result.getResponse().getContentAsString();

        MatcherAssert.assertThat(content, CoreMatchers.allOf(
                containsString("artifactName"),
                containsString("artifactVersion")));
    }

    @Test
    public void testGetAlertsCounts() throws Exception {
        // when
        MvcResult result = mockMvc.perform(get(TEST_ENDPOINT_ALERTS)
                        .with(httpBasic(TEST_PLUGIN_USERNAME, TEST_PLUGIN_PASSWORD))
                        .with(csrf()))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        // then
        String content = result.getResponse().getContentAsString();

        MatcherAssert.assertThat(content, CoreMatchers.allOf(
                containsString("SUCCESS"),
                containsString("RETRY"),
                containsString("FAILED")));
    }

    @Test
    public void testGetMessagesByStatusCounts() throws Exception {
        // when
        MvcResult result = mockMvc.perform(get(TEST_ENDPOINT_MESSAGES)
                        .with(httpBasic(TEST_PLUGIN_USERNAME, TEST_PLUGIN_PASSWORD))
                        .with(csrf()))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        // then
        String content = result.getResponse().getContentAsString();

        MatcherAssert.assertThat(content, CoreMatchers.allOf(
                containsString("RECEIVED"),
                containsString("WAITING_FOR_RETRY"),
                containsString("ACKNOWLEDGED")));
    }
}
