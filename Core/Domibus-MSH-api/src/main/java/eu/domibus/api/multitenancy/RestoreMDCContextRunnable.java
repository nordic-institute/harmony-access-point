package eu.domibus.api.multitenancy;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;

import java.util.Map;

/**
 * Copies the LOG context map before running the task and restores it after the task is finished.
 *
 * @author Ion Perpegel
 * @since 5.1.8
 */
public class RestoreMDCContextRunnable implements Runnable {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(RestoreMDCContextRunnable.class);

    private final Runnable task;

    private final Map<String, String> copyOfContextMap;

    public RestoreMDCContextRunnable(Runnable task) {
        this.task = task;
        LOG.trace("Copying MDC context map");
        this.copyOfContextMap = LOG.getCopyOfContextMap();
    }

    @Override
    public void run() {
        if (task == null) {
            LOG.warn("Task is null");
            return;
        }
        try {
            LOG.trace("Running task");
            task.run();
        } finally {
            LOG.trace("Restoring MDC context map");
            LOG.setContextMap(copyOfContextMap);
        }

    }
}
