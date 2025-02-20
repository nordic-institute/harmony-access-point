package eu.domibus.core.util;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;

/**
 * @author François Gautier
 * @since 5.1
 */
public class MessagingNodeNotFoundException extends DomibusCoreException {

    public MessagingNodeNotFoundException(String msg) {
        super(DomibusCoreErrorCode.DOM_007, msg);
    }

}
