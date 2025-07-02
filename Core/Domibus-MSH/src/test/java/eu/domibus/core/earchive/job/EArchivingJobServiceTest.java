package eu.domibus.core.earchive.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.uuid.NoArgGenerator;
import eu.domibus.api.earchive.DomibusEArchiveException;
import eu.domibus.api.earchive.EArchiveRequestType;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.payload.PartInfoService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.ReceptionAwareness;
import eu.domibus.core.earchive.*;
import eu.domibus.core.earchive.alerts.EArchivingEventService;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.pmode.provider.LegConfigurationPerMpc;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.util.DateUtilImpl;
import mockit.Expectations;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author François Gautier
 * @since 5.0
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(JMockit.class)
public class EArchivingJobServiceTest {

    private EArchivingJobService eArchivingJobService;

    @Injectable
    private EArchiveBatchUserMessageDao eArchiveBatchUserMessageDao;
    @Injectable
    private DomibusPropertyProvider domibusPropertyProvider;
    @Injectable
    private PModeProvider pModeProvider;
    @Injectable
    private EArchiveBatchDao eArchiveBatchDao;
    @Injectable
    private EArchiveBatchStartDao eArchiveBatchStartDao;
    @Injectable
    private NoArgGenerator uuidGenerator;
    @Injectable
    private ObjectMapper domibusJsonMapper;
    @Injectable
    private EArchiveBatchUtils eArchiveBatchUtils;
    @Injectable
    private UserMessageLogDao userMessageLogDao;
    @Injectable
    private EArchivingEventService eArchivingEventService;
    @Injectable
    private PartInfoService partInfoService;
    private DateUtilImpl dateUtil;

    @Before
    public void setUp() throws Exception {
        //DateUtil is a utility class, so we need to inject it manually to override the mocked instance
        dateUtil = new DateUtilImpl();
        eArchivingJobService = new EArchivingJobService(eArchiveBatchUserMessageDao, domibusPropertyProvider, pModeProvider,
                eArchiveBatchDao, eArchiveBatchStartDao, uuidGenerator, userMessageLogDao, eArchivingEventService, partInfoService,
                dateUtil);
    }

    @Test
    public void testGetMaxEntityIdToArchived_Sanitizer() {
        EArchiveRequestType type = EArchiveRequestType.SANITIZER;
        long sanitizerDelay = 5L;
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        long minEntityId = dateUtil.getMinEntityId(now.minusDays(10), 0L);

        new Expectations() {{
            eArchiveBatchStartDao.findByReference(EArchivingDefaultService.CONTINUOUS_ID).getLastPkUserMessage();
            result = dateUtil.getMinEntityId(now.minusDays(5), 0L) + 123;
            domibusPropertyProvider.getIntegerProperty(DOMIBUS_EARCHIVE_SANITIZER_TIME_WINDOW_LIMIT);
            result = 2;
            domibusPropertyProvider.getLongProperty(DOMIBUS_EARCHIVE_SANITY_DELAY);
            result = sanitizerDelay;
        }};

        long result = eArchivingJobService.getMaxEntityIdToArchived(type, minEntityId);
        //expected to be 10 days ago - 2 days (time window limit)
        assertEquals(dateUtil.getMaxEntityId(now.minusDays(8), 0L), result);
    }

    @Test
    public void testGetMaxEntityIdToArchived_Continuous_window_limit() {
        EArchiveRequestType type = EArchiveRequestType.CONTINUOUS;
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

        new Expectations() {{
            domibusPropertyProvider.getIntegerProperty(DOMIBUS_EARCHIVE_TIME_WINDOW_LIMIT);
            result = 3;
            domibusPropertyProvider.getLongProperty(DOMIBUS_EARCHIVE_BATCH_RETRY_TIMEOUT);
            result = 70L;
        }};
        //expected to be 5 days ago - 2 days (time window limit)
        long result = eArchivingJobService.getMaxEntityIdToArchived(type, dateUtil.getMinEntityId(now.minusDays(5), 0L));
        assertEquals(dateUtil.getMaxEntityId(now.minusDays(2), 0L), result);
    }

    @Test
    public void getMpcs() {

        new Expectations() {{
            domibusPropertyProvider.getProperty(DOMIBUS_EARCHIVE_BATCH_MPCS);
            result = "test1, test2,test3";
        }};
        List<String> mpcs = eArchivingJobService.getMpcs();
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
        List<String> mpcs = eArchivingJobService.getMpcs();
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
        map.put("mpc2", singletonList(null));
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

        int mpc1 = eArchivingJobService.getMaxRetryTimeOutFiltered(singletonList("mpc1"), new LegConfigurationPerMpc(map));

        assertEquals(3, mpc1);
        new FullVerifications() {
        };
    }

    @Test
    public void getId_CONTINUOUS() {
        int id = eArchivingJobService.getEArchiveBatchStartId(EArchiveRequestType.CONTINUOUS);
        assertEquals(1, id);
    }

    @Test
    public void getId_SANITY() {
        int id = eArchivingJobService.getEArchiveBatchStartId(EArchiveRequestType.SANITIZER);
        assertEquals(2, id);
    }

    @Test(expected = DomibusEArchiveException.class)
    public void getId_MANUAL() {
        eArchivingJobService.getEArchiveBatchStartId(EArchiveRequestType.MANUAL);
    }

    @Test
    public void createEventOnNonFinalMessages_activated() {
        String messageId = "someMessageId";
        new Expectations() {{
            eArchivingEventService.isEventMessageNotFinalActive();
            result = true;

            userMessageLogDao.findMessagesNotFinalAsc(0L, 1L);
            result = singletonList(new EArchiveBatchUserMessage(123L, messageId, MessageStatus.NOT_FOUND));
        }};
        eArchivingJobService.createEventOnNonFinalMessages(0L, 1L);

        new FullVerifications() {{
            eArchivingEventService.sendEventMessageNotFinal(messageId, MessageStatus.NOT_FOUND);
            times = 1;
        }};
    }

    @Test
    public void createEventOnNonFinalMessages_NotActivated() {
        new Expectations() {{
            eArchivingEventService.isEventMessageNotFinalActive();
            result = false;
        }};
        eArchivingJobService.createEventOnNonFinalMessages(0L, 1L);

        new FullVerifications() {
        };
    }

    @Test
    public void createEventOnNonFinalMessages_noAlert() {
        new Expectations() {{
            domibusPropertyProvider.getIntegerProperty(DOMIBUS_EARCHIVE_START_DATE_STOPPED_ALLOWED_HOURS);
            result = 5;
        }};
        eArchivingJobService.createEventOnStartDateContinuousJobStopped(new Date());

        new FullVerifications() {{
            eArchivingEventService.sendEventStartDateStopped();
            times = 0;
        }};
    }

    @Test
    public void createEventOnNonFinalMessages_alert() {
        new Expectations() {{
            domibusPropertyProvider.getIntegerProperty(DOMIBUS_EARCHIVE_START_DATE_STOPPED_ALLOWED_HOURS);
            result = 5;
        }};
        eArchivingJobService.createEventOnStartDateContinuousJobStopped(Date.from(ZonedDateTime.now(ZoneOffset.UTC).minusHours(10).toInstant()));

        new FullVerifications() {{
            eArchivingEventService.sendEventStartDateStopped();
            times = 1;
        }};
    }

    @Test
    public void createEventOnNonFinalMessages_wrongConfig() {
        new Expectations() {{
            domibusPropertyProvider.getIntegerProperty(DOMIBUS_EARCHIVE_START_DATE_STOPPED_ALLOWED_HOURS);
            result = null;
        }};
        eArchivingJobService.createEventOnStartDateContinuousJobStopped(new Date());

        new FullVerifications() {{
            eArchivingEventService.sendEventStartDateStopped();
            times = 1;
        }};
    }

    @Test
    public void createEventOnNonFinalMessages_dateNull() {
        new Expectations() {{
            domibusPropertyProvider.getIntegerProperty(DOMIBUS_EARCHIVE_START_DATE_STOPPED_ALLOWED_HOURS);
            result = 5;
        }};
        eArchivingJobService.createEventOnStartDateContinuousJobStopped(null);

        new FullVerifications() {{
            eArchivingEventService.sendEventStartDateStopped();
            times = 1;
        }};
    }

    @Test
    public void rounding60min_10() {
        assertEquals(60L, eArchivingJobService.rounding60min(10));
    }

    @Test
    public void rounding60min_70() {
        assertEquals(120L, eArchivingJobService.rounding60min(70));
    }

    @Test
    public void rounding60min_60() {
        assertEquals(60L, eArchivingJobService.rounding60min(60));
    }
}
