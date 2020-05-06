package eu.domibus.core.message.retention;

import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.MDCKey;
import org.springframework.stereotype.Service;

import javax.jms.Message;
import java.util.concurrent.atomic.AtomicInteger;

import static eu.domibus.core.metrics.MetricNames.PRIORITY_HIGH;

@Service
public class HighDLQListener extends AbstractDLQListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(HighDLQListener.class);

    private String priority = "HIGH";

    @MDCKey(DomibusLogger.MDC_MESSAGE_ID)
    @Timer(value = PRIORITY_HIGH)
    @Counter(PRIORITY_HIGH)
    public void onMessage(final Message message) {
        super.onMessage(message);
    }

    @Override
    protected String getPriority() {
        return priority;
    }
}
