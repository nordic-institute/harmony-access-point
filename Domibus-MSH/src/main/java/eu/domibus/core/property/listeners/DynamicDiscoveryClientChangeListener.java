package eu.domibus.core.property.listeners;

import eu.domibus.core.pmode.PModeProvider;
import eu.domibus.plugin.property.PluginPropertyChangeListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import static eu.domibus.api.property.DomibusPropertyMetadataManager.DOMIBUS_DYNAMICDISCOVERY_CLIENT_SPECIFICATION;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 *
 * Handles the change of dynamicdiscovery client propertiy
 */
@Service
public class DynamicDiscoveryClientChangeListener implements PluginPropertyChangeListener {

    @Autowired
    protected ApplicationContext applicationContext;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.equalsIgnoreCase(propertyName, DOMIBUS_DYNAMICDISCOVERY_CLIENT_SPECIFICATION);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        PModeProvider pModeProvider = applicationContext.getBean(PModeProvider.class);
        pModeProvider.refresh();
    }
}
