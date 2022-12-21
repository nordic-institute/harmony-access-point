package eu.domibus.core.proxy;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;

/**
 * A Domibus exception when executing HTTP requests involving proxies.
 *
 * @author Sebastian-Ion TINCU
 * @since 4.2.12
 */
public class DomibusProxyException extends DomibusCoreException {

    public DomibusProxyException(String message) {
        super(DomibusCoreErrorCode.DOM_006, message);
    }
}
