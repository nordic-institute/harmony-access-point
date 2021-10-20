package eu.domibus.core.earchive.job;

import com.fasterxml.uuid.NoArgGenerator;
import eu.domibus.api.model.ListUserMessageDto;
import eu.domibus.api.model.UserMessageDTO;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.ReceptionAwareness;
import eu.domibus.core.earchive.DomibusEArchiveException;
import eu.domibus.core.earchive.EArchiveBatchDao;
import eu.domibus.core.earchive.EArchiveBatchUserMessageDao;
import eu.domibus.core.pmode.provider.LegConfigurationPerMpc;
import eu.domibus.core.pmode.provider.PModeProvider;
import mockit.Expectations;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_EARCHIVE_BATCH_MPCS;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author François Gautier
 * @since 5.0
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(JMockit.class)
public class EArchiveBatchServiceTest {

    @Tested
    private EArchiveBatchService eArchiveBatchService;

    @Injectable
    private EArchiveBatchUserMessageDao eArchiveBatchUserMessageDao;
    @Injectable
    private DomibusPropertyProvider domibusPropertyProvider;
    @Injectable
    private PModeProvider pModeProvider;
    @Injectable
    private EArchiveBatchDao eArchiveBatchDao;
    @Injectable
    private NoArgGenerator uuidGenerator;


//    @Test(expected = DomibusEArchiveException.class)
//    public void batches_invalid() {
//        eArchiveBatchService.batches(new ListUserMessageDto(null), 0);
//    }
//
//    @Test
//    public void batches_empty() {
//        List<ListUserMessageDto> batches = eArchiveBatchService.batches(new ListUserMessageDto(null), 2);
//        Assert.assertEquals(1, batches.size());
//        Assert.assertEquals(0, batches.get(0).getUserMessageDtos().size());
//    }
//
//    @Test
//    public void batches_3messages() {
//        List<UserMessageDTO> resultList = asList(
//                new UserMessageDTO(new Random().nextLong(), UUID.randomUUID().toString()),
//                new UserMessageDTO(new Random().nextLong(), UUID.randomUUID().toString()),
//                new UserMessageDTO(new Random().nextLong(), UUID.randomUUID().toString()));
//
//        List<ListUserMessageDto> batches = eArchiveBatchService.batches(new ListUserMessageDto(resultList), 2);
//        Assert.assertEquals(2, batches.size());
//        Assert.assertEquals(2, batches.get(0).getUserMessageDtos().size());
//        Assert.assertEquals(1, batches.get(1).getUserMessageDtos().size());
//    }

    @Test
    public void getMpcs() {

        new Expectations() {{
            domibusPropertyProvider.getProperty(DOMIBUS_EARCHIVE_BATCH_MPCS);
            result = "test1, test2,test3";
        }};
        List<String> mpcs = eArchiveBatchService.getMpcs();
        Assert.assertEquals("test1", mpcs.get(0));
        Assert.assertEquals("test2", mpcs.get(1));
        Assert.assertEquals("test3", mpcs.get(2));

    }

    @Test
    public void getMpcs_empty() {

        new Expectations() {{
            domibusPropertyProvider.getProperty(DOMIBUS_EARCHIVE_BATCH_MPCS);
            result = null;
        }};
        List<String> mpcs = eArchiveBatchService.getMpcs();
        assertTrue(mpcs.isEmpty());

    }

    @Test
    public void getMaxRetryTimeOutFiltered(
            @Injectable LegConfiguration legConfiguration11,
            @Injectable LegConfiguration legConfiguration12,
            @Injectable LegConfiguration legConfiguration13,
            @Injectable ReceptionAwareness receptionAwareness11,
            @Injectable ReceptionAwareness receptionAwareness12,
            @Injectable ReceptionAwareness receptionAwareness13) {
        HashMap<String, List<LegConfiguration>> map = new HashMap<>();
        map.put("mpc1", asList(legConfiguration11,
                legConfiguration12,
                legConfiguration13));
        map.put("mpc2", Collections.singletonList(null));
        new Expectations() {{
            legConfiguration11.getReceptionAwareness();
            result = receptionAwareness11;
            legConfiguration12.getReceptionAwareness();
            result = receptionAwareness12;
            legConfiguration13.getReceptionAwareness();
            result = receptionAwareness13;

            receptionAwareness11.getRetryTimeout();
            result = 1;
            receptionAwareness12.getRetryTimeout();
            result = 2;
            receptionAwareness13.getRetryTimeout();
            result = 3;
        }};

        int mpc1 = eArchiveBatchService.getMaxRetryTimeOutFiltered(Collections.singletonList("mpc1"), new LegConfigurationPerMpc(map));

        assertEquals(3, mpc1);
        new FullVerifications() {
        };
    }
}