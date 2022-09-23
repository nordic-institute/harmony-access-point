package eu.domibus.web.rest.ro;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ion Perpegel
 * @since 5.1
 */
public class TestErrorsInfoRO {

    private String message;

    private List<TestErrorRo> issues;

    public TestErrorsInfoRO(String message) {
        this.message = message;
    }

    public TestErrorsInfoRO(List<TestErrorRo> issues) {
        this.issues = issues;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<TestErrorRo> getIssues() {
        return issues;
    }

    public void setIssues(List<TestErrorRo> issues) {
        this.issues = issues;
    }


}
