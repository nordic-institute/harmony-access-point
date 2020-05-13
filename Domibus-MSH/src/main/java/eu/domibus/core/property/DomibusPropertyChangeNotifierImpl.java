package eu.domibus.core.property;

import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.api.property.DomibusPropertyChangeNotifier;
import eu.domibus.api.property.DomibusPropertyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * Handles the change of a domibus property: notifies all listeners for custom actions and broadcasts to all nodes in the cluster
 */
@Service
public class DomibusPropertyChangeNotifierImpl implements DomibusPropertyChangeNotifier {

    @Autowired
    protected List<DomibusPropertyChangeListener> propertyChangeListeners;

    @Autowired
    private SignalService signalService;

    @Override
    @Transactional(noRollbackFor = DomibusCoreException.class)
    public void signalPropertyValueChanged(String domainCode, String propertyName, String propertyValue, boolean broadcast) throws DomibusPropertyException {
        //notify interested listeners that the property changed
        List<DomibusPropertyChangeListener> listeners = propertyChangeListeners.stream()
                .filter(listener -> listener.handlesProperty(propertyName))
                .collect(Collectors.toList());
        listeners.forEach(listener -> {
            try {
                listener.propertyValueChanged(domainCode, propertyName, propertyValue);
            } catch (Exception ex) {
                throw new DomibusPropertyException("Exception executing listener " + listener.getClass().getName() + " for property " + propertyName, ex);
            }
        });

        //signal for other nodes in the cluster
        if (broadcast) {
            try {
                signalService.signalDomibusPropertyChange(domainCode, propertyName, propertyValue);
            } catch (Exception ex) {
                throw new DomibusPropertyException("Exception signaling property change for property " + propertyName, ex);
            }
        }
    }
}
