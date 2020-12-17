package eu.domibus.core.crypto.spi.dss.listeners;

import eu.domibus.ext.services.DomibusSchedulerExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.property.PluginPropertyChangeListener;

import static eu.domibus.core.crypto.spi.dss.DssExtensionPropertyManager.AUTHENTICATION_DSS_REFRESH_CRON;

/**
 * @author Thomas Dussart
 * @since 4.2
 *
 * Listener for TL job scheduling property change.
 */

public class TriggerChangeListener implements PluginPropertyChangeListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(NetworkConfigurationListener.class);

    private final static String JOB_NAME = "dssRefreshJob";

    private DomibusSchedulerExtService domibusSchedulerExtService;

    public TriggerChangeListener(DomibusSchedulerExtService domibusSchedulerExtService) {
        this.domibusSchedulerExtService = domibusSchedulerExtService;
    }

    @Override
    public boolean handlesProperty(String propertyName) {
        return AUTHENTICATION_DSS_REFRESH_CRON.equals(propertyName);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        LOG.info("Reloading DSS job, domain code:[{}], property name:[{}], property value:[{}]",domainCode,propertyName,propertyValue);
        domibusSchedulerExtService.rescheduleJob(domainCode, JOB_NAME, propertyValue);
    }




}
