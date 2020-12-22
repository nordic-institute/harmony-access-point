package eu.domibus.common;

import java.util.Map;

/**
 *
 *
 * @author François Gautier
 * @version 5.0
 */
public interface MessageEvent {

    String getMessageId();

    Map<String, String> getProps() ;

    void addProperty(String key, String value);
}
