package eu.domibus.core.message;

import eu.domibus.api.exceptions.DomibusDateTimeException;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.util.DatabaseUtil;
import eu.domibus.api.util.DateUtil;
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

    @Tested
    private UnsentMessageSanitizingWorker unsentMessageSanitizingWorker;

    @Test
    public void testSanitize() {
        final ZonedDateTime currentDateTime = ZonedDateTime.of(2023, 12, 1, 20, 1 , 0, 0, ZoneOffset.UTC);
        final Date delayedDate = Date.from(currentDateTime.minusMinutes(360).toInstant());
        final long maxEntityId = 231201139999999999l;
        final List<String> unsentMessageIds = Arrays.asList("7b2736d0-69f8-48de-ac7a-d4bd76ac78c1");

        new Expectations() {{
            domibusPropertyProvider.getIntegerProperty(DOMIBUS_MESSAGES_STUCK_IGNORE_RECENT_MINUTES);
            result = 360;

            pModeProvider.getMaxRetryTimeout();
            result = 60;

            dateUtil.getDateMinutesAgo(360);
            result = delayedDate;

            dateUtil.getMaxEntityId(MINUTES.toSeconds(420));
            result = maxEntityId;

            userMessageLogDao.findUnsentMessageIds(delayedDate, maxEntityId);
            result = unsentMessageIds;
        }};

        unsentMessageSanitizingWorker.sanitize();

        new FullVerifications() {{
            userMessageService.sendEnqueuedMessage("7b2736d0-69f8-48de-ac7a-d4bd76ac78c1");
        }};
    }

    @Test
    public void testSanitize_DateTimeException() {
        new Expectations() {{
            domibusPropertyProvider.getIntegerProperty(DOMIBUS_MESSAGES_STUCK_IGNORE_RECENT_MINUTES);

            pModeProvider.getMaxRetryTimeout();

            dateUtil.getDateMinutesAgo(anyInt);
            result = new DomibusDateTimeException("");
        }};

        unsentMessageSanitizingWorker.sanitize();

        new FullVerifications() {{
            dateUtil.getMaxEntityId(anyLong);
            times = 0;
            userMessageLogDao.findUnsentMessageIds((Date) any, anyLong);
            times = 0;
            userMessageService.sendEnqueuedMessage(anyString);
            times = 0;
        }};
    }
}