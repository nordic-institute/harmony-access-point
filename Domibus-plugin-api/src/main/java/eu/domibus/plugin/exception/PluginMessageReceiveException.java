package eu.domibus.plugin.exception;

import eu.domibus.common.ErrorCode;

/**
 * To be used in case there is an issue while delivering the message to the plugin or while processing the message by the plugin
 */
public class PluginMessageReceiveException extends RuntimeException {

    //default value
    protected ErrorCode.EbMS3ErrorCode ebMS3ErrorCode;

    public PluginMessageReceiveException(ErrorCode.EbMS3ErrorCode ebMS3ErrorCode) {
        this.ebMS3ErrorCode = ebMS3ErrorCode;
    }

    public PluginMessageReceiveException(String message, ErrorCode.EbMS3ErrorCode ebMS3ErrorCode) {
        super(message);
        this.ebMS3ErrorCode = ebMS3ErrorCode;
    }

    public PluginMessageReceiveException(String message, Throwable cause, ErrorCode.EbMS3ErrorCode ebMS3ErrorCode) {
        super(message, cause);
        this.ebMS3ErrorCode = ebMS3ErrorCode;
    }

    public PluginMessageReceiveException(Throwable cause, ErrorCode.EbMS3ErrorCode ebMS3ErrorCode) {
        super(cause);
        this.ebMS3ErrorCode = ebMS3ErrorCode;
    }

    public ErrorCode.EbMS3ErrorCode getEbMS3ErrorCode() {
        return ebMS3ErrorCode;
    }

    public void setEbMS3ErrorCode(ErrorCode.EbMS3ErrorCode ebMS3ErrorCode) {
        this.ebMS3ErrorCode = ebMS3ErrorCode;
    }
}
