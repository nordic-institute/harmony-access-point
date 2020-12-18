package eu.domibus.plugin.webService.backend.rules;

import eu.domibus.plugin.webService.backend.WSBackendMessageType;
import eu.domibus.plugin.webService.backend.reliability.strategy.WSPluginRetryStrategyType;

import java.util.List;

/**
 * @author François Gautier
 * @since 5.0
 */
public class WSPluginDispatchRule {

    private final String description;
    private final String recipient;
    private final String ruleName;
    private final String endpoint;
    private final List<WSBackendMessageType> types;
    private final String retry;
    private final Integer retryTimeout;
    private final Integer retryCount;
    private final WSPluginRetryStrategyType retryStrategy;

    public WSPluginDispatchRule(String description,
                                String recipient,
                                String ruleName,
                                String endpoint,
                                List<WSBackendMessageType> types,
                                String retry,
                                Integer retryTimeout,
                                Integer retryCount,
                                WSPluginRetryStrategyType retryStrategy) {
        this.description = description;
        this.recipient = recipient;
        this.ruleName = ruleName;
        this.endpoint = endpoint;
        this.types = types;
        this.retry = retry;
        this.retryTimeout = retryTimeout;
        this.retryCount = retryCount;
        this.retryStrategy = retryStrategy;
    }

    public String getDescription() {
        return description;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getRuleName() {
        return ruleName;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public List<WSBackendMessageType> getTypes() {
        return types;
    }

    public String getRetry() {
        return retry;
    }

    public Integer getRetryTimeout() {
        return retryTimeout;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public WSPluginRetryStrategyType getRetryStrategy() {
        return retryStrategy;
    }

    @Override
    public String toString() {
        return "WSPluginDispatchRule{" +
                "description='" + description + '\'' +
                ", recipient='" + recipient + '\'' +
                ", ruleName='" + ruleName + '\'' +
                ", types=" + types +
                ", retry='" + retry + '\'' +
                ", retryTimeout=" + retryTimeout +
                ", retryCount=" + retryCount +
                ", retryStrategy=" + retryStrategy +
                '}';
    }
}
