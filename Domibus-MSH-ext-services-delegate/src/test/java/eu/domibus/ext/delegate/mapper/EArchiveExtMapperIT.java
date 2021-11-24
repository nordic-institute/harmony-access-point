package eu.domibus.ext.delegate.mapper;

import eu.domibus.api.earchive.EArchiveBatchRequestDTO;
import eu.domibus.api.earchive.EArchiveBatchStatus;
import eu.domibus.ext.domain.archive.BatchRequestType;
import eu.domibus.ext.domain.archive.ExportedBatchDTO;
import eu.domibus.ext.domain.archive.QueuedBatchDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Calendar;
import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Joze Rihtarsic
 * @since 5.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestMapperContextConfiguration.class)
public class EArchiveExtMapperIT {

    @Autowired
    private EArchiveExtMapper archiveExtMapper;

    @Test
    public void testArchiveBatchToQueuedBatch_Manual() {
        // given
        long datetime = 2108080L;
        EArchiveBatchRequestDTO toConvert = new EArchiveBatchRequestDTO();
        toConvert.setBatchId(UUID.randomUUID().toString());
        toConvert.setRequestType(BatchRequestType.MANUAL.name());
        toConvert.setMessages(Arrays.asList("messageId1", "messageId1"));
        toConvert.setTimestamp(Calendar.getInstance().getTime());
        toConvert.setMessageStartId(2108080 * 10000000000L + 1234L);
        toConvert.setMessageEndId(2108080 * 10000000000L + 12345L);
        // when
        final QueuedBatchDTO converted = archiveExtMapper.archiveBatchToQueuedBatch(toConvert);
        // then
        assertEquals(toConvert.getBatchId(), converted.getBatchId());
        assertArrayEquals(toConvert.getMessages().toArray(), converted.getMessages().toArray());
        assertEquals(toConvert.getRequestType(), converted.getRequestType().name());
        assertEquals(toConvert.getTimestamp(), converted.getEnqueuedTimestamp());
        // extract just dates
        assertEquals(datetime, converted.getMessageEndDate().longValue());
        assertEquals(datetime, converted.getMessageStartDate().longValue());
    }

    @Test
    public void testArchiveBatchToQueuedBatch_sanitizer() {
        // given
        long datetime = 2108080L;
        EArchiveBatchRequestDTO toConvert = new EArchiveBatchRequestDTO();
        toConvert.setBatchId(UUID.randomUUID().toString());
        toConvert.setRequestType("SANITIZER");
        toConvert.setMessages(Arrays.asList("messageId1", "messageId1"));
        toConvert.setTimestamp(Calendar.getInstance().getTime());
        toConvert.setMessageStartId(2108080 * 10000000000L + 1234L);
        toConvert.setMessageEndId(2108080 * 10000000000L + 12345L);
        // when
        final QueuedBatchDTO converted = archiveExtMapper.archiveBatchToQueuedBatch(toConvert);
        // then
        assertEquals(toConvert.getBatchId(), converted.getBatchId());
        assertArrayEquals(toConvert.getMessages().toArray(), converted.getMessages().toArray());
        assertEquals(BatchRequestType.CONTINUOUS, converted.getRequestType());
        assertEquals(toConvert.getTimestamp(), converted.getEnqueuedTimestamp());
        // extract just dates
        assertEquals(datetime, converted.getMessageEndDate().longValue());
        assertEquals(datetime, converted.getMessageStartDate().longValue());
    }

    @Test
    public void archiveBatchToExportBatch_continuous() {
        // given
        long datetime = 2108080L;
        EArchiveBatchRequestDTO toConvert = new EArchiveBatchRequestDTO();
        toConvert.setBatchId(UUID.randomUUID().toString());
        toConvert.setRequestType(BatchRequestType.CONTINUOUS.name());
        toConvert.setStatus(EArchiveBatchStatus.EXPORTED.name());
        toConvert.setMessages(Arrays.asList("messageId1", "messageId1"));
        toConvert.setTimestamp(Calendar.getInstance().getTime());
        toConvert.setMessageStartId(2108080 * 10000000000L + 1234L);
        toConvert.setMessageEndId(2108080 * 10000000000L + 12345L);
        // when
        final ExportedBatchDTO converted = archiveExtMapper.archiveBatchToExportBatch(toConvert);
        // then
        assertEquals(toConvert.getBatchId(), converted.getBatchId());
        assertArrayEquals(toConvert.getMessages().toArray(), converted.getMessages().toArray());
        assertEquals(toConvert.getRequestType(), converted.getRequestType().name());
        assertEquals(toConvert.getStatus(), converted.getStatus().name());
        assertEquals(toConvert.getTimestamp(), converted.getEnqueuedTimestamp());
        // extract just dates
        assertEquals(datetime, converted.getMessageEndDate().longValue());
        assertEquals(datetime, converted.getMessageStartDate().longValue());
    }

    @Test
    public void archiveBatchToExportBatch_sanitizer() {
        // given
        long datetime = 2108080L;
        EArchiveBatchRequestDTO toConvert = new EArchiveBatchRequestDTO();
        toConvert.setBatchId(UUID.randomUUID().toString());
        toConvert.setRequestType("SANITIZER");
        toConvert.setStatus(EArchiveBatchStatus.EXPORTED.name());
        toConvert.setMessages(Arrays.asList("messageId1", "messageId1"));
        toConvert.setTimestamp(Calendar.getInstance().getTime());
        toConvert.setMessageStartId(2108080 * 10000000000L + 1234L);
        toConvert.setMessageEndId(2108080 * 10000000000L + 12345L);
        // when
        final ExportedBatchDTO converted = archiveExtMapper.archiveBatchToExportBatch(toConvert);
        // then
        assertEquals(toConvert.getBatchId(), converted.getBatchId());
        assertArrayEquals(toConvert.getMessages().toArray(), converted.getMessages().toArray());
        assertEquals(BatchRequestType.CONTINUOUS, converted.getRequestType());
        assertEquals(toConvert.getStatus(), converted.getStatus().name());
        assertEquals(toConvert.getTimestamp(), converted.getEnqueuedTimestamp());
        // extract just dates
        assertEquals(datetime, converted.getMessageEndDate().longValue());
        assertEquals(datetime, converted.getMessageStartDate().longValue());
    }

}