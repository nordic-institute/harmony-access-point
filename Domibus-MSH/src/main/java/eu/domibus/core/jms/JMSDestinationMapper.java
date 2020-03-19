package eu.domibus.core.jms;

import eu.domibus.api.jms.JMSDestination;
import eu.domibus.jms.spi.InternalJMSDestination;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * // TODO Documentation
 * @author Cosmin Baciu
 * @since 3.2
 */
@Component
public class JMSDestinationMapper {

    public Map<String, JMSDestination> convert(Map<String, InternalJMSDestination> destinations) {
        Map<String, JMSDestination> result = new HashMap<>();
        for (Map.Entry<String, InternalJMSDestination> spiEntry : destinations.entrySet()) {
            result.put(spiEntry.getKey(), convert(spiEntry.getValue()));
        }
        return result;
    }

    public List<JMSDestination> convert(List<InternalJMSDestination> internalJmsDestinations) {
        List<JMSDestination> dests = new ArrayList<>();
        for (InternalJMSDestination internalJmsDestination : internalJmsDestinations) {
            dests.add(convert(internalJmsDestination));
        }
        return dests;
    }

    public JMSDestination convert(InternalJMSDestination internalJmsDestination) {
        JMSDestination result = new JMSDestination();
        result.setType(internalJmsDestination.getType());
        result.setName(internalJmsDestination.getName());
        result.setFullyQualifiedName(internalJmsDestination.getFullyQualifiedName());
        result.setNumberOfMessages(internalJmsDestination.getNumberOfMessages());
        result.setInternal(internalJmsDestination.isInternal());
        result.setProperties(internalJmsDestination.getProperties());
        return result;
    }

    public InternalJMSDestination convert(JMSDestination jmsDestination) {
        InternalJMSDestination result = new InternalJMSDestination();
        result.setType(jmsDestination.getType());
        result.setName(jmsDestination.getName());
        result.setFullyQualifiedName(jmsDestination.getFullyQualifiedName());
        result.setNumberOfMessages(jmsDestination.getNumberOfMessages());
        result.setInternal(jmsDestination.isInternal());
        result.setProperties(jmsDestination.getProperties());
        return result;
    }
}
