package eu.domibus.core.message;

import com.google.common.primitives.Longs;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.common.MessageDaoTestUtil;
import eu.domibus.test.AbstractIT;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.UUID;

public class UserMessageLogDefaultServiceIT extends AbstractIT {

    @Autowired
    UserMessageLogDefaultService userMessageLogDefaultService;

    @Autowired
    MessageDaoTestUtil messageDaoTestUtil;

    private UserMessageLog userMessageLog;

    @Before
    public void setUp() throws Exception {
        userMessageLog = messageDaoTestUtil.createUserMessageLog("UserMessageLogDefaultServiceIT-" + UUID.randomUUID(), new Date());
    }

    @After
    public void tearDown() throws Exception {
        messageDaoTestUtil.deleteMessages(Longs.asList(userMessageLog.getEntityId()));
    }

    @Test
    public void setWAITING_FOR_RECEIPT() {
        Assert.assertEquals(MessageStatus.RECEIVED, userMessageLogDefaultService.getMessageStatus(userMessageLog.getEntityId()));

        userMessageLogDefaultService.updateUserMessageStatus(userMessageLog.getUserMessage(), userMessageLog, MessageStatus.WAITING_FOR_RECEIPT);
        userMessageLogDefaultService.update(userMessageLog);

        Assert.assertEquals(MessageStatus.WAITING_FOR_RECEIPT, userMessageLogDefaultService.getMessageStatus(userMessageLog.getEntityId()));
    }

    @Test
    public void setFAILED() {
        Assert.assertEquals(MessageStatus.RECEIVED, userMessageLogDefaultService.getMessageStatus(userMessageLog.getEntityId()));

        userMessageLogDefaultService.updateUserMessageStatus(userMessageLog.getUserMessage(), userMessageLog, MessageStatus.SEND_FAILURE);
        userMessageLogDefaultService.update(userMessageLog);

        Assert.assertEquals(MessageStatus.SEND_FAILURE, userMessageLogDefaultService.getMessageStatus(userMessageLog.getEntityId()));
        UserMessageLog byId = userMessageLogDefaultService.findById(userMessageLog.getEntityId());
        Assert.assertNull(byId.getDeleted());
        Assert.assertNotNull(byId.getFailed());
    }
}
