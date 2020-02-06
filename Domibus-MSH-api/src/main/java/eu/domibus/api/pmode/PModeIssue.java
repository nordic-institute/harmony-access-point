package eu.domibus.api.pmode;

/**
 * @author Ion Perpegel
 * @since 4.2
 *
 * Class that encapsulates information about a pMode validation: code, level and message
 */
public class PModeIssue {

    private Level level;
    private String code;
    private String message;

    public PModeIssue() {}

    public PModeIssue(String message) {
        this.message = message;
        this.level = Level.NOTE;
    }

    public PModeIssue(String message, Level level) {
        this.message = message;
        this.level = level;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
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

    public enum Level {
        NOTE,
        WARNING,
        ERROR,
    }
}
