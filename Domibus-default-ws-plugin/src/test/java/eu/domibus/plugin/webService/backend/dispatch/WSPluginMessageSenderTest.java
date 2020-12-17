package eu.domibus.plugin.webService.backend.dispatch;

import eu.domibus.plugin.webService.backend.WSBackendMessageLogDao;
import eu.domibus.plugin.webService.backend.WSBackendMessageLogEntity;
import eu.domibus.plugin.webService.backend.WSBackendMessageStatus;
import eu.domibus.plugin.webService.backend.WSBackendMessageType;
import eu.domibus.plugin.webService.backend.reliability.WSPluginBackendReliabilityService;
import eu.domibus.plugin.webService.backend.rules.WSPluginDispatchRule;
import eu.domibus.plugin.webService.backend.rules.WSPluginDispatchRulesService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.xml.soap.SOAPMessage;

/**
 * @author François Gautier
 * @since 5.0
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(JMockit.class)
public class WSPluginMessageSenderTest {

    public static final String RULE_NAME = "ruleName";
    public static final String END_POINT = "endpoint";
    public static final String MESSAGE_ID = "messageId";
    @Tested
    private WSPluginMessageSender wsPluginMessageSender;

    @Injectable
    protected WSPluginMessageBuilder wsPluginMessageBuilder;

    @Injectable
    protected WSPluginDispatcher wsPluginDispatcher;

    @Injectable
    protected WSPluginDispatchRulesService wsPluginDispatchRulesService;

    @Injectable
    protected WSBackendMessageLogDao wsBackendMessageLogDao;

    @Injectable
    protected WSPluginBackendReliabilityService reliabilityService;


    @Test
    public void sendMessageSuccess(@Mocked WSBackendMessageLogEntity wsBackendMessageLogEntity,
                                   @Mocked SOAPMessage soapMessage,
                                   @Mocked WSPluginDispatchRule wsPluginDispatchRule) {
        new Expectations() {{

            wsPluginMessageBuilder.buildSOAPMessage(wsBackendMessageLogEntity);
            result = soapMessage;
            times = 1;

            wsBackendMessageLogEntity.getRuleName();
            result = RULE_NAME;

            wsBackendMessageLogEntity.getType();
            result = WSBackendMessageType.SEND_SUCCESS;

            wsBackendMessageLogEntity.getMessageId();
            result = MESSAGE_ID;

            wsPluginDispatchRulesService.getRule(RULE_NAME);
            result = wsPluginDispatchRule;

            wsPluginDispatchRule.getEndpoint();
            result = END_POINT;

            wsPluginDispatcher.dispatch(soapMessage, END_POINT);
            result = soapMessage;
            times = 1;
        }};

        wsPluginMessageSender.sendNotification(wsBackendMessageLogEntity);

        new FullVerifications() {{
            wsBackendMessageLogEntity.setMessageStatus(WSBackendMessageStatus.SEND_IN_PROGRESS);
            times = 1;
            wsBackendMessageLogEntity.setMessageStatus(WSBackendMessageStatus.SENT);
            times = 1;
        }};
    }

    @Test
    public void sendMessageSuccess_exception(
            @Mocked WSBackendMessageLogEntity wsBackendMessageLogEntity,
            @Mocked SOAPMessage soapMessage,
            @Mocked WSPluginDispatchRule wsPluginDispatchRule) {
        new Expectations() {{
            wsPluginMessageBuilder.buildSOAPMessage(wsBackendMessageLogEntity);
            result = soapMessage;
            times = 1;

            wsBackendMessageLogEntity.getRuleName();
            result = RULE_NAME;

            wsBackendMessageLogEntity.getType();
            result = WSBackendMessageType.SEND_SUCCESS;

            wsBackendMessageLogEntity.getMessageId();
            result = MESSAGE_ID;

            wsPluginDispatchRulesService.getRule(RULE_NAME);
            result = wsPluginDispatchRule;

            wsPluginDispatchRule.getEndpoint();
            result = END_POINT;

            wsPluginDispatcher.dispatch(soapMessage, END_POINT);
            result = new IllegalStateException("ERROR");
            times = 1;
        }};

        try {
            wsPluginMessageSender.sendNotification(wsBackendMessageLogEntity);
            Assert.fail();
        } catch (Exception e) {
            //OK
        }

        new FullVerifications() {{
            wsBackendMessageLogEntity.setMessageStatus(WSBackendMessageStatus.SEND_IN_PROGRESS);
            times = 1;
            reliabilityService.handleReliability(wsBackendMessageLogEntity, wsPluginDispatchRule);
            times = 1;
        }};
    }
}