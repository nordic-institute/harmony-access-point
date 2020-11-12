package eu.domibus.plugin.webService.backend.rules;

import eu.domibus.plugin.webService.backend.WSBackendMessageType;

import java.util.List;

/**
 * @author François Gautier
 * @since 5.0
 */
public class WSPluginDispatchRule {

    private final int index;
    private final String description;
    private final String recipient;
    private final String endpoint;
    private final List<WSBackendMessageType> types;
    private final String retry;
    private final int retryTimeout;
    private final int retryCount;
    private final WSPluginRetryStrategy retryStrategy;

    public WSPluginDispatchRule(int index,
                                String description,
                                String recipient,
                                String endpoint,
                                List<WSBackendMessageType> types,
                                String retry,
                                int retryTimeout,
                                int retryCount,
                                WSPluginRetryStrategy retryStrategy) {
        this.index = index;
        this.description = description;
        this.recipient = recipient;
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

    public String getEndpoint() {
        return endpoint;
    }

    public List<WSBackendMessageType> getTypes() {
        return types;
    }

    public String getRetry() {
        return retry;
    }

    public int getRetryTimeout() {
        return retryTimeout;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public WSPluginRetryStrategy getRetryStrategy() {
        return retryStrategy;
    }

    @Override
    public String toString() {
        return "WSPluginDispatchRule{" +
                "index=" + index +
                ", description='" + description + '\'' +
                ", recipient='" + recipient + '\'' +
                ", endpoint='" + endpoint + '\'' +
                ", retry='" + retry + '\'' +
                ", retryTimeout=" + retryTimeout +
                ", retryCount=" + retryCount +
                ", retryStrategy='" + retryStrategy + '\'' +
                '}';
    }
}
