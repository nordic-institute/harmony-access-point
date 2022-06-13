package eu.domibus.ext.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.domibus.AbstractIT;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.MessageDaoTestUtil;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.ext.domain.FailedMessagesCriteriaRO;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.XmlProcessingException;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The complete rest endpoint integration tests
 */
@Transactional
public class MessageMonitoringExtResourceIT extends AbstractIT {

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageMonitoringExtResourceIT.class);

    // The endpoints to test
    public static final String TEST_ENDPOINT_RESOURCE = "/ext/monitoring/messages";
    public static final String TEST_ENDPOINT_DELETE = TEST_ENDPOINT_RESOURCE + "/delete";
    public static final String TEST_ENDPOINT_DELETE_ID = TEST_ENDPOINT_RESOURCE + "/delete/{messageId}";

    public static final String TEST_ENDPOINT_FAILED = TEST_ENDPOINT_RESOURCE + "/failed";
    public static final String TEST_ENDPOINT_RESTORE = TEST_ENDPOINT_RESOURCE + "/failed/restore";
    public static final String TEST_ENDPOINT_ATTEMPTS = TEST_ENDPOINT_RESOURCE + "/{messageId}/attempts";

    public ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webAppContext;

    @Autowired
    MessageDaoTestUtil messageDaoTestUtil;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected UserMessageLogDao userMessageLogDao;

    UserMessageLog uml1;

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
    public void getAttempt_notFound() throws Exception {
        // when
        MvcResult result = mockMvc.perform(get(TEST_ENDPOINT_ATTEMPTS, uml1.getUserMessage().getMessageId())
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
    public void delete_ok() throws Exception {
        FailedMessagesCriteriaRO failedMessagesCriteriaRO = new FailedMessagesCriteriaRO();
        failedMessagesCriteriaRO.setFromDate(getDateFrom(uml1.getEntityId(), getHour(uml1.getEntityId())));
        failedMessagesCriteriaRO.setToDate(getDateFrom(uml1.getEntityId(), getHour(uml1.getEntityId()) + 1));
        // when
        MvcResult result = mockMvc.perform(delete(TEST_ENDPOINT_DELETE)
                        .with(httpBasic(TEST_PLUGIN_USERNAME, TEST_PLUGIN_PASSWORD))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(failedMessagesCriteriaRO)))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        // then
        String content = result.getResponse().getContentAsString();
        List<?> resultList = objectMapper.readValue(content, List.class);
        Assert.assertEquals(1, resultList.size());
        Assert.assertEquals(uml1.getUserMessage().getMessageId(), resultList.get(0));
    }

    @Test
    public void delete_toDateTooEarly() throws Exception {
        FailedMessagesCriteriaRO failedMessagesCriteriaRO = new FailedMessagesCriteriaRO();
        failedMessagesCriteriaRO.setFromDate(getDateFrom(uml1.getEntityId(), getHour(uml1.getEntityId())));
        failedMessagesCriteriaRO.setToDate("2000-01-01");
        // when
        mockMvc.perform(delete(TEST_ENDPOINT_DELETE)
                        .with(httpBasic(TEST_PLUGIN_USERNAME, TEST_PLUGIN_PASSWORD))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(failedMessagesCriteriaRO)))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }

    @Test
    public void delete_sameDate() throws Exception {
        FailedMessagesCriteriaRO failedMessagesCriteriaRO = new FailedMessagesCriteriaRO();
        failedMessagesCriteriaRO.setFromDate(getDateFrom(uml1.getEntityId(), getHour(uml1.getEntityId())));
        failedMessagesCriteriaRO.setToDate(getDateFrom(uml1.getEntityId(), getHour(uml1.getEntityId())));
        // when
        mockMvc.perform(delete(TEST_ENDPOINT_DELETE)
                        .with(httpBasic(TEST_PLUGIN_USERNAME, TEST_PLUGIN_PASSWORD))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(failedMessagesCriteriaRO)))
                .andExpect(status().is4xxClientError())
                .andReturn();

    }


    @Test
    public void restoreFailedMessages_ok() throws Exception {
        FailedMessagesCriteriaRO failedMessagesCriteriaRO = new FailedMessagesCriteriaRO();
        failedMessagesCriteriaRO.setFromDate(getDateFrom(uml1.getEntityId(), getHour(uml1.getEntityId())));
        failedMessagesCriteriaRO.setToDate(getDateFrom(uml1.getEntityId(), getHour(uml1.getEntityId()) + 1));
        // when
        MvcResult result = mockMvc.perform(post(TEST_ENDPOINT_RESTORE)
                        .with(httpBasic(TEST_PLUGIN_USERNAME, TEST_PLUGIN_PASSWORD))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(failedMessagesCriteriaRO)))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        // then
        String content = result.getResponse().getContentAsString();
        List<?> resultList = objectMapper.readValue(content, List.class);
        Assert.assertEquals(1, resultList.size());
        Assert.assertEquals(uml1.getUserMessage().getMessageId(), resultList.get(0));
    }

    @Test
    public void restoreFailedMessages_toDateTooEarly() throws Exception {
        FailedMessagesCriteriaRO failedMessagesCriteriaRO = new FailedMessagesCriteriaRO();
        failedMessagesCriteriaRO.setFromDate(getDateFrom(uml1.getEntityId(), getHour(uml1.getEntityId())));
        failedMessagesCriteriaRO.setToDate("2000-01-01");
        // when
        mockMvc.perform(post(TEST_ENDPOINT_RESTORE)
                        .with(httpBasic(TEST_PLUGIN_USERNAME, TEST_PLUGIN_PASSWORD))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(failedMessagesCriteriaRO)))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }

    @Test
    public void restoreFailedMessages_sameDate() throws Exception {
        FailedMessagesCriteriaRO failedMessagesCriteriaRO = new FailedMessagesCriteriaRO();
        failedMessagesCriteriaRO.setFromDate(getDateFrom(uml1.getEntityId(), getHour(uml1.getEntityId())));
        failedMessagesCriteriaRO.setToDate(getDateFrom(uml1.getEntityId(), getHour(uml1.getEntityId())));
        // when
        mockMvc.perform(post(TEST_ENDPOINT_RESTORE)
                        .with(httpBasic(TEST_PLUGIN_USERNAME, TEST_PLUGIN_PASSWORD))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(failedMessagesCriteriaRO)))
                .andExpect(status().is4xxClientError())
                .andReturn();

    }

    @Test
    public void failed_id_ok() throws Exception {

        // when
        MvcResult result = mockMvc.perform(delete(TEST_ENDPOINT_DELETE_ID, uml1.getUserMessage().getMessageId())
                        .with(httpBasic(TEST_PLUGIN_USERNAME, TEST_PLUGIN_PASSWORD))
                        .with(csrf()))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        // then
        UserMessageLog byMessageId = userMessageLogDao.findByMessageId(uml1.getUserMessage().getMessageId());
        Assert.assertNotNull(byMessageId.getDeleted());
    }

    @Test
    public void listFailedMessages_finalRecipient_id_ok() throws Exception {

        // when
        MvcResult result = mockMvc.perform(get(TEST_ENDPOINT_FAILED)
                        .param("finalRecipient", "finalRecipient2")
                        .with(httpBasic(TEST_PLUGIN_USERNAME, TEST_PLUGIN_PASSWORD))
                        .with(csrf()))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        // then
        String content = result.getResponse().getContentAsString();
        List<?> resultList = objectMapper.readValue(content, List.class);
        Assert.assertEquals(1, resultList.size());
        Assert.assertEquals(uml1.getUserMessage().getMessageId(), resultList.get(0));
    }

    @Test
    public void listFailedMessages_finalRecipient_id_nok() throws Exception {

        // when
        MvcResult result = mockMvc.perform(get(TEST_ENDPOINT_FAILED)
                        .param("finalRecipient", "finalRecipient3")
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
    public void listFailedMessages_id_ok() throws Exception {

        // when
        MvcResult result = mockMvc.perform(get(TEST_ENDPOINT_FAILED)
                        .with(httpBasic(TEST_PLUGIN_USERNAME, TEST_PLUGIN_PASSWORD))
                        .with(csrf()))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        // then
        String content = result.getResponse().getContentAsString();
        List<?> resultList = objectMapper.readValue(content, List.class);
        Assert.assertEquals(1, resultList.size());
        Assert.assertEquals(uml1.getUserMessage().getMessageId(), resultList.get(0));
    }

    @Test
    public void delete_id_notFound() throws Exception {

        // when
        MvcResult result = mockMvc.perform(delete(TEST_ENDPOINT_DELETE_ID, "notFound")
                        .with(httpBasic(TEST_PLUGIN_USERNAME, TEST_PLUGIN_PASSWORD))
                        .with(csrf()))
                .andExpect(status().is4xxClientError())
                .andReturn();
        // then
        String content = result.getResponse().getContentAsString();
        Assert.assertEquals("[DOM_009]:Message [notFound] does not exist", objectMapper.readValue(content, Exception.class).getMessage());
    }

    private String getDateFrom(long entityId, Long hour) {

        String year = "20" + StringUtils.substring("" + entityId, 0, 2);
        String month = StringUtils.substring("" + entityId, 2, 4);
        String day = StringUtils.substring("" + entityId, 4, 6);
        return String.format("%s-%s-%sT%02dH", year, month, day, hour);
    }

    private Long getHour(long entityId) {
        return Long.parseLong(StringUtils.substring("" + entityId, 6, 8));
    }

    public String asJsonString(final Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
