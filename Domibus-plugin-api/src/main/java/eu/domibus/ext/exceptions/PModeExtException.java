package eu.domibus.ext.exceptions;

import org.apache.commons.lang3.StringUtils;

/**
 * PMode upload/download operations Exception
 *
 * @since 4.2
 * @author Catalin Enache
 */
public class PModeExtException extends DomibusServiceExtException {
    public PModeExtException(DomibusErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public PModeExtException(Throwable throwable) {
        super(DomibusErrorCode.DOM_003, "PMode operations Exception", throwable);
    }

    public PModeExtException(DomibusErrorCode errorCode, Throwable throwable) {
        super(errorCode, StringUtils.isNotBlank(throwable.getMessage())  ? throwable.getMessage() :
                "PMode operations Exception", throwable);
    }


}
