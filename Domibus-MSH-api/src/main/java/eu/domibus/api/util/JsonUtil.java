package eu.domibus.api.util;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Created by Cosmin Baciu on 22-Aug-16.
 */
public interface JsonUtil<T> {

    Map<String, Object> jsonToMap(String map);

    List<String> jsonToList(String list);

    String listToJson(List<T> list);

    List<T> jsonToList(String list, Type type);
}
