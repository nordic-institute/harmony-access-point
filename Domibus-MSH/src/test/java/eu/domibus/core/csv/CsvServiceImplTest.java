package eu.domibus.core.csv;

import eu.domibus.api.csv.CsvException;
import eu.domibus.api.exceptions.RequestValidationException;
import eu.domibus.api.message.MessageSubtype;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.core.message.MessageLogInfo;
import eu.domibus.core.plugin.notification.NotificationStatus;
import eu.domibus.ebms3.common.model.MessageType;
import mockit.Expectations;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_UI_CSV_MAX_ROWS;

/**
 * @author Tiago Miguel
 * @since 4.0
 */

@RunWith(JMockit.class)
public class CsvServiceImplTest {

    public static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.of(2020, 1, 1, 12, 59);
    @Injectable
    private DomibusPropertyProvider domibusPropertyProvider;

    @Tested
    CsvServiceImpl csvServiceImpl;

    public Object objectNull = null;

    @Test
    public void testExportToCsv_EmptyList() throws CsvException {
        // Given
        // When
        final String exportToCSV = csvServiceImpl.exportToCSV(new ArrayList<>(), null, null, null);

        // Then
        Assert.assertTrue(exportToCSV.trim().isEmpty());
    }

    @Test
    public void testExportToCsv_NullList() throws CsvException {
        // Given
        // When
        final String exportToCSV = csvServiceImpl.exportToCSV(null, null, null, null);

        // Then
        Assert.assertTrue(exportToCSV.trim().isEmpty());
    }

    @Test
    public void testExportToCsv() throws CsvException {
        testExportCsvBySubtype(null);
    }

    @Test
    public void testExportToCsvTest() throws CsvException {
        testExportCsvBySubtype(MessageSubtype.TEST);
    }

    private void testExportCsvBySubtype(MessageSubtype messageSubtype) {
        // Given
        Date date = new Date();
        List<MessageLogInfo> messageLogInfoList = getMessageList(date, messageSubtype);

        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss'GMT'Z");
        ZonedDateTime d = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        String csvDate = d.format(f);

        // When
        final String exportToCSV = csvServiceImpl.exportToCSV(messageLogInfoList, MessageLogInfo.class, null, null);

        // Then
        Assert.assertTrue(exportToCSV.contains("Message Id,From Party Id,To Party Id,Message Status,Notification Status,Received,Msh Role,Send Attempts,Send Attempts Max,Next Attempt,Conversation Id,Message Type,Message Subtype,Deleted,Original Sender,Final Recipient,Ref To Message Id,Failed,Restored"));
        Assert.assertTrue(exportToCSV.contains("messageId,fromPartyId,toPartyId,ACKNOWLEDGED,NOTIFIED," + csvDate + ",RECEIVING,1,5," + csvDate + ",conversationId,USER_MESSAGE," + (messageSubtype != null ? messageSubtype.name() : "") + "," + csvDate + ",originalSender,finalRecipient,refToMessageId," + csvDate + "," + csvDate));
    }

    private List<MessageLogInfo> getMessageList(Date date, MessageSubtype messageSubtype) {
        List<MessageLogInfo> result = new ArrayList<>();
        MessageLogInfo messageLog = new MessageLogInfo("messageId", MessageStatus.ACKNOWLEDGED,
                NotificationStatus.NOTIFIED, MSHRole.RECEIVING, MessageType.USER_MESSAGE, date, date, 1, 5, date,
                "conversationId", "fromPartyId", "toPartyId", "originalSender", "finalRecipient",
                "refToMessageId", date, date, messageSubtype, false, false);
        result.add(messageLog);
        return result;
    }


    @Test(expected = RequestValidationException.class)
    public void validateMaxRows() {
        new Expectations() {{
            domibusPropertyProvider.getIntegerProperty(DOMIBUS_UI_CSV_MAX_ROWS);
            result = 1000;
        }};

        csvServiceImpl.validateMaxRows(5000);

        new FullVerifications() {
        };

    }

    @Test
    public void testValidateMaxRowsWithCount() {
        long actualCount = 8000L;
        Supplier<Long> actualCountSupplier = () -> actualCount;
        new Expectations() {{
            domibusPropertyProvider.getIntegerProperty(DOMIBUS_UI_CSV_MAX_ROWS);
            result = 1000;
        }};

        csvServiceImpl.validateMaxRows(1000, actualCountSupplier);

        try {
            csvServiceImpl.validateMaxRows(5000, actualCountSupplier);
            Assert.fail("RequestValidationException not thrown");
        } catch (RequestValidationException ex) {
            Assert.assertTrue("Row count present in message", ex.getMessage().contains(actualCount + ""));
        }

        new FullVerifications() {
        };
    }

    @Test
    public void serializeFieldValue_null() throws NoSuchFieldException, IllegalAccessException {
        TestCsvFields o = new TestCsvFields();

        Field declaredField = TestCsvFields.class.getDeclaredField("nullField");

        String s = csvServiceImpl.serializeFieldValue(declaredField, o);

        Assert.assertEquals("", s);
    }

    @Test
    public void serializeFieldValue_Map() throws NoSuchFieldException, IllegalAccessException {
        TestCsvFields o = new TestCsvFields();
        HashMap<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        o.setMapField(map);

        Field declaredField = TestCsvFields.class.getDeclaredField("mapField");

        String s = csvServiceImpl.serializeFieldValue(declaredField, o);

        Assert.assertEquals("{\"key1\":\"value1\",\"key2\":\"value2\"}", s);

        new FullVerifications() {
        };
    }

    @Test
    public void serializeFieldValue_Date() throws NoSuchFieldException, IllegalAccessException {
        TestCsvFields o = new TestCsvFields();
        o.setDateField(Date.from(LOCAL_DATE_TIME.atZone(ZoneId.systemDefault()).toInstant()));

        Field declaredField = TestCsvFields.class.getDeclaredField("dateField");

        String s = csvServiceImpl.serializeFieldValue(declaredField, o);

        Assert.assertEquals("2020-01-01 12:59:00GMT+0100", s);

        new FullVerifications(){};
    }

    @Test
    public void serializeFieldValue_LocalDateTime() throws IllegalAccessException, NoSuchFieldException {
        TestCsvFields o = new TestCsvFields();
        o.setLocalDateTimeField(LOCAL_DATE_TIME);

        Field declaredField = TestCsvFields.class.getDeclaredField("localDateTimeField");

        String s = csvServiceImpl.serializeFieldValue(declaredField, o);

        Assert.assertEquals("2020-01-01 12:59:00GMT+0100", s);

        new FullVerifications(){};
    }

    @Test
    public void serializeFieldValue_Objects() throws IllegalAccessException, NoSuchFieldException {
        TestCsvFields o = new TestCsvFields();
        o.setStringField("TEST");

        Field declaredField = TestCsvFields.class.getDeclaredField("stringField");

        String s = csvServiceImpl.serializeFieldValue(declaredField, o);

        Assert.assertEquals("TEST", s);

        new FullVerifications(){};
    }

    @Test(expected = CsvException.class)
    public void createCSVContents() throws IllegalAccessException, NoSuchFieldException {
        Object o = new Object();
        Field declaredField = TestCsvFields.class.getDeclaredField("stringField");

        new Expectations(csvServiceImpl){{
           csvServiceImpl.serializeFieldValue(declaredField, o);
           result = new IllegalAccessException("TEST");
        }};

        csvServiceImpl.createCSVContents(Collections.singletonList(o),    null, Collections.singletonList(declaredField));

        new FullVerifications(){};
    }

    @Test
    public void getCsvFilename() {
        String test = csvServiceImpl.getCsvFilename("test");
        Assert.assertThat(test, CoreMatchers.containsString("test_datatable_"));
        Assert.assertThat(test, CoreMatchers.containsString(".csv"));
    }

    static class TestCsvFields {
        public Object nullField = null;
        public String stringField = null;
        public Map<String, String> mapField = null;
        public Date dateField = null;
        public LocalDateTime localDateTimeField = null;

        public void setStringField(String stringField) {
            this.stringField = stringField;
        }

        public void setMapField(Map<String, String> mapField) {
            this.mapField = mapField;
        }

        public void setDateField(Date dateField) {
            this.dateField = dateField;
        }

        public void setLocalDateTimeField(LocalDateTime localDateTimeField) {
            this.localDateTimeField = localDateTimeField;
        }
    }
}
