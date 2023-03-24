package eu.domibus.core.pmode.provider;

import eu.domibus.common.model.configuration.LegConfiguration;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author François Gautier
 * @since 5.0
 */
public class LegConfigurationPerMpc {

    Map<String, List<LegConfiguration>> map;

    public LegConfigurationPerMpc(Map<String, List<LegConfiguration>> map) {
        this.map = map;
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public Set<Map.Entry<String, List<LegConfiguration>>> entrySet() {
        return map.entrySet();
    }
}
