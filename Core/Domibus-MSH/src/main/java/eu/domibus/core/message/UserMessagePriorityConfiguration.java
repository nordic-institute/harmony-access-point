package eu.domibus.core.message;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
public class UserMessagePriorityConfiguration {

    protected String ruleName;

    protected Integer priority;

    protected String concurrencyPropertyName;

    public UserMessagePriorityConfiguration(String ruleName, Integer priority, String concurrencyPropertyName) {
        this.ruleName = ruleName;
        this.priority = priority;
        this.concurrencyPropertyName = concurrencyPropertyName;
    }

    public Integer getPriority() {
        return priority;
    }

    public String getConcurrencyPropertyName() {
        return concurrencyPropertyName;
    }

    public String getRuleName() {
        return ruleName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("ruleName", ruleName)
                .append("priority", priority)
                .append("concurrency", concurrencyPropertyName)
                .toString();
    }
}
