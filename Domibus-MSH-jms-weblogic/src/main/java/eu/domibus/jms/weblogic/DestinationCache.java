package eu.domibus.jms.weblogic;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import javax.jms.Destination;
import javax.naming.InitialContext;
import javax.naming.NamingException;

@Component
public class DestinationCache {

    @Cacheable(value = "jmsDestinations")
    public Destination getByJndiName(final String destinationJndi) throws NamingException {
        return InitialContext.doLookup(destinationJndi);
    }

}
