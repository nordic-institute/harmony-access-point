package eu.domibus.core.property.listeners;

import eu.domibus.core.pmode.PModeProvider;
import eu.domibus.property.DomibusPropertyChangeListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 *
 * Handles the change of dynamicdiscovery client propertiy
 */
@Service
public class DynamicDiscoveryClientChangeListener implements DomibusPropertyChangeListener {

    @Autowired
    protected ApplicationContext applicationContext;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.equalsIgnoreCase(propertyName, "domibus.dynamicdiscovery.client.specification");
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        PModeProvider pModeProvider = applicationContext.getBean(PModeProvider.class);
        pModeProvider.refresh();
    }
}
