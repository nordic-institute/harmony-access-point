package eu.domibus.core.message.reliability;

import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.sender.ResponseResult;

import javax.xml.soap.SOAPMessage;

public class ReliabilityDTO {

    UserMessage userMessage;
    UserMessageLog userMessageLog;
    ReliabilityChecker.CheckResult reliabilityCheckStatus;
    String requestRawXMLMessage;
    SOAPMessage responseSoapMessage;
    ResponseResult responseResult;
    LegConfiguration legConfiguration;
    MessageAttempt attempt;
    Throwable throwable;

    public ReliabilityDTO(UserMessage userMessage,
                          UserMessageLog userMessageLog,
                          ReliabilityChecker.CheckResult reliabilityCheckStatus,
                          String requestRawXMLMessage,
                          SOAPMessage responseSoapMessage,
                          ResponseResult responseResult,
                          LegConfiguration legConfiguration,
                          MessageAttempt attempt,
                          Throwable throwable) {
        this.userMessage = userMessage;
        this.userMessageLog = userMessageLog;
        this.reliabilityCheckStatus = reliabilityCheckStatus;
        this.requestRawXMLMessage = requestRawXMLMessage;
        this.responseSoapMessage = responseSoapMessage;
        this.responseResult = responseResult;
        this.legConfiguration = legConfiguration;
        this.attempt = attempt;
        this.throwable = throwable;
    }

    public UserMessage getUserMessage() {
        return userMessage;
    }

    public UserMessageLog getUserMessageLog() {
        return userMessageLog;
    }

    public ReliabilityChecker.CheckResult getReliabilityCheck() {
        return reliabilityCheckStatus;
    }

    public String getRequestRawXMLMessage() {
        return requestRawXMLMessage;
    }

    public SOAPMessage getResponseSoapMessage() {
        return responseSoapMessage;
    }

    public ResponseResult getResponseResult() {
        return responseResult;
    }

    public LegConfiguration getLegConfiguration() {
        return legConfiguration;
    }

    public MessageAttempt getAttempt() {
        return attempt;
    }

    public Throwable getThrowable() {
        return throwable;
    }
}
