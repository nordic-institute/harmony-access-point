package eu.domibus.ext.rest;

import eu.domibus.AbstractIT;
import eu.domibus.core.plugin.routing.dao.BackendFilterDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.MessageFilterResource;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author François Gautier
 * @since 5.0
 */
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class MessageFilterResourceIT extends AbstractIT {

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageFilterResourceIT.class);
    public static final String TEST_ENDPOINT_RESOURCE = "/rest/messagefilters";

    public static final String NOT_FOUND = "not_found";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Autowired
    private MessageFilterResource messageFilterResource;

    @Autowired
    private BackendFilterDao backendFilterDao;

    private MockMvc mockMvc;


    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(messageFilterResource).build();

    }

    @After
    public void tearDown() throws Exception {

    }


    @Test
    public void getMessageFilter() throws Exception {

        mockMvc.perform(get(TEST_ENDPOINT_RESOURCE))
                .andExpect(status().is2xxSuccessful())
                .andExpect((jsonPath("$.messageFilterEntries", Matchers.hasSize(2))));

    }

}