package eu.domibus.core.message.retention;

import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.api.routing.BackendFilter;
import eu.domibus.core.message.DeleteMessageAbstractIT;
import eu.domibus.core.message.MessageStatusDao;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.plugin.routing.RoutingService;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.plugin.BackendConnector;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;

import javax.annotation.PostConstruct;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Transactional
public class MessageRetentionDefaultServiceIT extends DeleteMessageAbstractIT {

    public static final String MPC_URI = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/defaultMPC";
    @Autowired
    private RoutingService routingService;
    @Autowired
    private UserMessageLogDao userMessageLogDao;
    @Autowired
    MessageStatusDao messageStatusDao;
    @Autowired
    private MessageRetentionDefaultService service;

    @PostConstruct
    public void setupInfrastructure(){
        BackendConnector backendConnector = Mockito.mock(BackendConnector.class);
        Mockito.when(backendConnectorProvider.getBackendConnector(Mockito.any(String.class))).thenReturn(backendConnector);
        BackendFilter backendFilter = Mockito.mock(BackendFilter.class);
        Mockito.when(routingService.getMatchingBackendFilter(Mockito.any(UserMessage.class))).thenReturn(backendFilter);
    }

    @Before
    public void initTest(){
        deleteAllMessages();
    }

    @After
    public void cleanup(){
        deleteAllMessages();
    }

    @Test
    public void deleteExpiredNotDownloaded_deletesAll_ifIsDeleteMessageMetadataAndZeroOffset() throws XmlProcessingException, IOException, SOAPException, ParserConfigurationException, SAXException {
        //given
        uploadPmodeWithCustomMpc(true, 0, 2, 200);
        Map<String, Integer> initialMap = messageDBUtil.getTableCounts(tablesToExclude);
        String messageId = receiveMessageToDelete();
        makeMessageFieldOlder(messageId, "received", 10);
        //when
        service.deleteExpiredNotDownloadedMessages(MPC_URI, 100, false);
        //then
        Map<String, Integer> finalMap = messageDBUtil.getTableCounts(tablesToExclude);
        Assert.assertTrue("Expecting all data to be deleted but instead we have:\n" + getMessageDetails(initialMap, finalMap),
                CollectionUtils.isEqualCollection(initialMap.entrySet(), finalMap.entrySet()));
    }

    @Test
    public void deleteExpiredDownloaded_deletesAll_ifIsDeleteMessageMetadataAndZeroOffset() throws XmlProcessingException, IOException, SOAPException, ParserConfigurationException, SAXException {
        //given
        uploadPmodeWithCustomMpc(true, 0, 0, 2);
        Map<String, Integer> initialMap = messageDBUtil.getTableCounts(tablesToExclude);
        String messageId = receiveMessageToDelete();
        setMessageStatus(messageId, MessageStatus.DOWNLOADED);
        makeMessageFieldOlder(messageId, "downloaded", 10);
        //when
        service.deleteExpiredDownloadedMessages(MPC_URI, 100, false);
        //then
        Map<String, Integer> finalMap = messageDBUtil.getTableCounts(tablesToExclude);
        Assert.assertTrue("Expecting all data to be deleted but instead we have:\n" + getMessageDetails(initialMap, finalMap),
                CollectionUtils.isEqualCollection(initialMap.entrySet(), finalMap.entrySet()));
    }

    private static String getMessageDetails(Map<String, Integer> initialMap, Map<String, Integer> finalMap) {
        return initialMap.entrySet().stream()
                .filter(mapEntry -> !Objects.equals(mapEntry.getValue(), finalMap.get(mapEntry.getKey())))
                .map(mapEntry -> String.format("table '%s' had %d rows and now it has %d", mapEntry.getKey(), mapEntry.getValue(), finalMap.get(mapEntry.getKey())))
                .collect(Collectors.joining("\n"));
    }

    private void makeMessageFieldOlder(String messageId, String field, int nrMinutesBack) {
        Date date = DateUtils.addMinutes(new Date(), nrMinutesBack  * -1);
        em.createQuery("update UserMessageLog set " + field + "=:DATE where userMessage.entityId in (select entityId from UserMessage where messageId=:MESSAGE_ID)")
                .setParameter("MESSAGE_ID", messageId)
                .setParameter("DATE", date)
                .executeUpdate();
    }

    private void setMessageStatus(String messageId, MessageStatus status) {
        UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId);
        userMessageLog.setMessageStatus(messageStatusDao.findOrCreate(status));
        userMessageLogDao.update(userMessageLog);
    }

    private void uploadPmodeWithCustomMpc(boolean isDeleteMessageMetadata, int metadataOffset, int undownloaded, int downloaded) throws IOException, XmlProcessingException {
        Map<String, String> toReplace = new HashMap<>();
        toReplace.put("<mpc [^>]*>",
                "<mpc name=\"defaultMpc\"" +
                        " qualifiedName=\"" + MPC_URI + "\"" +
                        " enabled=\"true\"" +
                        " default=\"true\"" +
                        " retention_downloaded=\"" + downloaded + "\"" +
                        " retention_undownloaded=\"" + undownloaded + "\"" +
                        " delete_message_metadata=\"" + isDeleteMessageMetadata + "\"" +
                        " retention_metadata_offset=\"" + metadataOffset + "\"" +
                        "/>");
        uploadPmode(SERVICE_PORT, toReplace);
    }

}
