package eu.domibus.plugin;

import eu.domibus.ext.domain.CronJobInfoDTO;
import eu.domibus.ext.services.DomibusPropertyManagerExt;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.PluginMessageListenerContainer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ion Perpegel
 * @since 5.0
 * <p>
 * Interface implemented by external modules (backend connectors) to signal that they can be disabled
 */
public interface EnableAware {
    static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EnableAware.class);

    /**
     * @return The name of the plugin
     */
    String getName();

    /**
     * If the plugin is enabled
     *
     * @param domainCode On which domain
     * @return The enabled state
     */
    boolean isEnabled(final String domainCode);

    /**
     * Telling the plugin to become enabled/disabled
     * The plugin will change his domain property and then notify the domibus to crete/delete the resources managed by domibus
     *
     * @param domainCode On which domain
     * @param enabled    the requested state
     */
    void setEnabled(final String domainCode, final boolean enabled);

    /**
     * The names of the cron jobs that the plugin uses so that domibus can pause/resume then on a plugin bases;
     *
     * @return
     */
    default List<CronJobInfoDTO> getJobsInfo() {
        return new ArrayList<>();
    }

    /**
     * The property name which specifies if the plugin is enabled or disabled for a domain
     *
     * @return the name of the property
     */
    default String getDomainEnabledPropertyName() {
        LOG.debug("Should return the name of the enabled state property to support enabling and disabling the plugin on a domain");
        return null;
    }

    /**
     * The property manager of the plugin
     *
     * @return defaults to null
     */
    default DomibusPropertyManagerExt getPropertyManager() {
        return null;
    }

    /**
     * Tells Domibus if it should manage resources (message listener containers and cron jobs) or the plugin does this
     * @return true if Domibus manages them
     */
    default boolean shouldCoreManageResources() {
        return false;
    }

    /**
     * The Message Listener Container factory of the plugin: used by Domibus to manage these resources
     * @return a reference to the PluginMessageListenerContainer service
     */
    default PluginMessageListenerContainer getMessageListenerContainerFactory() {
        return null;
    }
}
