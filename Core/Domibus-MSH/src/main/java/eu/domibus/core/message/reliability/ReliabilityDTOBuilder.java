package eu.domibus.core.message.reliability;

import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.sender.ResponseResult;

import javax.xml.soap.SOAPMessage;

public class ReliabilityDTOBuilder {
    private UserMessage userMessage;
    private UserMessageLog userMessageLog;
    private ReliabilityChecker.CheckResult reliabilityCheckStatus;
    private String requestRawXMLMessage;
    private SOAPMessage responseSoapMessage;
    private ResponseResult responseResult;
    private LegConfiguration legConfiguration;
    private MessageAttempt attempt;
    private Throwable throwable;

    public static ReliabilityDTOBuilder builder() {
        return new ReliabilityDTOBuilder();
    }

    public ReliabilityDTOBuilder userMessage(UserMessage userMessage) {
        this.userMessage = userMessage;
        return this;
    }

    public ReliabilityDTOBuilder userMessageLog(UserMessageLog userMessageLog) {
        this.userMessageLog = userMessageLog;
        return this;
    }

    public ReliabilityDTOBuilder reliabilityCheckStatus(ReliabilityChecker.CheckResult reliabilityCheckStatus) {
        this.reliabilityCheckStatus = reliabilityCheckStatus;
        return this;
    }

    public ReliabilityDTOBuilder requestRawXMLMessage(String requestRawXMLMessage) {
        this.requestRawXMLMessage = requestRawXMLMessage;
        return this;
    }

    public ReliabilityDTOBuilder responseSoapMessage(SOAPMessage responseSoapMessage) {
        this.responseSoapMessage = responseSoapMessage;
        return this;
    }

    public ReliabilityDTOBuilder responseResult(ResponseResult responseResult) {
        this.responseResult = responseResult;
        return this;
    }

    public ReliabilityDTOBuilder legConfiguration(LegConfiguration legConfiguration) {
        this.legConfiguration = legConfiguration;
        return this;
    }

    public ReliabilityDTOBuilder attempt(MessageAttempt attempt) {
        this.attempt = attempt;
        return this;
    }

    public ReliabilityDTOBuilder throwable(Throwable throwable) {
        this.throwable = throwable;
        return this;
    }

    public ReliabilityDTO build() {
        return new ReliabilityDTO(
                userMessage,
                userMessageLog,
                reliabilityCheckStatus,
                requestRawXMLMessage,
                responseSoapMessage,
                responseResult,
                legConfiguration,
                attempt,
                throwable);
    }
}