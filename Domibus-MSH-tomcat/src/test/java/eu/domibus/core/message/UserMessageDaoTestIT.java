package eu.domibus.core.message;

import eu.domibus.AbstractIT;
import eu.domibus.ITTestsService;
import eu.domibus.api.model.MessageProperty;
import eu.domibus.api.model.UserMessage;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.junit.Assert.assertNotNull;

public class UserMessageDaoTestIT extends AbstractIT {

    @Autowired
    UserMessageDao userMessageDao;
    @Autowired
    ITTestsService itTestsService;

    @Test
    public void testSaveUserMessage() {
        final UserMessage userMessage = itTestsService.getUserMessage();

        final UserMessage dbUserMessage = userMessageDao.findByEntityId(userMessage.getEntityId());
        assertNotNull(dbUserMessage);
        final Set<MessageProperty> msgProperties = dbUserMessage.getMessageProperties();
        msgProperties.forEach(messageProperty -> assertNotNull(messageProperty.getValue()));

        assertNotNull( userMessage.getPartyInfo().getFrom().getFromRole().getValue());
    }
}
