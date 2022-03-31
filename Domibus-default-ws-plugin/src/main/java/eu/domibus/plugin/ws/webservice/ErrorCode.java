package eu.domibus.plugin.ws.webservice;

/**
 * @author Francois Gautier
 * @since 5.0
 */
public enum ErrorCode {

    WS_PLUGIN_0001("DOMIBUS:WS_PLUGIN:0001", "Generic error"),
    WS_PLUGIN_0002("DOMIBUS:WS_PLUGIN:0002", "Authentication or Authorization error"),
    WS_PLUGIN_0003("DOMIBUS:WS_PLUGIN:0003", "PMode error"),
    WS_PLUGIN_0004("DOMIBUS:WS_PLUGIN:0004", "Parties error"),
    WS_PLUGIN_0005("DOMIBUS:WS_PLUGIN:0005", "Payloads error"),
    WS_PLUGIN_0006("DOMIBUS:WS_PLUGIN:0006", "Proxy related exception."),
    WS_PLUGIN_0007("DOMIBUS:WS_PLUGIN:0007", "Invalid message exception"),
    WS_PLUGIN_0008("DOMIBUS:WS_PLUGIN:0008", "Convert exception"),
    WS_PLUGIN_0009("DOMIBUS:WS_PLUGIN:0009", "No message with id [%s] found"),
    WS_PLUGIN_0010("DOMIBUS:WS_PLUGIN:0010", "");

    String code;
    String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ErrorCode{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                "} " + super.toString();
    }
}
