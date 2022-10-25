package eu.domibus.plugin.jms.property;

import eu.domibus.ext.exceptions.DomibusPropertyExtException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.jms.JMSPluginImpl;
import eu.domibus.plugin.property.PluginPropertyChangeListener;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import static eu.domibus.plugin.jms.JMSMessageConstants.JMSPLUGIN_DOMAIN_ENABLED;

/**
 * @author Ion Perpegel
 * @since 5.1
 * <p>
 * Handles enabling/disabling of jms-plugin for the current domain.
 */
@Component
public class EnabledChangeListener implements PluginPropertyChangeListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EnabledChangeListener.class);

    final protected JMSPluginImpl jmsPlugin;

    public EnabledChangeListener(JMSPluginImpl jmsPlugin) {
        this.jmsPlugin = jmsPlugin;
    }

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.equals(JMSPLUGIN_DOMAIN_ENABLED, propertyName);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) throws DomibusPropertyExtException {
        LOG.debug("Executing enabled listener on domain [{}] for property [{}] with value [{}]", domainCode, propertyName, propertyValue);
        boolean enable = BooleanUtils.toBoolean(propertyValue);
        jmsPlugin.doSetEnabled(domainCode, enable);
    }

}
