package eu.domibus.plugin.fs.property.listeners;

import eu.domibus.ext.exceptions.DomibusPropertyExtException;
import eu.domibus.ext.services.DomibusSchedulerExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.fs.property.FSPluginProperties;
import eu.domibus.plugin.property.PluginPropertyChangeListener;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import static eu.domibus.plugin.fs.property.FSPluginPropertiesMetadataManagerImpl.DOMAIN_ENABLED;

/**
 * @author Ion Perpegel
 * @since 5.0
 * <p>
 * Handles enabling/disabling of fs-plugin for the current domain.
 */
@Component
public class EnabledChangeListener implements PluginPropertyChangeListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EnabledChangeListener.class);

    public static final String[] FSPLUGIN_JOB_NAMES = TriggerChangeListener.CRON_PROPERTY_NAMES_TO_JOB_MAP.values().toArray(new String[]{});

    final protected DomibusSchedulerExtService domibusSchedulerExt;
    final protected FSPluginProperties fsPluginProperties;

    public EnabledChangeListener(DomibusSchedulerExtService domibusSchedulerExt, FSPluginProperties fsPluginProperties) {
        this.domibusSchedulerExt = domibusSchedulerExt;
        this.fsPluginProperties = fsPluginProperties;
    }

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.equals(DOMAIN_ENABLED, propertyName);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) throws DomibusPropertyExtException {
        boolean enable = fsPluginProperties.getDomainEnabled(domainCode);
        LOG.info("Setting fs-plugin to: [{}] for domain: [{}]...", enable ? "enabled" : "disabled", domainCode);

        if (enable) {
            domibusSchedulerExt.resumeJobs(domainCode, FSPLUGIN_JOB_NAMES);
        } else {
            domibusSchedulerExt.pauseJobs(domainCode, FSPLUGIN_JOB_NAMES);
        }
    }

}