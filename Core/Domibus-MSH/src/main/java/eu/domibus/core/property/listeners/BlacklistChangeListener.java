package eu.domibus.core.property.listeners;

import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.core.rest.validators.BlacklistValidator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_USER_INPUT_BLACK_LIST;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_USER_INPUT_WHITE_LIST;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * Handles the change of blacklist and whitelist properties
 */
@Service
public class BlacklistChangeListener implements DomibusPropertyChangeListener {

    @Autowired
    List<BlacklistValidator> blacklistValidators;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.equalsAnyIgnoreCase(propertyName, DOMIBUS_USER_INPUT_BLACK_LIST, DOMIBUS_USER_INPUT_WHITE_LIST);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        blacklistValidators.forEach(v -> v.reset());
    }
}
