package eu.domibus.ext.rest;

import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.common.MessageDaoTestUtil;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.test.AbstractIT;
import eu.domibus.web.rest.MessageResource;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PULL_FORCE_BY_MPC;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PULL_MPC_INITIATOR_SEPARATOR;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author François Gautier
 * @since 5.1
 */
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class MessageResourceIT extends AbstractIT {

    public static final String TEST_ENDPOINT_RESOURCE = "/rest/message";
    public static final String TEST_ENDPOINT_RESTORE = TEST_ENDPOINT_RESOURCE+"/restore";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Autowired
    private MessageResource messageResource;

    @Autowired
    private MessageDaoTestUtil messageDaoTestUtil;

    private MockMvc mockMvc;

    private String pushedFailed;
    private String pulledFailed;
    private List<Long> userMessageCreated = new ArrayList<>();

    @Before
    public void setUp() throws XmlProcessingException, IOException {
        mockMvc = MockMvcBuilders.standaloneSetup(messageResource).build();

        pushedFailed = UUID.randomUUID().toString();
        pulledFailed = UUID.randomUUID().toString();
        UserMessageLog userMessageLog = messageDaoTestUtil.createUserMessageLog(pushedFailed, Calendar.getInstance().getTime(), MSHRole.SENDING, MessageStatus.SEND_FAILURE, true, MessageDaoTestUtil.DEFAULT_MPC, null);
        UserMessageLog userMessageLog1 = messageDaoTestUtil.createUserMessageLog(pulledFailed, Calendar.getInstance().getTime(), MSHRole.SENDING, MessageStatus.SEND_FAILURE, true, MessageDaoTestUtil.PULL_MPC+"/#/PARTYID", null);
        userMessageCreated.add(userMessageLog1.getEntityId());
        userMessageCreated.add(userMessageLog.getEntityId());
        uploadPmode(SERVICE_PORT);
    }

    @After
    public void tearDown() throws Exception {
//        messageDaoTestUtil.deleteMessages(userMessageCreated);
    }

    @Test
    public void getRestore_notFound() throws Exception {

        mockMvc.perform(put(TEST_ENDPOINT_RESTORE)
                        .param("messageId", "notFound"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void getMessageFilter_OK() throws Exception {
        mockMvc.perform(put(TEST_ENDPOINT_RESTORE)
                        .param("messageId", pushedFailed))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void getMessageFilter_Locked() throws Exception {

        domibusPropertyProvider.setProperty(DOMIBUS_PULL_FORCE_BY_MPC, "true");
        domibusPropertyProvider.setProperty(DOMIBUS_PULL_MPC_INITIATOR_SEPARATOR, "#");

        mockMvc.perform(put(TEST_ENDPOINT_RESTORE)
                        .param("messageId", pulledFailed))
                .andExpect(status().is2xxSuccessful());

        domibusPropertyProvider.setProperty(DOMIBUS_PULL_FORCE_BY_MPC, "false");
        domibusPropertyProvider.setProperty(DOMIBUS_PULL_MPC_INITIATOR_SEPARATOR, "");
    }

}
