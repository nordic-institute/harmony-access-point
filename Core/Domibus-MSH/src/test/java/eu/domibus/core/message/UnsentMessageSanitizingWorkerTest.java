package eu.domibus.core.message;

import eu.domibus.api.exceptions.DomibusDateTimeException;
import eu.domibus.api.message.UserMessageException;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.ProcessingType;
import eu.domibus.api.model.UserMessageLogDto;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.util.DatabaseUtil;
import eu.domibus.api.util.DateUtil;
import eu.domibus.core.alerts.service.EventService;
import eu.domibus.core.pmode.provider.PModeProvider;
import mockit.Expectations;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_MESSAGES_STUCK_IGNORE_RECENT_MINUTES;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_MESSAGES_STUCK_MAX_COUNT;
import static java.util.concurrent.TimeUnit.MINUTES;

@RunWith(JMockit.class)
public class UnsentMessageSanitizingWorkerTest {

    @Injectable
    private AuthUtils authUtils;

    @Injectable
    private UserMessageLogDao userMessageLogDao;

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    private UserMessageDefaultService userMessageService;

    @Injectable
    private DateUtil dateUtil;

    @Injectable
    private PModeProvider pModeProvider;

    @Injectable
    private DomainService domainService;

    @Injectable
    protected DomainContextProvider domainContextProvider;

    @Injectable
    protected DatabaseUtil databaseUtil;

    @Injectable
    private EventService eventService;

    @Tested
    private UnsentMessageSanitizingWorker unsentMessageSanitizingWorker;

    @Test
    public void testSanitize() {
        final ZonedDateTime currentDateTime = ZonedDateTime.of(2023, 12, 1, 20, 1 , 0, 0, ZoneOffset.UTC);
        final Date delayedDate = Date.from(currentDateTime.minusMinutes(360).toInstant());
        final long maxEntityId = 231201139999999999l;
        final List<UserMessageLogDto> unsentMessageDtos = Arrays.asList(new UserMessageLogDto(111L,"7b2736d0-69f8-48de-ac7a-d4bd76ac78c1", MSHRole.SENDING), new UserMessageLogDto(112L, "7b2736d0-69f8-48de-ac7a-d4bd76ac78c2", MSHRole.SENDING));

        new Expectations() {{
            domibusPropertyProvider.getIntegerProperty(DOMIBUS_MESSAGES_STUCK_IGNORE_RECENT_MINUTES);
            result = 360;

            domibusPropertyProvider.getIntegerProperty(DOMIBUS_MESSAGES_STUCK_MAX_COUNT);
            result = 1000;

            domibusPropertyProvider.getIntegerProperty("domibus.msh.retry.timeoutDelay");
            result = 10;

            dateUtil.getMinEntityId(anyLong);
            result = 0L;

            pModeProvider.getMaxRetryTimeout(ProcessingType.PUSH);
            result = 60;

            dateUtil.getDateMinutesAgo(360);
            result = delayedDate;

            dateUtil.getMaxEntityId(MINUTES.toSeconds(420));
            result = maxEntityId;

            userMessageLogDao.findUnsentMessageIds(delayedDate, maxEntityId, 1000 /* maxMessageCount */);
            result = unsentMessageDtos;

            userMessageService.sendEnqueuedMessage("7b2736d0-69f8-48de-ac7a-d4bd76ac78c1", anyLong);
            result = new UserMessageException("TEST");
        }};

        unsentMessageSanitizingWorker.sanitize();

        new FullVerifications() {{
            userMessageService.sendEnqueuedMessage("7b2736d0-69f8-48de-ac7a-d4bd76ac78c2", anyLong);
        }};
    }

    @Test
    public void testSanitize_DateTimeException() {
        new Expectations() {{
            domibusPropertyProvider.getIntegerProperty(DOMIBUS_MESSAGES_STUCK_IGNORE_RECENT_MINUTES);

            pModeProvider.getMaxRetryTimeout(ProcessingType.PUSH);

            dateUtil.getDateMinutesAgo(anyInt);
            result = new DomibusDateTimeException("");
        }};

        unsentMessageSanitizingWorker.sanitize();

        new FullVerifications() {{
            dateUtil.getMaxEntityId(anyLong);
            times = 0;
            userMessageLogDao.findUnsentMessageIds((Date) any, anyLong, anyInt);
            times = 0;
            userMessageService.sendEnqueuedMessage(anyString, anyLong);
            times = 0;
        }};
    }
}