package eu.domibus.web.rest.ro;

import java.util.List;

/**
 * @author Ion Perpegel
 * @since 5.1
 */
public class TestErrorsInfoRO {

    private String message;

    private List<TestMessageErrorRo> issues;

    public TestErrorsInfoRO(String message) {
        this.message = message;
    }

    public TestErrorsInfoRO(List<TestMessageErrorRo> issues) {
        this.issues = issues;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<TestMessageErrorRo> getIssues() {
        return issues;
    }

    public void setIssues(List<TestMessageErrorRo> issues) {
        this.issues = issues;
    }


}
