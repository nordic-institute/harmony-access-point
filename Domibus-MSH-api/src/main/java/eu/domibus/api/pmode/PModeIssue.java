package eu.domibus.api.pmode;

/**
 * @author Ion Perpegel
 * @since 4.2
 */
public class PModeIssue {

    private IssueLevel level;
    private String code;
    private String message;

    public PModeIssue() {}

    public PModeIssue(String message) {
        this.message = message;
        this.level = IssueLevel.NOTE;
    }

    public PModeIssue(String message, IssueLevel level) {
        this.message = message;
        this.level = level;
    }

    public IssueLevel getLevel() {
        return level;
    }

    public void setLevel(IssueLevel level) {
        this.level = level;
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
        return message;
    }
}
