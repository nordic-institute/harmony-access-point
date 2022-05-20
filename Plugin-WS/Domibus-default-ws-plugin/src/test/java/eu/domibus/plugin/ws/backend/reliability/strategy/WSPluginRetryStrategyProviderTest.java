package eu.domibus.plugin.ws.backend.reliability.strategy;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author François Gautier
 * @since 5.0
 */
public class WSPluginRetryStrategyProviderTest {

    private WSPluginRetryStrategyProvider strategyProvider;

    @Before
    public void setUp() {
        strategyProvider = new WSPluginRetryStrategyProvider(Arrays.asList(
                new WSPluginRetryStrategyConstant(),
                new WSPluginRetryStrategySendOnce()));
    }

    @Test
    public void getStrategyConstant() {
        WSPluginRetryStrategy strategy = strategyProvider.getStrategy(WSPluginRetryStrategyType.CONSTANT);
        assertEquals(WSPluginRetryStrategyConstant.class, strategy.getClass());
    }

    @Test
    public void getStrategySendOnce() {
        WSPluginRetryStrategy strategy = strategyProvider.getStrategy(WSPluginRetryStrategyType.SEND_ONCE);
        assertEquals(WSPluginRetryStrategySendOnce.class, strategy.getClass());
    }

    @Test
    public void getStrategyNull() {
        WSPluginRetryStrategy strategy = strategyProvider.getStrategy(null);
        assertNull(strategy);
    }
}