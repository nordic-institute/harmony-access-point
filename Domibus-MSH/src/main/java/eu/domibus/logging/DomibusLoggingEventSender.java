package eu.domibus.logging;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.ext.logging.event.LogEvent;
import org.apache.cxf.ext.logging.event.LogEventSender;
import org.apache.cxf.ext.logging.event.LogMessageFormatter;
import org.apache.cxf.ext.logging.slf4j.Slf4jEventSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DomibusLoggingEventSender extends Slf4jEventSender implements LogEventSender  {

    private static final Logger LOG = LoggerFactory.getLogger(DomibusLoggingEventSender.class);

    private boolean printPayload;

    public boolean isPrintPayload() {
        return printPayload;
    }

    public void setPrintPayload(boolean printPayload) {
        this.printPayload = printPayload;
    }


    @Override
    protected String getLogMessage(LogEvent event) {
        String payload = event.getPayload();
        LOG.info("printPayload={}", printPayload);

        return printPayload ? LogMessageFormatter.format(event) : StringUtils.EMPTY;
    }

}
