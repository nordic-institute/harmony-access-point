package eu.domibus.plugin.webService.backend.rules;

import java.util.Date;

/**
 * @author François Gautier
 * @since 5.0
 */
public class WSPluginRetryStrategySendOnce implements WSPluginRetryStrategy {

    @Override
    public Date calculateNextAttempt(Date received, int maxAttempts, int timeoutInMinutes) {
        return null;
    }

    @Override
    public boolean canHandle(WSPluginRetryStrategyType strategyType) {
        return strategyType == WSPluginRetryStrategyType.SEND_ONCE;
    }
}
