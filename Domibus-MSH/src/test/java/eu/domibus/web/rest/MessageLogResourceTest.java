package eu.domibus.web.rest;

import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.util.DateUtil;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.core.csv.CsvServiceImpl;
import eu.domibus.core.message.MessagesLogService;
import eu.domibus.core.message.testservice.TestService;
import eu.domibus.core.message.testservice.TestServiceException;
import eu.domibus.core.replication.UIMessageService;
import eu.domibus.core.replication.UIReplicationSignalService;
import eu.domibus.web.rest.ro.LatestIncomingMessageRequestRO;
import eu.domibus.web.rest.ro.LatestOutgoingMessageRequestRO;
import eu.domibus.web.rest.ro.TestServiceMessageInfoRO;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.ResponseEntity;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
@RunWith(JMockit.class)
public class MessageLogResourceTest {

    @Tested
    MessageLogResource messageLogResource;

    @Injectable
    TestService testService;

    @Injectable
    DateUtil dateUtil;

    @Injectable
    UIMessageService uiMessageService;

    @Injectable
    MessagesLogService messagesLogService;

    @Injectable
    UIReplicationSignalService uiReplicationSignalService;

    @Injectable
    DomibusConfigurationService domibusConfigurationService;

    @Injectable
    CsvServiceImpl csvServiceImpl;

    @Test
    public void testGetLastTestSent(@Injectable TestServiceMessageInfoRO testServiceMessageInfoResult) throws TestServiceException {
        // Given
        String partyId = "test";
        new Expectations() {{
            testService.getLastTestSentWithErrors(partyId);
            result = testServiceMessageInfoResult;
        }};

        // When
        ResponseEntity<TestServiceMessageInfoRO> lastTestSent = messageLogResource.getLastTestSent(
                new LatestOutgoingMessageRequestRO() {{
                    setPartyId(partyId);
                }});
        // Then
        TestServiceMessageInfoRO testServiceMessageInfoRO = lastTestSent.getBody();
        Assert.assertEquals(testServiceMessageInfoResult.getPartyId(), testServiceMessageInfoRO.getPartyId());
    }

    @Test(expected = TestServiceException.class)
    public void testGetLastTestSent_NotFound() throws TestServiceException {
        // Given
        String partyId = "partyId";
        new Expectations() {{
            testService.getLastTestSentWithErrors(partyId);
            result = new TestServiceException("No User Message found. Error Details in error log");
        }};

        // When
        messageLogResource.getLastTestSent(
                new LatestOutgoingMessageRequestRO() {{
                    setPartyId(partyId);
                }});
    }

    @Test
    public void testGetLastTestReceived(@Injectable TestServiceMessageInfoRO testServiceMessageInfoResult, @Injectable Party party) throws TestServiceException {
        // Given
        String partyId = "partyId";
        String userMessageId = "userMessageId";
        new Expectations() {{
            testService.getLastTestReceivedWithErrors(partyId, userMessageId);
            result = testServiceMessageInfoResult;
        }};

        // When
        ResponseEntity<TestServiceMessageInfoRO> lastTestReceived = messageLogResource.getLastTestReceived(
                new LatestIncomingMessageRequestRO() {{
                    setPartyId(partyId);
                    setUserMessageId(userMessageId);
                }});
        // Then
        TestServiceMessageInfoRO testServiceMessageInfoRO = lastTestReceived.getBody();
        Assert.assertEquals(testServiceMessageInfoRO.getPartyId(), testServiceMessageInfoResult.getPartyId());
        Assert.assertEquals(testServiceMessageInfoRO.getAccessPoint(), party.getEndpoint());
    }

    @Test(expected = TestServiceException.class)
    public void testGetLastTestReceived_NotFound() throws TestServiceException {
        // Given
        String partyId = "partyId";
        String userMessageId = "userMessageId";
        new Expectations() {{
            testService.getLastTestReceivedWithErrors(partyId, userMessageId);
            result = new TestServiceException("No Signal Message found. Error Details in error log");
        }};

        messageLogResource.getLastTestReceived(
                new LatestIncomingMessageRequestRO() {{
                    setPartyId(partyId);
                    setUserMessageId(userMessageId);
                }});
    }
}
