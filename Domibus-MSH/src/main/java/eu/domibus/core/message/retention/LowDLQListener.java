package eu.domibus.core.message.retention;

import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.MDCKey;
import org.springframework.stereotype.Service;

import javax.jms.Message;

import static eu.domibus.core.metrics.MetricNames.PRIORITY_LOW;

@Service
public class LowDLQListener extends AbstractDLQListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(LowDLQListener.class);

    private String priority = "LOW";

    @MDCKey(DomibusLogger.MDC_MESSAGE_ID)
    @Timer(value = PRIORITY_LOW)
    @Counter(PRIORITY_LOW)
    public void onMessage(final Message message) {
        super.onMessage(message);
    }

    @Override
    protected String getPriority() {
        return priority;
    }
}
