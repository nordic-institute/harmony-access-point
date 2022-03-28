package eu.domibus.ext.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.domibus.AbstractIT;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.common.MessageDaoTestUtil;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.security.AuthUtilsImpl;
import eu.domibus.messaging.XmlProcessingException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Catalin Enache
 * @since 4.2
 */
@Transactional
public class MessageAcknowledgementExtResourceIT extends AbstractIT {

    public static final String TEST_ENDPOINT_RESOURCE = "/ext/messages/acknowledgments";

    public static final String TEST_ENDPOINT_ACK = TEST_ENDPOINT_RESOURCE + "/{messageId}";

    private MockMvc mockMvc;

    public ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private WebApplicationContext webAppContext;

    @Autowired
    MessageDaoTestUtil messageDaoTestUtil;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected UserMessageLogDao userMessageLogDao;

    UserMessageLog uml1;

    @Configuration
    static class ContextConfiguration {
        @Bean
        public AuthUtils authUtils(DomibusPropertyProvider domibusPropertyProvider,
                                   DomibusConfigurationService domibusConfigurationService) {
            return new AuthUtilsImpl(domibusPropertyProvider, domibusConfigurationService);
        }
    }

    @Before
    public void setUp() throws XmlProcessingException, IOException {
        mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext)
                .build();
        // Do not use @Transactional on Class because it adds "false" transactions also to services.
        // Note here you can not use @Transactional annotation with the following code force commit on data preparation level!!
        Date currentDate = Calendar.getInstance().getTime();

        uml1 = messageDaoTestUtil.createUserMessageLog(UUID.randomUUID().toString(), currentDate, MessageStatus.SEND_FAILURE, MessageDaoTestUtil.DEFAULT_MPC);

        uploadPmode(SERVICE_PORT);
    }

    @Test
    public void geAck() throws Exception {
        // when
        MvcResult result = mockMvc.perform(get(TEST_ENDPOINT_ACK, uml1.getUserMessage().getMessageId())
                        .with(httpBasic(TEST_PLUGIN_USERNAME, TEST_PLUGIN_PASSWORD))
                        .with(csrf()))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        // then
        String content = result.getResponse().getContentAsString();
        List<?> resultList = objectMapper.readValue(content, List.class);
        Assert.assertEquals(0, resultList.size());
    }

    @Test
    public void getAck_notFound() throws Exception {
        // when
        mockMvc.perform(get(TEST_ENDPOINT_ACK, "notFound")
                        .with(httpBasic(TEST_PLUGIN_USERNAME, TEST_PLUGIN_PASSWORD))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    public void getAck_wrongUser() throws Exception {
        // when
        MvcResult result = mockMvc.perform(get(TEST_ENDPOINT_ACK, uml1.getUserMessage().getMessageId())
                        .with(httpBasic("user", TEST_PLUGIN_PASSWORD))
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        Exception resultList = objectMapper.readValue(content, Exception.class);
        Assert.assertEquals("You are not allowed to handle this message [" + uml1.getUserMessage().getMessageId() + "]. You are authorized as [urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1]", resultList.getMessage());
    }
}