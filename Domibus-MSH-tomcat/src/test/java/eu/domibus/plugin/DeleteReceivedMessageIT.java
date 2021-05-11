package eu.domibus.plugin;


import eu.domibus.messaging.XmlProcessingException;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import java.io.IOException;
import java.util.Map;

/**
 * @author idragusa
 * @since 5.0
 */
@DirtiesContext
@Rollback
public class DeleteReceivedMessageIT extends DeleteMessageIT {

    /**
     * Test to delete a received message
     */
    @Test
    public void testReceiveDeleteMessage() throws SOAPException, IOException, ParserConfigurationException, SAXException, XmlProcessingException {
        uploadPmode(wireMockRule.port());
        Map<String, Integer> initialMap = messageDBUtil.getTableCounts(tablesToExclude);
        receiveMessageToDelete();

        Map<String, Integer> beforeDeletionMap = messageDBUtil.getTableCounts(tablesToExclude);
        deleteMessages();

        Map<String, Integer> finalMap = messageDBUtil.getTableCounts(tablesToExclude);

        Assert.assertTrue(initialMap.size() > 0);
        Assert.assertTrue(beforeDeletionMap.size() > 0);
        Assert.assertTrue(CollectionUtils.isEqualCollection(initialMap.entrySet(), finalMap.entrySet()));
        Assert.assertFalse(CollectionUtils.isEqualCollection(initialMap.entrySet(), beforeDeletionMap.entrySet()));
    }
}