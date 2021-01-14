package eu.domibus.plugin.webService.property.listeners;

import eu.domibus.ext.services.DomibusSchedulerExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.property.PluginPropertyChangeListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import static eu.domibus.plugin.webService.property.WSPluginPropertyManager.DISPATCHER_CRON_EXPRESSION;

/**
 * @author François Gautier
 * @since 5.0
 *
 * Handles the rescheduling of ws-plugin wsPluginBackendSendRetryWorker.
 */
@Component
public class WSPluginDispatcherCronExpressionChangeListener implements PluginPropertyChangeListener {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSPluginDispatcherCronExpressionChangeListener.class);

    protected final DomibusSchedulerExtService domibusSchedulerExt;

    public static final String SEND_RETRY_JOB_NAME = "wsPluginBackendSendRetryWorker";

    public WSPluginDispatcherCronExpressionChangeListener(DomibusSchedulerExtService domibusSchedulerExt) {
        this.domibusSchedulerExt = domibusSchedulerExt;
    }

    @Override
    public boolean handlesProperty(String propertyName) {
        boolean doesHandle = StringUtils.equals(propertyName, DISPATCHER_CRON_EXPRESSION);
        LOG.trace("Handling [{}] property: [{}]", propertyName, doesHandle);
        return doesHandle;
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        LOG.info("Reloading [{}] job, domain code:[{}], property name:[{}], property value:[{}]", SEND_RETRY_JOB_NAME, domainCode, propertyName, propertyValue);
        domibusSchedulerExt.rescheduleJob(domainCode, SEND_RETRY_JOB_NAME, propertyValue);
    }

}
