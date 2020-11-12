package eu.domibus.plugin.webService.backend.rules;

import eu.domibus.plugin.webService.backend.WSBackendMessageType;

import java.util.List;

/**
 * @author François Gautier
 * @since 5.0
 */
public class WSPluginDispatchRuleBuilder {
    private int index;
    private String description;
    private String recipient;
    private String endpoint;
    private List<WSBackendMessageType> types;
    private String retry;
    private int retryTimeout;
    private int retryCount;
    private WSPluginRetryStrategy retryStrategy;

    WSPluginDispatchRule build() {
        return new WSPluginDispatchRule(
                index,
                description,
                recipient,
                endpoint,
                types,
                retry,
                retryTimeout,
                retryCount,
                retryStrategy);
    }

    public int getIndex() {
        return index;
    }

    public WSPluginDispatchRuleBuilder(int index) {
        this.index = index;
    }

    public WSPluginDispatchRuleBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public WSPluginDispatchRuleBuilder withRecipient(String recipient) {
        this.recipient = recipient;
        return this;
    }

    public WSPluginDispatchRuleBuilder withEndpoint(String endpoint) {
        this.endpoint = endpoint;
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

    public WSPluginDispatchRuleBuilder withRetryStrategy(WSPluginRetryStrategy strategy) {
        this.retryStrategy = strategy;
        return this;
    }
}
