package eu.domibus.plugin.webService.backend.rules;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author François Gautier
 * @since 5.0
 */
@Service
public class WSPluginRetryStrategyProvider {

    protected List<WSPluginRetryStrategy> pluginEventNotifierList;

    public WSPluginRetryStrategyProvider(List<WSPluginRetryStrategy> pluginEventNotifierList) {
        this.pluginEventNotifierList = pluginEventNotifierList;
    }

    public WSPluginRetryStrategy getStrategy(WSPluginRetryStrategyType strategyType) {
        for (WSPluginRetryStrategy retryStrategy : pluginEventNotifierList) {
            if (retryStrategy.canHandle(strategyType)) {
                return retryStrategy;
            }
        }
        return null;
    }
}