package eu.domibus.core.message;

import java.util.Map;

public interface FilterParameterExtractor {
    Object execute(Map.Entry<String, Object> parameter);
}