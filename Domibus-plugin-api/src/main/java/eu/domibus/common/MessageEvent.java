package eu.domibus.common;

import java.util.Map;

/**
 * This interface describes the message events sent from Domibus to Plugins
 * to inform the plugins about the domibus message
 *
 * @author François Gautier
 * @version 5.0
 */
public interface MessageEvent {

    String getMessageId();

    long getMessageEntityId();

    Map<String, String> getProps() ;

    void addProperty(String key, String value);
}
