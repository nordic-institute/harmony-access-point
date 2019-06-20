package eu.domibus.core.property.listeners;

import eu.domibus.core.pmode.PModeProvider;
import eu.domibus.property.DomibusPropertyChangeListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DynamicDiscoveryClientChangeListener implements DomibusPropertyChangeListener {

    @Autowired
    private PModeProvider pModeProvider;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.equalsIgnoreCase(propertyName, "domibus.dynamicdiscovery.client.specification");
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        pModeProvider.refresh();
    }
}
