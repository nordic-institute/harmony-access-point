package eu.domibus.core.property.listeners;

import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.core.message.pull.PullFrequencyHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * Handles the change of pull property values that are cached in PullFrequencyHelper.
 */
@Service
public class PullConfigurationChangeListener implements DomibusPropertyChangeListener {

    @Autowired
    private PullFrequencyHelper pullFrequencyHelper;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.startsWithAny(propertyName,
                DomibusPropertyMetadataManagerSPI.DOMIBUS_PULL_REQUEST_SEND_PER_JOB_CYCLE,
                DomibusPropertyMetadataManagerSPI.DOMIBUS_PULL_REQUEST_FREQUENCY_RECOVERY_TIME,
                DomibusPropertyMetadataManagerSPI.DOMIBUS_PULL_REQUEST_FREQUENCY_ERROR_COUNT);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        pullFrequencyHelper.reset();
    }

}
