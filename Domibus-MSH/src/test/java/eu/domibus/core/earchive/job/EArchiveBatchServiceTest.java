package eu.domibus.core.earchive.job;

import eu.domibus.api.model.ListUserMessageDto;
import eu.domibus.api.model.UserMessageDTO;
import eu.domibus.core.earchive.DomibusEArchiveException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * @author François Gautier
 * @since 5.0
 */
public class EArchiveBatchServiceTest {

    private EArchiveBatchService eArchiveBatchService;

    @Before
    public void setUp() throws Exception {
        eArchiveBatchService = new EArchiveBatchService(null, null, null, null, null);
    }

    @Test(expected = DomibusEArchiveException.class)
    public void batches_invalid() {
        eArchiveBatchService.batches(new ListUserMessageDto(null), 0);
    }

    @Test
    public void batches_empty() {
        List<ListUserMessageDto> batches = eArchiveBatchService.batches(new ListUserMessageDto(null), 2);
        Assert.assertEquals(1, batches.size());
        Assert.assertEquals(0, batches.get(0).getUserMessageDtos().size());
    }

    @Test
    public void batches_3messages() {
        List<UserMessageDTO> resultList = Arrays.asList(
                new UserMessageDTO(new Random().nextLong(), UUID.randomUUID().toString()),
                new UserMessageDTO(new Random().nextLong(), UUID.randomUUID().toString()),
                new UserMessageDTO(new Random().nextLong(), UUID.randomUUID().toString()));

        List<ListUserMessageDto> batches = eArchiveBatchService.batches(new ListUserMessageDto(resultList), 2);
        Assert.assertEquals(2, batches.size());
        Assert.assertEquals(2, batches.get(0).getUserMessageDtos().size());
        Assert.assertEquals(1, batches.get(1).getUserMessageDtos().size());
    }
}