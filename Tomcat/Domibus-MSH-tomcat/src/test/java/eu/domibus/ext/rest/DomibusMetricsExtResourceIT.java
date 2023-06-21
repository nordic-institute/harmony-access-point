package eu.domibus.ext.rest;

import eu.domibus.AbstractIT;
import eu.domibus.messaging.XmlProcessingException;
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

import java.io.IOException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The complete rest endpoint integration tests
 */
@Transactional
public class DomibusMetricsExtResourceIT extends AbstractIT {

    // The endpoints to test
    public static final String TEST_ENDPOINT_RESOURCE = "/ext/metrics";
    public static final String TEST_ENDPOINT_METRICS = TEST_ENDPOINT_RESOURCE + "/metrics";

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webAppContext;


    @Before
    public void setUp() throws XmlProcessingException, IOException {
        mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext)
                .build();
    }

    @Test
    public void getMetrics() throws Exception {

        // when
        MvcResult result = mockMvc.perform(get(TEST_ENDPOINT_METRICS)
                        .with(httpBasic(TEST_PLUGIN_USERNAME, TEST_PLUGIN_PASSWORD))
                        .with(csrf()))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        // then
        String content = result.getResponse().getContentAsString();

        MatcherAssert.assertThat(content, CoreMatchers.allOf(
                containsString("metrics"),
                containsString("names"),
                containsString("gauges"),
                containsString("histograms"),
                containsString("counters"),
                containsString("timers"),
                containsString("meters")));
    }

}
