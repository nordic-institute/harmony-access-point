package eu.domibus.core.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.domibus.api.earchive.EArchiveBatchRequestDTO;
import eu.domibus.api.earchive.EArchiveBatchStatus;
import eu.domibus.api.model.ListUserMessageDto;
import eu.domibus.api.model.UserMessageDTO;
import eu.domibus.core.earchive.EArchiveBatchEntity;
import eu.domibus.core.earchive.EArchiveBatchSummaryEntity;
import eu.domibus.core.earchive.EArchiveBatchUtils;
import eu.domibus.core.util.JsonFormatterConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Calendar;
import java.util.UUID;
import java.util.stream.Collectors;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        classes = {EArchiveBatchMapperImpl.class, JsonFormatterConfiguration.class, EArchiveBatchUtils.class}
)
public class EArchiveBatchMapperTest {


    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    EArchiveBatchMapper testInstance;

    @Test
    public void testEArchiveBatchSummaryEntityToDto() {
        // given
        EArchiveBatchSummaryEntity testEntity = new EArchiveBatchSummaryEntity();
        testEntity.setBatchId(UUID.randomUUID().toString());
        testEntity.setBatchSize(1123);
        testEntity.setDateRequested(Calendar.getInstance().getTime());
        testEntity.seteArchiveBatchStatus(EArchiveBatchStatus.EXPORTED);
        testEntity.setErrorCode("DOM10");
        testEntity.setErrorMessage("Error message: " + UUID.randomUUID().toString());
        testEntity.setFirstPkUserMessage(10l);
        testEntity.setLastPkUserMessage(20l);
        testEntity.setStorageLocation("/test");
        //when
        EArchiveBatchRequestDTO result = testInstance.eArchiveBatchRequestEntityToDto(testEntity);
        // then
        Assert.assertNotNull(result);
        Assert.assertEquals(testEntity.getBatchId(), result.getBatchId());
        Assert.assertEquals(testEntity.getDateRequested(), result.getTimestamp());
        Assert.assertEquals(testEntity.getEArchiveBatchStatus().name(), result.getStatus());
        Assert.assertEquals(testEntity.getErrorCode(), result.getErrorCode());
        Assert.assertEquals(testEntity.getErrorMessage(), result.getErrorDescription());
        Assert.assertEquals(testEntity.getFirstPkUserMessage(), result.getMessageStartId());
        Assert.assertEquals(testEntity.getLastPkUserMessage(), result.getMessageEndId());

        Assert.assertTrue(result.getMessages().isEmpty());
    }

    @Test
    public void testEArchiveBatchEntityToDto() throws JsonProcessingException {

        // given
        ListUserMessageDto listUserMessageDto = new ListUserMessageDto(Arrays.asList(new UserMessageDTO(1, UUID.randomUUID().toString()),
                new UserMessageDTO(2, UUID.randomUUID().toString()),
                new UserMessageDTO(3, UUID.randomUUID().toString())
        ));
        EArchiveBatchEntity testEntity = new EArchiveBatchEntity();
        testEntity.setBatchId(UUID.randomUUID().toString());
        testEntity.setBatchSize(1123);
        testEntity.setDateRequested(Calendar.getInstance().getTime());
        testEntity.seteArchiveBatchStatus(EArchiveBatchStatus.EXPORTED);
        testEntity.setErrorCode("DOM10");
        testEntity.setErrorMessage("Error message: " + UUID.randomUUID().toString());
        testEntity.setFirstPkUserMessage(10l);
        testEntity.setLastPkUserMessage(20l);
        testEntity.setStorageLocation("/test");
        testEntity.setMessageIdsJson(objectMapper.writeValueAsString(listUserMessageDto));
        // when
        EArchiveBatchRequestDTO result = testInstance.eArchiveBatchRequestEntityToDto(testEntity);
        // then
        Assert.assertNotNull(result);
        Assert.assertEquals(testEntity.getBatchId(), result.getBatchId());
        Assert.assertEquals(testEntity.getDateRequested(), result.getTimestamp());
        Assert.assertEquals(testEntity.getEArchiveBatchStatus().name(), result.getStatus());
        Assert.assertEquals(testEntity.getErrorCode(), result.getErrorCode());
        Assert.assertEquals(testEntity.getErrorMessage(), result.getErrorDescription());
        Assert.assertEquals(testEntity.getFirstPkUserMessage(), result.getMessageStartId());
        Assert.assertEquals(testEntity.getLastPkUserMessage(), result.getMessageEndId());
        // test list messages
        Assert.assertEquals(listUserMessageDto.getUserMessageDtos().size(), result.getMessages().size());
        Assert.assertArrayEquals(
                listUserMessageDto.getUserMessageDtos().stream().map(userMessageDTO -> userMessageDTO.getMessageId()).collect(Collectors.toList()).toArray(new String[]{}),
                result.getMessages().toArray(new String[]{}));

    }
}