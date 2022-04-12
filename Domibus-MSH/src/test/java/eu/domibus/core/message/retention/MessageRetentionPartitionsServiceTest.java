package eu.domibus.core.message.retention;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.alerts.service.EventService;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.pmode.provider.PModeProvider;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author idragusa
 * @since 5.0
 */
@RunWith(JMockit.class)
public class MessageRetentionPartitionsServiceTest {

    @Tested
    MessageRetentionPartitionsService messageRetentionPartitionsService;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    PModeProvider pModeProvider;

    @Injectable
    UserMessageDao userMessageDao;

    @Injectable
    UserMessageLogDao userMessageLogDao;

    @Injectable
    EventService eventService;

    @Injectable
    DomibusConfigurationService domibusConfigurationService;

    @Injectable
    DomainService domainService;

    @Injectable
    DomainContextProvider domainContextProvider;

    @Test
    public void deleteExpiredMessagesTest() {

        List<String> mpcs = new ArrayList<>();
        mpcs.add("mpc1");
        mpcs.add("mpc2");
        new Expectations() {{
            pModeProvider.getRetentionDownloadedByMpcURI("mpc1");
            result = 1200;

            pModeProvider.getRetentionDownloadedByMpcURI("mpc2");
            result = 1440;

            pModeProvider.getRetentionUndownloadedByMpcURI(anyString);
            result = 1300;

            pModeProvider.getRetentionSentByMpcURI(anyString);
            result = 600;

            pModeProvider.getMpcURIList();
            result = mpcs;
        }};

        messageRetentionPartitionsService.deleteExpiredMessages();

    }

    @Test
    public void dateUTCTest() {
        String DATETIME_FORMAT_DEFAULT = "yyMMddHH";
        final SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FORMAT_DEFAULT, Locale.ENGLISH);

        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date newDate = DateUtils.addMinutes(new Date(), 10);
        Integer partitionNameUTC = new Integer(sdf.format(newDate).substring(0, 8));

        sdf.setTimeZone(TimeZone.getTimeZone("EST"));
        Integer partitionNameEES = new Integer(sdf.format(newDate).substring(0, 8));

        Assert.assertTrue(partitionNameUTC - partitionNameEES > 0);
    }
}