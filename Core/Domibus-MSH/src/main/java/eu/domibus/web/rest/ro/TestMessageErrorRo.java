package eu.domibus.web.rest.ro;

/**
 * @author Ion Perpegel
 * @since 5.1
 *
 * Class that encapsulates information about an error in test service: code, level and message
 */
public class TestMessageErrorRo {

    private String code;
    private String message;

    public TestMessageErrorRo(String code, String message) {
        this.message = message;
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return code + ':' +message;
    }

}
