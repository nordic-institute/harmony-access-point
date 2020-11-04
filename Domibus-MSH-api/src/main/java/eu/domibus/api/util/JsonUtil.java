package eu.domibus.api.util;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Created by Cosmin Baciu on 22-Aug-16.
 */
public interface JsonUtil {

    Map<String, Object> jsonToMap(String map);

    String listToJson(List list);

    List jsonToList(String list);

    List jsonToList(String list, Type type);
}
