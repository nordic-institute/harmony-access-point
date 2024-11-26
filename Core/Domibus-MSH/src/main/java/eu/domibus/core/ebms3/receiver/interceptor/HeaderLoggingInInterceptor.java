package eu.domibus.core.ebms3.receiver.interceptor;

import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.ebms3.receiver.policy.SetPolicyInInterceptor;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.cxf.ext.logging.AbstractLoggingInterceptor;
import org.apache.cxf.ext.logging.event.LogEvent;
import org.apache.cxf.ext.logging.event.LogEventSender;
import org.apache.cxf.ext.logging.slf4j.Slf4jVerboseEventSender;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Print the headers before SetPolicyInInterceptor
 * Always print the REQ_IN in case {@value eu.domibus.api.property.DomibusPropertyMetadataManagerSPI#DOMIBUS_LOGGING_DIAGNOSTIC_ENABLED} is {@code true}
 *
 * @author François Gautier
 * @since 5.1
 */
@Service("headerLoggingInInterceptor")
public class HeaderLoggingInInterceptor extends AbstractLoggingInterceptor {

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    public HeaderLoggingInInterceptor() {
        this(new Slf4jVerboseEventSender());
    }

    public HeaderLoggingInInterceptor(LogEventSender sender) {
        super(Phase.RECEIVE, sender);
        addBefore(SetPolicyInInterceptor.class.getName());
    }

    public void handleMessage(Message message) throws Fault {
        Boolean diagnostic = domibusPropertyProvider.getBooleanProperty(DomibusPropertyMetadataManagerSPI.DOMIBUS_LOGGING_DIAGNOSTIC_ENABLED);
        if (BooleanUtils.isTrue(diagnostic)) {
            LogEvent event = this.eventMapper.map(message, this.sensitiveProtocolHeaderNames);

            event.setPayload(this.transform(message, this.stripBinaryParts(event, this.maskSensitiveElements(message, event.getPayload()))));
            this.sender.send(event);
        }
    }
}
