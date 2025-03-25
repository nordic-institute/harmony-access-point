package eu.domibus.logging;

import eu.domibus.logging.api.MessageCode;
import eu.domibus.logging.api.MessageConverter;
import org.slf4j.Marker;
import org.slf4j.helpers.MessageFormatter;

import java.util.Arrays;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public class DefaultMessageConverter implements MessageConverter {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DefaultMessageConverter.class);

    @Override
    public String getMessage(Marker marker, MessageCode messageCode, Object... args) {
        String message = null;
        try {
            message = MessageFormatter.arrayFormat(messageCode.getMessage(), args).getMessage();
        } catch (Exception throwable) {
            LOG.debug("Could not format the code [" + messageCode.getCode() + "]: message [" + messageCode.getMessage() + "] and arguments [" + Arrays.asList(args) + "]");
            message = messageCode.getMessage();
        }
        if (marker != null) {
            return "[" + marker + " - " + messageCode.getCode() + "] " + message;
        } else {
            return "[" + messageCode.getCode() + "] " + message;
        }
    }

    @Override
    public String getMessageWithSummaryThrowable(Marker marker, MessageCode messageCode, Throwable t, Object... args) {
        return getMessage(marker, messageCode, args) + getThrowableSummary(t);
    }

    private String getThrowableSummary(Throwable t) {
        StringBuilder summary = new StringBuilder();
        if (t != null) {
            summary.append(getString("(1) ", t));

            Throwable cause = t.getCause();
            if (cause != null) {
                summary.append(getString("(2) ", cause));
                Throwable subCause = cause.getCause();
                if (subCause != null) {
                    summary.append(getString("(3) ", subCause));
                }
            }
        }
        return summary.toString();
    }

    private static String getString(String prefix, Throwable t) {
        return " [" + prefix + t.getMessage() + "] ";
    }
}
