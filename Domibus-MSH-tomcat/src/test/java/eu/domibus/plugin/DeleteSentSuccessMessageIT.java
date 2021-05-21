/*
package eu.domibus.plugin;


import eu.domibus.api.model.MessageStatus;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.plugin.ws.generated.SubmitMessageFault;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

*/
/**
 * @author idragusa
 * @since 5.0
 *//*

//@DirtiesContext
//@Rollback
@Ignore
public class DeleteSentSuccessMessageIT extends DeleteMessageIT {

    @Before
    public void updatePmodeForAcknowledged() throws IOException, XmlProcessingException {
        Map<String, String> toReplace = new HashMap<>();
        toReplace.put("security=\"eDeliveryAS4Policy\"", "security=\"noSigNoEnc\"");
        uploadPmode(wireMockRule.port(), toReplace);
    }

    */
/**
     * Test to delete a sent success message
     *//*

    @Test
    public void testDeleteSentMessage() throws SubmitMessageFault {
        Map<String, Integer> initialMap = messageDBUtil.getTableCounts(tablesToExclude);
        sendMessageToDelete(MessageStatus.ACKNOWLEDGED);

        Map<String, Integer> beforeDeletionMap = messageDBUtil.getTableCounts(tablesToExclude);
        deleteMessages();

        Map<String, Integer> finalMap = messageDBUtil.getTableCounts(tablesToExclude);

        Assert.assertTrue(initialMap.size() > 0);
        Assert.assertTrue(beforeDeletionMap.size() > 0);
        Assert.assertTrue(CollectionUtils.isEqualCollection(initialMap.entrySet(), finalMap.entrySet()));
        Assert.assertFalse(CollectionUtils.isEqualCollection(initialMap.entrySet(), beforeDeletionMap.entrySet()));
    }
}*/
