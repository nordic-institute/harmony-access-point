package eu.domibus.ext.delegate.mapper;

import eu.domibus.api.earchive.EArchiveBatchRequestDTO;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.domain.archive.BatchRequestType;
import eu.domibus.ext.domain.archive.QueuedBatchDTO;
import eu.europa.ec.digit.commons.test.api.ObjectService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
    @Autowired
    private ObjectService objectService;


    @Test
    public void testArchiveBatchToQueuedBatch() {
        // given
        EArchiveBatchRequestDTO toConvert = new EArchiveBatchRequestDTO();
        toConvert.setBatchId(UUID.randomUUID().toString());
        toConvert.setRequestType(BatchRequestType.CONTINUOUS.name());
        // when
        final QueuedBatchDTO converted = archiveExtMapper.archiveBatchToQueuedBatch(toConvert);
        // then
        assertEquals(toConvert.getBatchId(), converted.getBatchId());
        assertArrayEquals(toConvert.getMessages().toArray(), converted.getMessages().toArray());
        assertEquals(toConvert.getRequestType(), converted.getRequestType().name());
        assertEquals(toConvert.getTimestamp(), converted.getEnqueuedTimestamp());
        // extract just dates
        assertEquals(toConvert.getMessageEndId(), converted.getMessageStartDate());
        assertEquals(toConvert.getMessageStartId(), converted.getMessageEndDate());
    }

}
