package eu.domibus.api.party;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;

/**
 * @author Razvan Cretu
 * @since 5.1
 */
public class PartyNotReachableException extends DomibusCoreException {

    public PartyNotReachableException(DomibusCoreErrorCode dce, String message) {
        super(dce, message);
    }
    public PartyNotReachableException(DomibusCoreErrorCode dce, String message, Throwable cause) {
        super(dce, message, cause);
    }

}
