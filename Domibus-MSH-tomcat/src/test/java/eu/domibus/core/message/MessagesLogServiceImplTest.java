package eu.domibus.core.message;

import eu.domibus.AbstractIT;
import eu.domibus.api.model.MessageType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.HashMap;

public class MessagesLogServiceImplTest extends AbstractIT {

    @Autowired
    MessagesLogServiceImpl messagesLogService;

    @Test
    public void countAndFindPaged() {
        final HashMap<String, Object> filters = new HashMap<>();
        filters.put("receivedTo", new Date());
        messagesLogService.countAndFindPaged(MessageType.USER_MESSAGE, 0, 10, "received", false, filters);
    }
}
