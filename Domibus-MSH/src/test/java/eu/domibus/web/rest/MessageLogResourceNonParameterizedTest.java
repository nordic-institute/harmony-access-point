package eu.domibus.web.rest;

import com.google.common.collect.Sets;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.util.DateUtil;
import eu.domibus.core.csv.CsvServiceImpl;
import eu.domibus.core.message.MessagesLogService;
import eu.domibus.core.message.testservice.TestService;
import eu.domibus.core.party.PartyDao;
import eu.domibus.core.replication.UIMessageDao;
import eu.domibus.core.replication.UIMessageService;
import eu.domibus.core.replication.UIReplicationSignalService;
import eu.domibus.web.rest.ro.MessageLogFilterRequestRO;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Map;

@RunWith(JMockit.class)
public class MessageLogResourceNonParameterizedTest {

    @Tested
    MessageLogResource messageLogResource;

    @Injectable
    TestService testService;

    @Injectable
    PartyDao partyDao;

    @Injectable
    DateUtil dateUtil;

    @Injectable
    CsvServiceImpl csvServiceImpl;

    @Injectable
    private UIMessageService uiMessageService;

    @Injectable
    private UIMessageDao uiMessageDao;

    @Injectable
    private MessagesLogService messagesLogService;

    @Injectable
    private UIReplicationSignalService uiReplicationSignalService;

    @Injectable
    private DomibusConfigurationService domibusConfigurationService;

    @Test
    public void getCsv_fourCornersModeEnabled(@Injectable MessageLogFilterRequestRO messageLogFilter) {
        // GIVEN
        new Expectations(messageLogResource) {{
            domibusConfigurationService.isFourCornerEnabled(); result = true;
            messageLogResource.exportToCSV((List<?>) any, (Class<?>) any, (Map<String, String>) any, (List<String>) any, anyString); result = any;
        }};

        // WHEN
        messageLogResource.getCsv(messageLogFilter);

        // THEN
        new Verifications() {{
            List<String> excludedColumns;
            messageLogResource.exportToCSV((List<?>) any, (Class<?>) any, (Map<String, String>) any, excludedColumns = withCapture(), anyString);

            Assert.assertTrue("Should have not excluded the Original Sender and the Final Recipient columns when the four corners mode is enabled",
                    excludedColumns.stream().allMatch(excludedColumn -> !Sets.newHashSet("originalSender", "finalRecipient").contains(excludedColumn)));
        }};
    }

    @Test
    public void getCsv_fourCornersModeDisabled(@Injectable MessageLogFilterRequestRO messageLogFilter) {
        // GIVEN
        new Expectations(messageLogResource) {{
            domibusConfigurationService.isFourCornerEnabled(); result = false;
            messageLogResource.exportToCSV((List<?>) any, (Class<?>) any, (Map<String, String>) any, (List<String>) any, anyString); result = any;
        }};

        // WHEN
        messageLogResource.getCsv(messageLogFilter);

        // THEN
        new Verifications() {{
            List<String> excludedColumns;
            messageLogResource.exportToCSV((List<?>) any, (Class<?>) any, (Map<String, String>) any, excludedColumns = withCapture(), anyString);

            Assert.assertTrue("Should have excluded the Original Sender and the Final Recipient columns when the four corners mode is disabled",
                    excludedColumns.containsAll(Sets.newHashSet("originalSender", "finalRecipient")));
        }};
    }
}
