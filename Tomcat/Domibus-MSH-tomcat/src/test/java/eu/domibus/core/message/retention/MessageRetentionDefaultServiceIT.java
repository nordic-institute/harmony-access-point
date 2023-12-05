package eu.domibus.core.message.retention;

import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.PartInfo;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.common.MessageStatusChangeEvent;
import eu.domibus.common.NotificationType;
import eu.domibus.core.message.DeleteMessageAbstractIT;
import eu.domibus.core.message.MessageStatusDao;
import eu.domibus.core.message.PartInfoDao;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.plugin.BackendConnectorHelper;
import eu.domibus.core.plugin.notification.AsyncNotificationConfigurationService;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.plugin.BackendConnector;
import eu.domibus.plugin.notification.AsyncNotificationConfiguration;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;

import javax.annotation.PostConstruct;
import javax.jms.Queue;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Integer.MAX_VALUE;
import static org.junit.Assert.*;

@Transactional
public class MessageRetentionDefaultServiceIT extends DeleteMessageAbstractIT {

    public static final String MPC_URI = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/defaultMPC";

    @Autowired
    private UserMessageLogDao userMessageLogDao;

    @Autowired
    private PartInfoDao partInfoDao;

    @Autowired
    private MessageStatusDao messageStatusDao;

    @Autowired
    private MessageRetentionDefaultService service;

    @Autowired
    BackendConnectorHelper backendConnectorHelper;

    ArgumentCaptor<MessageStatusChangeEvent> argCaptor = ArgumentCaptor.forClass(MessageStatusChangeEvent.class);

    BackendConnector backendConnector = Mockito.mock(BackendConnector.class);

    @PostConstruct
    public void setupInfrastructure() {
        Mockito.when(backendConnectorProvider.getBackendConnector(Mockito.any(String.class))).thenReturn(backendConnector);
    }


    @Test
    public void deleteExpiredNotDownloaded_deletesAll_ifIsDeleteMessageMetadataAndZeroOffset() throws XmlProcessingException, IOException, SOAPException, ParserConfigurationException, SAXException {
        //given
        uploadPmodeWithCustomMpc(true, 0, 2, MAX_VALUE, MAX_VALUE);
        Map<String, Integer> initialMap = messageDBUtil.getTableCounts(tablesToExclude);
        String messageId = receiveMessageToDelete();
        makeMessageFieldOlder(messageId, "received", 10);
        //when
        service.deleteExpiredNotDownloadedMessages(MPC_URI, 100, false);
        //then
        Map<String, Integer> finalMap = messageDBUtil.getTableCounts(tablesToExclude);
        assertTrue("Expecting all data to be deleted but instead we have:\n" + getMessageDetails(initialMap, finalMap),
                CollectionUtils.isEqualCollection(initialMap.entrySet(), finalMap.entrySet()));
    }

    @Test
    public void deleteExpiredNotDownloaded_deletesOnlyPayload_ifIsDeleteMessageMetadataAndNotZeroOffset() throws XmlProcessingException, IOException, SOAPException, ParserConfigurationException, SAXException {
        uploadPmodeWithCustomMpc(true, MAX_VALUE, 2, MAX_VALUE, MAX_VALUE);
        Map<String, Integer> initialMap = messageDBUtil.getTableCounts(tablesToExclude);
        String messageId = receiveMessageToDelete();
        makeMessageFieldOlder(messageId, "received", 10);
        //when
        service.deleteExpiredNotDownloadedMessages(MPC_URI, 100, false);
        //then
        Map<String, Integer> finalMap = messageDBUtil.getTableCounts(tablesToExclude);
        assertFalse("Expecting the metadata to not be deleted but instead all data has been removed",
                CollectionUtils.isEqualCollection(initialMap.entrySet(), finalMap.entrySet()));
        assertMedatadaNotDeleted(initialMap, finalMap);
        assertPayloadDeleted(messageId);
    }

    private static void assertMedatadaNotDeleted(Map<String, Integer> initialMap, Map<String, Integer> finalMap) {
        assertDataNotDeletedInTable("tb_signal_message_log", 1, initialMap, finalMap);
        assertDataNotDeletedInTable("tb_user_message", 1, initialMap, finalMap);
        assertDataNotDeletedInTable("tb_part_info", 1, initialMap, finalMap);
        assertDataNotDeletedInTable("tb_user_message_raw", 1, initialMap, finalMap);
        assertDataNotDeletedInTable("tb_user_message_log", 1, initialMap, finalMap);
        assertDataNotDeletedInTable("tb_part_properties", 2, initialMap, finalMap);
        assertDataNotDeletedInTable("tb_message_properties", 2, initialMap, finalMap);
        assertDataNotDeletedInTable("tb_signal_message", 1, initialMap, finalMap);
        assertDataNotDeletedInTable("tb_receipt", 1, initialMap, finalMap);
    }

    private void assertPayloadDeleted(String messageId) {
        UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId);
        em.refresh(userMessageLog);
        assertEquals("Expecting change of status of message", MessageStatus.DELETED, userMessageLog.getMessageStatus());
        long entityId = userMessageLog.getEntityId();
        List<PartInfo> partInfoList = partInfoDao.findPartInfoByUserMessageEntityId(entityId);
        partInfoList.forEach(partInfo -> {
            em.refresh(partInfo);
            assertNull("Expecting payload to be deleted", partInfo.getBinaryData());
        });
    }

    private static void assertDataNotDeletedInTable(String table, int expectedRecordCount, Map<String, Integer> initialMap, Map<String, Integer> finalMap) {
        assertEquals("Expecting data to not be deleted from " + table,
                initialMap.getOrDefault(table, 0) + expectedRecordCount, (int) finalMap.getOrDefault(table, 0));
    }

    @Test
    public void deleteExpiredDownloaded_deletesAll_ifIsDeleteMessageMetadataAndZeroOffset() throws XmlProcessingException, IOException, SOAPException, ParserConfigurationException, SAXException {
        //given
        uploadPmodeWithCustomMpc(true, 0, MAX_VALUE, 2, MAX_VALUE);
        Map<String, Integer> initialMap = messageDBUtil.getTableCounts(tablesToExclude);
        String messageId = receiveMessageToDelete();
        setMessageStatus(messageId, MessageStatus.DOWNLOADED);
        makeMessageFieldOlder(messageId, "downloaded", 10);
        //when
        service.deleteExpiredDownloadedMessages(MPC_URI, 100, false);
        //then
        Map<String, Integer> finalMap = messageDBUtil.getTableCounts(tablesToExclude);
        assertTrue("Expecting all data to be deleted but instead we have:\n" + getMessageDetails(initialMap, finalMap),
                CollectionUtils.isEqualCollection(initialMap.entrySet(), finalMap.entrySet()));
    }

    @Test
    public void deleteExpiredDownloaded_deletesOnlyPayload_ifIsDeleteMessageMetadataAndNotZeroOffset() throws XmlProcessingException, IOException, SOAPException, ParserConfigurationException, SAXException {
        //given
        uploadPmodeWithCustomMpc(true, MAX_VALUE, MAX_VALUE, 2, MAX_VALUE);
        Map<String, Integer> initialMap = messageDBUtil.getTableCounts(tablesToExclude);
        String messageId = receiveMessageToDelete();
        setMessageStatus(messageId, MessageStatus.DOWNLOADED);
        makeMessageFieldOlder(messageId, "downloaded", 10);

        Mockito.when(backendConnectorHelper.getRequiredNotificationTypeList(backendConnector)).thenReturn(Arrays.asList(NotificationType.MESSAGE_STATUS_CHANGE));

        //when
        service.deleteExpiredDownloadedMessages(MPC_URI, 100, false);
        //then
        Map<String, Integer> finalMap = messageDBUtil.getTableCounts(tablesToExclude);
        assertFalse("Expecting the metadata to not be deleted but instead all data has been removed",
                CollectionUtils.isEqualCollection(initialMap.entrySet(), finalMap.entrySet()));
        assertMedatadaNotDeleted(initialMap, finalMap);
        assertPayloadDeleted(messageId);

        Mockito.verify(backendConnector, Mockito.times(1)).messageStatusChanged(argCaptor.capture());
        MessageStatusChangeEvent event = argCaptor.getValue();
        assertEquals(eu.domibus.common.MessageStatus.DOWNLOADED, event.getFromStatus());
        assertEquals(eu.domibus.common.MessageStatus.DELETED, event.getToStatus());
        assertEquals(messageId, event.getMessageId());
    }


    @Test
    public void deleteExpiredSent_deletesAll_ifIsDeleteMessageMetadataAndZeroOffset() throws XmlProcessingException, IOException, SOAPException, ParserConfigurationException, SAXException {
        //given
        uploadPmodeWithCustomMpc(true, 0, MAX_VALUE, MAX_VALUE, 2);
        Map<String, Integer> initialMap = messageDBUtil.getTableCounts(tablesToExclude);
        String messageId = receiveMessageToDelete();
        setMessageStatus(messageId, MessageStatus.ACKNOWLEDGED);
        makeMessageFieldOlder(messageId, "modificationTime", 10);
        //when
        service.deleteExpiredSentMessages(MPC_URI, 100, false);
        //then
        Map<String, Integer> finalMap = messageDBUtil.getTableCounts(tablesToExclude);
        assertTrue("Expecting all data to be deleted but instead we have:\n" + getMessageDetails(initialMap, finalMap),
                CollectionUtils.isEqualCollection(initialMap.entrySet(), finalMap.entrySet()));
    }

    @Test
    public void deleteExpiredSent_deletesOnlyPayload_ifIsDeleteMessageMetadataAndNotZeroOffset() throws XmlProcessingException, IOException, SOAPException, ParserConfigurationException, SAXException {
        //given
        uploadPmodeWithCustomMpc(true, MAX_VALUE, MAX_VALUE, MAX_VALUE, 2);
        Map<String, Integer> initialMap = messageDBUtil.getTableCounts(tablesToExclude);
        String messageId = receiveMessageToDelete();
        setMessageStatus(messageId, MessageStatus.ACKNOWLEDGED);
        makeMessageFieldOlder(messageId, "modificationTime", 10);
        //when
        service.deleteExpiredSentMessages(MPC_URI, 100, false);
        //then
        Map<String, Integer> finalMap = messageDBUtil.getTableCounts(tablesToExclude);
        assertFalse("Expecting the metadata to not be deleted but instead all data has been removed",
                CollectionUtils.isEqualCollection(initialMap.entrySet(), finalMap.entrySet()));
        assertMedatadaNotDeleted(initialMap, finalMap);
        assertPayloadDeleted(messageId);
    }

    @Test
    public void deleteExpiredPayloadDeleted_deletesAll_ifIsDeleteMessageMetadata() throws XmlProcessingException, IOException, SOAPException, ParserConfigurationException, SAXException {
        //given
        uploadPmodeWithCustomMpc(true, 0, MAX_VALUE, MAX_VALUE, 2);
        Map<String, Integer> initialMap = messageDBUtil.getTableCounts(tablesToExclude);
        String messageId = receiveMessageToDelete();
        setMessageStatus(messageId, MessageStatus.DELETED);
        makeMessageFieldOlder(messageId, "deleted", 10);
        //when
        service.deleteExpiredPayloadDeletedMessages(MPC_URI, 100, false);
        //then
        Map<String, Integer> finalMap = messageDBUtil.getTableCounts(tablesToExclude);
        assertTrue("Expecting all data to be deleted but instead we have:\n" + getMessageDetails(initialMap, finalMap),
                CollectionUtils.isEqualCollection(initialMap.entrySet(), finalMap.entrySet()));
    }

    private static String getMessageDetails(Map<String, Integer> initialMap, Map<String, Integer> finalMap) {
        return initialMap.entrySet().stream()
                .filter(mapEntry -> !Objects.equals(mapEntry.getValue(), finalMap.get(mapEntry.getKey())))
                .map(mapEntry -> String.format("table '%s' had %d rows and now it has %d", mapEntry.getKey(), mapEntry.getValue(), finalMap.get(mapEntry.getKey())))
                .collect(Collectors.joining("\n"));
    }

    private void makeMessageFieldOlder(String messageId, String field, int nrMinutesBack) {
        Date date = DateUtils.addMinutes(new Date(), nrMinutesBack * -1);
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

    private void uploadPmodeWithCustomMpc(boolean isDeleteMessageMetadata, int metadataOffset, int undownloaded, int downloaded, int sent) throws IOException, XmlProcessingException {
        Map<String, String> toReplace = new HashMap<>();
        toReplace.put("MPC_PLACEHOLDER",
                "<mpc name=\"defaultMpc\"" +
                        " qualifiedName=\"" + MPC_URI + "\"" +
                        " enabled=\"true\"" +
                        " default=\"true\"" +
                        " retention_downloaded=\"" + downloaded + "\"" +
                        " retention_undownloaded=\"" + undownloaded + "\"" +
                        " retention_sent=\"" + sent + "\"" +
                        " delete_message_metadata=\"" + isDeleteMessageMetadata + "\"" +
                        " retention_metadata_offset=\"" + metadataOffset + "\"" +
                        "/>");
        uploadPmode(SERVICE_PORT, "dataset/pmode/PMode_custom_mpc.xml", toReplace);
    }

}
