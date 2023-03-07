package eu.domibus.api.crypto;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;

/**
 * An exception indicating a keystore operation cannot be completed because the keystore content information is missing.
 *
 * @author Sebastian-Ion TINCU
 * @since 5.1
 */
public class NoKeyStoreContentInformationException extends DomibusCoreException {

    public NoKeyStoreContentInformationException(String message) {
        super(DomibusCoreErrorCode.DOM_001, message);
    }

}
