package eu.domibus.core.message;


import eu.domibus.api.model.MessageStatus;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.messaging.XmlProcessingException;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
*
 * @author idragusa
 * @since 5.0
 */
@Ignore("EDELIVERY-8052 Failing tests must be ignored (FAILS ON BAMBOO)")
public class DeleteSentFailedMessageIT extends DeleteMessageAbstractIT {

    @Before
    public void updatePmodeForSendFailure() throws IOException, XmlProcessingException {
        Map<String, String> toReplace = new HashMap<>();
        toReplace.put("retry=\"12;4;CONSTANT\"", "retry=\"1;0;CONSTANT\"");
        uploadPmode(SERVICE_PORT, toReplace);
    }

    @Transactional
    @Test
    public void testDeleteFailedMessage() throws MessagingProcessingException, IOException {
        Map<String, Integer> initialMap = messageDBUtil.getTableCounts(tablesToExclude);
        sendMessageToDelete(MessageStatus.SEND_FAILURE);

        Map<String, Integer> beforeDeletionMap = messageDBUtil.getTableCounts(tablesToExclude);
        deleteMessages();

        Map<String, Integer> finalMap = messageDBUtil.getTableCounts(tablesToExclude);

        Assert.assertTrue(initialMap.size() > 0);
        Assert.assertTrue(beforeDeletionMap.size() > 0);
        Assert.assertTrue(CollectionUtils.isEqualCollection(initialMap.entrySet(), finalMap.entrySet()));
        Assert.assertFalse(CollectionUtils.isEqualCollection(initialMap.entrySet(), beforeDeletionMap.entrySet()));
    }

}
