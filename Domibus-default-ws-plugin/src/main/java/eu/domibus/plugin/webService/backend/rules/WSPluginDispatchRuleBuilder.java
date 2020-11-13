package eu.domibus.plugin.webService.backend.rules;

import eu.domibus.plugin.webService.backend.WSBackendMessageType;

import java.util.List;

/**
 * @author François Gautier
 * @since 5.0
 */
public class WSPluginDispatchRuleBuilder {
    private String description;
    private String recipient;
    private String ruleName;
    private String endpoint;
    private List<WSBackendMessageType> types;
    private String retry;
    private int retryTimeout;
    private int retryCount;
    private WSPluginRetryStrategyType retryStrategy;

    public WSPluginDispatchRule build() {
        return new WSPluginDispatchRule(
                description,
                recipient,
                ruleName,
                endpoint,
                types,
                retry,
                retryTimeout,
                retryCount,
                retryStrategy);
    }

    public WSPluginDispatchRuleBuilder(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getRuleName() {
        return ruleName;
    }

    public WSPluginDispatchRuleBuilder withEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }
    public WSPluginDispatchRuleBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public WSPluginDispatchRuleBuilder withRecipient(String recipient) {
        this.recipient = recipient;
        return this;
    }

    public WSPluginDispatchRuleBuilder withType(List<WSBackendMessageType> types) {
        this.types = types;
        return this;
    }

    public WSPluginDispatchRuleBuilder withRetry(String retry) {
        this.retry = retry;
        return this;
    }

    public WSPluginDispatchRuleBuilder withRetryTimeout(int retryTimeout) {
        this.retryTimeout = retryTimeout;
        return this;
    }

    public WSPluginDispatchRuleBuilder withRetryCount(int retryCount) {
        this.retryCount = retryCount;
        return this;
    }

    public WSPluginDispatchRuleBuilder withRetryStrategy(WSPluginRetryStrategyType strategy) {
        this.retryStrategy = strategy;
        return this;
    }
}
