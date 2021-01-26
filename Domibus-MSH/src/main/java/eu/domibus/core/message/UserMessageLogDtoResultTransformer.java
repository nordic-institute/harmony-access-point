package eu.domibus.core.message;

import eu.domibus.api.model.UserMessageLogDto;
import org.hibernate.transform.ResultTransformer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author François Gautier
 * @since 5.0
 */
public class UserMessageLogDtoResultTransformer implements ResultTransformer {

    private final Map<String, UserMessageLogDto> postDTOMap = new LinkedHashMap<>();

    @Override
    public Object transformTuple(Object[] tuple, String[] aliases) {

        Map<String, Integer> aliasToIndexMap = aliasToIndexMap(aliases);

        String messageId = (String) tuple[aliasToIndexMap.get(UserMessageLogDto.MESSAGE_ID)];

        UserMessageLogDto userMessageLogDto = postDTOMap.computeIfAbsent(
                messageId,
                id -> new UserMessageLogDto(tuple, aliasToIndexMap)
        );

        Object propValue = tuple[aliasToIndexMap.get(UserMessageLogDto.PROP_VALUE)];
        if (propValue != null) {
            userMessageLogDto.getProperties().put(
                    (String) propValue,
                    (String) tuple[aliasToIndexMap.get(UserMessageLogDto.PROP_NAME)]
            );
        }

        return userMessageLogDto;
    }

    @Override
    public List transformList(List collection) {
        return new ArrayList<>(postDTOMap.values());
    }

    public Map<String, Integer> aliasToIndexMap(String[] aliases) {

        Map<String, Integer> aliasToIndexMap = new LinkedHashMap<>();

        for (int i = 0; i < aliases.length; i++) {
            aliasToIndexMap.put(aliases[i], i);
        }

        return aliasToIndexMap;
    }
}