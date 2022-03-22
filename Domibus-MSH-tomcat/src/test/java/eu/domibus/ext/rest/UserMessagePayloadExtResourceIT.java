package eu.domibus.ext.rest;


import com.fasterxml.jackson.databind.ObjectMapper;
import eu.domibus.AbstractIT;
import eu.domibus.api.model.PartInfo;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.api.payload.PartInfoService;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.common.MessageDaoTestUtil;
import eu.domibus.core.payload.persistence.DatabasePayloadPersistence;
import eu.domibus.core.spi.validation.UserMessageValidatorSpi;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class UserMessagePayloadExtResourceIT extends AbstractIT {

    public static final String TEST_ENDPOINT_DOWNLOAD_PAYLOAD = "/ext/messages/ids/{messageEntityId}/payloads/{cid}";

    @Autowired
    UserMessageValidatorSpi userMessageValidatorSpi;

    @Autowired
    UserMessagePayloadExtResource payloadExtResource;

    @Autowired
    MessageDaoTestUtil messageDaoTestUtil;

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webAppContext;

    @Autowired
    DatabasePayloadPersistence databasePayloadPersistence;

    @Autowired
    UserMessageService userMessageService;

    @Autowired
    PartInfoService partInfoService;

    public ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext)
                .build();
    }

    @Test
    public void testPayloadValidation_ok() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
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

    @Test
    @Transactional
    public void testDownloadPayload() throws Exception {
        // when
        String cid = "message";
        String content = "hello world";

        final UserMessageLog userMessageLog = messageDaoTestUtil.createUserMessageLog("myMessage", new Date());
        UserMessage userMessage = userMessageService.getByMessageEntityId(userMessageLog.getEntityId());

        PartInfo partInfo = new PartInfo();
        partInfo.setHref("cid:" + cid);
        partInfo.setBinaryData(content.getBytes(StandardCharsets.UTF_8));
        partInfo.setMime("application/text");
        partInfo.loadBinary();
        partInfoService.create(partInfo, userMessage);


        MvcResult result = mockMvc.perform(get(TEST_ENDPOINT_DOWNLOAD_PAYLOAD, userMessage.getEntityId(), cid)
                        .with(httpBasic(TEST_PLUGIN_USERNAME, TEST_PLUGIN_PASSWORD))
                )
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        // then
        String resultContent = result.getResponse().getContentAsString();
        Assert.assertEquals(content, resultContent);
    }

    @Test
    @Transactional
    public void testDownloadPayload_notAllowed() throws Exception {
        // when
        String cid = "message";
        String content = "hello world";

        final UserMessageLog userMessageLog = messageDaoTestUtil.createUserMessageLog("myMessage", new Date());
        UserMessage userMessage = userMessageService.getByMessageEntityId(userMessageLog.getEntityId());

        PartInfo partInfo = new PartInfo();
        partInfo.setHref("cid:" + cid);
        partInfo.setBinaryData(content.getBytes(StandardCharsets.UTF_8));
        partInfo.setMime("application/text");
        partInfo.loadBinary();
        partInfoService.create(partInfo, userMessage);


        MvcResult result = mockMvc.perform(get(TEST_ENDPOINT_DOWNLOAD_PAYLOAD, userMessage.getEntityId(), cid)
                        .with(httpBasic("user", TEST_PLUGIN_PASSWORD))
                )
                .andExpect(status().is4xxClientError())
                .andReturn();
        // then
        String contentResult = result.getResponse().getContentAsString();
        Exception resultList = objectMapper.readValue(contentResult, Exception.class);
        Assert.assertEquals("[DOM_001]:[DOM_002]:You are not allowed to access message [myMessage]. Reason: [You are not allowed to handle this message [myMessage]. You are authorized as [urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1]]", resultList.getMessage());
    }


}