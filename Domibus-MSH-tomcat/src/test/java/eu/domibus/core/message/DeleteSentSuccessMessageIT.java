package eu.domibus.core.message;


import eu.domibus.api.model.MessageStatus;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.plugin.BackendConnector;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author idragusa
 * @since 5.0
 */

@Transactional
@Ignore("EDELIVERY-8052 Failing tests must be ignored (FAILS ON BAMBOO) ")
public class DeleteSentSuccessMessageIT extends DeleteMessageIT {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DeleteSentSuccessMessageIT.class);

    @Before
    public void updatePmodeForAcknowledged() throws IOException, XmlProcessingException {
        Map<String, String> toReplace = new HashMap<>();
        toReplace.put("security=\"eDeliveryAS4Policy\"", "security=\"noSigNoEnc\"");
        uploadPmode(SERVICE_PORT, toReplace);
    }

    /**
     * Test to delete a sent success message
     */
    @Test
    public void testDeleteSentMessage()  throws MessagingProcessingException, IOException {
    BackendConnector backendConnector = Mockito.mock(BackendConnector.class);
        Mockito.when(backendConnectorProvider.getBackendConnector(Mockito.any(String.class))).thenReturn(backendConnector);

        LOG.trace("Get counters of the db tables, initial state");
        Map<String, Integer> initialMap = messageDBUtil.getTableCounts(tablesToExclude);
        sendMessageToDelete(MessageStatus.ACKNOWLEDGED);

        LOG.trace("Get counters of the db tables, after sending");
        Map<String, Integer> beforeDeletionMap = messageDBUtil.getTableCounts(tablesToExclude);
        deleteMessages();
        LOG.trace("Get counters of the db tables, after deletion");

        Map<String, Integer> finalMap = messageDBUtil.getTableCounts(tablesToExclude);

        Assert.assertTrue(initialMap.size() > 0);
        Assert.assertTrue(beforeDeletionMap.size() > 0);
        Assert.assertTrue(CollectionUtils.isEqualCollection(initialMap.entrySet(), finalMap.entrySet()));
        Assert.assertFalse(CollectionUtils.isEqualCollection(initialMap.entrySet(), beforeDeletionMap.entrySet()));
    }
}