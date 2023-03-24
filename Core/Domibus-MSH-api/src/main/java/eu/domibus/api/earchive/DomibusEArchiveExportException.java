package eu.domibus.api.earchive;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;

/**
 * Exception raised when export fails for a message
 *
 * @author Ion Perpegel
 * @since 5.1
 */
public class DomibusEArchiveExportException extends DomibusCoreException {

    Long entityId;

    String message;

    public DomibusEArchiveExportException(Long entityId, String message, Throwable cause) {
        super(DomibusCoreErrorCode.DOM_001, message, cause);
        this.entityId = entityId;
        this.message = message;
    }

    public DomibusEArchiveExportException(DomibusCoreErrorCode code, Long entityId, String message) {
        super(code, message);
        this.entityId = entityId;
        this.message = message;
    }

    public Long getEntityId() {
        return entityId;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
