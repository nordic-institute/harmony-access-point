package eu.domibus.ext.rest;


import eu.domibus.AbstractIT;
import eu.domibus.core.spi.validation.UserMessageValidatorSpi;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static eu.domibus.ext.rest.DomibusEArchiveExtResourceIT.TEST_PLUGIN_PASSWORD;
import static eu.domibus.ext.rest.DomibusEArchiveExtResourceIT.TEST_PLUGIN_USERNAME;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserMessagePayloadExtResourceIT extends AbstractIT {

    @Autowired
    UserMessageValidatorSpi userMessageValidatorSpi;

    @Autowired
    UserMessagePayloadExtResource payloadExtResource;

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webAppContext;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext)
                .build();
    }

    @Test
    public void testPayloadValidation_ok() throws Exception {

        MockMultipartFile file
                = new MockMultipartFile(
                "file",
                "hello.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );

        mockMvc.perform(multipart("/ext/messages/payloads/validation")
                        .file(file)
                        .with(httpBasic(TEST_PLUGIN_USERNAME, TEST_PLUGIN_PASSWORD))
                        .with(csrf())
                )
                .andExpect(status().is2xxSuccessful())
                .andReturn();
    }


}