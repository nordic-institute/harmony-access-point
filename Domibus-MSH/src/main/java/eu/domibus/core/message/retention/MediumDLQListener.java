package eu.domibus.core.message.retention;

import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.MDCKey;
import org.springframework.stereotype.Service;

import javax.jms.Message;

import static eu.domibus.core.metrics.MetricNames.PRIORITY_MEDIUM;

@Service
public class MediumDLQListener extends AbstractDLQListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MediumDLQListener.class);

    private String priority = "5";

    @MDCKey(DomibusLogger.MDC_MESSAGE_ID)
    @Timer(value = PRIORITY_MEDIUM)
    @Counter(PRIORITY_MEDIUM)
    public void onMessage(final Message message) {
        super.onMessage(message);
    }

    @Override
    protected String getPriority() {
        return priority;
    }
}
