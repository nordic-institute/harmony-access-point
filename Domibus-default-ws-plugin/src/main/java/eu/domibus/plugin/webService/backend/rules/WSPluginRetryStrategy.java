package eu.domibus.plugin.webService.backend.rules;

import java.util.Date;

/**
 * @author François Gautier
 * @since 5.0
 */
public interface WSPluginRetryStrategy {

    Date calculateNextAttempt(Date received, int maxAttempts, int timeoutInMinutes);

    boolean canHandle(WSPluginRetryStrategyType strategyType);
}
