package eu.domibus.ext.delegate.services.message;

import eu.domibus.api.util.DomibusStringUtil;
import mockit.Injectable;
import mockit.Tested;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Sebastian-Ion TINCU
 */
public class MessageServiceImplTest {

    @Injectable
    private DomibusStringUtil domibusStringUtil;

    private MessageServiceImpl messageService = new MessageServiceImpl(domibusStringUtil);

    @Test
    public void trimsTheMessageIdWhenCleaningIt() {
        String messageId = " \n\t -Dom137--  \t\n ";

        String trimmedMessageId = messageService.cleanMessageIdentifier(messageId);

        Assert.assertEquals("Should have trimmed control characters at the beginning and the end of the message identifier when cleaning it",
                "-Dom137--", trimmedMessageId);
    }

    @Test
    public void doesNothingToTheMessageIdMissingControlCharactersWhenCleaningIt() {
        String messageId = "-Dom138--";

        String trimmedMessageId = messageService.cleanMessageIdentifier(messageId);

        Assert.assertEquals("Should have returned the message as is when cleaning it if the message does not contain control characters",
                "-Dom138--", trimmedMessageId);
    }

}